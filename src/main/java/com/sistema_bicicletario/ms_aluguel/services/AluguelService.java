package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.AluguelDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.NovoAluguelDTO;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Status;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessableEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AluguelService {
    private final CiclistaService ciclistaService;

    public AluguelService(CiclistaService ciclistaService) {
        this.ciclistaService = ciclistaService;
    }

    public AluguelDTO realizaAluguel(NovoAluguelDTO novoAluguel) {
        Integer idCiclista = novoAluguel.getCiclista();
        CiclistaEntity c = ciclistaService.buscarCiclistaporId(idCiclista);

        if (!Status.ATIVO.equals(c.getStatus())) {
            throw new TrataUnprocessableEntity("Ciclista não está ativo.");
        }

        if (!ciclistaService.permiteAluguel(idCiclista)){
            throw new TrataUnprocessableEntity("Ciclista já possui uma bicicleta alugada.");
        }


        Integer cobranca = 10;
        LocalDateTime agora = LocalDateTime.now();

        AluguelDTO aluguel = new AluguelDTO();
        aluguel.setBicicleta(aluguel.getBicicleta());
        aluguel.setHorarioInicio(agora);
        aluguel.setHorarioFim(agora.plusHours(2));
        aluguel.setTrancaInicio(novoAluguel.getTrancaInicio());
        aluguel.setTrancaFim(0);
        aluguel.setCiclista(c.getId());
        aluguel.setCobranca(cobranca);
        return aluguel;
    }
}
