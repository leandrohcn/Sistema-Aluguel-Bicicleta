package com.sistema_bicicletario.ms_aluguel.clients;

import com.sistema_bicicletario.ms_aluguel.dtos.CobrancaDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.EnviaEmailDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.NovaCobranca;
import com.sistema_bicicletario.ms_aluguel.dtos.NovoCartaoDeCreditoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-externo", url = "${feign.externo.url}")
public interface ExternoClient {

    @PostMapping("/cobranca")
    ResponseEntity<CobrancaDTO> realizarCobranca(@RequestBody NovaCobranca cobrancaDTO);

    @PostMapping("/filaCobranca")
    ResponseEntity<CobrancaDTO> filaCobranca(@RequestBody NovaCobranca cobrancaDTO);

    @PostMapping("/enviarEmail")
    ResponseEntity<Void> enviarEmail(@RequestBody EnviaEmailDTO email);

    @GetMapping("/cobranca/{id}")
    ResponseEntity<CobrancaDTO> obterCobrancaPorId(@PathVariable Long id);

    @PostMapping("/validaCartaoDeCredito")
    ResponseEntity<Void> validarCartaoDeCredito(@RequestBody NovoCartaoDeCreditoDTO cartaoDeCredito);
}
