package com.sistema_bicicletario.ms_aluguel.dtos;

import com.sistema_bicicletario.ms_aluguel.entities.funcionario.Funcao;
import com.sistema_bicicletario.ms_aluguel.entities.funcionario.FuncionarioEntity;
import lombok.Data;

@Data
public class FuncionarioResponseDTO {
    private int matricula;
    private String nome;
    private String senha;
    private String confirmaSenha;
    private String email;
    private Integer idade;
    private String cpf;
    private Funcao funcao;

    public FuncionarioResponseDTO(FuncionarioEntity funcionario) {
        this.matricula = funcionario.getMatricula();
        this.nome = funcionario.getNome();
        this.senha = funcionario.getSenha();
        this.email = funcionario.getEmail();
        this.idade = funcionario.getIdade();
        this.cpf = funcionario.getCpf();
        this.funcao = funcionario.getFuncao();
    }
}
