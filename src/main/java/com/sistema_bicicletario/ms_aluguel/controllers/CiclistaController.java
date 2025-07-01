package com.sistema_bicicletario.ms_aluguel.controllers;

import com.sistema_bicicletario.ms_aluguel.dtos.*;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.services.CiclistaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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

        enviarEmail();

        return new ResponseEntity<>(responseBody, HttpStatus.CREATED);
    }


    @GetMapping("/{id}")
    public ResponseEntity<CiclistaResponseDTO> buscarCiclista(@PathVariable Integer id) {
        CiclistaEntity c = ciclistaService.buscarCiclistaporId(id);
        CiclistaResponseDTO responseBody = new CiclistaResponseDTO(c);
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CiclistaResponseDTO> atualizarCiclista(@PathVariable Integer id,
                                                                 @Valid @RequestBody AtualizaCiclistaDTO ciclista) {

        CiclistaResponseDTO responseBody = ciclistaService.atualizarCiclista(id, ciclista);
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @PostMapping("/{id}/ativar")
    public ResponseEntity<CiclistaResponseDTO> ativarCiclista(@PathVariable Integer id) {
        CiclistaEntity ciclista = ciclistaService.ativarCiclista(id);
        return ResponseEntity.ok().body(new CiclistaResponseDTO(ciclista));
    }

    @GetMapping("/existeEmail/{email}")
    public ResponseEntity<Boolean> existeEmail(@PathVariable @Valid String email) {
        Boolean existe = ciclistaService.existeEmail(email);
        return new ResponseEntity<>(existe, HttpStatus.OK);
    }

    @GetMapping("/{id}/permiteAluguel")
    public ResponseEntity<Boolean> permiteAluguel(@PathVariable Integer id) {
        if (ciclistaService.permiteAluguel(id)) {
            return ResponseEntity.ok(true);
        }
            return ResponseEntity.ok(false);
    }

    @GetMapping("{id}/bicicletaAlugada")
    public ResponseEntity<BicicletaDTO> bicicletaAlugada(@PathVariable Integer id) {
            Optional<BicicletaDTO> bicicleta = ciclistaService.bicicletaAlugada(id);
            return bicicleta.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.noContent().build());
    }

    public void enviarEmail(){

    }
}