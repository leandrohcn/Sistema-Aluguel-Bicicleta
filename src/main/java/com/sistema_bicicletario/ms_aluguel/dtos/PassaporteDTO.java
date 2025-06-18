package com.sistema_bicicletario.ms_aluguel.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.Date;


@Getter
public class PassaporteDTO {
    @NotBlank (message = "Preencha o número do Passaporte")
    private final String numeroPassaporte;
    @NotBlank (message = "Preencha o país")
    private final String pais;
    @NotNull (message = "Erro na validade")
    private final Date validadePassaporte;

    @JsonCreator
    public PassaporteDTO(@JsonProperty("numero") String numero,
                         @JsonProperty("pais") String pais,
                         @JsonProperty("validade") Date validade) {
        this.numeroPassaporte = numero;
        this.pais = pais;
        this.validadePassaporte = validade;
    }


}

