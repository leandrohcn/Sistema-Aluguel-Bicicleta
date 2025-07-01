package com.sistema_bicicletario.ms_aluguel.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.PassaporteEntity;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PassaporteDTO {
    private String numeroPassaporte;
    private String pais;
    @JsonFormat(pattern = ("MM/yyyy"))
    private String validadePassaporte;


    public PassaporteDTO(PassaporteEntity passaporteEntity){
        this.numeroPassaporte = passaporteEntity.getNumeroPassaporte();
        this.pais = passaporteEntity.getPais();
        this.validadePassaporte = passaporteEntity.getValidadePassaporte();
    }

    public PassaporteDTO(){}

}

