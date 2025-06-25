package com.sistema_bicicletario.ms_aluguel.dtos;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AluguelDTO {
    private BicicletaDTO bicicleta;
    private LocalDateTime horarioInicio;
    private LocalDateTime horarioFim;
    private Integer trancaInicio;
    private Integer trancaFim;
    private Integer cobranca;
    private Integer ciclista;

}

