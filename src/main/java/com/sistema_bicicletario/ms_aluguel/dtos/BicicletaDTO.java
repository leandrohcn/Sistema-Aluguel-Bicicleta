package com.sistema_bicicletario.ms_aluguel.dtos;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class BicicletaDTO {
    private Integer idBicicleta;
    private String marca;
    private String modelo;
    private String ano;
    private int numero;
    private String status;
}
