package com.sistema_bicicletario.ms_aluguel.dtos;

import com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito.CartaoDeCreditoEntity;
import lombok.Data;

@Data
public class CartaoDeCreditoDTO {
    private Integer id;
    private String nomeTitular;
    private String numero;
    private String cvv;

    public CartaoDeCreditoDTO(CartaoDeCreditoEntity entity) {
        this.id = entity.getId();
        this.nomeTitular = entity.getNomeTitular();
        this.numero = entity.getNumero();
        this.cvv = entity.getCvv();
    }
}
