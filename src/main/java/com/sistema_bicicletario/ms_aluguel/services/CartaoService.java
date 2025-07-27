package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.clients.ExternoClient;
import com.sistema_bicicletario.ms_aluguel.dtos.CartaoDeCreditoDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.NovoCartaoDeCreditoDTO;
import com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito.CartaoDeCreditoEntity;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessableEntityException;
import com.sistema_bicicletario.ms_aluguel.listeners.EmailRealizadoEvent;
import com.sistema_bicicletario.ms_aluguel.repositories.CartaoRepository;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class CartaoService {
    private final CartaoRepository cartaoRepository;
    private final ExternoClient externoClient;
    private final ApplicationEventPublisher eventPublisher;

    public CartaoService(CartaoRepository cartaoRepository, ExternoClient externoClient, ApplicationEventPublisher eventPublisher) {
        this.cartaoRepository = cartaoRepository;
        this.externoClient = externoClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void atualizaCartao(Integer id, NovoCartaoDeCreditoDTO novoCartao) {
        String numeroCartao = novoCartao.getNumero();
        String finalCartao = numeroCartao.substring(numeroCartao.length() - 4);

        CartaoDeCreditoEntity cartao = cartaoRepository.findByCiclistaId(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        cartao.setNomeTitular(novoCartao.getNomeTitular());
        cartao.setNumero(novoCartao.getNumero());
        cartao.setCvv(novoCartao.getCvv());
        cartao.setValidade(novoCartao.getValidade());
        if (validarCartao(novoCartao)) {
            cartaoRepository.save(cartao);
        } else {
            throw new TrataUnprocessableEntityException("Cartao Invalido");
        }

        String assunto = "Atualização do cartão";
        String email = cartao.getCiclista().getEmail();
        String mensagem = "Dados atualizados com sucesso para seu cartão com final: " + finalCartao;

        EmailRealizadoEvent eventoEmail = EmailRealizadoEvent.of(this, email, assunto, mensagem);
        eventPublisher.publishEvent(eventoEmail);
    }

    public CartaoDeCreditoDTO buscaCartao(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }
        CartaoDeCreditoEntity cartao = cartaoRepository.findByCiclistaId(id)
                                                       .orElseThrow(() -> new EntityNotFoundException("Cartão não encontrado"));

        return new CartaoDeCreditoDTO(cartao);
    }

    public boolean cartaoExiste(String numero) {
        return cartaoRepository.findByNumero(numero).isPresent();
    }

    public boolean validarCartao(NovoCartaoDeCreditoDTO cartao) {
        try {
            externoClient.validarCartaoDeCredito(cartao);
            return true;
        } catch (FeignException e) {
            return false;
        }
    }
}
