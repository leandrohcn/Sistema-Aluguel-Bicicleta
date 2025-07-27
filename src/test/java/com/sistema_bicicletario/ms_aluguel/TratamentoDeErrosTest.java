package com.sistema_bicicletario.ms_aluguel;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.sistema_bicicletario.ms_aluguel.dtos.ErroDTO;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessableEntityException;
import com.sistema_bicicletario.ms_aluguel.exceptions.TratamentoDeErros;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

class TratamentoDeErrosTest {
    // A classe que estamos testando
    private TratamentoDeErros tratamentoDeErros;

    // Mocks para simular objetos complexos
    @Mock
    private BindingResult bindingResult;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private InvalidFormatException invalidFormatException;

    @BeforeEach
    void setUp() {
        // Instancia a classe antes de cada teste
        tratamentoDeErros = new TratamentoDeErros();
    }

    // Enum de apoio para o teste de ENUM_INVALIDO
    private enum StatusTeste { ATIVO, INATIVO }

    @Test
    @DisplayName("Deve tratar MethodArgumentNotValidException e retornar 422")
    void handleValidacao() {
        // Cenário (Arrange)
        FieldError fieldError = new FieldError("objeto", "campoInvalido", "A mensagem de erro");
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        // Ação (Act)
        ResponseEntity<List<ErroDTO>> response = tratamentoDeErros.handleValidacao(ex);

        // Verificação (Assert)
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("campoInvalido", response.getBody().get(0).getCodigo());
        assertEquals("A mensagem de erro", response.getBody().get(0).getMensagem());
    }

    @Test
    @DisplayName("Deve tratar TrataUnprocessableEntityException e retornar 422")
    void handleRegraNegocio() {
        // Cenário
        TrataUnprocessableEntityException ex = new TrataUnprocessableEntityException("Violação de regra de negócio");

        // Ação
        ResponseEntity<List<ErroDTO>> response = tratamentoDeErros.handleRegraNegocio(ex);

        // Verificação
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("REGRA_NEGOCIO", response.getBody().get(0).getCodigo());
        assertEquals("Violação de regra de negócio", response.getBody().get(0).getMensagem());
    }

    @Test
    @DisplayName("Deve tratar EntityNotFoundException e retornar 404")
    void handleNotFound() {
        // Cenário
        EntityNotFoundException ex = new EntityNotFoundException("Recurso não existe");

        // Ação
        ResponseEntity<ErroDTO> response = tratamentoDeErros.handleNotFound(ex);

        // Verificação
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RECURSO_NAO_ENCONTRADO", response.getBody().getCodigo());
        assertEquals("O recurso solicitado não foi encontrado.", response.getBody().getMensagem());
    }

    @Test
    @DisplayName("Deve tratar HttpMessageNotReadableException para JSON genérico e retornar 400")
    void handleJsonMalFormado_Generico() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON com sintaxe errada");

        ResponseEntity<ErroDTO> response = tratamentoDeErros.handleJsonMalFormado(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("JSON_INVALIDO", response.getBody().getCodigo());
        assertEquals("O corpo da requisição contém um JSON malformado ou inválido.", response.getBody().getMensagem());
    }

    @Test
    @DisplayName("Deve tratar HttpMessageNotReadableException para Enum inválido e retornar 400")
    void handleJsonMalFormado_EnumInvalido() {

        when(invalidFormatException.getTargetType()).thenAnswer(invocation -> StatusTeste.class);
        when(invalidFormatException.getValue()).thenReturn("VALOR_ERRADO");
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Erro de Enum", invalidFormatException);

        ResponseEntity<ErroDTO> response = tratamentoDeErros.handleJsonMalFormado(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ENUM_INVALIDO", response.getBody().getCodigo());
        assertTrue(response.getBody().getMensagem().contains("Valor inválido 'VALOR_ERRADO'"));
        assertTrue(response.getBody().getMensagem().contains("[ATIVO, INATIVO]"));
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException e retornar 422")
    void handleIllegalArgument() {
        // Cenário
        IllegalArgumentException ex = new IllegalArgumentException("Parâmetro com valor ilegal");

        // Ação
        ResponseEntity<ErroDTO> response = tratamentoDeErros.handleIllegalArgument(ex);

        // Verificação
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Argumento inválido", response.getBody().getCodigo());
        assertEquals("Parâmetro com valor ilegal", response.getBody().getMensagem());
    }

    @Test
    @DisplayName("Deve tratar Exception genérica e retornar 500")
    void handleGenericException() {
        // Cenário
        Exception ex = new Exception("Erro inesperado e catastrófico");

        // Ação
        ResponseEntity<ErroDTO> response = tratamentoDeErros.handleGenericException(ex);

        // Verificação
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERRO_INTERNO", response.getBody().getCodigo());
        assertEquals("Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.", response.getBody().getMensagem());
    }

    @Test
    @DisplayName("Deve tratar NoResourceFoundException e retornar 400")
    void handleResourceNotFound() {
        // Cenário
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/recurso/inexistente");

        // Ação
        ResponseEntity<ErroDTO> response = tratamentoDeErros.handleResourceNotFound(ex);

        // Verificação
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Argumento inexistente", response.getBody().getCodigo());
        assertEquals("Não foram passados parâmetros", response.getBody().getMensagem());
    }

}
