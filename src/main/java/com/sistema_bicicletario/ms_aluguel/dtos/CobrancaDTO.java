package com.sistema_bicicletario.ms_aluguel.dtos;

import com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito.CartaoDeCreditoEntity;
import lombok.Data;

@Data
public class CobrancaDTO {
    private Integer id;
    private Double valor;
    private String status;
    private Integer ciclistaId;
    private CartaoDeCreditoEntity cartao;
}
