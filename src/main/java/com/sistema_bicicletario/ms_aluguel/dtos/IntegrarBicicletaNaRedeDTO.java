package com.sistema_bicicletario.ms_aluguel.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IntegrarBicicletaNaRedeDTO {
    @NotNull
    private int idTranca;
    @NotNull
    private int idBicicleta;
    @NotNull
    private int idFuncionario;

}
