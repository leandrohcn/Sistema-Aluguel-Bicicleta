package com.sistema_bicicletario.ms_aluguel.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;



@Getter @AllArgsConstructor @NoArgsConstructor @Setter
public class NovoCartaoDeCreditoDTO {

    @NotBlank
    private String nomeTitular;

    @NotNull
    private int cvv;

    @NotNull
    @DateTimeFormat(pattern = "MM/yyyy")
    private String validadeCartao;

    @NotNull
    private long numeroCartao;

}
