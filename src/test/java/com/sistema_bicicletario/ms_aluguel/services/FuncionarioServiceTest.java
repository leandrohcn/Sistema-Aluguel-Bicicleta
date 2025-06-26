package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.NovoFuncionarioDTO;
import com.sistema_bicicletario.ms_aluguel.entities.funcionario.Funcao;
import com.sistema_bicicletario.ms_aluguel.entities.funcionario.FuncionarioEntity;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessableEntity;
import com.sistema_bicicletario.ms_aluguel.repositories.FuncionarioRepository;
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
public class FuncionarioServiceTest {

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @InjectMocks
    private FuncionarioService funcionarioService;

    @Test
    public void deveCriarFuncionarioComDadosValidos() {
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
    public void deveLancarErroQuandoDadosInvalidosAoCriar() {
        NovoFuncionarioDTO dto = new NovoFuncionarioDTO("João", "123", "456", "joao@email.com",
                                                        -1, "12345678900", Funcao.REPARADOR);

        assertThrows(TrataUnprocessableEntity.class, () -> funcionarioService.criaFuncionario(dto));
        verify(funcionarioRepository, never()).save(any());
    }

    @Test
    public void deveAtualizarFuncionario() {
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
    public void deveExcluirFuncionarioComIdValido() {
        Integer id = 1;
        when(funcionarioRepository.existsById(id)).thenReturn(true);

        funcionarioService.excluiFuncionario(id);

        verify(funcionarioRepository).deleteById(id);
    }

    @Test
    public void deveBuscarFuncionarioPorId() {
        Integer id = 1;
        FuncionarioEntity funcionario = new FuncionarioEntity("Carlos", "123", "123", "carlos@email.com", 28, "000", Funcao.ADMINISTRATIVO);

        when(funcionarioRepository.findById(id)).thenReturn(Optional.of(funcionario));
        when(funcionarioRepository.findAll()).thenReturn(List.of(funcionario));

        FuncionarioEntity result = funcionarioService.buscaFuncionarioPorId(id);

        assertEquals("Carlos", result.getNome());
    }

    @Test
    public void deveRetornarTodosFuncionarios() {
        List<FuncionarioEntity> lista = List.of(
                new FuncionarioEntity("A", "1", "1", "a@a.com", 20, "123", Funcao.REPARADOR)
        );
        when(funcionarioRepository.findAll()).thenReturn(lista);

        List<FuncionarioEntity> resultado = funcionarioService.buscaTodosFuncionario();

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
    }
}

