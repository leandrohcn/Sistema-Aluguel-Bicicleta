package com.sistema_bicicletario.ms_aluguel.dtos;

import lombok.Getter;

@Getter
public class ErroDTO {
    private final String codigo;
    private final String mensagem;

    public ErroDTO(String codigo, String mensagem) {
        this.codigo = codigo;
        this.mensagem = mensagem;
    }

}

