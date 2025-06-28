package com.sistema_bicicletario.ms_aluguel.dtos;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NovoAluguelDTO {
    // nao sei se precisa desse ciclista nao mas por enquanto pode ficar ai
    @NotNull(message = "ID do ciclista é obrigatório")
    private int ciclista;
    @NotNull
    private int trancaInicio;
}
