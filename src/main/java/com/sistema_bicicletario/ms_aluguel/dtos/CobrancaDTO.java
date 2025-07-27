package com.sistema_bicicletario.ms_aluguel.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CobrancaDTO {
    private Long id;
    private Long valor;
    private String status;
    private Integer ciclistaId;
    private LocalDateTime horaSolicitacao;
    private LocalDateTime horaFinalizacao;

}
