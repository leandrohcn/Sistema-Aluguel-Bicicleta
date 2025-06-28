package com.sistema_bicicletario.ms_aluguel.dtos;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Nacionalidade;
import lombok.Getter;

import java.time.LocalDate;


@Getter
public class CiclistaResponseDTO {
    private final int id;
    private final String status;
    private final String nome;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private final LocalDate nascimento;
    private final String cpf;
    private final Nacionalidade nacionalidade;
    private final String email;
    private final String urlFotoDocumento;

    private final PassaporteDTO passaporte;

    //esse dto deveria ter um construtor (ou criar outro dto) que nao tenha passaporte
    // se nao toda vez q o usuario inserir um ciclista nacional ele vai receber erro 500 pq ele vai procurar o passapporte na entity e nao vai encontrar
    // impossibilitando de criar esse dto


    public CiclistaResponseDTO(CiclistaEntity ciclista) {
        this.id = ciclista.getId();
        this.status = String.valueOf(ciclista.getStatus());
        this.nome = ciclista.getNome();
        this.nascimento = ciclista.getDataNascimento();
        this.cpf = ciclista.getCpf();
        this.nacionalidade = ciclista.getNacionalidade();
        this.email = ciclista.getEmail();
        this.urlFotoDocumento = ciclista.getUrlFotoDocumento();
        this.passaporte = new PassaporteDTO(
                ciclista.getPassaporteEntity().getNumeroPassaporte(),
                ciclista.getPassaporteEntity().getPais(),
                ciclista.getPassaporteEntity().getValidadePassaporte()
        );
    }

}