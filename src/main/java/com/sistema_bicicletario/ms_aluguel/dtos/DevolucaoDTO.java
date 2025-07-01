package com.sistema_bicicletario.ms_aluguel.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DevolucaoDTO {
    private Integer bicicleta;
    private LocalDateTime horaInicio;
    private Integer trancaFim;
    private LocalDateTime horaFim;
    private Integer cobranca;
    private Integer ciclista;
}
