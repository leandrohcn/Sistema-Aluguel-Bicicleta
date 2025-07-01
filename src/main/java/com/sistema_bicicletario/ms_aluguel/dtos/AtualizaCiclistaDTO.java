package com.sistema_bicicletario.ms_aluguel.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Nacionalidade;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@Getter @AllArgsConstructor @Setter @NoArgsConstructor
public class AtualizaCiclistaDTO {
    private String nome;
    private String senha;
    private String confirmaSenha;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataNascimento;
    @Pattern(regexp = "(^\\d{11}$)|(^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$)", message = "CPF deve estar no formato 12345678901 ou 123.456.789-01")
    private String cpf;
    private Nacionalidade nacionalidade;
    @Email
    private String email;
    private String urlFotoDocumento;
    private PassaporteDTO passaporte;

    public boolean senhaValida() {
        if (senha == null || confirmaSenha == null) {
            return false;
        }
        return senha.equals(confirmaSenha);
    }
}
