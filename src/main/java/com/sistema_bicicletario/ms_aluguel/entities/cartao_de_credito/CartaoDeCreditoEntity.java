package com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter @Setter
@Table(name="Cartao_De_Credito")
public class CartaoDeCreditoEntity {
    @Id
    private Integer id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    @JsonBackReference
    private CiclistaEntity ciclista;

    private String nomeTitular;
    @Column(unique = true)
    private @NotNull long numero;

    private String validade;

    @Column(length = 4)
    private @NotNull int cvv;


    public CartaoDeCreditoEntity(String nomeTitular, long numero, int cvv,
                                 String validade, CiclistaEntity ciclista) {
        this.nomeTitular = nomeTitular;
        this.numero = numero;
        this.cvv = cvv;
        this.validade = validade;
        this.ciclista = ciclista;
    }

    public CartaoDeCreditoEntity() {

    }
}
