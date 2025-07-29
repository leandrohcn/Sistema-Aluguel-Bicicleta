package com.sistema_bicicletario.ms_aluguel.controllers;

import com.sistema_bicicletario.ms_aluguel.repositories.AluguelRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.CartaoRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.CiclistaRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.FuncionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RestController
public class BancoController {

    @Autowired
    private DataSource dataSource;


    @GetMapping("/restaurarBanco")
    public ResponseEntity<String> restaurarBanco(){

        resetDatabase();

        return ResponseEntity.status(HttpStatus.OK).body("Banco restaurado com sucesso!");

    }

    public void resetDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            Resource resource = new ClassPathResource("data.sql");

            ScriptUtils.executeSqlScript(connection, resource);

        } catch (SQLException e) {

            throw new RuntimeException("Falha ao executar o script do banco de dados.", e);
        }
    }
}
