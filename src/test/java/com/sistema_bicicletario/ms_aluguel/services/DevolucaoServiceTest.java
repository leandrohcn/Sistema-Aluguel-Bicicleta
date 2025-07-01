//package com.sistema_bicicletario.ms_aluguel.services;
//
//import com.sistema_bicicletario.ms_aluguel.dtos.CobrancaDTO;
//import com.sistema_bicicletario.ms_aluguel.dtos.DevolucaoDTO;
//import com.sistema_bicicletario.ms_aluguel.dtos.NovoDevolucaoDTO;
//import com.sistema_bicicletario.ms_aluguel.entities.aluguel.AluguelEntity;
//import com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito.CartaoDeCreditoEntity;
//import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
//import com.sistema_bicicletario.ms_aluguel.repositories.AluguelRepository;
//import com.sistema_bicicletario.ms_aluguel.repositories.CiclistaRepository;
//import jakarta.persistence.EntityNotFoundException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyInt;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class DevolucaoServiceTest {
//
//    @Mock
//    private AluguelRepository aluguelRepository;
//    @Mock
//    private ExternoSimulacao externoSimulacao;
//    @Mock
//    private CiclistaRepository ciclistaRepository;
//    @InjectMocks
//    private DevolucaoService devolucaoService;
//
//    private NovoDevolucaoDTO novoDevolucaoDTO;
//    private AluguelEntity aluguelAtivo;
//    private CiclistaEntity ciclista;
//
//    @BeforeEach
//    void setUp() {
//        novoDevolucaoDTO = new NovoDevolucaoDTO();
//        novoDevolucaoDTO.setIdBicicleta(1);
//        novoDevolucaoDTO.setIdTranca(202);
//
//        CartaoDeCreditoEntity cartao = new CartaoDeCreditoEntity();
//        cartao.setId(5);
//        cartao.setNumero("1231243456345");
//        cartao.setNomeTitular("Dranka");
//
//        aluguelAtivo = new AluguelEntity();
//        aluguelAtivo.setId(10);
//        aluguelAtivo.setIdBicicleta(1);
//        aluguelAtivo.setCiclista(5);
//        aluguelAtivo.setHoraInicio(LocalDateTime.now().minusHours(1));
//        aluguelAtivo.setNumeroBicicleta("324523");
//        aluguelAtivo.setTrancaInicio(22);
//        aluguelAtivo.setCobranca(10);
//        aluguelAtivo.setFinalCartao(cartao.getNumero());
//        aluguelAtivo.setNomeTitular(cartao.getNomeTitular());
//
//        ciclista = new CiclistaEntity();
//        ciclista.setId(5);
//        ciclista.setAluguelAtivo(true);
//
//    }
//
//    @Test
//    void deveRealizarDevolucaoComSucesso() {
//        when(aluguelRepository.findByIdBicicletaAndHoraFimIsNull(1)).thenReturn(Optional.of(aluguelAtivo));
//        when(aluguelRepository.save(any(AluguelEntity.class))).thenReturn(aluguelAtivo);
//        when(ciclistaRepository.findById(5)).thenReturn(Optional.of(ciclista));
//
//        DevolucaoDTO resultado = devolucaoService.realizarDevolucao(novoDevolucaoDTO);
//
//        assertNotNull(resultado);
//        assertEquals(202, resultado.getTrancaFim());
//        assertNotNull(resultado.getHoraFim());
//
//        verify(externoSimulacao, never()).realizarCobranca(anyInt(), anyDouble());
//        verify(externoSimulacao, times(1)).alterarStatusBicicleta(1, "DISPONIVEL");
//        verify(externoSimulacao, times(1)).trancarBicicletaNaTranca(202, 1);
//        verify(externoSimulacao, times(1)).enviarEmail(eq("Dados da devolucao: "), anyString());
//        verify(ciclistaRepository, times(1)).save(any(CiclistaEntity.class));
//    }
//
//    @Test
//    void deveRealizarDevolucaoComCobrancaExtra() {
//        aluguelAtivo.setHoraInicio(LocalDateTime.now().minusHours(3).minusMinutes(15));
//
//        CobrancaDTO cobrancaExtraPaga = new CobrancaDTO();
//        cobrancaExtraPaga.setStatus("PAGO");
//
//        when(aluguelRepository.findByIdBicicletaAndHoraFimIsNull(1)).thenReturn(Optional.of(aluguelAtivo));
//        when(aluguelRepository.save(any(AluguelEntity.class))).thenReturn(aluguelAtivo);
//        when(ciclistaRepository.findById(5)).thenReturn(Optional.of(ciclista));
//        when(externoSimulacao.realizarCobranca(5, 15.00)).thenReturn(cobrancaExtraPaga);
//
//
//        devolucaoService.realizarDevolucao(novoDevolucaoDTO);
//        verify(externoSimulacao, times(1)).realizarCobranca(5, 15.00);
//    }
//
//    @Test
//    void deveAlterarStatusParaReparo() {
//        novoDevolucaoDTO.setAcao("REPARO_SOLICITADO");
//
//        when(aluguelRepository.findByIdBicicletaAndHoraFimIsNull(1)).thenReturn(Optional.of(aluguelAtivo));
//        when(aluguelRepository.save(any(AluguelEntity.class))).thenReturn(aluguelAtivo);
//        when(ciclistaRepository.findById(5)).thenReturn(Optional.of(ciclista));
//
//        devolucaoService.realizarDevolucao(novoDevolucaoDTO);
//        verify(externoSimulacao, times(1)).alterarStatusBicicleta(1, "EM_REPARO");
//    }
//
//    @Test
//    void deveLancarExcecaoQuandoNaoHouverAluguelAtivo() {
//        when(aluguelRepository.findByIdBicicletaAndHoraFimIsNull(1)).thenReturn(Optional.empty());
//        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> devolucaoService.realizarDevolucao(novoDevolucaoDTO));
//        assertEquals("Nenhum aluguel ativo encontrado para esta bicicleta.", exception.getMessage());
//    }
//}