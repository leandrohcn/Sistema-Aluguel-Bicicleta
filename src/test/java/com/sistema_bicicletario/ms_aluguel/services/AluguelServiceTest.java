//package com.sistema_bicicletario.ms_aluguel.services;
//
//import com.sistema_bicicletario.ms_aluguel.dtos.*;
//import com.sistema_bicicletario.ms_aluguel.entities.aluguel.AluguelEntity;
//import com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito.CartaoDeCreditoEntity;
//import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
//import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Status;
//import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessableEntityException;
//import com.sistema_bicicletario.ms_aluguel.repositories.AluguelRepository;
//import com.sistema_bicicletario.ms_aluguel.repositories.CartaoRepository;
//import com.sistema_bicicletario.ms_aluguel.repositories.CiclistaRepository;
//import jakarta.persistence.EntityNotFoundException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class AluguelServiceTest {
//    @Mock
//    private AluguelRepository aluguelRepository;
//    @Mock
//    private CiclistaRepository ciclistaRepository;
//    @Mock
//    private ExternoSimulacao externoSimulacao;
//    @Mock
//    private CartaoRepository cartaoRepository;
//    @InjectMocks
//    private AluguelService aluguelService;
//
//    private NovoAluguelDTO novoAluguelDTO;
//    private CiclistaEntity ciclistaValido;
//    private TrancaDTO trancaValida;
//    private BicicletaDTO bicicletaDisponivel;
//    private CobrancaDTO cobrancaPaga;
//    private CartaoDeCreditoEntity cartaoValido;
//
//    @BeforeEach
//    void setUp() {
//        novoAluguelDTO = new NovoAluguelDTO();
//        novoAluguelDTO.setCiclista(10);
//        novoAluguelDTO.setTrancaInicio(101);
//
//        ciclistaValido = new CiclistaEntity();
//        ciclistaValido.setId(10);
//        ciclistaValido.setAluguelAtivo(false);
//        ciclistaValido.setStatus(Status.ATIVO);
//
//        trancaValida = new TrancaDTO();
//        trancaValida.setIdTranca(101);
//        trancaValida.setIdBicicleta(1);
//
//        bicicletaDisponivel = new BicicletaDTO();
//        bicicletaDisponivel.setIdBicicleta(1);
//        bicicletaDisponivel.setStatus("DISPONIVEL");
//        bicicletaDisponivel.setNumero("123456");
//
//        cobrancaPaga = new CobrancaDTO();
//        cobrancaPaga.setId(123);
//        cobrancaPaga.setStatus("PAGO");
//
//        cartaoValido = new CartaoDeCreditoEntity();
//        cartaoValido.setId(novoAluguelDTO.getCiclista());
//        cartaoValido.setNomeTitular("Leandro");
//        cartaoValido.setNumero("123456789101112");
//    }
//
//    @Test
//    void deveRealizarAluguelComSucesso() {
//        when(externoSimulacao.getTranca(101)).thenReturn(Optional.of(trancaValida));
//        when(externoSimulacao.getBicicleta(1)).thenReturn(Optional.of(bicicletaDisponivel));
//        when(ciclistaRepository.findById(10)).thenReturn(Optional.of(ciclistaValido));
//        when(cartaoRepository.findById(10)).thenReturn(Optional.of(cartaoValido));
//        when(externoSimulacao.realizarCobranca(10, 10.00)).thenReturn(cobrancaPaga);
//        when(aluguelRepository.save(any(AluguelEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//
//        AluguelDTO resultado = aluguelService.realizarAluguel(novoAluguelDTO);
//        assertNotNull(resultado);
//        assertEquals(10, resultado.getCiclista());
//        assertEquals(1, resultado.getIdBicicleta());
//
//        verify(cartaoRepository, times(1)).findById(novoAluguelDTO.getCiclista());
//        verify(aluguelRepository, times(1)).save(any(AluguelEntity.class));
//        verify(ciclistaRepository, times(1)).save(any(CiclistaEntity.class));
//        verify(externoSimulacao, times(1)).destrancarBicicleta(101);
//        verify(externoSimulacao, times(1)).enviarEmail(anyString(), anyString());
//    }
//
//    @Test
//    void deveLancarExcecao_QuandoTrancaNaoEncontrada() {
//        when(externoSimulacao.getTranca(101)).thenReturn(Optional.empty());
//        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> aluguelService.realizarAluguel(novoAluguelDTO));
//        assertEquals("Tranca não encontrada.", exception.getMessage());
//    }
//
//    @Test
//    void deveLancarExcecao_QuandoCiclistaJaTemAluguelAtivo() {
//        ciclistaValido.setAluguelAtivo(true);
//        when(externoSimulacao.getTranca(101)).thenReturn(Optional.of(trancaValida));
//        when(externoSimulacao.getBicicleta(1)).thenReturn(Optional.of(bicicletaDisponivel));
//        when(ciclistaRepository.findById(10)).thenReturn(Optional.of(ciclistaValido));
//        TrataUnprocessableEntityException exception = assertThrows(TrataUnprocessableEntityException.class, () -> aluguelService.realizarAluguel(novoAluguelDTO));
//        assertEquals("Ciclista já possui um aluguel ativo.", exception.getMessage());
//    }
//
//    @Test
//    void deveLancarExcecao_QuandoBicicletaEmReparo() {
//        bicicletaDisponivel.setStatus("EM_REPARO");
//        when(externoSimulacao.getTranca(101)).thenReturn(Optional.of(trancaValida));
//        when(externoSimulacao.getBicicleta(1)).thenReturn(Optional.of(bicicletaDisponivel));
//        when(ciclistaRepository.findById(10)).thenReturn(Optional.of(ciclistaValido));
//        TrataUnprocessableEntityException exception = assertThrows(TrataUnprocessableEntityException.class, () -> aluguelService.realizarAluguel(novoAluguelDTO));
//        assertEquals("Esta bicicleta não pode ser alugada, pois está marcada para reparo.", exception.getMessage());
//    }
//
//    @Test
//    void deveLancarExcecao_QuandoPagamentoRecusado() {
//        CobrancaDTO cobrancaFalhou = new CobrancaDTO();
//        cobrancaFalhou.setStatus("FALHOU");
//        when(externoSimulacao.getTranca(101)).thenReturn(Optional.of(trancaValida));
//        when(externoSimulacao.getBicicleta(1)).thenReturn(Optional.of(bicicletaDisponivel));
//        when(ciclistaRepository.findById(10)).thenReturn(Optional.of(ciclistaValido));
//        when(cartaoRepository.findById(10)).thenReturn(Optional.of(cartaoValido));
//        when(externoSimulacao.realizarCobranca(10, 10.00)).thenReturn(cobrancaFalhou);
//        assertThrows(TrataUnprocessableEntityException.class, () -> aluguelService.realizarAluguel(novoAluguelDTO));
//        verify(aluguelRepository, never()).save(any());
//        verify(externoSimulacao, never()).destrancarBicicleta(anyInt());
//    }
//
//    @Test
//    void deveLancarExcecao_QuandoTrancaNaoAbre() {
//        doThrow(new RuntimeException("Simulação de falha de comunicação com a tranca."))
//                .when(externoSimulacao).destrancarBicicleta(101);
//
//        when(externoSimulacao.getTranca(101)).thenReturn(Optional.of(trancaValida));
//        when(externoSimulacao.getBicicleta(1)).thenReturn(Optional.of(bicicletaDisponivel));
//        when(ciclistaRepository.findById(10)).thenReturn(Optional.of(ciclistaValido));
//        when(cartaoRepository.findById(10)).thenReturn(Optional.of(cartaoValido));
//        when(externoSimulacao.realizarCobranca(10, 10.00)).thenReturn(cobrancaPaga);
//        when(aluguelRepository.save(any(AluguelEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        assertThrows(RuntimeException.class, () -> aluguelService.realizarAluguel(novoAluguelDTO));
//    }
//}