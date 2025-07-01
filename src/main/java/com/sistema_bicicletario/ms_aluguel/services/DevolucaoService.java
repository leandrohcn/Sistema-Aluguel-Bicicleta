package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.CobrancaDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.DevolucaoDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.NovoDevolucaoDTO;
import com.sistema_bicicletario.ms_aluguel.entities.aluguel.AluguelEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessableEntity;
import com.sistema_bicicletario.ms_aluguel.repositories.AluguelRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.CiclistaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class DevolucaoService {
    private final AluguelRepository aluguelRepository;
    private final ExternoSimulacao externoSimulacao;
    private final CiclistaRepository ciclistaRepository;

    public DevolucaoService(AluguelRepository aluguelRepository, ExternoSimulacao externoSimulacao, CiclistaRepository ciclistaRepository) {
        this.aluguelRepository = aluguelRepository;
        this.externoSimulacao = externoSimulacao;
        this.ciclistaRepository = ciclistaRepository;
    }

    @Transactional
    public DevolucaoDTO realizarDevolucao(NovoDevolucaoDTO novoDevolucaoDTO) {
        AluguelEntity aluguelAberto = aluguelRepository.findByIdBicicletaAndHoraFimIsNull(novoDevolucaoDTO.getIdBicicleta())
                .orElseThrow(() -> new EntityNotFoundException("Nenhum aluguel ativo encontrado para esta bicicleta."));

        if (aluguelAberto.getHoraFim() == null) {
            LocalDateTime horaDevolucao = LocalDateTime.now();
            long horasDeUso = Duration.between(aluguelAberto.getHoraInicio(), horaDevolucao).toHours();
            double valorExtra = 0.0;

            if (horasDeUso > 2) {
                long minutosExtras = Duration.between(aluguelAberto.getHoraInicio(), horaDevolucao).minusHours(2).toMinutes();
                long meiasHorasExtras = (long) Math.ceil(minutosExtras / 30.0);
                valorExtra = meiasHorasExtras * 5.00;
            }

            if (valorExtra > 0) {
                CobrancaDTO cobrancaExtra = externoSimulacao.realizarCobranca(aluguelAberto.getCiclista(), valorExtra);
                if (!"PAGO".equals(cobrancaExtra.getStatus())) {
                    System.out.println("AVISO: Falha na cobrança do valor extra de R$" + valorExtra);
                }
            }

            aluguelAberto.setHoraFim(horaDevolucao);
            aluguelAberto.setTrancaFim(novoDevolucaoDTO.getIdTranca());
            aluguelAberto.setValorExtra(valorExtra);
            AluguelEntity aluguelFechado = aluguelRepository.save(aluguelAberto);
            desativarAluguelParaCiclista(aluguelFechado.getCiclista());

            String novoStatusBicicleta = "REPARO_SOLICITADO".equals(novoDevolucaoDTO.getAcao()) ? "EM_REPARO" : "DISPONIVEL";
            externoSimulacao.alterarStatusBicicleta(novoDevolucaoDTO.getIdBicicleta(), novoStatusBicicleta);
            externoSimulacao.trancarBicicletaNaTranca(novoDevolucaoDTO.getIdTranca(), novoDevolucaoDTO.getIdBicicleta());
            externoSimulacao.enviarEmail("Dados da devolucao: ", aluguelFechado.toString());

            return responseDevolucao(aluguelFechado);
        }
        throw new TrataUnprocessableEntity("Erro na devolução");
    }

    private void desativarAluguelParaCiclista(Integer idCiclista) {
        CiclistaEntity ciclista = ciclistaRepository.findById(idCiclista)
                .orElseThrow(() -> new EntityNotFoundException("Ciclista do aluguel não encontrado."));
        ciclista.setAluguelAtivo(false);
        ciclistaRepository.save(ciclista);
    }

    private DevolucaoDTO responseDevolucao(AluguelEntity entidade) {
        DevolucaoDTO dto = new DevolucaoDTO();
        dto.setBicicleta(entidade.getIdBicicleta());
        dto.setCiclista(entidade.getCiclista());
        dto.setCobranca(entidade.getCobranca());
        dto.setHoraInicio(entidade.getHoraInicio());
        dto.setHoraFim(entidade.getHoraFim());
        dto.setTrancaFim(entidade.getTrancaFim());
        return dto;
    }
}
