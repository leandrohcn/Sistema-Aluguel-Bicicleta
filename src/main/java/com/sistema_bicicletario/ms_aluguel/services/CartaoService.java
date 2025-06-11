package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.NovoCartaoDeCreditoDTO;
import com.sistema_bicicletario.ms_aluguel.entitys.cartao_de_credito.CartaoDeCreditoEntity;
import com.sistema_bicicletario.ms_aluguel.repositorys.CartaoRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CartaoService {
    private final CartaoRepository cartaoRepository;

    public CartaoService(CartaoRepository cartaoRepository) {
        this.cartaoRepository = cartaoRepository;
    }

    public void atualizaCartao(Integer id, NovoCartaoDeCreditoDTO novoCartao) {
        CartaoDeCreditoEntity cartao = cartaoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cartão não encontrado"));

        cartao.setNomeTitular(novoCartao.getNomeTitular() != null ? novoCartao.getNomeTitular() : cartao.getNomeTitular());
        cartao.setNumero(novoCartao.getNumeroCartao() > 0 ? novoCartao.getNumeroCartao() : cartao.getNumero());
        cartao.setCvv(novoCartao.getCvv() > 0 ? novoCartao.getCvv() : cartao.getCvv());
        cartao.setValidade(novoCartao.getValidadeCartao() != null ? novoCartao.getValidadeCartao() : cartao.getValidade());

        cartaoRepository.save(cartao);
    }

    public Optional<CartaoDeCreditoEntity> buscaCartao(Integer id) {
        return cartaoRepository.findById(id);
    }
}
