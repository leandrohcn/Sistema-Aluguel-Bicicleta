package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.BicicletaDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.CobrancaDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.TrancaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ExternoSimulacaoTest {
    private ExternoSimulacao externoSimulacao;

    @BeforeEach
    void setUp() {
        externoSimulacao = new ExternoSimulacao();
        externoSimulacao.resetCounters();
    }


    @Test
    @DisplayName("Deve retornar TrancaDTO quando o ID da tranca existe")
    void getTranca_QuandoIdExiste_RetornaOptionalComTranca() {
        Integer idTranca = 10;

        Optional<TrancaDTO> resultado = externoSimulacao.getTranca(idTranca);

        assertTrue(resultado.isPresent(), "O Optional de TrancaDTO não deveria estar vazio");
        assertEquals(idTranca, resultado.get().getIdTranca());
        assertEquals(1, resultado.get().getIdBicicleta());
    }

    @Test
    @DisplayName("Deve retornar TrancaDTO com o primeiro idBicicleta (1) após o reset")
    void getTranca_AposReset_RetornaPrimeiroIdBicicleta() {
        //Como o @BeforeEach reseta o contador, a primeira chamada sempre retornará o ID de bicicleta 1.
        Integer idTranca = 100;

        Optional<TrancaDTO> resultado = externoSimulacao.getTranca(idTranca);

        assertTrue(resultado.isPresent());
        assertEquals(1, resultado.get().getIdBicicleta());
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando o ID da tranca não existe (999)")
    void getTranca_QuandoIdNaoExiste_RetornaOptionalVazio() {
        Integer idTranca = 999;
        Optional<TrancaDTO> resultado = externoSimulacao.getTranca(idTranca);
        assertTrue(resultado.isEmpty(), "O Optional de TrancaDTO deveria estar vazio");
    }

    @Test
    @DisplayName("Deve retornar BicicletaDTO com status DISPONIVEL")
    void getBicicleta_QuandoStatusDisponivel_RetornaBicicleta() {
        Integer idBicicleta = 1;

        Optional<BicicletaDTO> resultado = externoSimulacao.getBicicleta(idBicicleta);

        assertTrue(resultado.isPresent());
        assertEquals(idBicicleta, resultado.get().getIdBicicleta());
        assertEquals("DISPONIVEL", resultado.get().getStatus());
    }

    @Test
    @DisplayName("Deve retornar BicicletaDTO com status EM_REPARO")
    void getBicicleta_QuandoStatusEmReparo_RetornaBicicleta() {
        Integer idBicicleta = 3;

        Optional<BicicletaDTO> resultado = externoSimulacao.getBicicleta(idBicicleta);

        assertTrue(resultado.isPresent());
        assertEquals(idBicicleta, resultado.get().getIdBicicleta());
        assertEquals("EM_REPARO", resultado.get().getStatus());
    }

    @Test
    @DisplayName("Deve retornar cobrança PAGA para ciclista válido")
    void realizarCobranca_QuandoCiclistaValido_RetornaStatusPago() {
        Integer idCiclista = 2;
        Double valor = 100.0;

        CobrancaDTO cobranca = externoSimulacao.realizarCobranca(idCiclista, valor);

        assertNotNull(cobranca);
        assertEquals(idCiclista, cobranca.getCiclistaId());
        assertEquals(valor, cobranca.getValor());
        assertEquals("PAGO", cobranca.getStatus());
    }

    @Test
    @DisplayName("Deve retornar cobrança FALHOU para ciclista inválido (ID 3)")
    void realizarCobranca_QuandoCiclistaInvalido_RetornaStatusFalhou() {
        Integer idCiclista = 3;
        Double valor = 50.0;

        CobrancaDTO cobranca = externoSimulacao.realizarCobranca(idCiclista, valor);

        assertNotNull(cobranca);
        assertEquals(idCiclista, cobranca.getCiclistaId());
        assertEquals(valor, cobranca.getValor());
        assertEquals("FALHOU", cobranca.getStatus());
    }

    @Test
    @DisplayName("Deve incrementar o ID da cobrança a cada chamada")
    void realizarCobranca_DeveIncrementarId() {
        CobrancaDTO cobranca1 = externoSimulacao.realizarCobranca(10, 10.0);
        CobrancaDTO cobranca2 = externoSimulacao.realizarCobranca(11, 20.0);

        assertNotNull(cobranca1.getId());
        assertNotNull(cobranca2.getId());
        assertTrue(cobranca2.getId() > cobranca1.getId(), "O ID da segunda cobrança deve ser maior que o da primeira");
    }

    @Test
    @DisplayName("Não deve lançar exceção ao destrancar bicicleta com ID válido")
    void destrancarBicicleta_QuandoIdValido_NaoLancaExcecao() {
        assertDoesNotThrow(() -> externoSimulacao.destrancarBicicleta(10));
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao destrancar bicicleta com ID de falha (500)")
    void destrancarBicicleta_QuandoIdDeFalha_LancaRuntimeException() {
        Exception exception = assertThrows(RuntimeException.class, () -> externoSimulacao.destrancarBicicleta(500));
        assertEquals("Simulação de falha de comunicação com a tranca.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve executar o método de enviar email sem erros")
    void enviarEmail_DeveExecutarSemErro() {
        assertDoesNotThrow(() -> externoSimulacao.enviarEmail("CONFIRMACAO_ALUGUEL", "Ciclista: 1, Valor: R$10.00"));
    }

    @Test
    @DisplayName("Deve executar a alteração de status da bicicleta sem erros")
    void alterarStatusBicicleta_DeveExecutarSemErro() {
        assertDoesNotThrow(() -> externoSimulacao.alterarStatusBicicleta(123, "EM_USO"));
    }

    @Test
    @DisplayName("Deve executar o método de trancar bicicleta sem erros")
    void trancarBicicletaNaTranca_DeveExecutarSemErro() {
        assertDoesNotThrow(() -> externoSimulacao.trancarBicicletaNaTranca(45, 67));
    }

    @Test
    @DisplayName("Deve executar a alteração de status da tranca sem erros")
    void alterarStatusTranca_DeveExecutarSemErro() {
        assertDoesNotThrow(() -> externoSimulacao.alterarStatusTranca(88, "LIVRE"));
    }
}