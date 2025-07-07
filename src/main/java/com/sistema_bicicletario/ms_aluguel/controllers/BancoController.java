package com.sistema_bicicletario.ms_aluguel.controllers;

import com.sistema_bicicletario.ms_aluguel.repositories.AluguelRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.CartaoRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.CiclistaRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.FuncionarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BancoController {
    AluguelRepository aluguelRepository;
    CartaoRepository cartaoRepository;
    CiclistaRepository ciclistaRepository;
    FuncionarioRepository funcionarioRepository;

    public BancoController(AluguelRepository aluguelRepository, CartaoRepository cartaoRepository, CiclistaRepository ciclistaRepository, FuncionarioRepository funcionarioRepository) {
        this.aluguelRepository = aluguelRepository;
        this.cartaoRepository = cartaoRepository;
        this.ciclistaRepository = ciclistaRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    @GetMapping("/restaurarBanco")
    public ResponseEntity<String> restaurarBanco(){

        ciclistaRepository.deleteAll();
        aluguelRepository.deleteAll();
        cartaoRepository.deleteAll();
        funcionarioRepository.deleteAll();

        return ResponseEntity.status(HttpStatus.OK).body("Banco restaurado com sucesso!");

    }
}
