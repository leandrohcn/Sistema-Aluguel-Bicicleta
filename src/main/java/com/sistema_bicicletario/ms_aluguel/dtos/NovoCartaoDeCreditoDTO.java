package com.sistema_bicicletario.ms_aluguel.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @AllArgsConstructor @NoArgsConstructor @Setter
public class NovoCartaoDeCreditoDTO {

    @NotBlank
    private String nomeTitular;

    @NotNull
    private String cvv;

    @NotBlank
    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "Formato de validade deve ser MM/yy")
    private String validadeCartao;

    @NotNull
    @Positive(message = "Deve ser um número positivo")
    private String numeroCartao;

}
