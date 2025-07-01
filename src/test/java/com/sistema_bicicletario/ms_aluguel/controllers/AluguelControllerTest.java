//package com.sistema_bicicletario.ms_aluguel.controllers;
//
//import com.sistema_bicicletario.ms_aluguel.dtos.AluguelDTO;
//import com.sistema_bicicletario.ms_aluguel.dtos.DevolucaoDTO;
//import com.sistema_bicicletario.ms_aluguel.dtos.NovoAluguelDTO;
//import com.sistema_bicicletario.ms_aluguel.dtos.NovoDevolucaoDTO;
//import com.sistema_bicicletario.ms_aluguel.services.AluguelService;
//import com.sistema_bicicletario.ms_aluguel.services.DevolucaoService;
//import jakarta.persistence.EntityNotFoundException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import java.time.LocalDateTime;
//
//@ExtendWith(MockitoExtension.class)
//public class AluguelControllerTest {
//
//    @Mock
//    private AluguelService aluguelService;
//    @Mock
//    private DevolucaoService devolucaoService;
//    @InjectMocks
//    private AluguelController aluguelController;
//
//    private NovoAluguelDTO novoAluguel;
//    private AluguelDTO aluguelRetornado;
//
//    @BeforeEach
//    void setUp() {
//        novoAluguel = new NovoAluguelDTO();
//        novoAluguel.setCiclista(1);
//        novoAluguel.setTrancaInicio(100);
//
//        aluguelRetornado = new AluguelDTO();
//        aluguelRetornado.setIdBicicleta(1);
//        aluguelRetornado.setCobranca(10);
//        aluguelRetornado.setTrancaFim(0);
//        aluguelRetornado.setTrancaInicio(100);
//        aluguelRetornado.setHoraInicio(LocalDateTime.now());
//    }
//
//    @Test
//    void deveRealizarAluguelComSucesso() {
//        when(aluguelService.realizarAluguel(novoAluguel)).thenReturn(aluguelRetornado);
//
//        ResponseEntity<AluguelDTO> resposta = aluguelController.realizarAluguel(novoAluguel);
//
//        assertEquals(HttpStatus.OK, resposta.getStatusCode());
//        assertNotNull(resposta.getBody());
//        assertEquals(aluguelRetornado.getIdBicicleta(), resposta.getBody().getIdBicicleta());
//        assertEquals(aluguelRetornado.getTrancaInicio(), resposta.getBody().getTrancaInicio());
//
//        verify(aluguelService).realizarAluguel(novoAluguel);
//    }
//
//    @Test
//    void deveDevolverBicicletaComSucesso() {
//        NovoDevolucaoDTO requestDto = new NovoDevolucaoDTO();
//        requestDto.setIdBicicleta(1);
//        requestDto.setIdTranca(202);
//
//        DevolucaoDTO respostaDoServico = new DevolucaoDTO();
//        respostaDoServico.setBicicleta(1);
//        respostaDoServico.setTrancaFim(202);
//        when(devolucaoService.realizarDevolucao(requestDto)).thenReturn(respostaDoServico);
//
//        ResponseEntity<DevolucaoDTO> responseEntity = aluguelController.devolverBicicleta(requestDto);
//        assertNotNull(responseEntity);
//        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//        assertNotNull(responseEntity.getBody());
//        assertSame(respostaDoServico, responseEntity.getBody());
//        assertEquals(1, responseEntity.getBody().getBicicleta());
//        assertEquals(202, responseEntity.getBody().getTrancaFim());
//    }
//
//    @Test
//    void devePropagarExcecaoQuandoServicoFalhar() {
//
//        NovoDevolucaoDTO requestDto = new NovoDevolucaoDTO();
//        requestDto.setIdBicicleta(999);
//
//        when(devolucaoService.realizarDevolucao(requestDto))
//                .thenThrow(new EntityNotFoundException("Aluguel não encontrado"));
//        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> aluguelController.devolverBicicleta(requestDto));
//        assertEquals("Aluguel não encontrado", exception.getMessage());
//    }
//
//}