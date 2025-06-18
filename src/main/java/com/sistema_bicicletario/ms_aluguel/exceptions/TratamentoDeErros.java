package com.sistema_bicicletario.ms_aluguel.exceptions;

import com.sistema_bicicletario.ms_aluguel.dtos.ErroDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class TratamentoDeErros {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErroDTO>> handleValidacao(MethodArgumentNotValidException ex) {
        List<ErroDTO> erros = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(erro -> new ErroDTO(erro.getField(), erro.getDefaultMessage()))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erros);
    }

    @ExceptionHandler({EntityNotFoundException.class, RuntimeException.class})
    public ResponseEntity<ErroDTO> handleNotFound(RuntimeException ex) {
        ErroDTO erro = new ErroDTO("erro", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(TrataUnprocessabeEntity.class)
    public ResponseEntity<ErroDTO> handleRegraNegocio(TrataUnprocessabeEntity ex) {
        ErroDTO erro = new ErroDTO("Requisição inválida", ex.getMessage());
        return ResponseEntity.status(422).body(erro);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroDTO> handleIllegalArgument(IllegalArgumentException ex) {
        ErroDTO erro = new ErroDTO("400", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }


}
