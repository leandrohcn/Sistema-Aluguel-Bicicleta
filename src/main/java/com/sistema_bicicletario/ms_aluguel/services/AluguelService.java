package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.*;
import com.sistema_bicicletario.ms_aluguel.entities.aluguel.AluguelEntity;
import com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito.CartaoDeCreditoEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Status;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessableEntityException;
import com.sistema_bicicletario.ms_aluguel.repositories.AluguelRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.CartaoRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.CiclistaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AluguelService {
    private final AluguelRepository aluguelRepository;
    private final CiclistaRepository ciclistaRepository;
    private final CartaoRepository cartaoRepository;
    private final ExternoSimulacao externoEquipamentoSimulacao;

    public AluguelService(AluguelRepository aluguelRepository, CiclistaRepository ciclistaRepository, CartaoRepository cartaoRepository, ExternoSimulacao externo) {
        this.aluguelRepository = aluguelRepository;
        this.ciclistaRepository = ciclistaRepository;
        this.cartaoRepository = cartaoRepository;
        this.externoEquipamentoSimulacao = externo;

    }

    @Transactional
    public AluguelDTO realizarAluguel(NovoAluguelDTO novoAluguel) {
        TrancaDTO tranca = externoEquipamentoSimulacao.getTranca(novoAluguel.getTrancaInicio())
                .orElseThrow(() -> new EntityNotFoundException("Tranca não encontrada."));

        BicicletaDTO bicicleta = externoEquipamentoSimulacao.getBicicleta(tranca.getIdBicicleta())
                .orElseThrow(() -> new EntityNotFoundException("Bicicleta não encontrada."));

        validarCiclistaAptoParaAluguel(novoAluguel.getCiclista());
        validarCondicoesDaBicicleta(bicicleta);

        CartaoDeCreditoEntity cartaoDoCiclista = cartaoRepository.findById(novoAluguel.getCiclista())
                .orElseThrow(() -> new TrataUnprocessableEntityException("Ciclista não encontrado"));

        CobrancaDTO cobranca = externoEquipamentoSimulacao.realizarCobranca(novoAluguel.getCiclista(), 10.00);
        if (!"PAGO".equals(cobranca.getStatus())) {
            throw new TrataUnprocessableEntityException("Pagamento recusado.");
        }

        AluguelEntity aluguelSalvo = registrarDadosDoAluguel(novoAluguel.getCiclista(), tranca, bicicleta, cobranca, cartaoDoCiclista);
        atualizarStatusDoCiclista(novoAluguel.getCiclista());
        externoEquipamentoSimulacao.alterarStatusBicicleta(bicicleta.getIdBicicleta(), "EM_USO");
        externoEquipamentoSimulacao.alterarStatusTranca(tranca.getIdTranca(), "LIVRE");
        externoEquipamentoSimulacao.destrancarBicicleta(tranca.getIdTranca());
        externoEquipamentoSimulacao.enviarEmail("Dados do aluguel: ", aluguelSalvo.toString());

        return responseAluguel(aluguelSalvo);
    }

    private AluguelDTO responseAluguel(AluguelEntity entidade) {
        AluguelDTO dto = new AluguelDTO();
        dto.setCiclista(entidade.getCiclista());
        dto.setTrancaInicio(entidade.getTrancaInicio());
        dto.setIdBicicleta(entidade.getIdBicicleta());
        dto.setHoraInicio(entidade.getHoraInicio());
        dto.setCobranca(entidade.getCobranca());
        return dto;
    }

    private void validarCiclistaAptoParaAluguel(int idCiclista) {
        CiclistaEntity ciclista = ciclistaRepository.findById(idCiclista)
                .orElseThrow(() -> new EntityNotFoundException("Ciclista não encontrado."));

        Optional<AluguelEntity> aluguelExiste = aluguelRepository.findByCiclista(idCiclista);

        if (ciclista.isAluguelAtivo()){
           externoEquipamentoSimulacao.enviarEmail("Aluguel Ativo", aluguelExiste.toString());
           throw new TrataUnprocessableEntityException("Ciclista já possui um aluguel ativo.");
        }
        if (!ciclista.getStatus().equals(Status.ATIVO)){
            throw new TrataUnprocessableEntityException("Ciclista não está ativo no sistema.");
        }
    }

    private void validarCondicoesDaBicicleta(BicicletaDTO bicicleta) {
        if ("EM_REPARO".equals(bicicleta.getStatus())) {
            throw new TrataUnprocessableEntityException("Esta bicicleta não pode ser alugada, pois está marcada para reparo.");
        }
    }

    private AluguelEntity registrarDadosDoAluguel(Integer idCiclista, TrancaDTO tranca, BicicletaDTO bicicleta, CobrancaDTO cobranca, CartaoDeCreditoEntity cartao) {
        String numeroCartao = cartao.getNumero();
        String finalCartao = numeroCartao.substring(numeroCartao.length() - 4);
        AluguelEntity novoAluguel = new AluguelEntity();
        novoAluguel.setCiclista(idCiclista);
        novoAluguel.setTrancaInicio(tranca.getIdTranca());
        novoAluguel.setIdBicicleta(bicicleta.getIdBicicleta());
        novoAluguel.setNumeroBicicleta(bicicleta.getNumero());
        novoAluguel.setHoraInicio(LocalDateTime.now());
        novoAluguel.setCobranca(cobranca.getId());
        novoAluguel.setNomeTitular(cartao.getNomeTitular());
        novoAluguel.setFinalCartao(finalCartao);
        return aluguelRepository.save(novoAluguel);
    }

    private void atualizarStatusDoCiclista(int idCiclista) {
        CiclistaEntity ciclista = ciclistaRepository.findById(idCiclista)
                .orElseThrow(() -> new EntityNotFoundException("Ciclista não encontrado"));
        ciclista.setAluguelAtivo(true);
        ciclistaRepository.save(ciclista);
    }

    public Optional<BicicletaDTO> buscarBicicletaDoAluguelAtivo(Integer idCiclista) {
        CiclistaEntity ciclista = ciclistaRepository.findById(idCiclista)
                .orElseThrow(() -> new EntityNotFoundException("Ciclista não encontrado."));

        if (ciclista.isAluguelAtivo()){
            return aluguelRepository.findByCiclista(idCiclista)
                    .flatMap(aluguel -> externoEquipamentoSimulacao.getBicicleta(aluguel.getIdBicicleta()));
        }
            return Optional.empty();
    }
}
