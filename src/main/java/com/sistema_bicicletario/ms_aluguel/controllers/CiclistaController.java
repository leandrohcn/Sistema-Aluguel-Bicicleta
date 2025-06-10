package com.sistema_bicicletario.ms_aluguel.controllers;

import com.sistema_bicicletario.ms_aluguel.dtos.AtualizaCiclistaDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.NovoCiclistaDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.CiclistaResponseDTO;
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
    public ResponseEntity<CiclistaResponseDTO> criaCiclista(@Valid @RequestBody NovoCiclistaDTO ciclista) {
        try {
            CiclistaResponseDTO response = ciclistaService.cadastrarCiclista(ciclista);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CiclistaResponseDTO> buscaCiclista(@PathVariable Integer id) {
        return ciclistaService.buscaCiclistaporId(id).map(ciclistaEntity ->
                ResponseEntity.ok().body(new CiclistaResponseDTO(ciclistaEntity)))
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