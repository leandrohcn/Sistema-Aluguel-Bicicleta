package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.BicicletaDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.CobrancaDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.TrancaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ExternoSimulacaoTest {
    private ExternoSimulacao externoSimulacao;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        externoSimulacao = new ExternoSimulacao();
        // Redireciona a saída padrão (System.out) para capturá-la nos testes
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    @DisplayName("Deve retornar TrancaDTO quando o ID da tranca existe")
    void getTranca_QuandoIdExiste_RetornaOptionalComTranca() {
        // Cenário
        Integer idTranca = 10;

        // Ação
        Optional<TrancaDTO> resultado = externoSimulacao.getTranca(idTranca);

        // Verificação
        assertTrue(resultado.isPresent(), "O Optional de TrancaDTO não deveria estar vazio");
        assertEquals(idTranca, resultado.get().getIdTranca());
        assertEquals(1, resultado.get().getIdBicicleta(), "Para idTranca != 100, idBicicleta deveria ser 1");
    }

    @Test
    @DisplayName("Deve retornar TrancaDTO com idBicicleta 2 quando idTranca é 100")
    void getTranca_QuandoId100_RetornaIdBicicleta2() {
        // Cenário
        Integer idTranca = 100;

        // Ação
        Optional<TrancaDTO> resultado = externoSimulacao.getTranca(idTranca);

        // Verificação
        assertTrue(resultado.isPresent());
        assertEquals(2, resultado.get().getIdBicicleta(), "Para idTranca == 100, idBicicleta deveria ser 2");
    }


    @Test
    @DisplayName("Deve retornar Optional vazio quando o ID da tranca não existe (999)")
    void getTranca_QuandoIdNaoExiste_RetornaOptionalVazio() {
        // Cenário
        Integer idTranca = 999;

        // Ação
        Optional<TrancaDTO> resultado = externoSimulacao.getTranca(idTranca);

        // Verificação
        assertTrue(resultado.isEmpty(), "O Optional de TrancaDTO deveria estar vazio");
    }

    @Test
    @DisplayName("Deve retornar BicicletaDTO com status DISPONIVEL")
    void getBicicleta_QuandoStatusDisponivel_RetornaBicicleta() {
        // Cenário
        Integer idBicicleta = 1;

        // Ação
        Optional<BicicletaDTO> resultado = externoSimulacao.getBicicleta(idBicicleta);

        // Verificação
        assertTrue(resultado.isPresent());
        assertEquals(idBicicleta, resultado.get().getIdBicicleta());
        assertEquals("DISPONIVEL", resultado.get().getStatus());
    }

    @Test
    @DisplayName("Deve retornar BicicletaDTO com status EM_REPARO")
    void getBicicleta_QuandoStatusEmReparo_RetornaBicicleta() {
        // Cenário
        Integer idBicicleta = 2;

        // Ação
        Optional<BicicletaDTO> resultado = externoSimulacao.getBicicleta(idBicicleta);

        // Verificação
        assertTrue(resultado.isPresent());
        assertEquals(idBicicleta, resultado.get().getIdBicicleta());
        assertEquals("EM_REPARO", resultado.get().getStatus());
    }

    @Test
    @DisplayName("Deve retornar cobrança PAGA para ciclista válido")
    void realizarCobranca_QuandoCiclistaValido_RetornaStatusPago() {
        // Cenário
        Integer idCiclista = 2;
        Double valor = 100.0;

        // Ação
        CobrancaDTO cobranca = externoSimulacao.realizarCobranca(idCiclista, valor);

        // Verificação
        assertNotNull(cobranca);
        assertEquals(idCiclista, cobranca.getCiclistaId());
        assertEquals(valor, cobranca.getValor());
        assertEquals("PAGO", cobranca.getStatus());
    }

    @Test
    @DisplayName("Deve retornar cobrança FALHOU para ciclista inválido (ID 1)")
    void realizarCobranca_QuandoCiclistaInvalido_RetornaStatusFalhou() {
        // Cenário
        Integer idCiclista = 1;
        Double valor = 50.0;

        // Ação
        CobrancaDTO cobranca = externoSimulacao.realizarCobranca(idCiclista, valor);

        // Verificação
        assertNotNull(cobranca);
        assertEquals(idCiclista, cobranca.getCiclistaId());
        assertEquals(valor, cobranca.getValor());
        assertEquals("FALHOU", cobranca.getStatus());
    }

    @Test
    @DisplayName("Deve incrementar o ID da cobrança a cada chamada")
    void realizarCobranca_DeveIncrementarId() {
        // Ação
        CobrancaDTO cobranca1 = externoSimulacao.realizarCobranca(10, 10.0);
        CobrancaDTO cobranca2 = externoSimulacao.realizarCobranca(11, 20.0);

        // Verificação
        assertNotNull(cobranca1.getId());
        assertNotNull(cobranca2.getId());
        assertTrue(cobranca2.getId() > cobranca1.getId(), "O ID da segunda cobrança deve ser maior que o da primeira");
    }

    @Test
    @DisplayName("Não deve lançar exceção ao destrancar bicicleta com ID válido")
    void destrancarBicicleta_QuandoIdValido_NaoLancaExcecao() {
        // Cenário e Ação
        assertDoesNotThrow(() -> externoSimulacao.destrancarBicicleta(10));
    }

    @Test
    @DisplayName("Deve lançar RuntimeException ao destrancar bicicleta com ID de falha (500)")
    void destrancarBicicleta_QuandoIdDeFalha_LancaRuntimeException() {
        // Cenário e Ação
        Exception exception = assertThrows(RuntimeException.class, () -> externoSimulacao.destrancarBicicleta(500));

        // Verificação
        assertEquals("Simulação de falha de comunicação com a tranca.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve imprimir os dados corretos ao enviar email")
    void enviarEmail_DeveImprimirDadosCorretos() {
        // Cenário
        String tipo = "CONFIRMACAO_ALUGUEL";
        String dados = "Ciclista: 1, Valor: R$10.00";

        // Ação
        externoSimulacao.enviarEmail(tipo, dados);

        // Verificação
        String saidaEsperada = "Tipo de E-mail: " + tipo + System.lineSeparator() + "Dados Enviados: " + dados;
        assertEquals(saidaEsperada.trim(), outputStreamCaptor.toString().trim());
    }

    @Test
    @DisplayName("Deve imprimir a alteração de status correta da bicicleta")
    void alterarStatusBicicleta_DeveImprimirStatusCorreto() {
        // Cenário
        Integer idBicicleta = 123;
        String novoStatus = "EM_USO";

        // Ação
        externoSimulacao.alterarStatusBicicleta(idBicicleta, novoStatus);

        // Verificação
        String saidaEsperada = "Alterando status da bicicleta " + idBicicleta + " para " + novoStatus;
        assertEquals(saidaEsperada, outputStreamCaptor.toString().trim());
    }

    @Test
    @DisplayName("Deve imprimir a mensagem correta ao trancar bicicleta")
    void trancarBicicletaNaTranca_DeveImprimirMensagemCorreta() {
        // Cenário
        Integer idTranca = 45;
        Integer idBicicleta = 67;

        // Ação
        externoSimulacao.trancarBicicletaNaTranca(idTranca, idBicicleta);

        // Verificação
        String saidaEsperada = "Trancando bicicleta " + idBicicleta + " na tranca " + idTranca + " e alterando status para OCUPADA.";
        assertEquals(saidaEsperada, outputStreamCaptor.toString().trim());
    }

    @Test
    @DisplayName("Deve imprimir a mensagem correta na abertura de tranca")
    void aberturaDeTranca_DeveImprimirMensagemCorreta() {
        // Cenário
        Integer idTranca = 88;
        String novoStatus = "DESTRANCADA";

        // Ação
        externoSimulacao.aberturaDeTranca(idTranca, novoStatus);

        // Verificação
        String saidaEsperada = "A tranca: " + idTranca + "está" + novoStatus;
        assertEquals(saidaEsperada, outputStreamCaptor.toString().trim());
    }
}
