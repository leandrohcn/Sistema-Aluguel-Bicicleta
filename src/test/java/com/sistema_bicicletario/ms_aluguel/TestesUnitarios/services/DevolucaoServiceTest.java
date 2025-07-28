package com.sistema_bicicletario.ms_aluguel.TestesUnitarios.services;

import com.sistema_bicicletario.ms_aluguel.clients.EquipamentoClient;
import com.sistema_bicicletario.ms_aluguel.clients.ExternoClient;
import com.sistema_bicicletario.ms_aluguel.dtos.*;
import com.sistema_bicicletario.ms_aluguel.entities.aluguel.AluguelEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessableEntityException;
import com.sistema_bicicletario.ms_aluguel.listeners.EmailRealizadoEvent;
import com.sistema_bicicletario.ms_aluguel.repositories.AluguelRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.CiclistaRepository;
import com.sistema_bicicletario.ms_aluguel.services.DevolucaoService;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita o uso de Mockito com JUnit 5
class DevolucaoServiceTest {

    @Mock // Cria um mock para as dependências
    private AluguelRepository aluguelRepository;
    @Mock
    private CiclistaRepository ciclistaRepository;
    @Mock
    private EquipamentoClient equipamentoClient;
    @Mock
    private ExternoClient externoClient;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks // Injeta os mocks na instância de DevolucaoService
    private DevolucaoService devolucaoService;

    // Entidades e DTOs de exemplo para os testes
    private NovoDevolucaoDTO novoDevolucaoDTO;
    private TrancaDTO trancaLivreDTO;
    private TrancaDTO trancaOcupadaDTO;
    private AluguelEntity aluguelAtivo;
    private CiclistaEntity ciclistaAtivo;
    private CobrancaDTO cobrancaPagaDTO;
    private CobrancaDTO cobrancaPendenteDTO;
    private NovaCobranca novaCobrancaRequest;
    private IntegrarBicicletaNaRedeDTO integrarBicicletaNaRedeDTO;

    @BeforeEach
    void setUp() {
        // Inicialização dos DTOs e entidades para serem usados nos testes
        novoDevolucaoDTO = new NovoDevolucaoDTO();
        novoDevolucaoDTO.setNumero(1); // ID da tranca
        novoDevolucaoDTO.setBicicleta(10); // ID da bicicleta
        novoDevolucaoDTO.setAcao("DEVOLVER"); // ou "REPARO_SOLICITADO"

        trancaLivreDTO = new TrancaDTO();
        trancaLivreDTO.setNumero(1);
        trancaLivreDTO.setStatusTranca("LIVRE");
        trancaLivreDTO.setNumero(1); // Mock para o ID da tranca

        trancaOcupadaDTO = new TrancaDTO();
        trancaOcupadaDTO.setNumero(2);
        trancaOcupadaDTO.setStatusTranca("OCUPADA");
        trancaOcupadaDTO.setNumero(2);

        aluguelAtivo = new AluguelEntity();
        aluguelAtivo.setId(1);
        aluguelAtivo.setNumeroBicicleta(10);
        aluguelAtivo.setCiclista(100); // ID do ciclista
        aluguelAtivo.setHoraInicio(LocalDateTime.now().minusHours(1)); // Aluguel de 1 hora
        aluguelAtivo.setTrancaInicio(5);
        aluguelAtivo.setNomeTitular("João Silva"); // Para o email
        aluguelAtivo.setFinalCartao("1234"); // Para o email
        aluguelAtivo.setCobranca(200L); // ID da cobrança para buscar

        ciclistaAtivo = new CiclistaEntity();
        ciclistaAtivo.setId(100);
        ciclistaAtivo.setNome("João Silva");
        ciclistaAtivo.setEmail("joao.silva@example.com");
        ciclistaAtivo.setAluguelAtivo(true);

        cobrancaPagaDTO = new CobrancaDTO();
        cobrancaPagaDTO.setStatus("PAGO");
        cobrancaPagaDTO.setValor(500L); // 5 reais
        cobrancaPagaDTO.setHoraSolicitacao(LocalDateTime.now());

        cobrancaPendenteDTO = new CobrancaDTO();
        cobrancaPendenteDTO.setStatus("PENDENTE");
        cobrancaPendenteDTO.setValor(750L);
        cobrancaPendenteDTO.setHoraSolicitacao(LocalDateTime.now());

        integrarBicicletaNaRedeDTO = new IntegrarBicicletaNaRedeDTO();
        integrarBicicletaNaRedeDTO.setIdBicicleta(10);
        integrarBicicletaNaRedeDTO.setIdTranca(1);

        long valorExtra = 0;
        novaCobrancaRequest = new NovaCobranca();
        novaCobrancaRequest.setValor(valorExtra);
        novaCobrancaRequest.setCiclista(aluguelAtivo.getCiclista());
    }

    @Test
    @DisplayName("Deve realizar devolução sem valor extra e com cobrança bem-sucedida")
    void deveRealizarDevolucaoSemValorExtra() {
        aluguelAtivo.setHoraInicio(LocalDateTime.now().minusMinutes(30));

        when(equipamentoClient.buscarTrancaPorId(novoDevolucaoDTO.getNumero()))
                .thenReturn(ResponseEntity.ok(trancaLivreDTO));
        when(aluguelRepository.findByNumeroBicicletaAndHoraFimIsNull(novoDevolucaoDTO.getBicicleta()))
                .thenReturn(Optional.of(aluguelAtivo));
        when(ciclistaRepository.findById(aluguelAtivo.getCiclista()))
                .thenReturn(Optional.of(ciclistaAtivo));
        when(aluguelRepository.save(any(AluguelEntity.class)))
                .thenReturn(aluguelAtivo);
        when(ciclistaRepository.save(any(CiclistaEntity.class)))
                .thenReturn(ciclistaAtivo);
        when(equipamentoClient.integrarNaRede(any(IntegrarBicicletaNaRedeDTO.class)))
                .thenReturn(ResponseEntity.ok(new IntegrarBicicletaNaRedeDTO()));
        when(externoClient.obterCobrancaPorId(aluguelAtivo.getCobranca()))
                .thenReturn(ResponseEntity.ok(cobrancaPagaDTO));

        DevolucaoDTO result = devolucaoService.realizarDevolucao(novoDevolucaoDTO);

        assertNotNull(result);
        assertEquals(aluguelAtivo.getNumeroBicicleta(), result.getBicicleta());
        assertEquals(0L, aluguelAtivo.getValorExtra());
        assertNotNull(aluguelAtivo.getHoraFim());

        verify(equipamentoClient, times(1)).buscarTrancaPorId(novoDevolucaoDTO.getNumero());
        verify(aluguelRepository, times(1)).findByNumeroBicicletaAndHoraFimIsNull(novoDevolucaoDTO.getBicicleta());
        verify(ciclistaRepository, times(2)).findById(aluguelAtivo.getCiclista());
        verify(aluguelRepository, times(1)).save(aluguelAtivo);
        verify(ciclistaRepository, times(1)).save(ciclistaAtivo);
        verify(equipamentoClient, times(1)).atualizarStatusBicicleta(aluguelAtivo.getNumeroBicicleta(), "NOVA");
        verify(equipamentoClient, times(1)).integrarNaRede(any(IntegrarBicicletaNaRedeDTO.class));
        verify(externoClient, times(1)).obterCobrancaPorId(aluguelAtivo.getCobranca());
        verify(eventPublisher, times(1)).publishEvent(any(EmailRealizadoEvent.class));
        verify(externoClient, never()).realizarCobranca(any(NovaCobranca.class));
        verify(externoClient, never()).filaCobranca(any(NovaCobranca.class));
    }

    @Test
    @DisplayName("Deve realizar devolução com valor extra e cobrança paga")
    void deveRealizarDevolucaoComValorExtraPago() {
        // Cenario
        novoDevolucaoDTO.setAcao("DEVOLVER");
        aluguelAtivo.setHoraInicio(LocalDateTime.now().minusHours(3));
        aluguelAtivo.setValorExtra(500L); // Mock para o valor extra no save

        when(equipamentoClient.buscarTrancaPorId(novoDevolucaoDTO.getNumero()))
                .thenReturn(ResponseEntity.ok(trancaLivreDTO));
        when(aluguelRepository.findByNumeroBicicletaAndHoraFimIsNull(novoDevolucaoDTO.getBicicleta()))
                .thenReturn(Optional.of(aluguelAtivo));
        when(ciclistaRepository.findById(aluguelAtivo.getCiclista()))
                .thenReturn(Optional.of(ciclistaAtivo));

        // Mock para enviaCobranca (chamada interna)
        when(externoClient.realizarCobranca(any(NovaCobranca.class)))
                .thenReturn(ResponseEntity.ok(cobrancaPagaDTO));

        when(aluguelRepository.save(any(AluguelEntity.class)))
                .thenReturn(aluguelAtivo);
        when(ciclistaRepository.save(any(CiclistaEntity.class)))
                .thenReturn(ciclistaAtivo);
        when(equipamentoClient.integrarNaRede(any(IntegrarBicicletaNaRedeDTO.class)))
                .thenReturn(ResponseEntity.ok(new IntegrarBicicletaNaRedeDTO()));
        when(externoClient.obterCobrancaPorId(aluguelAtivo.getCobranca()))
                .thenReturn(ResponseEntity.ok(cobrancaPagaDTO));

        // Acao
        DevolucaoDTO result = devolucaoService.realizarDevolucao(novoDevolucaoDTO);

        // Verificacao
        assertNotNull(result);
        assertTrue(aluguelAtivo.getValorExtra() > 0); // Deve ter valor extra
        assertNotNull(aluguelAtivo.getHoraFim());

        verify(externoClient, times(1)).realizarCobranca(any(NovaCobranca.class)); // Deve chamar cobrança extra
        verify(externoClient, never()).filaCobranca(any(NovaCobranca.class)); // Não deve chamar fila pois foi pago
        verify(equipamentoClient, times(1)).atualizarStatusBicicleta(aluguelAtivo.getNumeroBicicleta(), "NOVA");
        verify(eventPublisher, times(1)).publishEvent(any(EmailRealizadoEvent.class));
    }

    @Test
    @DisplayName("Deve realizar devolução com valor extra e cobrança pendente (enviado para fila)")
    void deveRealizarDevolucaoComValorExtraPendente() {
        // Cenario
        novoDevolucaoDTO.setAcao("DEVOLVER");
        aluguelAtivo.setHoraInicio(LocalDateTime.now().minusHours(3));
        aluguelAtivo.setValorExtra(750L);

        when(equipamentoClient.buscarTrancaPorId(novoDevolucaoDTO.getNumero()))
                .thenReturn(ResponseEntity.ok(trancaLivreDTO));
        when(aluguelRepository.findByNumeroBicicletaAndHoraFimIsNull(novoDevolucaoDTO.getBicicleta()))
                .thenReturn(Optional.of(aluguelAtivo));
        when(ciclistaRepository.findById(aluguelAtivo.getCiclista()))
                .thenReturn(Optional.of(ciclistaAtivo));

        when(externoClient.realizarCobranca(any(NovaCobranca.class)))
                .thenReturn(ResponseEntity.ok(cobrancaPendenteDTO));

        when(aluguelRepository.save(any(AluguelEntity.class)))
                .thenReturn(aluguelAtivo);
        when(ciclistaRepository.save(any(CiclistaEntity.class)))
                .thenReturn(ciclistaAtivo);
        when(equipamentoClient.integrarNaRede(any(IntegrarBicicletaNaRedeDTO.class)))
                .thenReturn(ResponseEntity.ok(new IntegrarBicicletaNaRedeDTO()));
        when(externoClient.obterCobrancaPorId(aluguelAtivo.getCobranca()))
                .thenReturn(ResponseEntity.ok(cobrancaPagaDTO)); // Pode ser qualquer status válido para o email

        // Acao
        DevolucaoDTO result = devolucaoService.realizarDevolucao(novoDevolucaoDTO);

        // Verificacao
        assertNotNull(result);
        assertTrue(aluguelAtivo.getValorExtra() > 0);

        verify(externoClient, times(1)).realizarCobranca(any(NovaCobranca.class));
        verify(externoClient, times(1)).filaCobranca(any(NovaCobranca.class)); // Deve chamar fila pois foi pendente
        verify(equipamentoClient, times(1)).atualizarStatusBicicleta(aluguelAtivo.getNumeroBicicleta(), "NOVA");
        verify(eventPublisher, times(1)).publishEvent(any(EmailRealizadoEvent.class));
    }

    @Test
    @DisplayName("Deve lançar TrataUnprocessableEntityException se a tranca estiver OCUPADA")
    void deveLancarExcecaoSeTrancaOcupada() {
        // Cenario
        when(equipamentoClient.buscarTrancaPorId(novoDevolucaoDTO.getNumero()))
                .thenReturn(ResponseEntity.ok(trancaOcupadaDTO)); // Mocka tranca como OCUPADA

        // Acao e Verificacao
        TrataUnprocessableEntityException thrown = assertThrows(TrataUnprocessableEntityException.class, () ->
                devolucaoService.realizarDevolucao(novoDevolucaoDTO)
        );

        assertEquals("A tranca de destino está OCUPADA.", thrown.getMessage());
        verify(aluguelRepository, never()).findByNumeroBicicletaAndHoraFimIsNull(anyInt()); // Não deve seguir
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException se nenhum aluguel ativo for encontrado")
    void deveLancarExcecaoSeAluguelNaoEncontrado() {
        // Cenario
        when(equipamentoClient.buscarTrancaPorId(novoDevolucaoDTO.getNumero()))
                .thenReturn(ResponseEntity.ok(trancaLivreDTO));
        when(aluguelRepository.findByNumeroBicicletaAndHoraFimIsNull(novoDevolucaoDTO.getBicicleta()))
                .thenReturn(Optional.empty()); // Mocka que não encontrou aluguel

        // Acao e Verificacao
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () ->
                devolucaoService.realizarDevolucao(novoDevolucaoDTO)
        );

        assertEquals("Nenhum aluguel ativo encontrado para esta bicicleta.", thrown.getMessage());
        verify(ciclistaRepository, never()).findById(anyInt()); // Não deve seguir
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException se o ciclista do aluguel não for encontrado")
    void deveLancarExcecaoSeCiclistaNaoEncontrado() {
        when(equipamentoClient.buscarTrancaPorId(novoDevolucaoDTO.getNumero()))
                .thenReturn(ResponseEntity.ok(trancaLivreDTO));
        when(aluguelRepository.findByNumeroBicicletaAndHoraFimIsNull(novoDevolucaoDTO.getBicicleta()))
                .thenReturn(Optional.of(aluguelAtivo));
        when(ciclistaRepository.findById(aluguelAtivo.getCiclista()))
                .thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () ->
                devolucaoService.realizarDevolucao(novoDevolucaoDTO)
        );
        assertEquals("", thrown.getMessage());
        verify(externoClient, never()).realizarCobranca(any(NovaCobranca.class));
    }


    @Test
    @DisplayName("Deve atualizar status da bicicleta para 'EM_REPARO' se a ação for 'REPARO_SOLICITADO'")
    void deveAtualizarStatusBicicletaParaEmReparo() {
        // Cenario
        novoDevolucaoDTO.setAcao("REPARO_SOLICITADO");
        aluguelAtivo.setHoraInicio(LocalDateTime.now().minusMinutes(30)); // Sem valor extra

        when(equipamentoClient.buscarTrancaPorId(novoDevolucaoDTO.getNumero()))
                .thenReturn(ResponseEntity.ok(trancaLivreDTO));
        when(aluguelRepository.findByNumeroBicicletaAndHoraFimIsNull(novoDevolucaoDTO.getBicicleta()))
                .thenReturn(Optional.of(aluguelAtivo));
        when(ciclistaRepository.findById(aluguelAtivo.getCiclista()))
                .thenReturn(Optional.of(ciclistaAtivo));
        when(aluguelRepository.save(any(AluguelEntity.class)))
                .thenReturn(aluguelAtivo);
        when(ciclistaRepository.save(any(CiclistaEntity.class)))
                .thenReturn(ciclistaAtivo);
        when(equipamentoClient.integrarNaRede(any(IntegrarBicicletaNaRedeDTO.class)))
                .thenReturn(ResponseEntity.ok(new IntegrarBicicletaNaRedeDTO()));
        when(externoClient.obterCobrancaPorId(aluguelAtivo.getCobranca()))
                .thenReturn(ResponseEntity.ok(cobrancaPagaDTO));

        // Acao
        devolucaoService.realizarDevolucao(novoDevolucaoDTO);

        // Verificacao
        verify(equipamentoClient, times(1)).atualizarStatusBicicleta(aluguelAtivo.getNumeroBicicleta(), "EM_REPARO");
    }

}