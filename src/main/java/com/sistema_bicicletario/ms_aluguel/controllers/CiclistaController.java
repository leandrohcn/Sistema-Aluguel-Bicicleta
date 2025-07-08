package com.sistema_bicicletario.ms_aluguel.controllers;

import com.sistema_bicicletario.ms_aluguel.dtos.*;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.services.AluguelService;
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
    private final AluguelService aluguelService;

    public CiclistaController(CiclistaService ciclistaService, AluguelService aluguelService) {
        this.ciclistaService = ciclistaService;
        this.aluguelService = aluguelService;
    }

    EnviaEmailDTO enviaEmail = new EnviaEmailDTO();

    @PostMapping
    public ResponseEntity<CiclistaResponseDTO> cadastrarCiclista(@Valid @RequestBody NovoCiclistaDTO ciclista) {
        CiclistaResponseDTO responseBody = ciclistaService.cadastrarCiclista(ciclista);
        enviaEmail.envioDeEmail();
        return new ResponseEntity<>(responseBody, HttpStatus.CREATED);
    }


    @GetMapping("/{id:\\d+}")
    public ResponseEntity<CiclistaResponseDTO> buscarCiclista(@PathVariable Integer id) {
        CiclistaEntity c = ciclistaService.buscarCiclistaporId(id);
        CiclistaResponseDTO responseBody = new CiclistaResponseDTO(c);
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<CiclistaResponseDTO> atualizarCiclista(@PathVariable Integer id,
                                                                 @Valid @RequestBody AtualizaCiclistaDTO ciclista) {

        CiclistaResponseDTO responseBody = ciclistaService.atualizarCiclista(id, ciclista);
        enviaEmail.envioDeEmail();
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @PostMapping("/{id:\\d+}/ativar")
    public ResponseEntity<CiclistaResponseDTO> ativarCiclista(@PathVariable Integer id) {
        CiclistaEntity ciclista = ciclistaService.ativarCiclista(id);
        return ResponseEntity.ok().body(new CiclistaResponseDTO(ciclista));
    }

    @GetMapping("/existeEmail/{email}")
    public ResponseEntity<Boolean> existeEmail(@PathVariable @Valid String email) {
        Boolean existe = ciclistaService.existeEmail(email);
        return new ResponseEntity<>(existe, HttpStatus.OK);
    }

    @GetMapping("/{id:\\d+}/permiteAluguel")
    public ResponseEntity<Boolean> permiteAluguel(@PathVariable Integer id) {

        return ResponseEntity.ok(ciclistaService.permiteAluguel(id));

    }

    @GetMapping("/{id:\\d+}/bicicletaAlugada")
    public ResponseEntity <Optional<BicicletaDTO>> bicicletaAlugada(@PathVariable Integer id) {
        Optional<BicicletaDTO> bicicleta = aluguelService.buscarBicicletaDoAluguelAtivo(id);
        return ResponseEntity.ok(bicicleta);
    }
}