package com.sistema_bicicletario.ms_aluguel.entitys.ciclista;


import com.sistema_bicicletario.ms_aluguel.entitys.cartao_de_credito.CartaoDeCreditoEntity;
import jakarta.persistence.*;
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
    private String email;

    @Enumerated(EnumType.STRING)
    private Nacionalidade nacionalidade;

    private String urlFotoDocumento;
    private String senha;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToOne(mappedBy = "ciclista", cascade = CascadeType.ALL, orphanRemoval = true)
    private CartaoDeCreditoEntity cartao;


}
