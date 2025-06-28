package com.sistema_bicicletario.ms_aluguel.entities.ciclista;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sistema_bicicletario.ms_aluguel.dtos.ConfirmaEmailDTO;
import com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito.CartaoDeCreditoEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;


@Data
@Entity
@Table(name = "ciclista")
@Getter
@Setter
// nao acho uma boa ideia usar o allargsconstructor aqui, pq um ciclista nacional nunca vai ter passaporte e um ciclista estrangeiro nunca vai ter cpf
// criar dois construtores, um pra cada
@AllArgsConstructor
@NoArgsConstructor
public class CiclistaEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;
    @Column(unique = true)
    // criar um regex pra validar numero do cpf e usar o @pattern
    // validar que o ciclista estrangeiro NAO TEM CPF ( acho q oq eu falei do construtor garante isso)
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

    @OneToOne(mappedBy = "ciclista", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private CartaoDeCreditoEntity cartao;

    @Transient
    public ConfirmaEmailDTO confirmaEmail;

    public CiclistaEntity(String nome, LocalDate dataNascimento, String cpf, String email,
                          Nacionalidade nacionalidade, String urlFotoDocumento, String senha, String confirmaSenha) {
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.cpf = cpf;
        this.email = email;
        this.nacionalidade = nacionalidade;
        this.urlFotoDocumento = urlFotoDocumento;
        this.senha = senha;
        this.confirmaSenha = confirmaSenha;
    }

}
