package com.sistema_bicicletario.ms_aluguel.entities.ciclista;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Embeddable
@Getter
@Setter
public class PassaporteEntity {

    private String numeroPassaporte;
    @DateTimeFormat(pattern = "MM/yyyy")
    private Date validadePassaporte;
    @Column(length = 2)
    private String pais;


    public PassaporteEntity(String numeroPassaporte, Date validadePassaporte,
                            String pais) {
        this.numeroPassaporte = numeroPassaporte;
        this.validadePassaporte = validadePassaporte;
        this.pais = pais;
    }

   public PassaporteEntity() {}

}
