package com.sistema_bicicletario.ms_aluguel.repositorys;

import com.sistema_bicicletario.ms_aluguel.entitys.cartao_de_credito.CartaoDeCreditoEntity;
import com.sistema_bicicletario.ms_aluguel.entitys.ciclista.CiclistaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartaoRepository extends JpaRepository<CartaoDeCreditoEntity, Integer> {

}
