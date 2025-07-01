//package com.sistema_bicicletario.ms_aluguel.controllers;
//
//import com.sistema_bicicletario.ms_aluguel.dtos.AluguelDTO;
//import com.sistema_bicicletario.ms_aluguel.dtos.DevolucaoDTO;
//import com.sistema_bicicletario.ms_aluguel.dtos.NovoAluguelDTO;
//import com.sistema_bicicletario.ms_aluguel.dtos.NovoDevolucaoDTO;
//import com.sistema_bicicletario.ms_aluguel.services.AluguelService;
//import com.sistema_bicicletario.ms_aluguel.services.DevolucaoService;
//import jakarta.validation.Valid;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//
//@RestController
//@RequestMapping("/aluguel")
//public class AluguelController {
//
//    private final AluguelService aluguelService;
//    private final DevolucaoService devolucaoService;
//
//    public AluguelController(AluguelService aluguelService, DevolucaoService devolucaoService) {
//        this.aluguelService = aluguelService;
//        this.devolucaoService = devolucaoService;
//    }
//
//    @PostMapping
//    public ResponseEntity<AluguelDTO> realizarAluguel(@RequestBody @Valid NovoAluguelDTO novoAluguel) {
//        AluguelDTO aluguelRealizado = aluguelService.realizarAluguel(novoAluguel);
//        return ResponseEntity.ok(aluguelRealizado);
//    }
//
//    @PostMapping("/devolucao")
//    public ResponseEntity<DevolucaoDTO> devolverBicicleta(@RequestBody @Valid NovoDevolucaoDTO devolucaoDTO) {
//        DevolucaoDTO devolucaoRealizada = devolucaoService.realizarDevolucao(devolucaoDTO);
//        return ResponseEntity.ok(devolucaoRealizada);
//    }
//}
