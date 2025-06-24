package com.sistema_bicicletario.ms_aluguel.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class BicicletaDTO {
    private int idBicicleta;
    private String marca;
    private String modelo;
    private String ano;
    private int numero;
    private String status;
}
