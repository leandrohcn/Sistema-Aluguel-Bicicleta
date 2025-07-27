package com.sistema_bicicletario.ms_aluguel.clients;

import com.sistema_bicicletario.ms_aluguel.dtos.BicicletaDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.IntegrarBicicletaNaRedeDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.TrancaDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.TrancarDestrancarDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-equipamento", url = "${feign.equipamento.url}")
public interface EquipamentoClient {

    @GetMapping(value = "/tranca/{idTranca}")
    ResponseEntity<TrancaDTO> buscarTrancaPorId(@PathVariable(value = "idTranca") Integer idTranca);

    @PostMapping("/tranca/{idTranca}/destrancar")
    ResponseEntity<TrancaDTO> liberaTranca(@PathVariable(value = "idTranca") Integer idTranca, @RequestBody TrancarDestrancarDTO dto);

    @GetMapping("/bicicleta/{idBicicleta}")
    ResponseEntity<BicicletaDTO> buscarBicicletaPorId(@PathVariable(value = "idBicicleta") Integer idBicicleta);

    @PostMapping("/bicicleta/{idBicicleta}/status/{acao}")
    ResponseEntity<BicicletaDTO> atualizarStatusBicicleta(@PathVariable Integer idBicicleta, @PathVariable String acao);

    @PostMapping("/tranca/{idTranca}/status/{acao}")
    ResponseEntity<TrancaDTO> atualizarStatusTranca(@PathVariable Integer idTranca, @PathVariable String acao);

    @PostMapping("bicicleta/integrarNaRede")
    ResponseEntity<IntegrarBicicletaNaRedeDTO> integrarNaRede (@RequestBody IntegrarBicicletaNaRedeDTO dto);
}

