package com.sistema_bicicletario.ms_aluguel.repositorys;

import com.sistema_bicicletario.ms_aluguel.entitys.funcionario.FuncionarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuncionarioRepository extends JpaRepository<FuncionarioEntity, Long> {

}
