package com.sistema_bicicletario.ms_aluguel.TestesUnitarios.controllers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.sistema_bicicletario.ms_aluguel.controllers.BancoController;
import com.sistema_bicicletario.ms_aluguel.repositories.AluguelRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.CartaoRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.CiclistaRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.FuncionarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class BancoControllerTest {
    @InjectMocks
    private BancoController controller;

    @Mock
    private CiclistaRepository ciclistaRepository;
    @Mock
    private AluguelRepository aluguelRepository;
    @Mock
    private CartaoRepository cartaoRepository;
    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Test
    void deveRestaurarBancoComSucesso() {
        ResponseEntity<String> response = (controller.restaurarBanco());

        verify(ciclistaRepository, times(1)).deleteAll();
        verify(aluguelRepository, times(1)).deleteAll();
        verify(cartaoRepository, times(1)).deleteAll();
        verify(funcionarioRepository, times(1)).deleteAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Banco restaurado com sucesso!", response.getBody());
    }

    @Test
    void deveLancarExcecaoQuandoRepositorioFalhar() {
        doThrow(new RuntimeException("Erro de conexão com o banco"))
                .when(ciclistaRepository).deleteAll();
        assertThrows(RuntimeException.class, () -> controller.restaurarBanco());
        verify(aluguelRepository, never()).deleteAll();
        verify(cartaoRepository, never()).deleteAll();
        verify(funcionarioRepository, never()).deleteAll();
    }
}
