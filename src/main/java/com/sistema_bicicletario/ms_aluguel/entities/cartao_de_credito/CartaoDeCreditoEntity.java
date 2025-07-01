package com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "(^\\d{13,19}$)", message = "Quantidade de digitos entre 13 e 19")
    private @NotNull String numero;

    private String validade;

    @Pattern(regexp = "^[0-9]{3,4}$",
             message = "Cvv maximo de 4 digitos")
    private @NotNull String cvv;

    public CartaoDeCreditoEntity(String nomeTitular, String numero, String cvv,
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
