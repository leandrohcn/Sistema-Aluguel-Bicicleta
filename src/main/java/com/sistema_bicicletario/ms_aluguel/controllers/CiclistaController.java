package com.sistema_bicicletario.ms_aluguel.controllers;

import com.sistema_bicicletario.ms_aluguel.dtos.AtualizaCiclistaDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.NovoCiclistaDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.CiclistaResponseDTO;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.services.CiclistaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ciclista")

public class CiclistaController {

    private final CiclistaService ciclistaService;

    public CiclistaController(CiclistaService ciclistaService) {
        this.ciclistaService = ciclistaService;
    }

    @PostMapping
    public ResponseEntity<CiclistaResponseDTO> cadastrarCiclista(@Valid @RequestBody NovoCiclistaDTO ciclista) {
        CiclistaResponseDTO responseBody = ciclistaService.cadastrarCiclista(ciclista);
        return new ResponseEntity<>(responseBody, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CiclistaResponseDTO> buscarCiclista(@PathVariable Integer id) {
        return ciclistaService.buscarCiclistaporId(id).map(ciclistaEntity ->
                        ResponseEntity.ok().body(new CiclistaResponseDTO(ciclistaEntity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CiclistaResponseDTO> atualizarCiclista(@PathVariable Integer id,
                                                                 @Valid @RequestBody AtualizaCiclistaDTO ciclista) {
        try {
            CiclistaEntity c = ciclistaService.atualizarCiclista(id, ciclista);
            CiclistaResponseDTO dto = new CiclistaResponseDTO(c);
            return ResponseEntity.ok().body(dto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }

    }

    @PostMapping("/{id}/ativar")
    public ResponseEntity<CiclistaResponseDTO> ativarCiclista(@PathVariable Integer id) {
        CiclistaResponseDTO responseBody = ciclistaService.ativarCiclista(id);
        return ResponseEntity.ok().body(responseBody);
    }

    @GetMapping("/existeEmail/{email}")
    public ResponseEntity<Boolean> existeEmail(@PathVariable @Valid String email) {
        ciclistaService.existeEmail(email);
        return ResponseEntity.ok(true);
    }
}