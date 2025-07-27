package com.sistema_bicicletario.ms_aluguel.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@Getter @AllArgsConstructor @NoArgsConstructor @Setter
public class NovoCartaoDeCreditoDTO {

    @NotBlank
    private String nomeTitular;

    @NotNull
    @Pattern(regexp = "^\\d{3,4}$",
            message = "Cvv entre 3 e 4 digitos")
    private String cvv;

    @NotNull
    @Future(message = "A validade do cartão deve ser uma data futura.")
    private LocalDate validade;

    @NotNull
    @Positive(message = "Deve ser um número positivo")
    @Pattern(regexp = "(^\\d{13,19}$)", message = "Quantidade de digitos entre 13 e 19")
    private String numero;

}
