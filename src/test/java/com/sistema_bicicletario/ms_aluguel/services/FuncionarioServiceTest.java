package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.NovoFuncionarioDTO;
import com.sistema_bicicletario.ms_aluguel.entities.funcionario.Funcao;
import com.sistema_bicicletario.ms_aluguel.entities.funcionario.FuncionarioEntity;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessableEntityException;
import com.sistema_bicicletario.ms_aluguel.repositories.FuncionarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuncionarioServiceTest {

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @InjectMocks
    private FuncionarioService funcionarioService;

    @Test
    void deveCriarFuncionarioComDadosValidos() {
        NovoFuncionarioDTO dto = new NovoFuncionarioDTO("João", "123", "123",
                                                        "joao@email.com", 25, "12345678900", Funcao.REPARADOR);

        FuncionarioEntity funcionario = new FuncionarioEntity(dto.getNome(), dto.getSenha(), dto.getConfirmaSenha(),
                                                         dto.getEmail(), dto.getIdade(), dto.getCpf(), dto.getFuncao());

        when(funcionarioRepository.save(any())).thenReturn(funcionario);

        FuncionarioEntity result = funcionarioService.criaFuncionario(dto);

        assertNotNull(result);
        assertEquals(result, funcionario);
        verify(funcionarioRepository).save(any(FuncionarioEntity.class));
    }

    @Test
    void deveLancarErroQuandoDadosInvalidosAoCriar() {
        NovoFuncionarioDTO dto = new NovoFuncionarioDTO("João", "123", "456", "joao@email.com",
                                                        -1, "12345678900", Funcao.REPARADOR);

        assertThrows(TrataUnprocessableEntityException.class, () -> funcionarioService.criaFuncionario(dto));
        verify(funcionarioRepository, never()).save(any());
    }

    @Test
    void deveAtualizarFuncionario() {
        Integer id = 1;
        NovoFuncionarioDTO dto = new NovoFuncionarioDTO("Maria", "senha", "senha", "maria@email.com", 30, "11111111111", Funcao.ADMINISTRATIVO);
        FuncionarioEntity existente = new FuncionarioEntity("Antigo", "x", "x", "antigo@email.com", 50, "222", Funcao.REPARADOR);

        when(funcionarioRepository.findById(id)).thenReturn(Optional.of(existente));
        when(funcionarioRepository.save(any())).thenReturn(existente);

        FuncionarioEntity atualizado = funcionarioService.atualizaFuncionario(dto, id);

        assertEquals("Maria", atualizado.getNome());
        verify(funcionarioRepository).save(any());
    }

    @Test
    void deveExcluirFuncionarioComIdValido() {
        Integer id = 1;
        when(funcionarioRepository.existsById(id)).thenReturn(true);

        funcionarioService.excluiFuncionario(id);

        verify(funcionarioRepository).deleteById(id);
    }

    @Test
    void deveBuscarFuncionarioPorId() {
        Integer id = 1;
        FuncionarioEntity funcionario = new FuncionarioEntity("Carlos", "123", "123", "carlos@email.com", 28, "000", Funcao.ADMINISTRATIVO);
        when(funcionarioRepository.findById(id)).thenReturn(Optional.of(funcionario));

        FuncionarioEntity result = funcionarioService.buscaFuncionarioPorId(id);
        assertEquals("Carlos", result.getNome());
        verify(funcionarioRepository, times(1)).findById(id);
        verify(funcionarioRepository, never()).findAll();
    }

    @Test
    void deveRetornarTodosFuncionarios() {
        List<FuncionarioEntity> lista = List.of(
                new FuncionarioEntity("A", "1", "1", "a@a.com", 20, "123", Funcao.REPARADOR)
        );
        when(funcionarioRepository.findAll()).thenReturn(lista);

        List<FuncionarioEntity> resultado = funcionarioService.buscaTodosFuncionario();

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
    }
    @Test
    void deveLancarExcecaoAoAtualizarFuncionarioInexistente() {
        Integer idInexistente = 99;
        NovoFuncionarioDTO dto = new NovoFuncionarioDTO("Maria", "senha", "senha", "maria@email.com", 30, "111", Funcao.ADMINISTRATIVO);
        when(funcionarioRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> funcionarioService.atualizaFuncionario(dto, idInexistente));
        verify(funcionarioRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoAtualizarComSenhaInvalida() {
        Integer id = 1;
        NovoFuncionarioDTO dto = new NovoFuncionarioDTO("Maria", "senha1", "senha2", "maria@email.com", 30, "111", Funcao.ADMINISTRATIVO);
        FuncionarioEntity existente = new FuncionarioEntity();
        when(funcionarioRepository.findById(id)).thenReturn(Optional.of(existente));

        assertThrows(TrataUnprocessableEntityException.class, () -> funcionarioService.atualizaFuncionario(dto, id));
    }

    @Test
    void deveLancarExcecaoAoExcluirFuncionarioComIdInvalido() {
        Integer idInvalido = 0;

        assertThrows(TrataUnprocessableEntityException.class, () -> funcionarioService.excluiFuncionario(idInvalido));
        verify(funcionarioRepository, never()).deleteById(anyInt());
    }

    @Test
    void deveLancarExcecaoAoExcluirFuncionarioInexistente() {
        Integer idInexistente = 99;
        when(funcionarioRepository.existsById(idInexistente)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> funcionarioService.excluiFuncionario(idInexistente));
    }

    @Test
    void deveLancarExcecaoAoBuscarFuncionarioPorIdInexistente() {
        Integer idInexistente = 99;
        when(funcionarioRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> funcionarioService.buscaFuncionarioPorId(idInexistente));
    }

    @Test
    void deveLancarExcecaoAoBuscarFuncionarioPorIdInvalido() {
        Integer idInvalido = -1;

        assertThrows(TrataUnprocessableEntityException.class, () -> funcionarioService.buscaFuncionarioPorId(idInvalido));
    }
    @Test
    void deveLancarExcecaoAoCadastrarComSenhaNula() {
        NovoFuncionarioDTO dtoComSenhaNula = new NovoFuncionarioDTO("dranka", "123",
                null, "dranka@email.com", 30, "111", Funcao.REPARADOR);
        var exception = assertThrows(TrataUnprocessableEntityException.class, () -> funcionarioService.criaFuncionario(dtoComSenhaNula));
        assertEquals("Senha Invalida", exception.getMessage());
        verify(funcionarioRepository, never()).save(any());
    }
}

