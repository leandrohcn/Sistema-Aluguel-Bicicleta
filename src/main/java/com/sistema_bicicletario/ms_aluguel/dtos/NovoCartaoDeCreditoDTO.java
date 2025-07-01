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
    @Pattern(regexp = "^[0-9]{3,4}$",
            message = "Cvv entre 3 e 4 digitos")
    private String cvv;

    @NotBlank
    @Pattern(regexp = "^(0[1-9]|1[0-2])/(?:2[6-9]|[3-9]\\d)$", message = "Formato de validade deve ser MM/yy")


    //testar validade
    private String validadeCartao;

    @NotNull
    @Positive(message = "Deve ser um número positivo")
    @Pattern(regexp = "(^\\d{13,19}$)", message = "Quantidade de digitos entre 13 e 19")
    private String numeroCartao;

}
