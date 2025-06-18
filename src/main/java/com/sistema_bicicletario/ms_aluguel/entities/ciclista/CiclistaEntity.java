package com.sistema_bicicletario.ms_aluguel.entities.ciclista;


import com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito.CartaoDeCreditoEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;


@Data
@Entity
@Table(name = "ciclista")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CiclistaEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;
    @Column(unique = true)
    private String cpf;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date dataNascimento;

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
    private CartaoDeCreditoEntity cartao;

    public CiclistaEntity(String nome, Date dataNascimento, String cpf, String email,
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
