package com.sistema_bicicletario.ms_aluguel.controllers;

import com.sistema_bicicletario.ms_aluguel.dtos.NovoCartaoDeCreditoDTO;
import com.sistema_bicicletario.ms_aluguel.entitys.cartao_de_credito.CartaoDeCreditoEntity;
import com.sistema_bicicletario.ms_aluguel.services.CartaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;


@RestController
@RequestMapping("cartaoDeCredito")
public class CartaoController {

    private final CartaoService cartaoService;

    public CartaoController(CartaoService cartaoService) {
        this.cartaoService = cartaoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartaoDeCreditoEntity> buscarCartao(@PathVariable Integer id) {
        return cartaoService.buscaCartao(id).map(cartaoDeCreditoEntity ->
                ResponseEntity.ok().body(cartaoDeCreditoEntity))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> atualizarCartao(@PathVariable Integer id,
                                                  @RequestBody NovoCartaoDeCreditoDTO cartao) {

        try {
            cartaoService.atualizaCartao(id, cartao);
            return ResponseEntity.ok("Dados atualizados com sucesso!");

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
