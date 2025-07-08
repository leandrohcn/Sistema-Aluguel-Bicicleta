package com.sistema_bicicletario.ms_aluguel.controllers;

import com.sistema_bicicletario.ms_aluguel.dtos.CartaoDeCreditoDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.NovoCartaoDeCreditoDTO;
import com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito.CartaoDeCreditoEntity;
import com.sistema_bicicletario.ms_aluguel.services.CartaoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class CartaoControllerTest {

    @Mock
    private CartaoService cartaoService;

    @InjectMocks
    private CartaoController cartaoController;

    private CartaoDeCreditoEntity cartao;

    @BeforeEach
    void setUp() {
        cartao = new CartaoDeCreditoEntity();
        cartao.setId(1);
        cartao.setCvv("1234");
        cartao.setNomeTitular("João da Silva");
        cartao.setNumero("1232341234");
        cartao.setValidade("12/30");
    }

    @Test
    void deveBuscarCartaoPorId() {
        CartaoDeCreditoDTO cartaoDeCreditoDTO = new CartaoDeCreditoDTO(cartao);
        when(cartaoService.buscaCartao(1)).thenReturn(cartaoDeCreditoDTO);

        ResponseEntity<CartaoDeCreditoDTO> resposta = cartaoController.buscarCartao(1);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(cartao.getId(), resposta.getBody().getId());
        assertEquals(cartao.getNomeTitular(), resposta.getBody().getNomeTitular());

        verify(cartaoService).buscaCartao(1);
    }

    @Test
    void deveAtualizarCartaoComSucesso() {
        NovoCartaoDeCreditoDTO novoCartao = new NovoCartaoDeCreditoDTO(
                "João da Silva", "1264", "12/29", "1457445522"
        );

        doNothing().when(cartaoService).atualizaCartao(eq(1), any());

        ResponseEntity<String> resposta = cartaoController.atualizarCartao(1, novoCartao);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals("Dados atualizados com sucesso!", resposta.getBody());

        verify(cartaoService).atualizaCartao(eq(1), any());
    }

    @Test
    void deveRetornarNotFoundAoAtualizarCartaoInexistente() {
        NovoCartaoDeCreditoDTO novoCartao = new NovoCartaoDeCreditoDTO(
                "Maria", "434", "11/28", "223435363"
        );

        doThrow(EntityNotFoundException.class)
                .when(cartaoService).atualizaCartao(eq(999), any());

        ResponseEntity<String> resposta = cartaoController.atualizarCartao(999, novoCartao);

        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());

        verify(cartaoService).atualizaCartao(eq(999), any());
    }
}
