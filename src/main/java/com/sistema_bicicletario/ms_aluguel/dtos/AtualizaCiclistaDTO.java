package com.sistema_bicicletario.ms_aluguel.dtos;

import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Nacionalidade;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Getter @AllArgsConstructor @Setter
public class AtualizaCiclistaDTO {

    private final String nome;
    private final LocalDate dataNascimento;
    private final String cpf;
    private final Nacionalidade nacionalidade;
    private final String email;
    private final String urlFotoDocumento;
    private final PassaporteDTO passaporte;

}
