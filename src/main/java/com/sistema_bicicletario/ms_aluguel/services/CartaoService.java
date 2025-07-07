package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.CartaoDeCreditoDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.NovoCartaoDeCreditoDTO;
import com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito.CartaoDeCreditoEntity;
import com.sistema_bicicletario.ms_aluguel.repositories.CartaoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CartaoService {
    private final CartaoRepository cartaoRepository;

    public CartaoService(CartaoRepository cartaoRepository) {
        this.cartaoRepository = cartaoRepository;
    }

    public void atualizaCartao(Integer id, NovoCartaoDeCreditoDTO novoCartao) {
        CartaoDeCreditoEntity cartao = cartaoRepository.findByCiclistaId(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        cartao.setNomeTitular(novoCartao.getNomeTitular());
        cartao.setNumero(novoCartao.getNumeroCartao());
        cartao.setCvv(novoCartao.getCvv());
        cartao.setValidade(novoCartao.getValidadeCartao());
        cartaoRepository.save(cartao);
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
}
