package com.sistema_bicicletario.ms_aluguel.controllers;

import com.sistema_bicicletario.ms_aluguel.dtos.NovoCartaoDTO;
import com.sistema_bicicletario.ms_aluguel.entitys.cartao_de_credito.CartaoDeCreditoEntity;
import com.sistema_bicicletario.ms_aluguel.repositorys.CartaoRepository;
import com.sistema_bicicletario.ms_aluguel.services.CartaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("cartao")
public class CartaoController {
    @Autowired
    private CartaoRepository cartaoRepository;
    @Autowired
    private CartaoService cartaoService;


    @GetMapping("/{id}")
    public ResponseEntity<CartaoDeCreditoEntity> buscarCartao(@PathVariable Integer id) {
        return cartaoRepository.findById(id).map(cartaoDeCreditoEntity ->
                ResponseEntity.ok().body(cartaoDeCreditoEntity)).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> atualizarCartao(@PathVariable Integer id,
                                                  @RequestBody NovoCartaoDTO cartao) {

        return cartaoService.atualizaCartao(id, cartao);
    }

}
