package com.sistema_bicicletario.ms_aluguel.dtos;

import com.sistema_bicicletario.ms_aluguel.entities.funcionario.Funcao;
import jakarta.validation.constraints.Email;
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
    @Email
    private String email;
    @NotNull
    private int idade;
    @NotBlank
    private String cpf;
    @NotNull
    private Funcao funcao;

    public boolean senhaValida() {
        if (senha == null || confirmaSenha == null) {
            return false;
        }
            return senha.equals(confirmaSenha);
    }
    public boolean idadeValida() {
        return idade >= 16;
    }

    // as funções de validação deveriam estar dentro do construtor.
    // se eu nao posso criar um usuario < 16, nao tem pq deixar construir esse objeto
    public NovoFuncionarioDTO(String nome, String senha, String confirmaSenha,
                              String email, int idade, String cpf, Funcao funcao) {
        this.nome = nome;
        this.senha = senha;
        this.confirmaSenha = confirmaSenha;
        this.email = email;
        this.idade = idade;
        this.cpf = cpf;
        this.funcao = funcao;
    }
}
