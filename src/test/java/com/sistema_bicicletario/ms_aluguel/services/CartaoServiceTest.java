package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.repositories.CartaoRepository;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import com.sistema_bicicletario.ms_aluguel.dtos.*;
import com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartaoServiceTest {

    @Mock
    private CartaoRepository cartaoRepository;

    @InjectMocks
    private CartaoService cartaoService;

    @Test
    void deveAtualizarCartaoComDadosValidos() {
        Integer id = 1;
        CartaoDeCreditoEntity cartaoExistente = new CartaoDeCreditoEntity();
        NovoCartaoDeCreditoDTO novoCartao = new NovoCartaoDeCreditoDTO(
                "João Silva", "2334", "12/30", "234523456"
        );

        when(cartaoRepository.findById(id)).thenReturn(Optional.of(cartaoExistente));

        cartaoService.atualizaCartao(id, novoCartao);

        assertEquals("João Silva", cartaoExistente.getNomeTitular());
        assertEquals("234523456", cartaoExistente.getNumero());
        assertEquals("2334", cartaoExistente.getCvv());
        assertEquals("12/30", cartaoExistente.getValidade());

        verify(cartaoRepository).save(cartaoExistente);
    }

    @Test
    void deveLancarExcecaoQuandoCartaoNaoExisteNaAtualizacao() {
        Integer id = 99;
        NovoCartaoDeCreditoDTO novoCartao = new NovoCartaoDeCreditoDTO(
                "João Silva", "1256", "12/30", "1245323245"
        );

        when(cartaoRepository.findById(id)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                cartaoService.atualizaCartao(id, novoCartao)
        );

        assertEquals("Cartão não encontrado", exception.getMessage());
        verify(cartaoRepository, never()).save(any());
    }

    @Test
    void deveRetornarCartaoQuandoIdEhValidoEExiste() {
        Integer id = 1;
        CartaoDeCreditoEntity cartao = new CartaoDeCreditoEntity();
        when(cartaoRepository.findById(id)).thenReturn(Optional.of(cartao));

        CartaoDeCreditoEntity resultado = cartaoService.buscaCartao(id);

        assertNotNull(resultado);
        assertEquals(cartao, resultado);
    }

    @Test
    void deveLancarExcecaoQuandoIdForNuloNaBusca() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                cartaoService.buscaCartao(null)
        );
        assertEquals("ID inválido", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoIdForMenorOuIgualAZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                cartaoService.buscaCartao(0)
        );
        assertEquals("ID inválido", exception.getMessage());
    }

    @Test
    void deveLancarExcecaoQuandoCartaoNaoForEncontradoNaBusca() {
        Integer id = 123;
        when(cartaoRepository.findById(id)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                cartaoService.buscaCartao(id)
        );

        assertEquals("Cartão não encontrado", exception.getMessage());
    }
}