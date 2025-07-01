package com.sistema_bicicletario.ms_aluguel.repositories;

import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CiclistaRepository extends JpaRepository<CiclistaEntity, Integer> {
    boolean existsByEmail(String email);
    Optional<CiclistaEntity> findByCpf(String email);

}
