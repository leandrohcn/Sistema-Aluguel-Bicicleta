package com.sistema_bicicletario.ms_aluguel.entitys.cartao_de_credito;

import com.sistema_bicicletario.ms_aluguel.entitys.ciclista.CiclistaEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;


@Entity
@Getter @Setter
@Table(name="Cartao_De_Credito")
public class CartaoDeCreditoEntity {
    @Id
    private Integer id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private CiclistaEntity ciclista;

    private String nomeTitular;
    @Column(unique = true)
    private @NotNull long numero;

    @DateTimeFormat(pattern = "MM/yyyy")
    private String validade;

    @Column(length = 4)
    private @NotNull int cvv;


   public CartaoDeCreditoEntity(String nomeTitular, @NotNull long numero, String validade, @NotNull int cvv) {
        this.nomeTitular = nomeTitular;
        this.numero = numero;
        this.validade = validade;
        this.cvv = cvv;
   }

    public CartaoDeCreditoEntity() {

    }
}
