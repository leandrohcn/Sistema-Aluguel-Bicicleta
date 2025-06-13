package com.sistema_bicicletario.ms_aluguel.repositories;

import com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito.CartaoDeCreditoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartaoRepository extends JpaRepository<CartaoDeCreditoEntity, Integer> {

}
