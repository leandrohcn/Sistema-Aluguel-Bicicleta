package com.sistema_bicicletario.ms_aluguel.dtos;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NovoAluguelDTO {
    @NotNull
    private Integer ciclista;

    @NotNull
    private Integer trancaInicio;
}
