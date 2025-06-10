package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.NovoCartaoDTO;
import com.sistema_bicicletario.ms_aluguel.repositorys.CartaoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CartaoService {
    private final CartaoRepository cartaoRepository;

    public CartaoService(CartaoRepository cartaoRepository) {
        this.cartaoRepository = cartaoRepository;
    }

    public ResponseEntity<String> atualizaCartao(Integer id, NovoCartaoDTO novoCartao) {
        return cartaoRepository.findById(id).map(cartaoDeCredito -> {
            cartaoDeCredito.setNomeTitular(novoCartao.getNomeTitular() != null ? novoCartao.getNomeTitular() : cartaoDeCredito.getNomeTitular());
            cartaoDeCredito.setNumero(novoCartao.getNumeroCartao() > 0 ? novoCartao.getNumeroCartao() : cartaoDeCredito.getNumero());
            cartaoDeCredito.setCvv(novoCartao.getCvv() > 0 ? novoCartao.getCvv() : cartaoDeCredito.getCvv());
            cartaoDeCredito.setValidade(novoCartao.getValidade() != null ? novoCartao.getValidade() : cartaoDeCredito.getValidade());

            cartaoRepository.save(cartaoDeCredito);
            return ResponseEntity.ok("Dados cadastrados com sucesso!");

        }).orElse(ResponseEntity.notFound().build());
    }
}
