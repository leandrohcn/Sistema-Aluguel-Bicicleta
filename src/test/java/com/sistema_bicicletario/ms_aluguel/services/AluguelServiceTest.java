package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.AluguelDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.NovoAluguelDTO;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Status;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessabeEntity;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AluguelServiceTest {

    @Mock
    private CiclistaService ciclistaService;

    @InjectMocks
    private AluguelService aluguelService;

    @Test
    void deveRealizarAluguelComSucesso() {

        Integer idCiclista = 1;
        Integer trancaInicioId = 101;

        NovoAluguelDTO novoAluguelDTO = new NovoAluguelDTO();
        novoAluguelDTO.setCiclista(idCiclista);
        novoAluguelDTO.setTrancaInicio(trancaInicioId);

        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setId(idCiclista);
        ciclista.setStatus(Status.ATIVO);
        when(ciclistaService.buscarCiclistaporId(idCiclista)).thenReturn(ciclista);
        when(ciclistaService.permiteAluguel(idCiclista)).thenReturn(true);


        LocalDateTime inicioTeste = LocalDateTime.now();
        AluguelDTO resultadoAluguel = aluguelService.realizaAluguel(novoAluguelDTO);


        verify(ciclistaService, times(1)).buscarCiclistaporId(idCiclista);
        verify(ciclistaService, times(1)).permiteAluguel(idCiclista);


        assertNotNull(resultadoAluguel);
        assertEquals(idCiclista, resultadoAluguel.getCiclista());
        assertEquals(trancaInicioId, resultadoAluguel.getTrancaInicio());
        assertTrue(resultadoAluguel.getHorarioInicio().isAfter(inicioTeste.minusSeconds(1)));
        assertTrue(resultadoAluguel.getHorarioInicio().isBefore(inicioTeste.plusSeconds(1)));
        assertEquals(resultadoAluguel.getHorarioInicio().plusHours(2), resultadoAluguel.getHorarioFim());
        assertEquals(10, resultadoAluguel.getCobranca());
        assertEquals(0, resultadoAluguel.getTrancaFim());
    }

    @Test
    void deveLancarExcecaoQuandoCiclistaNaoExiste() {
        NovoAluguelDTO dto = new NovoAluguelDTO();
        dto.setCiclista(999);

        when(ciclistaService.buscarCiclistaporId(999)).thenThrow(new EntityNotFoundException("Ciclista não encontrado"));

        assertThrows(EntityNotFoundException.class, () -> aluguelService.realizaAluguel(dto));
    }

    @Test
    void deveLancarExcecaoQuandoCiclistaNaoPodeAlugar() {
        Integer idCiclista = 1;
        NovoAluguelDTO novoAluguelDTO = new NovoAluguelDTO();
        novoAluguelDTO.setCiclista(idCiclista);
        novoAluguelDTO.setTrancaInicio(101);

        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setId(idCiclista);
        ciclista.setStatus(Status.INATIVO); // causa o lançamento da exceção "Ciclista não está ativo."

        when(ciclistaService.buscarCiclistaporId(idCiclista)).thenReturn(ciclista);

        TrataUnprocessabeEntity exception = assertThrows(
                TrataUnprocessabeEntity.class,
                () -> aluguelService.realizaAluguel(novoAluguelDTO)
        );

        assertEquals("Ciclista não está ativo.", exception.getMessage());

        verify(ciclistaService, times(1)).buscarCiclistaporId(idCiclista);
        verify(ciclistaService, never()).permiteAluguel(idCiclista);
    }
}
