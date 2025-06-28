package com.sistema_bicicletario.ms_aluguel.entities.funcionario;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "funcionario")

//funcionario e ciclista sao parecidos, poderiam ser uma classe abstrata? nao sei
public class FuncionarioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int matricula;

    private String nome;
    private String senha;
    @Transient
    private String confirmaSenha;
    @Column(unique = true)
    //regex pro email
    private String email;
    private Integer idade;
    @Column(unique = true)
    //regex pro cpf
    private String cpf;
    @Enumerated(EnumType.STRING)
    private Funcao funcao;

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
                             Integer idade, String cpf, Funcao funcao) {
        this.nome = nome;
        this.senha = senha;
        this.confirmaSenha = confirmaSenha;
        this.email = email;
        this.idade = idade;
        this.cpf = cpf;
        this.funcao = funcao;
    }

}
