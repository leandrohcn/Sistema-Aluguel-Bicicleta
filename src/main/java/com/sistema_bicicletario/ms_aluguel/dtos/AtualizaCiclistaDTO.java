package com.sistema_bicicletario.ms_aluguel.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Nacionalidade;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Getter @AllArgsConstructor @Setter
public class AtualizaCiclistaDTO {

    private final String nome;
    private final String senha;
    private final String confirmaSenha;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private final LocalDate dataNascimento;
    private final String cpf;
    private final Nacionalidade nacionalidade;
    @Email
    private final String email;
    private final String urlFotoDocumento;
    private final PassaporteDTO passaporte;

    public boolean senhaValida() {
        if (senha == null || confirmaSenha == null) {
            return false;
        }
        return senha.equals(confirmaSenha);
    }
}
