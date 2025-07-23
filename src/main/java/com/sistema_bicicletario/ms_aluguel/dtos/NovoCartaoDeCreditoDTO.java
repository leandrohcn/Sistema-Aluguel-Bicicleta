package com.sistema_bicicletario.ms_aluguel.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
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

    @NotBlank
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$", message = "Formato de validade deve ser yyyy-mm-dd")
    private LocalDate validadeCartao;

    @NotNull
    @Positive(message = "Deve ser um número positivo")
    @Pattern(regexp = "(^\\d{13,19}$)", message = "Quantidade de digitos entre 13 e 19")
    private String numeroCartao;

}
