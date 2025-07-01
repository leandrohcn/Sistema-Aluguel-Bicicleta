package com.sistema_bicicletario.ms_aluguel.entities.aluguel;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Data
public class AluguelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer idBicicleta;
    private String numeroBicicleta;
    private Integer ciclista;
    private Integer trancaInicio;
    private Integer cobranca;
    private LocalDateTime horaInicio;
    private LocalDateTime horaFim;
    private Integer trancaFim;
    private String nomeTitular;
    private String finalCartao;
    private Double valorExtra;
}
