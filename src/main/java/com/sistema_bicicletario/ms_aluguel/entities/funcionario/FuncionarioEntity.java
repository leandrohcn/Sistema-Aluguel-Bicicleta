package com.sistema_bicicletario.ms_aluguel.entities.funcionario;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "funcionario")

//funcionario e ciclista sao parecidos, poderiam ser uma classe abstrata? nao sei (Tambem nao sei)
public class FuncionarioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int matricula;

    private String nome;
    private String senha;
    @Transient
    private String confirmaSenha;
    @Column(unique = true)
    @Email
    private String email;
    private Integer idade;
    @Column(unique = true)
    @Pattern(
            regexp = "(^\\d{11}$)|(^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$)",
            message = "CPF deve estar no formato 12345678901 ou 123.456.789-01"
    )
    private String cpf;
    @Enumerated(EnumType.STRING)
    private Funcao funcao;

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
