package com.sistema_bicicletario.ms_aluguel.controllers;

import com.sistema_bicicletario.ms_aluguel.dtos.NovoFuncionarioDTO;
import com.sistema_bicicletario.ms_aluguel.entitys.funcionario.FuncionarioEntity;
import com.sistema_bicicletario.ms_aluguel.repositorys.FuncionarioRepository;
import com.sistema_bicicletario.ms_aluguel.services.FuncionarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/funcionarios")
public class FuncionarioController {
    @Autowired
    private FuncionarioRepository funcionarioRepository;
    @Autowired
    private FuncionarioService funcionarioService;

    @GetMapping
    public ResponseEntity<List<FuncionarioEntity>> getAllFuncionarios() {
        return new ResponseEntity<>(funcionarioRepository.findAll(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<FuncionarioEntity> addFuncionario(@RequestBody NovoFuncionarioDTO funcionario) {
        FuncionarioEntity response = funcionarioService.addFuncionario(funcionario);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioEntity> getFuncionarioById(@PathVariable Long id) {
        return funcionarioRepository.findById(id)
                .map(funcionario -> ResponseEntity.ok(new FuncionarioEntity()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FuncionarioEntity> atualizaDadosFuncionario(@PathVariable Long id,
                                                                      @RequestBody NovoFuncionarioDTO funcionario) {

        return funcionarioService.atualizaFuncionario(funcionario, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFuncionario(@PathVariable Long id) {
        try {
            funcionarioService.excluiFuncionario(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
