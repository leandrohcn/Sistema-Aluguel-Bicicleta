package com.sistema_bicicletario.ms_aluguel.dtos;

import lombok.Data;

@Data
public class TrancaDTO {
    private Integer idTranca;
    private Integer numero;
    private String statusTranca;
    private Integer bicicleta;
}
