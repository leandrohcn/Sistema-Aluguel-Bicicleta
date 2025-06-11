package com.sistema_bicicletario.ms_aluguel.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


@Getter
public class PassaporteDTO {
    @NotBlank
    private String numeroPassaporte;
    @NotBlank
    private String pais;
    @NotNull
    @DateTimeFormat(pattern = "MM/yyyy")
    private Date validadePassaporte;

    @JsonCreator
    public PassaporteDTO(@JsonProperty("numero") String numero,
                         @JsonProperty("pais") String pais,
                         @JsonProperty("validade") Date validade) {
        this.numeroPassaporte = numero;
        this.pais = pais;
        this.validadePassaporte = validade;
    }

    public PassaporteDTO() {
    }


}

