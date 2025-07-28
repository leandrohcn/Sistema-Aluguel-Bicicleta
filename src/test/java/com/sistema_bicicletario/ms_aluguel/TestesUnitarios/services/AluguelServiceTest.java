package com.sistema_bicicletario.ms_aluguel.TestesUnitarios.services;

import com.sistema_bicicletario.ms_aluguel.clients.EquipamentoClient;
import com.sistema_bicicletario.ms_aluguel.clients.ExternoClient;
import com.sistema_bicicletario.ms_aluguel.dtos.*;
import com.sistema_bicicletario.ms_aluguel.entities.aluguel.AluguelEntity;
import com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito.CartaoDeCreditoEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Status;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessableEntityException;
import com.sistema_bicicletario.ms_aluguel.repositories.AluguelRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.CartaoRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.CiclistaRepository;
import com.sistema_bicicletario.ms_aluguel.services.AluguelService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;


import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AluguelServiceTest {

    @Mock private AluguelRepository aluguelRepository;
    @Mock private CiclistaRepository ciclistaRepository;
    @Mock private EquipamentoClient equipamentoClient;
    @Mock private ExternoClient externoClient;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private CartaoRepository cartaoRepository;

    @InjectMocks private AluguelService aluguelService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private NovoAluguelDTO novoAluguelValido() {
        NovoAluguelDTO dto = new NovoAluguelDTO();
        dto.setCiclista(1);
        dto.setTrancaInicio(10);
        return dto;
    }

    private CiclistaEntity ciclistaAtivoSemAluguel() {
        CiclistaEntity c = new CiclistaEntity();
        c.setId(1);
        c.setStatus(Status.ATIVO);
        c.setAluguelAtivo(false);
        c.setEmail("teste@teste.com");
        c.setNome("Nome Teste");
        return c;
    }

    private CiclistaEntity ciclistaComAluguelAtivo() {
        CiclistaEntity c = new CiclistaEntity();
        c.setId(1);
        c.setStatus(Status.ATIVO);
        c.setAluguelAtivo(true);
        c.setEmail("teste@teste.com");
        c.setNome("Nome Teste");
        return c;
    }

    private CartaoDeCreditoEntity cartaoDeCredito() {
        CartaoDeCreditoEntity cartao = new CartaoDeCreditoEntity();
        cartao.setNumero("1234567812345678");
        return cartao;
    }

    private TrancaDTO trancaDTO() {
        TrancaDTO t = new TrancaDTO();
        t.setNumero(10);
        t.setBicicleta(100);
        return t;
    }

    private BicicletaDTO bicicletaDTO(String status) {
        BicicletaDTO b = new BicicletaDTO();
        b.setNumero(100);
        b.setStatus(status);
        return b;
    }

    private CobrancaDTO cobrancaDTO(String status) {
        CobrancaDTO c = new CobrancaDTO();
        c.setId(555L);
        c.setStatus(status);
        c.setValor(1000L);
        return c;
    }

    private AluguelEntity aluguelEntity() {
        AluguelEntity a = new AluguelEntity();
        a.setId(1);
        a.setCiclista(1);
        a.setTrancaInicio(10);
        a.setNumeroBicicleta(100);
        a.setHoraInicio(LocalDateTime.now());
        a.setCobranca(555L);
        a.setNomeTitular("Nome Teste");
        a.setFinalCartao("5678");
        return a;
    }

    // --- Testes ---

    @Test
    void realizarAluguel_comSucesso() {
        NovoAluguelDTO dto = novoAluguelValido();

        when(ciclistaRepository.findById(dto.getCiclista())).thenReturn(Optional.of(ciclistaAtivoSemAluguel()));
        when(aluguelRepository.findByCiclista(dto.getCiclista())).thenReturn(Optional.empty());
        when(equipamentoClient.buscarTrancaPorId(dto.getTrancaInicio())).thenReturn(ResponseEntity.ok(trancaDTO()));
        when(equipamentoClient.buscarBicicletaPorId(100)).thenReturn(ResponseEntity.ok(bicicletaDTO("DISPONIVEL")));
        when(externoClient.realizarCobranca(any())).thenReturn(ResponseEntity.ok(cobrancaDTO("PAGA")));
        when(cartaoRepository.findByCiclistaId(dto.getCiclista())).thenReturn(Optional.of(cartaoDeCredito()));
        when(aluguelRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AluguelDTO result = aluguelService.realizarAluguel(dto);

        assertNotNull(result);
        assertEquals(dto.getCiclista(), result.getCiclista());
        verify(eventPublisher, times(1)).publishEvent(any());
        verify(equipamentoClient).liberaTranca(anyInt(), any());
        verify(equipamentoClient).atualizarStatusBicicleta(anyInt(), eq("EM_USO"));
        verify(ciclistaRepository).save(any());
    }

    @Test
    void realizarAluguel_pagamentoRecusado_lancaExcecao() {
        NovoAluguelDTO dto = novoAluguelValido();

        when(ciclistaRepository.findById(dto.getCiclista())).thenReturn(Optional.of(ciclistaAtivoSemAluguel()));
        when(aluguelRepository.findByCiclista(dto.getCiclista())).thenReturn(Optional.empty());
        when(equipamentoClient.buscarTrancaPorId(dto.getTrancaInicio())).thenReturn(ResponseEntity.ok(trancaDTO()));
        when(equipamentoClient.buscarBicicletaPorId(100)).thenReturn(ResponseEntity.ok(bicicletaDTO("DISPONIVEL")));
        when(externoClient.realizarCobranca(any())).thenReturn(ResponseEntity.ok(cobrancaDTO("NEGADA")));

        TrataUnprocessableEntityException ex = assertThrows(TrataUnprocessableEntityException.class,
                () -> aluguelService.realizarAluguel(dto));
        assertEquals("Pagamento recusado.", ex.getMessage());
    }

    @Test
    void realizarAluguel_ciclistaNaoEncontrado_lancaExcecao() {
        NovoAluguelDTO dto = novoAluguelValido();

        when(ciclistaRepository.findById(dto.getCiclista())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> aluguelService.realizarAluguel(dto));
    }

    @Test
    void realizarAluguel_bicicletaEmReparo_lancaExcecao() {
        NovoAluguelDTO dto = novoAluguelValido();

        when(ciclistaRepository.findById(dto.getCiclista())).thenReturn(Optional.of(ciclistaAtivoSemAluguel()));
        when(aluguelRepository.findByCiclista(dto.getCiclista())).thenReturn(Optional.empty());
        when(equipamentoClient.buscarTrancaPorId(dto.getTrancaInicio())).thenReturn(ResponseEntity.ok(trancaDTO()));
        when(equipamentoClient.buscarBicicletaPorId(100)).thenReturn(ResponseEntity.ok(bicicletaDTO("EM_REPARO")));

        TrataUnprocessableEntityException ex = assertThrows(TrataUnprocessableEntityException.class,
                () -> aluguelService.realizarAluguel(dto));
        assertEquals("Esta bicicleta não pode ser alugada", ex.getMessage());
    }

    @Test
    void realizarAluguel_ciclistaComAluguelAtivo_lancaExcecao() {
        NovoAluguelDTO dto = novoAluguelValido();

        when(ciclistaRepository.findById(dto.getCiclista())).thenReturn(Optional.of(ciclistaComAluguelAtivo()));
        when(aluguelRepository.findByCiclista(dto.getCiclista())).thenReturn(Optional.of(aluguelEntity()));

        TrataUnprocessableEntityException ex = assertThrows(TrataUnprocessableEntityException.class,
                () -> aluguelService.realizarAluguel(dto));
        assertEquals("Ciclista já possui um aluguel ativo.", ex.getMessage());

        verify(externoClient).enviarEmail(any());
    }

    @Test
    void buscarBicicletaDoAluguelAtivo_comSucesso() {
        CiclistaEntity ciclista = ciclistaComAluguelAtivo();
        AluguelEntity aluguel = aluguelEntity();
        BicicletaDTO bikeDTO = bicicletaDTO("DISPONIVEL");

        when(ciclistaRepository.findById(ciclista.getId())).thenReturn(Optional.of(ciclista));
        when(aluguelRepository.findByCiclista(ciclista.getId())).thenReturn(Optional.of(aluguel));
        when(equipamentoClient.buscarBicicletaPorId(aluguel.getNumeroBicicleta())).thenReturn(ResponseEntity.ok(bikeDTO));

        Optional<BicicletaDTO> resultado = aluguelService.buscarBicicletaDoAluguelAtivo(ciclista.getId());

        assertTrue(resultado.isPresent());
        assertEquals(bikeDTO.getNumero(), resultado.get().getNumero());
    }

    @Test
    void buscarBicicletaDoAluguelAtivo_semAluguelAtivo_retornaVazio() {
        CiclistaEntity ciclista = ciclistaAtivoSemAluguel();

        when(ciclistaRepository.findById(ciclista.getId())).thenReturn(Optional.of(ciclista));

        Optional<BicicletaDTO> resultado = aluguelService.buscarBicicletaDoAluguelAtivo(ciclista.getId());

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarBicicletaDoAluguelAtivo_comErroNaConsulta_retornaVazio() {
        CiclistaEntity ciclista = ciclistaComAluguelAtivo();
        AluguelEntity aluguel = aluguelEntity();

        when(ciclistaRepository.findById(ciclista.getId())).thenReturn(Optional.of(ciclista));
        when(aluguelRepository.findByCiclista(ciclista.getId())).thenReturn(Optional.of(aluguel));
        when(equipamentoClient.buscarBicicletaPorId(aluguel.getNumeroBicicleta())).thenThrow(new RuntimeException());

        Optional<BicicletaDTO> resultado = aluguelService.buscarBicicletaDoAluguelAtivo(ciclista.getId());

        assertTrue(resultado.isEmpty());
    }
}
