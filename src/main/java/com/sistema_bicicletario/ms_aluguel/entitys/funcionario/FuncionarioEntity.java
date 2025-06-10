package com.sistema_bicicletario.ms_aluguel.entitys.funcionario;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "funcionario")
public class FuncionarioEntity {
    @Id
    private long matricula;

    private String nome;
    private String senha;
    private String confirmaSenha;
    @Column(unique = true)
    private String email;
    private Integer idade;
    @Column(unique = true)
    private String cpf;
    private String funcao;

    public FuncionarioEntity(FuncionarioEntity funcionario) {
    }
}
