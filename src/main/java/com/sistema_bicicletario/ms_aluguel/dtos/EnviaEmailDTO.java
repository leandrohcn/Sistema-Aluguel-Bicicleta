package com.sistema_bicicletario.ms_aluguel.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnviaEmailDTO {
    private String email;
    private String assunto;
    private String mensagem;
}
