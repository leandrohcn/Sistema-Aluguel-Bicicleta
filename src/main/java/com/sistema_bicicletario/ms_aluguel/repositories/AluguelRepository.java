package com.sistema_bicicletario.ms_aluguel.repositories;

import com.sistema_bicicletario.ms_aluguel.entities.aluguel.AluguelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AluguelRepository extends JpaRepository<AluguelEntity, Integer> {
    Optional<AluguelEntity> findByIdBicicletaAndHoraFimIsNull(Integer bicicletaId);
    Optional<AluguelEntity> findByCiclista(Integer ciclista);
}
