package com.sistema_bicicletario.ms_aluguel.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class NovoFuncionarioDTO {

    @NotBlank
    private String nome;
    @NotBlank
    private String senha;
    @NotBlank
    private String confirmaSenha;
    @NotBlank
    private String email;
    @NotNull
    private int idade;
    @NotBlank
    private String cpf;
    @NotBlank
    private String funcao;

    public boolean senhaValida() {
        if (senha == null || confirmaSenha == null) {
            return false;
        }
            return senha.equals(confirmaSenha);
    }
    public boolean idadeValida() {
        return idade >= 16;
    }
}
