package com.sistema_bicicletario.ms_aluguel.repositorys;

import com.sistema_bicicletario.ms_aluguel.entitys.ciclista.CiclistaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CiclistaRepository extends JpaRepository<CiclistaEntity, Integer> {
    boolean existsByEmail(String email);
}
