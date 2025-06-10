package com.sistema_bicicletario.ms_aluguel.entitys.ciclista;

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


    public PassaporteEntity(String numero, Date validade, String pais) {
        this.numeroPassaporte = numero;
        this.validadePassaporte = validade;
        this.pais = pais;
    }

   public PassaporteEntity() {}

    public void setCiclista(CiclistaEntity ciclista) {
    }
}
