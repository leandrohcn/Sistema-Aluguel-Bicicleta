package com.sistema_bicicletario.ms_aluguel.controllers;

import com.sistema_bicicletario.ms_aluguel.dtos.AluguelDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.NovoAluguelDTO;
import com.sistema_bicicletario.ms_aluguel.services.AluguelService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/aluguel")
public class AluguelController {

    private final AluguelService aluguelService;

    public AluguelController(AluguelService aluguelService) {
        this.aluguelService = aluguelService;
    }

    //cara ta faltando a entidade aluguel aqui
    //Devem ser registrados: a data/hora da retirada, o número da tranca,  o número da bicicleta,
    // o cartão usado para cobrança e o ciclista que a pegou
    // + as informações que estao no swagger


    // cade o caso de uso de devolver bicicleta?


    @PostMapping
    public ResponseEntity<AluguelDTO> realizarAluguel(@RequestBody @Valid NovoAluguelDTO dto) {

        // faltou a chamada fake pro serviço de tranca

        try {
            AluguelDTO aluguel = aluguelService.realizaAluguel(dto);
            //chamada fake pra enviar email

            return ResponseEntity.ok().body(aluguel);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
