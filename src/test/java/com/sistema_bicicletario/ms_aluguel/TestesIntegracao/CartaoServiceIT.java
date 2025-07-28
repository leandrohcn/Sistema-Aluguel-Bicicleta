package com.sistema_bicicletario.ms_aluguel.TestesIntegracao;

import com.sistema_bicicletario.ms_aluguel.clients.ExternoClient;
import com.sistema_bicicletario.ms_aluguel.dtos.CartaoDeCreditoDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.EnviaEmailDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.NovoCartaoDeCreditoDTO;
import com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito.CartaoDeCreditoEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessableEntityException;
import com.sistema_bicicletario.ms_aluguel.repositories.CartaoRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.CiclistaRepository;
import com.sistema_bicicletario.ms_aluguel.services.CartaoService;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
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
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
public class CartaoServiceIT {

    @Autowired
    private CartaoService cartaoService;

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private CiclistaRepository ciclistaRepository;

    @MockBean
    private ExternoClient externoClient;

    @MockBean
    private ApplicationEventPublisher eventPublisher;

    private CiclistaEntity ciclista;
    private NovoCartaoDeCreditoDTO novoCartaoDTO;

    @BeforeEach
    void setup() {
        // Limpa os repositórios para garantir isolamento entre os testes
        cartaoRepository.deleteAll();
        ciclistaRepository.deleteAll();

        // Cria e salva um ciclista
        ciclista = new CiclistaEntity();
        ciclista.setEmail("ciclista.teste@email.com");
        ciclista = ciclistaRepository.save(ciclista);

        // Cria e salva um cartão associado ao ciclista
        CartaoDeCreditoEntity cartao = new CartaoDeCreditoEntity();
        cartao.setCiclista(ciclista);
        cartao.setNomeTitular("Nome Antigo");
        cartao.setNumero("1111222233334444");
        cartao.setValidade(LocalDate.of(2027,12,12));
        cartao.setCvv("123");
        cartaoRepository.save(cartao);

        // Cria um DTO com os novos dados do cartão para os testes de atualização
        novoCartaoDTO = new NovoCartaoDeCreditoDTO();
        novoCartaoDTO.setNomeTitular("Nome Novo Titular");
        novoCartaoDTO.setNumero("9999888877776666");
        novoCartaoDTO.setValidade(LocalDate.of(2027,12,12));
        novoCartaoDTO.setCvv("321");
    }

    @Test
    void deveAtualizarCartaoComSucesso() {
        when(externoClient.validarCartaoDeCredito(novoCartaoDTO)).thenReturn(ResponseEntity.ok().build());

        doReturn(ResponseEntity.ok().build()).when(externoClient).enviarEmail(any(EnviaEmailDTO.class));
        externoClient.enviarEmail(new EnviaEmailDTO(
                ciclista.getEmail(),
                "Atualizado com Sucesso!",
                "Você Atualizou o cartao: " + novoCartaoDTO.getNumero()
        ));
        cartaoService.atualizaCartao(ciclista.getId(), novoCartaoDTO);

        Optional<CartaoDeCreditoEntity> cartaoAtualizadoOpt = cartaoRepository.findByCiclistaId(ciclista.getId());
        assertTrue(cartaoAtualizadoOpt.isPresent());
        CartaoDeCreditoEntity cartaoAtualizado = cartaoAtualizadoOpt.get();

        assertEquals("Nome Novo Titular", cartaoAtualizado.getNomeTitular());
        assertEquals("9999888877776666", cartaoAtualizado.getNumero());
        verify(externoClient, times(1)).validarCartaoDeCredito(novoCartaoDTO);
        verify(externoClient, times(1)).enviarEmail(any(EnviaEmailDTO.class));
    }

    @Test
    void naoDeveAtualizarQuandoCartaoInvalido() {
        Request request = Request.create(Request.HttpMethod.POST, "/url", new HashMap<>(), null, new RequestTemplate());
        doThrow(new FeignException.BadRequest("Cartão inválido", request, null, null))
                .when(externoClient).validarCartaoDeCredito(novoCartaoDTO);

        Exception exception = assertThrows(TrataUnprocessableEntityException.class, () -> cartaoService.atualizaCartao(ciclista.getId(), novoCartaoDTO));
        assertEquals("Cartao Invalido", exception.getMessage());

        Optional<CartaoDeCreditoEntity> cartaoNaoAlteradoOpt = cartaoRepository.findByCiclistaId(ciclista.getId());
        assertTrue(cartaoNaoAlteradoOpt.isPresent());
        assertEquals("Nome Antigo", cartaoNaoAlteradoOpt.get().getNomeTitular());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void deveLancarExcecaoAoAtualizarCartaoDeUsuarioInexistente() {

        Integer idInexistente = 999;
        assertThrows(EntityNotFoundException.class, () -> cartaoService.atualizaCartao(idInexistente, novoCartaoDTO));
    }

    @Test
    void deveBuscarCartaoComSucesso() {
        CartaoDeCreditoDTO cartaoEncontrado = cartaoService.buscaCartao(ciclista.getId());

        assertNotNull(cartaoEncontrado);
        assertEquals("Nome Antigo", cartaoEncontrado.getNomeTitular());
        assertEquals("1111222233334444", cartaoEncontrado.getNumero());
    }

    @Test
    void deveLancarExcecaoAoBuscarCartaoInexistente() {
        Integer idSemCartao = 998;
        ciclistaRepository.save(new CiclistaEntity()); // Garante que o ID existe mas não tem cartão

        assertThrows(EntityNotFoundException.class, () -> cartaoService.buscaCartao(idSemCartao));
    }

    @Test
    void deveLancarExcecaoAoBuscarCartaoComIdInvalido() {
        assertThrows(IllegalArgumentException.class, () -> cartaoService.buscaCartao(null));
        assertThrows(IllegalArgumentException.class, () -> cartaoService.buscaCartao(0));
    }

    @Test
    void deveRetornarTrueSeCartaoExiste() {
        assertTrue(cartaoService.cartaoExiste("1111222233334444"));
    }

    @Test
    void deveRetornarFalseSeCartaoNaoExiste() {
        assertFalse(cartaoService.cartaoExiste("0000000000000000"));
    }
}