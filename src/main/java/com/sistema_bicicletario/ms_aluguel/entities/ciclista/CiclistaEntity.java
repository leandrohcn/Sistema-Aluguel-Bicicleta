package com.sistema_bicicletario.ms_aluguel.entities.ciclista;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "ciclista")
@Getter
@Setter
@NoArgsConstructor
public class CiclistaEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;
    @Column(unique = true)
    @Pattern(regexp = "(^\\d{11}$)|(^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$)", message = "CPF deve estar no formato 12345678901 ou 123.456.789-01")
    private String cpf;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataNascimento;

    @Embedded
    private PassaporteEntity passaporteEntity;

    @Column(unique = true)
    @Email
    private String email;

    @Enumerated(EnumType.STRING)
    private Nacionalidade nacionalidade;

    private String urlFotoDocumento;

    @Transient
    private String confirmaSenha;

    private String senha;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime horaConfirmacaoEmail;
    private boolean aluguelAtivo  = false;


    public CiclistaEntity(String nome, LocalDate dataNascimento, String email,
                          Nacionalidade nacionalidade, String urlFotoDocumento, String senha, String confirmaSenha) {
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.email = email;
        this.nacionalidade = nacionalidade;
        this.urlFotoDocumento = urlFotoDocumento;
        this.senha = senha;
        this.confirmaSenha = confirmaSenha;
    }

}
