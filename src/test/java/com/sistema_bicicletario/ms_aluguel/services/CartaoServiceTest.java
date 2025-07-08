package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
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
class CartaoServiceTest {

    @Mock
    private CartaoRepository cartaoRepository;

    @InjectMocks
    private CartaoService cartaoService;

    @Test
    void deveAtualizarCartaoComDadosValidos() {
        Integer id = 1;
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setId(id);
        CartaoDeCreditoEntity cartaoExistente = new CartaoDeCreditoEntity();
        cartaoExistente.setNomeTitular("Leandro");
        NovoCartaoDeCreditoDTO novoCartao = new NovoCartaoDeCreditoDTO(
                "João Silva", "2334", "12/30", "234523456"
        );
        when(cartaoRepository.findByCiclistaId(id)).thenReturn(Optional.of(cartaoExistente));

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
        CiclistaEntity ciclistaExistente = new CiclistaEntity();
        ciclistaExistente.setId(id);
        NovoCartaoDeCreditoDTO novoCartao = new NovoCartaoDeCreditoDTO(
                "João Silva", "1256", "12/30", "1245323245"
        );
        when(cartaoRepository.findByCiclistaId(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> cartaoService.atualizaCartao(id, novoCartao));
        verify(cartaoRepository, never()).save(any());
    }

    @Test
    void deveRetornarCartaoQuandoIdEhValidoEExiste() {
        Integer ciclistaId = 1;

        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setId(ciclistaId);

        CartaoDeCreditoEntity cartaoEntity = new CartaoDeCreditoEntity();
        cartaoEntity.setId(10);
        cartaoEntity.setNomeTitular("Leandro");
        cartaoEntity.setNumero("1234567890123456");
        cartaoEntity.setCvv("1234");
        cartaoEntity.setCiclista(ciclista);

        when(cartaoRepository.findByCiclistaId(ciclistaId)).thenReturn(Optional.of((cartaoEntity)));

        CartaoDeCreditoDTO resultadoDTO = cartaoService.buscaCartao(ciclistaId);

        assertNotNull(resultadoDTO);
        assertEquals(cartaoEntity.getNomeTitular(), resultadoDTO.getNomeTitular());
        assertEquals("1234567890123456", resultadoDTO.getNumero());
        assertEquals("1234", resultadoDTO.getCvv());

        verify(cartaoRepository, times(1)).findByCiclistaId(ciclistaId);
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
        CiclistaEntity ciclistaQueNaoExiste = new CiclistaEntity();
        ciclistaQueNaoExiste.setId(1646);
        when(cartaoRepository.findByCiclistaId(1646)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> cartaoService.buscaCartao(1646));
        verify(cartaoRepository, times(1)).findByCiclistaId(1646);
    }

    @Test
    void deveRetornarTrueQuandoCartaoExiste() {
        String numeroCartaoExistente = "123456789";
        when(cartaoRepository.findByNumero(numeroCartaoExistente)).thenReturn(Optional.of(new CartaoDeCreditoEntity()));
        boolean resultado = cartaoService.cartaoExiste(numeroCartaoExistente);
        assertTrue(resultado);
    }

    @Test
    void deveRetornarFalseQuandoCartaoNaoExiste() {
        String numeroCartaoInexistente = "987654321";
        when(cartaoRepository.findByNumero(numeroCartaoInexistente)).thenReturn(Optional.empty());
        boolean resultado = cartaoService.cartaoExiste(numeroCartaoInexistente);
        assertFalse(resultado);
    }
}