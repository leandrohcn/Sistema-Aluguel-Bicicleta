package com.sistema_bicicletario.ms_aluguel.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.sistema_bicicletario.ms_aluguel.dtos.ErroDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class TratamentoDeErros {


    //precisa dar uma refinada nessas mensagens de erro pra nao vazar pro cliente

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErroDTO>> handleValidacao(MethodArgumentNotValidException ex) {
        List<ErroDTO> erros = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(erro -> new ErroDTO(erro.getField(), erro.getDefaultMessage()))
                .collect(Collectors.toList());

        return ResponseEntity.status(422).body(erros);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErroDTO> handleNotFound(EntityNotFoundException ex) {
        ErroDTO erro = new ErroDTO("404", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroDTO> handleJsonMalFormado(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException invalidFormatException) {
            Class<?> targetType = invalidFormatException.getTargetType();
            if (targetType.isEnum()) {
                String campo = invalidFormatException.getPath().get(0).getFieldName();
                String valorInvalido = invalidFormatException.getValue().toString();
                String valoresValidos = String.join(", ",
                        Arrays.stream(targetType.getEnumConstants())
                                .map(Object::toString)
                                .toList());

                String mensagem = String.format(
                        "Valor inválido '%s' para o campo '%s'. Os valores aceitos são: %s.",
                        valorInvalido, campo, valoresValidos
                );

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErroDTO("400", mensagem));
            }
        }


        ErroDTO erro = new ErroDTO("400", "Requisição malformada: " + ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroDTO> handleIllegalArgument(IllegalArgumentException ex) {
        ErroDTO erro = new ErroDTO("400", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }


    @ExceptionHandler(TrataUnprocessableEntity.class)
    public ResponseEntity<List<ErroDTO>> handleRegraNegocio(TrataUnprocessableEntity ex) {
        ErroDTO erro = new ErroDTO("422", ex.getMessage());
        return ResponseEntity.status(422).body(List.of(erro));
    }

    //ACHO Q CABE BOTAR UM EXCEPTIONHANDLER GENERICO PRA TRATAR QUALUQER COISA

}
