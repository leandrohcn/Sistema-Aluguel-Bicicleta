package com.sistema_bicicletario.ms_aluguel.controllers;

import com.sistema_bicicletario.ms_aluguel.dtos.AluguelDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.BicicletaDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.NovoAluguelDTO;
import com.sistema_bicicletario.ms_aluguel.services.AluguelService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class AluguelControllerTest {

    @Mock
    private AluguelService aluguelService;

    @InjectMocks
    private AluguelController aluguelController;

    private NovoAluguelDTO novoAluguel;
    private AluguelDTO aluguelRetornado;

    @BeforeEach
    void setUp() {
        novoAluguel = new NovoAluguelDTO();
        novoAluguel.setCiclista(1);
        novoAluguel.setTrancaInicio(100);

        aluguelRetornado = new AluguelDTO();
        aluguelRetornado.setBicicleta(new BicicletaDTO(1, "caloi", "seila", "2024", 20, "Ativa"));
        aluguelRetornado.setCobranca(10);
        aluguelRetornado.setTrancaFim(0);
        aluguelRetornado.setTrancaInicio(100);
        aluguelRetornado.setHorarioInicio(LocalDateTime.now());
        aluguelRetornado.setHorarioFim(LocalDateTime.now());
    }

    @Test
    void deveRealizarAluguelComSucesso() {
        when(aluguelService.realizaAluguel(novoAluguel)).thenReturn(aluguelRetornado);

        ResponseEntity<AluguelDTO> resposta = aluguelController.realizarAluguel(novoAluguel);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(aluguelRetornado.getBicicleta(), resposta.getBody().getBicicleta());
        assertEquals(aluguelRetornado.getTrancaInicio(), resposta.getBody().getTrancaInicio());

        verify(aluguelService).realizaAluguel(novoAluguel);
    }

    @Test
    void deveRetornarNotFoundQuandoEntidadeNaoExiste() {
        when(aluguelService.realizaAluguel(novoAluguel)).thenThrow(EntityNotFoundException.class);

        ResponseEntity<AluguelDTO> resposta = aluguelController.realizarAluguel(novoAluguel);

        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
        assertNull(resposta.getBody());

        verify(aluguelService).realizaAluguel(novoAluguel);
    }
}
