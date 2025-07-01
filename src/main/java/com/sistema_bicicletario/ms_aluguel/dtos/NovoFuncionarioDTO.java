package com.sistema_bicicletario.ms_aluguel.dtos;

import com.sistema_bicicletario.ms_aluguel.entities.funcionario.Funcao;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class NovoFuncionarioDTO {
    private String nome;
    private String senha;
    private String confirmaSenha;
    @Email
    private String email;
    @Min(value = 16, message = "A idade mínima para funcionário é 16 anos")
    private int idade;
    @Pattern(regexp = "(^\\d{11}$)|(^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$)", message = "CPF deve estar no formato 12345678901 ou 123.456.789-01")
    private String cpf;
    private Funcao funcao;

    public NovoFuncionarioDTO(@NotBlank String nome, @NotBlank String senha, @NotBlank String confirmaSenha,
                              @NotBlank String email, @NotNull int idade, @NotBlank String cpf, @NotNull Funcao funcao) {
        this.nome = nome;
        this.senha = senha;
        this.confirmaSenha = confirmaSenha;
        this.email = email;
        this.idade = idade;
        this.cpf = cpf;
        this.funcao = funcao;
    }
    public boolean senhaValida() {
        if (senha == null || confirmaSenha == null) {
            return false;
        }
        return senha.equals(confirmaSenha);
    }
}
