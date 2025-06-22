package com.sistema_bicicletario.ms_aluguel.controllers;

import com.sistema_bicicletario.ms_aluguel.dtos.NovoCartaoDeCreditoDTO;
import com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito.CartaoDeCreditoEntity;
import com.sistema_bicicletario.ms_aluguel.services.CartaoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("cartaoDeCredito")
public class CartaoController {

    private final CartaoService cartaoService;

    public CartaoController(CartaoService cartaoService) {
        this.cartaoService = cartaoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartaoDeCreditoEntity> buscarCartao(@PathVariable Integer id) {
        CartaoDeCreditoEntity c = cartaoService.buscaCartao(id);
        return ResponseEntity.ok().body(c);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> atualizarCartao(@PathVariable Integer id,
                                                  @Valid @RequestBody NovoCartaoDeCreditoDTO cartao) {

        try {
            cartaoService.atualizaCartao(id, cartao);
            return ResponseEntity.ok("Dados atualizados com sucesso!");

        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
