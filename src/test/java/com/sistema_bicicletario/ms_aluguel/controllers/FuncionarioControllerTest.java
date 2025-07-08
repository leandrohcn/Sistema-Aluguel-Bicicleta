package com.sistema_bicicletario.ms_aluguel.controllers;

import com.sistema_bicicletario.ms_aluguel.dtos.FuncionarioResponseDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.NovoFuncionarioDTO;
import com.sistema_bicicletario.ms_aluguel.entities.funcionario.Funcao;
import com.sistema_bicicletario.ms_aluguel.entities.funcionario.FuncionarioEntity;
import com.sistema_bicicletario.ms_aluguel.services.FuncionarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuncionarioControllerTest {

    @InjectMocks
    private FuncionarioController controller;

    @Mock
    private FuncionarioService funcionarioService;

    private FuncionarioEntity funcionario;
    private NovoFuncionarioDTO novoFuncionario;
    private FuncionarioResponseDTO funcionarioResponseDTO;
    @BeforeEach
    void setUp() {

        funcionario = new FuncionarioEntity();
        funcionario.setMatricula(1);
        funcionario.setNome("Fulano");
        funcionario.setEmail("fulano@gmail.com");
        funcionario.setCpf("123456789");
        funcionario.setSenha("123456");
        funcionario.setConfirmaSenha("123456");
        funcionario.setFuncao(Funcao.ADMINISTRATIVO);
        funcionario.setIdade(19);

        novoFuncionario = new NovoFuncionarioDTO(
                "Maria", "1234", "1234", "maria@dominio",
                20, "123234245", Funcao.ADMINISTRATIVO
        );

        List<FuncionarioEntity> lista = List.of(funcionario);
        lenient().when(funcionarioService.buscaTodosFuncionario()).thenReturn(lista);

        funcionarioResponseDTO = new FuncionarioResponseDTO(funcionario);

    }

    @Test
    void deveRetornarTodosFuncionarios() {
        ResponseEntity<List<FuncionarioEntity>> resposta = controller.buscaTodosFuncionarios();

        assertNotNull(resposta.getBody());
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(1, resposta.getBody().size());
        verify(funcionarioService).buscaTodosFuncionario();
    }

    @Test
    void deveCriarFuncionario() {
        FuncionarioEntity funcionario2 = new FuncionarioEntity();
        funcionario2.setMatricula(3);

        novoFuncionario = new NovoFuncionarioDTO(
                "Maria", "1234", "1234", "maria@dominio",
                20, "123234245", Funcao.ADMINISTRATIVO
        );
        FuncionarioResponseDTO funcionarioResponseDTOLocal = new FuncionarioResponseDTO(funcionario2);
        when(funcionarioService.criaFuncionario(novoFuncionario)).thenReturn(funcionarioResponseDTOLocal);

        ResponseEntity<FuncionarioResponseDTO> resposta = controller.criaFuncionario(novoFuncionario);

        assertNotNull(resposta.getBody());
        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertEquals(funcionario2.getMatricula(), resposta.getBody().getMatricula());
        verify(funcionarioService).criaFuncionario(novoFuncionario);
    }

    @Test
    void deveBuscarFuncionarioPorId() {
        when(funcionarioService.buscaFuncionarioPorId(1)).thenReturn(funcionarioResponseDTO);

        ResponseEntity<FuncionarioResponseDTO> resposta = controller.buscaFuncionarioPorId(1);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(funcionario);
        assertEquals(1, funcionario.getMatricula());
        verify(funcionarioService).buscaFuncionarioPorId(1);
    }

    @Test
    void deveAtualizarFuncionario() {
        FuncionarioEntity funcionario2 = new FuncionarioEntity();
        FuncionarioResponseDTO funcionarioResponseDTO1 = new FuncionarioResponseDTO(funcionario2);
        funcionarioResponseDTO1.setMatricula(1);
        funcionarioResponseDTO1.setNome(novoFuncionario.getNome());
        when(funcionarioService.atualizaFuncionario(novoFuncionario, 1)).thenReturn(funcionarioResponseDTO1);

        ResponseEntity<FuncionarioResponseDTO> resposta = controller.atualizaDadosFuncionario(1, novoFuncionario);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals("Maria", resposta.getBody().getNome());
        verify(funcionarioService).atualizaFuncionario(novoFuncionario, 1);
    }

    @Test
    void deveExcluirFuncionario() {
        doNothing().when(funcionarioService).excluiFuncionario(1);

        ResponseEntity<String> resposta = controller.excluiFuncionario(1);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        verify(funcionarioService).excluiFuncionario(1);
    }
}
