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
    public ResponseEntity<CiclistaResponseDTO> criarCiclista(@Valid @RequestBody NovoCiclistaDTO ciclista) {
        try {
            CiclistaResponseDTO response = ciclistaService.cadastrarCiclista(ciclista);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
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
        } catch (Exception e){
            return ResponseEntity.notFound().build();
        }

    }

    @PostMapping("/{id}/ativar")
    public ResponseEntity<CiclistaResponseDTO> ativarCiclista(@PathVariable Integer id){
        try {
            CiclistaResponseDTO ciclistaAtivado = ciclistaService.ativarCiclista(id);
            return ResponseEntity.ok(ciclistaAtivado);
        } catch (Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/existeEmail/{email}")
    public ResponseEntity<Boolean> existeEmail(@PathVariable String email) {
        if (ciclistaService.existeEmail(email)) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}