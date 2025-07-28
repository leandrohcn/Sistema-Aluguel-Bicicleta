package com.sistema_bicicletario.ms_aluguel.TestesIntegracao;

import com.sistema_bicicletario.ms_aluguel.clients.EquipamentoClient;
import com.sistema_bicicletario.ms_aluguel.clients.ExternoClient;
import com.sistema_bicicletario.ms_aluguel.dtos.*;
import com.sistema_bicicletario.ms_aluguel.entities.aluguel.AluguelEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Status;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessableEntityException;
import com.sistema_bicicletario.ms_aluguel.repositories.AluguelRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.CiclistaRepository;
import com.sistema_bicicletario.ms_aluguel.services.DevolucaoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;


import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class DevolucaoServiceIT {

    @Autowired
    private DevolucaoService devolucaoService;

    @Autowired
    private AluguelRepository aluguelRepository;

    @Autowired
    private CiclistaRepository ciclistaRepository;

    @MockBean
    private EquipamentoClient equipamentoClient;

    @MockBean
    private ExternoClient externoClient;

    private AluguelEntity aluguel;
    private TrancaDTO trancaLivre;
    private NovoDevolucaoDTO devolucaoDTO;
    private CobrancaDTO cobranca;

    @BeforeEach
    void setup() {
        ciclistaRepository.deleteAll();
        aluguelRepository.deleteAll();

        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setEmail("teste@email.com");
        ciclista.setStatus(Status.ATIVO);
        ciclista = ciclistaRepository.save(ciclista);

        aluguel = new AluguelEntity();
        aluguel.setCiclista(ciclista.getId());
        aluguel.setNumeroBicicleta(100);
        aluguel.setHoraInicio(LocalDateTime.now().minusHours(3));
        aluguel.setNomeTitular("Titular Teste");
        aluguel.setFinalCartao("1234");
        aluguel.setCobranca(123L);
        aluguel = aluguelRepository.save(aluguel);

        trancaLivre = new TrancaDTO();
        trancaLivre.setNumero(10);
        trancaLivre.setStatusTranca("LIVRE");

        devolucaoDTO = new NovoDevolucaoDTO();
        devolucaoDTO.setBicicleta(100);
        devolucaoDTO.setNumero(10);
        devolucaoDTO.setAcao("NORMAL");

        cobranca = new CobrancaDTO();
        cobranca.setId(123L);
        cobranca.setStatus("PAGO");
        cobranca.setHoraSolicitacao(LocalDateTime.now());
    }

    @Test
    void deveRealizarDevolucaoComSucesso() {
        when(equipamentoClient.buscarTrancaPorId(10)).thenReturn(ResponseEntity.ok(trancaLivre));
        when(externoClient.realizarCobranca(any())).thenReturn(ResponseEntity.ok(cobranca));
        when(externoClient.obterCobrancaPorId(anyLong())).thenReturn(ResponseEntity.ok(cobranca));
        IntegrarBicicletaNaRedeDTO responseBody = new IntegrarBicicletaNaRedeDTO();
        responseBody.setIdBicicleta(100);
        responseBody.setIdTranca(10);
        when(equipamentoClient.integrarNaRede(any())).thenReturn(ResponseEntity.ok(responseBody));
        doReturn(ResponseEntity.ok().build()).when(externoClient).enviarEmail(any(EnviaEmailDTO.class));

        DevolucaoDTO resposta = devolucaoService.realizarDevolucao(devolucaoDTO);

        assertNotNull(resposta);
        assertEquals(100, resposta.getBicicleta());
    }
    @Test
    void deveRealizarDevolucaoSemCobrancaExtra() {
        aluguel.setHoraInicio(LocalDateTime.now().minusHours(1));
        aluguelRepository.save(aluguel);

        when(equipamentoClient.buscarTrancaPorId(10)).thenReturn(ResponseEntity.ok(trancaLivre));
        IntegrarBicicletaNaRedeDTO responseBody = new IntegrarBicicletaNaRedeDTO();
        responseBody.setIdBicicleta(100);
        responseBody.setIdTranca(10);
        when(equipamentoClient.integrarNaRede(any())).thenReturn(ResponseEntity.ok(responseBody));
        when(externoClient.obterCobrancaPorId(anyLong())).thenReturn(ResponseEntity.ok(cobranca));
        doReturn(ResponseEntity.ok().build()).when(externoClient).enviarEmail(any(EnviaEmailDTO.class));

        DevolucaoDTO resposta = devolucaoService.realizarDevolucao(devolucaoDTO);


        assertNotNull(resposta);

        verify(externoClient, never()).realizarCobranca(any());

        Optional<AluguelEntity> aluguelFechado = aluguelRepository.findById(aluguel.getId());
        assertTrue(aluguelFechado.isPresent());
        assertEquals(0, aluguelFechado.get().getValorExtra());
    }

    @Test
    void deveColocarCobrancaNaFilaQuandoPagamentoExtraFalha() {
        cobranca.setStatus("FALHA");
        when(equipamentoClient.buscarTrancaPorId(10)).thenReturn(ResponseEntity.ok(trancaLivre));
        when(externoClient.realizarCobranca(any())).thenReturn(ResponseEntity.ok(cobranca));

        IntegrarBicicletaNaRedeDTO responseBody = new IntegrarBicicletaNaRedeDTO();
        responseBody.setIdBicicleta(100);
        responseBody.setIdTranca(10);
        when(equipamentoClient.integrarNaRede(any())).thenReturn(ResponseEntity.ok(responseBody));
        when(externoClient.obterCobrancaPorId(anyLong())).thenReturn(ResponseEntity.ok(cobranca));
        doReturn(ResponseEntity.ok().build()).when(externoClient).enviarEmail(any(EnviaEmailDTO.class));

        DevolucaoDTO resposta = devolucaoService.realizarDevolucao(devolucaoDTO);

        assertNotNull(resposta);
        verify(externoClient, times(1)).realizarCobranca(any());
        verify(externoClient, times(1)).filaCobranca(any());
    }

    @Test
    void deveAtualizarStatusBicicletaParaReparoQuandoAcaoSolicitada() {
        devolucaoDTO.setAcao("REPARO_SOLICITADO");

        when(equipamentoClient.buscarTrancaPorId(10)).thenReturn(ResponseEntity.ok(trancaLivre));
        when(externoClient.realizarCobranca(any())).thenReturn(ResponseEntity.ok(cobranca));
        when(externoClient.obterCobrancaPorId(anyLong())).thenReturn(ResponseEntity.ok(cobranca));
        IntegrarBicicletaNaRedeDTO responseBody = new IntegrarBicicletaNaRedeDTO();
        responseBody.setIdBicicleta(100);
        responseBody.setIdTranca(10);
        when(equipamentoClient.integrarNaRede(any())).thenReturn(ResponseEntity.ok(responseBody));
        doReturn(ResponseEntity.ok().build()).when(externoClient).enviarEmail(any(EnviaEmailDTO.class));

        devolucaoService.realizarDevolucao(devolucaoDTO);

        verify(equipamentoClient, times(1)).atualizarStatusBicicleta(100, "EM_REPARO");
    }

    @Test
    void naoDeveRealizarDevolucaoQuandoTrancaOcupada() {
        trancaLivre.setStatusTranca("OCUPADA");
        when(equipamentoClient.buscarTrancaPorId(10)).thenReturn(ResponseEntity.ok(trancaLivre));

        Exception exception = assertThrows(TrataUnprocessableEntityException.class, () -> devolucaoService.realizarDevolucao(devolucaoDTO));

        assertEquals("A tranca de destino está OCUPADA.", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoAluguelAtivoNaoEncontrado() {
        devolucaoDTO.setBicicleta(999);
        when(equipamentoClient.buscarTrancaPorId(10)).thenReturn(ResponseEntity.ok(trancaLivre));

        Exception exception = assertThrows(EntityNotFoundException.class, () -> devolucaoService.realizarDevolucao(devolucaoDTO));

        assertEquals("Nenhum aluguel ativo encontrado para esta bicicleta.", exception.getMessage());
    }
}
