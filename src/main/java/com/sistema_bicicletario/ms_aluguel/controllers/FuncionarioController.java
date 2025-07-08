package com.sistema_bicicletario.ms_aluguel.controllers;


import com.sistema_bicicletario.ms_aluguel.dtos.FuncionarioResponseDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.NovoFuncionarioDTO;
import com.sistema_bicicletario.ms_aluguel.entities.funcionario.FuncionarioEntity;
import com.sistema_bicicletario.ms_aluguel.services.FuncionarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("/funcionario")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @GetMapping
    public ResponseEntity<List<FuncionarioEntity>> buscaTodosFuncionarios() {
            List<FuncionarioEntity> funcionarios = funcionarioService.buscaTodosFuncionario();
            return new ResponseEntity<>(funcionarios, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<FuncionarioResponseDTO> criaFuncionario(@Valid @RequestBody NovoFuncionarioDTO funcionario) {
       FuncionarioResponseDTO response = funcionarioService.criaFuncionario(funcionario);
       return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioResponseDTO> buscaFuncionarioPorId(@PathVariable Integer id) {
        FuncionarioResponseDTO response = funcionarioService.buscaFuncionarioPorId(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FuncionarioResponseDTO> atualizaDadosFuncionario(@PathVariable Integer id, @Valid @RequestBody NovoFuncionarioDTO funcionario) {

        FuncionarioResponseDTO response = funcionarioService.atualizaFuncionario(funcionario, id);
        return ResponseEntity.ok().body(response);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> excluiFuncionario(@PathVariable Integer id) {
        funcionarioService.excluiFuncionario(id);
        return ResponseEntity.ok().body("Dados removidos");
    }
}