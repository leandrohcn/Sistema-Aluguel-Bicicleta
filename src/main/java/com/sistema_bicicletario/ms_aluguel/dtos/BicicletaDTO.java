package com.sistema_bicicletario.ms_aluguel.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BicicletaDTO {
    private Integer idBicicleta;
    private String marca;
    private String modelo;
    private String ano;
    private Integer numero;
    private String status;
}
