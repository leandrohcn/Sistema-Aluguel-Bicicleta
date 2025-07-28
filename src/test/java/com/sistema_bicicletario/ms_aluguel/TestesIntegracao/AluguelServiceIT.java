package com.sistema_bicicletario.ms_aluguel.TestesIntegracao;

import com.sistema_bicicletario.ms_aluguel.clients.EquipamentoClient;
import com.sistema_bicicletario.ms_aluguel.clients.ExternoClient;
import com.sistema_bicicletario.ms_aluguel.dtos.*;
import com.sistema_bicicletario.ms_aluguel.entities.aluguel.AluguelEntity;
import com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito.CartaoDeCreditoEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Status;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessableEntityException;
import com.sistema_bicicletario.ms_aluguel.repositories.AluguelRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.CartaoRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.CiclistaRepository;
import com.sistema_bicicletario.ms_aluguel.services.AluguelService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class AluguelServiceIT {

    @Autowired
    private AluguelService aluguelService;

    @Autowired
    private CiclistaRepository ciclistaRepository;

    @Autowired
    private AluguelRepository aluguelRepository;

    @Autowired
    private CartaoRepository cartaoRepository;

    @MockBean
    private EquipamentoClient equipamentoClient;

    @MockBean
    private ExternoClient externoClient;

    @MockBean
    private ApplicationEventPublisher eventPublisher;

    private CiclistaEntity ciclistaAtivo;
    private TrancaDTO trancaLivre;
    private BicicletaDTO bicicletaDisponivel;
    private CobrancaDTO cobrancaPaga;
    private NovoAluguelDTO novoAluguelDTO;
    private CartaoDeCreditoEntity cartaoCiclista;
    @BeforeEach
    void setup() {
        // Limpa os repositórios antes de cada teste
        aluguelRepository.deleteAll();
        cartaoRepository.deleteAll();
        ciclistaRepository.deleteAll();

        // Configuração de um ciclista ativo
        ciclistaAtivo = new CiclistaEntity();
        ciclistaAtivo.setNome("Ciclista Teste");
        ciclistaAtivo.setEmail("ciclista@teste.com");
        ciclistaAtivo.setStatus(Status.ATIVO);
        ciclistaAtivo.setAluguelAtivo(false); // Garante que não tem aluguel ativo
        ciclistaAtivo = ciclistaRepository.save(ciclistaAtivo);

        cartaoCiclista = new CartaoDeCreditoEntity();
        cartaoCiclista.setNumero("1111222233334444");
        cartaoCiclista.setNomeTitular("Ciclista Teste");
        cartaoCiclista.setValidade(LocalDate.of(2027,12,12));
        cartaoCiclista.setCvv("123");
        cartaoCiclista.setCiclista(ciclistaAtivo);
        cartaoCiclista = cartaoRepository.save(cartaoCiclista);

        // Configuração de uma tranca livre
        trancaLivre = new TrancaDTO();
        trancaLivre.setNumero(1);
        trancaLivre.setStatusTranca("LIVRE");
        trancaLivre.setBicicleta(10); // Bicicleta associada à tranca

        // Configuração de uma bicicleta disponível
        bicicletaDisponivel = new BicicletaDTO();
        bicicletaDisponivel.setNumero(10);
        bicicletaDisponivel.setMarca("Caloi");
        bicicletaDisponivel.setModelo("Mountain");
        bicicletaDisponivel.setStatus("DISPONIVEL");

        // Configuração de uma cobrança paga
        cobrancaPaga = new CobrancaDTO();
        cobrancaPaga.setId(1L);
        cobrancaPaga.setStatus("PAGA");
        cobrancaPaga.setValor(1000L); // 10 reais

        // DTO para o novo aluguel
        novoAluguelDTO = new NovoAluguelDTO();
        novoAluguelDTO.setCiclista(ciclistaAtivo.getId());
        novoAluguelDTO.setTrancaInicio(trancaLivre.getNumero());
    }

    @Test
    void deveRealizarAluguelComSucesso() {
        when(equipamentoClient.buscarTrancaPorId(trancaLivre.getNumero())).thenReturn(ResponseEntity.ok(trancaLivre));
        when(equipamentoClient.buscarBicicletaPorId(bicicletaDisponivel.getNumero())).thenReturn(ResponseEntity.ok(bicicletaDisponivel));
        when(externoClient.realizarCobranca(any(NovaCobranca.class))).thenReturn(ResponseEntity.ok(cobrancaPaga));
        when(equipamentoClient.liberaTranca(eq(trancaLivre.getNumero()), any(TrancarDestrancarDTO.class))).thenReturn(ResponseEntity.ok().build());
        when(equipamentoClient.atualizarStatusBicicleta(eq(bicicletaDisponivel.getNumero()), eq("EM_USO"))).thenReturn(ResponseEntity.ok().build());


        AluguelDTO resultado = aluguelService.realizarAluguel(novoAluguelDTO);
        doReturn(ResponseEntity.ok().build()).when(externoClient).enviarEmail(any(EnviaEmailDTO.class));
        externoClient.enviarEmail(new EnviaEmailDTO(
                ciclistaAtivo.getEmail(),
                "Testando",
                "Testando: " + cartaoCiclista.getNumero()
        ));
        assertNotNull(resultado);
        assertEquals(ciclistaAtivo.getId(), resultado.getCiclista());
        assertEquals(trancaLivre.getNumero(), resultado.getTrancaInicio());
        assertEquals(bicicletaDisponivel.getNumero(), resultado.getIdBicicleta());
        assertNotNull(resultado.getHoraInicio());
        assertEquals(cobrancaPaga.getId(), resultado.getCobranca());

        verify(equipamentoClient, times(1)).buscarTrancaPorId(trancaLivre.getNumero());
        verify(equipamentoClient, times(1)).buscarBicicletaPorId(bicicletaDisponivel.getNumero());
        verify(externoClient, times(1)).realizarCobranca(any(NovaCobranca.class));
        verify(equipamentoClient, times(1)).liberaTranca(eq(trancaLivre.getNumero()), any(TrancarDestrancarDTO.class));
        verify(equipamentoClient, times(1)).atualizarStatusBicicleta(eq(bicicletaDisponivel.getNumero()), eq("EM_USO"));
        verify(externoClient, times(1)).enviarEmail(any());

        CiclistaEntity ciclistaAtualizado = ciclistaRepository.findById(ciclistaAtivo.getId()).orElseThrow();
        assertTrue(ciclistaAtualizado.isAluguelAtivo());

        Optional<AluguelEntity> aluguelSalvo = aluguelRepository.findByCiclista(ciclistaAtivo.getId());
        assertTrue(aluguelSalvo.isPresent());
        assertEquals(ciclistaAtivo.getId(), aluguelSalvo.get().getCiclista());
    }

    @Test
    void naoDeveRealizarAluguelQuandoCiclistaNaoAtivo() {
        ciclistaAtivo.setStatus(Status.INATIVO);
        ciclistaRepository.save(ciclistaAtivo); // Atualiza o status no banco

        Exception exception = assertThrows(TrataUnprocessableEntityException.class, () ->
                aluguelService.realizarAluguel(novoAluguelDTO));

        assertEquals("Ciclista não está ativo no sistema.", exception.getMessage());
        verifyNoInteractions(equipamentoClient, externoClient, eventPublisher);
    }

    @Test
    void naoDeveRealizarAluguelQuandoCiclistaJaPossuiAluguelAtivo() {
        ciclistaAtivo.setAluguelAtivo(true);
        ciclistaRepository.save(ciclistaAtivo); // Atualiza o status no banco

        // Cria um aluguel fictício para o ciclista para que findByCiclista encontre
        AluguelEntity aluguelExistente = new AluguelEntity();
        aluguelExistente.setCiclista(ciclistaAtivo.getId());
        aluguelExistente.setTrancaInicio(1);
        aluguelExistente.setNumeroBicicleta(10);
        aluguelExistente.setHoraInicio(LocalDateTime.now().minusHours(1));
        aluguelExistente.setCobranca(123L);
        aluguelExistente.setNomeTitular(ciclistaAtivo.getNome());
        aluguelExistente.setFinalCartao("4444");
        aluguelRepository.save(aluguelExistente);

        when(externoClient.enviarEmail(any(EnviaEmailDTO.class))).thenReturn(ResponseEntity.ok().build());

        Exception exception = assertThrows(TrataUnprocessableEntityException.class, () ->
                aluguelService.realizarAluguel(novoAluguelDTO));

        assertEquals("Ciclista já possui um aluguel ativo.", exception.getMessage());
        verify(externoClient, times(1)).enviarEmail(any(EnviaEmailDTO.class)); // Verifica se o email foi enviado
        verifyNoInteractions(equipamentoClient); // Não deve interagir com equipamentoClient
    }



    @Test
    void naoDeveRealizarAluguelQuandoBicicletaEmReparo() {
        bicicletaDisponivel.setStatus("EM_REPARO"); // Altera o status da bicicleta
        when(equipamentoClient.buscarTrancaPorId(trancaLivre.getNumero())).thenReturn(ResponseEntity.ok(trancaLivre));
        when(equipamentoClient.buscarBicicletaPorId(bicicletaDisponivel.getNumero())).thenReturn(ResponseEntity.ok(bicicletaDisponivel));

        Exception exception = assertThrows(TrataUnprocessableEntityException.class, () ->
                aluguelService.realizarAluguel(novoAluguelDTO));

        assertEquals("Esta bicicleta não pode ser alugada", exception.getMessage());
        verify(equipamentoClient, times(1)).buscarTrancaPorId(trancaLivre.getNumero());
        verify(equipamentoClient, times(1)).buscarBicicletaPorId(bicicletaDisponivel.getNumero());
        verifyNoMoreInteractions(equipamentoClient, externoClient, eventPublisher);
    }

    @Test
    void naoDeveRealizarAluguelQuandoCobrancaRecusada() {
        cobrancaPaga.setStatus("RECUSADA"); // Altera o status da cobrança
        when(equipamentoClient.buscarTrancaPorId(trancaLivre.getNumero())).thenReturn(ResponseEntity.ok(trancaLivre));
        when(equipamentoClient.buscarBicicletaPorId(bicicletaDisponivel.getNumero())).thenReturn(ResponseEntity.ok(bicicletaDisponivel));
        when(externoClient.realizarCobranca(any(NovaCobranca.class))).thenReturn(ResponseEntity.ok(cobrancaPaga));

        Exception exception = assertThrows(TrataUnprocessableEntityException.class, () ->
                aluguelService.realizarAluguel(novoAluguelDTO));

        assertEquals("Pagamento recusado.", exception.getMessage());
        verify(equipamentoClient, times(1)).buscarTrancaPorId(trancaLivre.getNumero());
        verify(equipamentoClient, times(1)).buscarBicicletaPorId(bicicletaDisponivel.getNumero());
        verify(externoClient, times(1)).realizarCobranca(any(NovaCobranca.class));
        verifyNoMoreInteractions(equipamentoClient, externoClient, eventPublisher);
    }



    @Test
    void deveRetornarBicicletaDoAluguelAtivoComSucesso() {
        // Primeiro, realiza um aluguel para que haja um ativo
        when(equipamentoClient.buscarTrancaPorId(trancaLivre.getNumero())).thenReturn(ResponseEntity.ok(trancaLivre));
        when(equipamentoClient.buscarBicicletaPorId(bicicletaDisponivel.getNumero())).thenReturn(ResponseEntity.ok(bicicletaDisponivel));
        when(externoClient.realizarCobranca(any(NovaCobranca.class))).thenReturn(ResponseEntity.ok(cobrancaPaga));
        when(equipamentoClient.liberaTranca(eq(trancaLivre.getNumero()), any(TrancarDestrancarDTO.class))).thenReturn(ResponseEntity.ok().build());
        when(equipamentoClient.atualizarStatusBicicleta(eq(bicicletaDisponivel.getNumero()), eq("EM_USO"))).thenReturn(ResponseEntity.ok().build());
        doNothing().when(eventPublisher).publishEvent(any());

        aluguelService.realizarAluguel(novoAluguelDTO); // Realiza o aluguel para ter um registro

        // Mock para a busca da bicicleta após o aluguel estar ativo
        when(equipamentoClient.buscarBicicletaPorId(bicicletaDisponivel.getNumero())).thenReturn(ResponseEntity.ok(bicicletaDisponivel));

        Optional<BicicletaDTO> result = aluguelService.buscarBicicletaDoAluguelAtivo(ciclistaAtivo.getId());

        assertTrue(result.isPresent());
        assertEquals(bicicletaDisponivel.getNumero(), result.get().getNumero());
        assertEquals(bicicletaDisponivel.getModelo(), result.get().getModelo());
        verify(equipamentoClient, times(2)).buscarBicicletaPorId(bicicletaDisponivel.getNumero()); // Uma vez no aluguel, outra na busca
    }

    @Test
    void deveRetornarOptionalVazioQuandoCiclistaNaoTemAluguelAtivo() {
        Optional<BicicletaDTO> result = aluguelService.buscarBicicletaDoAluguelAtivo(ciclistaAtivo.getId());

        assertFalse(result.isPresent());
        verifyNoInteractions(equipamentoClient); // Não deve chamar o cliente de equipamento se não há aluguel ativo
    }

    @Test
    void deveLancarExcecaoAoBuscarBicicletaDoAluguelAtivoQuandoCiclistaNaoEncontrado() {
        Exception exception = assertThrows(EntityNotFoundException.class, () ->
                aluguelService.buscarBicicletaDoAluguelAtivo(999));

        assertEquals("Ciclista não encontrado.", exception.getMessage());
        verifyNoInteractions(equipamentoClient);
    }

    @Test
    void deveRetornarOptionalVazioQuandoBuscarBicicletaFalha() {
        // Primeiro, realiza um aluguel para que haja um ativo
        when(equipamentoClient.buscarTrancaPorId(trancaLivre.getNumero())).thenReturn(ResponseEntity.ok(trancaLivre));
        when(equipamentoClient.buscarBicicletaPorId(bicicletaDisponivel.getNumero())).thenReturn(ResponseEntity.ok(bicicletaDisponivel));
        when(externoClient.realizarCobranca(any(NovaCobranca.class))).thenReturn(ResponseEntity.ok(cobrancaPaga));
        when(equipamentoClient.liberaTranca(eq(trancaLivre.getNumero()), any(TrancarDestrancarDTO.class))).thenReturn(ResponseEntity.ok().build());
        when(equipamentoClient.atualizarStatusBicicleta(eq(bicicletaDisponivel.getNumero()), eq("EM_USO"))).thenReturn(ResponseEntity.ok().build());
        doNothing().when(eventPublisher).publishEvent(any());

        aluguelService.realizarAluguel(novoAluguelDTO); // Realiza o aluguel para ter um registro

        // Mock para simular falha na busca da bicicleta
        when(equipamentoClient.buscarBicicletaPorId(bicicletaDisponivel.getNumero())).thenThrow(new RuntimeException("Erro ao buscar bicicleta"));

        Optional<BicicletaDTO> result = aluguelService.buscarBicicletaDoAluguelAtivo(ciclistaAtivo.getId());

        assertFalse(result.isPresent());
        verify(equipamentoClient, times(2)).buscarBicicletaPorId(bicicletaDisponivel.getNumero());
    }
}