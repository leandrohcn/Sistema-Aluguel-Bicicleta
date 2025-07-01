package com.sistema_bicicletario.ms_aluguel.exceptions; // ou o pacote correto

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.sistema_bicicletario.ms_aluguel.dtos.ErroDTO;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class TratamentoDeErros {

    private static final Logger log = LoggerFactory.getLogger(TratamentoDeErros.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErroDTO>> handleValidacao(MethodArgumentNotValidException ex) {
        List<ErroDTO> erros = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(erro -> new ErroDTO(erro.getField(), erro.getDefaultMessage()))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erros);
    }

    @ExceptionHandler(TrataUnprocessableEntity.class)
    public ResponseEntity<List<ErroDTO>> handleRegraNegocio(TrataUnprocessableEntity ex) {
        ErroDTO erro = new ErroDTO("REGRA_NEGOCIO", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Collections.singletonList(erro));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErroDTO> handleNotFound(EntityNotFoundException ex) {
        log.warn("Recurso não encontrado: {}", ex.getMessage());
        ErroDTO erro = new ErroDTO("RECURSO_NAO_ENCONTRADO", "O recurso solicitado não foi encontrado.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroDTO> handleJsonMalFormado(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof InvalidFormatException invalidFormatException) {
            Class<?> targetType = invalidFormatException.getTargetType();
            if (targetType != null && targetType.isEnum()) {
                String mensagem = String.format("Valor inválido '%s' para o campo enum. Os valores aceitos são: %s.",
                        invalidFormatException.getValue(),
                        Arrays.toString(targetType.getEnumConstants()));
                return ResponseEntity.badRequest().body(new ErroDTO("ENUM_INVALIDO", mensagem));
            }
        }
        log.warn("Erro de parsing no JSON: {}", ex.getMessage());
        ErroDTO erro = new ErroDTO("JSON_INVALIDO", "O corpo da requisição contém um JSON malformado ou inválido.");
        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroDTO> handleGenericException(Exception ex) {
        log.error("Ocorreu um erro inesperado no servidor.", ex);
        ErroDTO erro = new ErroDTO("ERRO_INTERNO", "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroDTO> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Argumento ilegal passado para um método: {}", ex.getMessage(), ex);
        ErroDTO erro = new ErroDTO("ERRO_INTERNO", "Ocorreu um erro inesperado processando sua requisição.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }
}