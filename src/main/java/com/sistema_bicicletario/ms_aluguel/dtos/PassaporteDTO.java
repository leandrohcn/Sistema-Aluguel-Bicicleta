package com.sistema_bicicletario.ms_aluguel.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


@Getter
public class PassaporteDTO {

    private String numero;
    private String pais;
    @NotNull
    @DateTimeFormat(pattern = "MM/yyyy")
    private Date validade;

    @JsonCreator
    public PassaporteDTO(@JsonProperty("numero") String numero,
                         @JsonProperty("pais") String pais,
                         @JsonProperty("validade") Date validade) {
        this.numero = numero;
        this.pais = pais;
        this.validade = validade;
    }

    public PassaporteDTO() {
    }


}

