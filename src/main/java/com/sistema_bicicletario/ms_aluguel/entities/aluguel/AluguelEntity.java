package com.sistema_bicicletario.ms_aluguel.entities.aluguel;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Data
@Table(name = "aluguel")
public class AluguelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer numeroBicicleta;
    private Integer ciclista;
    private Integer trancaInicio;
    private Long cobranca;
    private LocalDateTime horaInicio;
    private LocalDateTime horaFim;
    private Integer trancaFim;
    private String nomeTitular;
    private String finalCartao;
    private Long valorExtra;
}
