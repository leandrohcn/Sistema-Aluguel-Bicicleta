package com.sistema_bicicletario.ms_aluguel.repositories;

import com.sistema_bicicletario.ms_aluguel.entities.funcionario.FuncionarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuncionarioRepository extends JpaRepository<FuncionarioEntity, Integer> {

}
