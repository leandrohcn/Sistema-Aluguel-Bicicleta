package com.sistema_bicicletario.ms_aluguel.entities.funcionario;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "funcionario")
public class FuncionarioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int matricula;

    private String nome;
    private String senha;
    private String confirmaSenha;
    @Column(unique = true)
    private String email;
    private Integer idade;
    @Column(unique = true)
    private String cpf;
    private String funcao;

    public FuncionarioEntity(FuncionarioEntity funcionario){
        this.matricula = funcionario.getMatricula();
        this.nome = funcionario.getNome();
        this.senha = funcionario.getSenha();
        this.confirmaSenha = funcionario.getConfirmaSenha();
        this.email = funcionario.getEmail();
        this.idade = funcionario.getIdade();
        this.cpf = funcionario.getCpf();
        this.funcao = funcionario.getFuncao();
    }

    public FuncionarioEntity(String nome, String senha,
                             String confirmaSenha, String email,
                             Integer idade, String cpf, String funcao) {
        this.nome = nome;
        this.senha = senha;
        this.confirmaSenha = confirmaSenha;
        this.email = email;
        this.idade = idade;
        this.cpf = cpf;
        this.funcao = funcao;
    }

}
