package com.sistema_bicicletario.ms_aluguel.controllers;


import com.sistema_bicicletario.ms_aluguel.dtos.NovoFuncionarioDTO;
import com.sistema_bicicletario.ms_aluguel.entities.funcionario.FuncionarioEntity;
import com.sistema_bicicletario.ms_aluguel.services.FuncionarioService;
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
    public ResponseEntity<FuncionarioEntity> criaFuncionario(@RequestBody NovoFuncionarioDTO funcionario) {
       FuncionarioEntity funcionarioEntity = funcionarioService.criaFuncionario(funcionario);
       return new ResponseEntity<>(funcionarioEntity, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioEntity> buscaFuncionarioPorId(@PathVariable Integer id) {
        FuncionarioEntity funcionario = funcionarioService.buscaFuncionarioPorId(id);
        return ResponseEntity.ok(funcionario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FuncionarioEntity> atualizaDadosFuncionario(@PathVariable Integer id, @RequestBody NovoFuncionarioDTO funcionario) {

        FuncionarioEntity funcionarioAtualizado = funcionarioService.atualizaFuncionario(funcionario, id);
        funcionarioAtualizado = new FuncionarioEntity(funcionarioAtualizado);
        return ResponseEntity.ok().body(funcionarioAtualizado);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> excluiFuncionario(@PathVariable Integer id) {
        funcionarioService.excluiFuncionario(id);
        return ResponseEntity.ok().body("Dados removidos");
    }
}