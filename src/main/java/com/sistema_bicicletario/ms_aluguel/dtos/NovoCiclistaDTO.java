package com.sistema_bicicletario.ms_aluguel.dtos;

import com.sistema_bicicletario.ms_aluguel.entitys.ciclista.Nacionalidade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Getter @Setter
public class NovoCiclistaDTO {

    @NotBlank
    private String nome;
    @NotNull
    private Date nascimento;
    @NotBlank
    private String cpf;

    @Valid
    @NotNull
    private PassaporteDTO passaporte;

    private Nacionalidade nacionalidade;

    @Email
    @NotBlank
    private String email;

    private String urlFotoDocumento;
    private String senha;

    @Valid
    @NotNull
    private NovoCartaoDeCreditoDTO meioDePagamento;
}
