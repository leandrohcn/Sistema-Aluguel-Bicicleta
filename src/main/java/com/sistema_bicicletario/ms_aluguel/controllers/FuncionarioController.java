package com.sistema_bicicletario.ms_aluguel.controllers;

import com.sistema_bicicletario.ms_aluguel.dtos.NovoFuncionarioDTO;
import com.sistema_bicicletario.ms_aluguel.entitys.funcionario.FuncionarioEntity;
import com.sistema_bicicletario.ms_aluguel.services.FuncionarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @GetMapping
    public ResponseEntity<List<FuncionarioEntity>> buscaTodosFuncionarios() {
        try{
            List<FuncionarioEntity> funcionarios = funcionarioService.buscaTodosFuncionario();
            return new ResponseEntity<>(funcionarios, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<FuncionarioEntity> criaFuncionario(@RequestBody NovoFuncionarioDTO funcionario) {
        try {
            FuncionarioEntity response = funcionarioService.criaFuncionario(funcionario);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioEntity> buscaFuncionarioPorId(@PathVariable Long id) {
        try {
            FuncionarioEntity response = funcionarioService.buscaFuncionarioPorId(id);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<FuncionarioEntity> atualizaDadosFuncionario(@PathVariable Long id,
                                                                      @RequestBody NovoFuncionarioDTO funcionario) {
        try {
            funcionarioService.atualizaFuncionario(funcionario, id);
            return ResponseEntity.ok(new FuncionarioEntity());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluiFuncionario (@PathVariable Long id){
        try {
            funcionarioService.excluiFuncionario(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}
