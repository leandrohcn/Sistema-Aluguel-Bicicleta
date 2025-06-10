package com.sistema_bicicletario.ms_aluguel.controllers;

import com.sistema_bicicletario.ms_aluguel.dtos.AtualizaCiclistaDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.NovoCiclistaDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.CiclistaResponseDTO;
import com.sistema_bicicletario.ms_aluguel.services.CiclistaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sistema_bicicletario.ms_aluguel.repositorys.CiclistaRepository;
import org.springframework.beans.factory.annotation.Autowired;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ciclista")

public class CiclistaController {
    @Autowired
    private CiclistaRepository ciclistaRepository;

    private final CiclistaService ciclistaService;

    @PostMapping
    public ResponseEntity<CiclistaResponseDTO> addCiclista(@Valid @RequestBody NovoCiclistaDTO ciclista) {
        CiclistaResponseDTO response = ciclistaService.cadastrarCiclista(ciclista);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CiclistaResponseDTO> getCiclista(@PathVariable Integer id) {
        return ciclistaRepository.findById(id)
                .map(ciclista -> ResponseEntity.ok(new CiclistaResponseDTO(ciclista)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CiclistaResponseDTO> atualizaCiclista(@PathVariable Integer id,
                                                                 @Valid @RequestBody AtualizaCiclistaDTO ciclista) {

        return ciclistaService.atualizarCiclista(id, ciclista);
    }

    @PostMapping("/{id}/ativar")
    public ResponseEntity<CiclistaResponseDTO> ativarCiclista(@PathVariable Integer id){
        CiclistaResponseDTO ciclistaAtivado = ciclistaService.ativarCiclista(id).getBody();
        return ResponseEntity.ok(ciclistaAtivado);
    }

    @GetMapping("/existeEmail")
    public ResponseEntity<Boolean> existeEmail(@RequestParam String email) {
        boolean existe = ciclistaService.existeEmail(email);
        return ResponseEntity.ok(existe);
    }
}