package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.clients.EquipamentoClient;
import com.sistema_bicicletario.ms_aluguel.clients.ExternoClient;
import com.sistema_bicicletario.ms_aluguel.dtos.*;
import com.sistema_bicicletario.ms_aluguel.entities.aluguel.AluguelEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessableEntityException;
import com.sistema_bicicletario.ms_aluguel.listeners.EmailRealizadoEvent;
import com.sistema_bicicletario.ms_aluguel.repositories.AluguelRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.CiclistaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class DevolucaoService {
    private final AluguelRepository aluguelRepository;
    private final CiclistaRepository ciclistaRepository;
    private final EquipamentoClient equipamentoClient;
    private final ExternoClient externoClient;
    private final ApplicationEventPublisher eventPublisher;
    private static final Logger logger = LoggerFactory.getLogger(DevolucaoService.class);


    public DevolucaoService(AluguelRepository aluguelRepository, CiclistaRepository ciclistaRepository,
                            EquipamentoClient equipamentoClient, ExternoClient externoClient, ApplicationEventPublisher eventPublisher) {
        this.aluguelRepository = aluguelRepository;
        this.ciclistaRepository = ciclistaRepository;
        this.equipamentoClient = equipamentoClient;
        this.externoClient = externoClient;
        this.eventPublisher = eventPublisher;

    }

    @Transactional
    public DevolucaoDTO realizarDevolucao(NovoDevolucaoDTO novoDevolucaoDTO) {
        TrancaDTO trancaFinal = validarTranca(novoDevolucaoDTO.getNumero());

        if (!"LIVRE".equals(trancaFinal.getStatusTranca())) {
            throw new TrataUnprocessableEntityException("A tranca de destino está OCUPADA.");
        }

        AluguelEntity aluguelAberto = aluguelRepository.findByNumeroBicicletaAndHoraFimIsNull(novoDevolucaoDTO.getBicicleta())
                .orElseThrow(() -> new EntityNotFoundException("Nenhum aluguel ativo encontrado para esta bicicleta."));

        CiclistaEntity ciclista = ciclistaRepository.findById(aluguelAberto.getCiclista())
                .orElseThrow(() -> new EntityNotFoundException(""));

        LocalDateTime horaDevolucao = LocalDateTime.now();
        long horasDeUso = Duration.between(aluguelAberto.getHoraInicio(), horaDevolucao).toHours();
        long valorExtra = 0;

        if (horasDeUso > 2) {
            long minutosExtras = Duration.between(aluguelAberto.getHoraInicio(), horaDevolucao).minusHours(2).toMinutes();
            long meiasHorasExtras = (long) Math.ceil(minutosExtras / 30.0);
            valorExtra = (meiasHorasExtras * 500);
        }

        if (valorExtra > 0) {
            CobrancaDTO cobrancaExtra = enviaCobranca(aluguelAberto.getCiclista(), valorExtra);
            if (!"PAGO".equals(cobrancaExtra.getStatus())) {
                NovaCobranca novaCobranca = new NovaCobranca();
                novaCobranca.setValor(valorExtra);
                novaCobranca.setCiclista(aluguelAberto.getCiclista());
                logger.warn("Falha na cobrança do valor extra de R$ {} para o ciclista {}", valorExtra, aluguelAberto.getCiclista());
                externoClient.filaCobranca(novaCobranca);
                logger.info("Colocando a cobrança na fila");
            }
        }

        aluguelAberto.setHoraFim(horaDevolucao);
        aluguelAberto.setTrancaFim(novoDevolucaoDTO.getNumero());
        aluguelAberto.setValorExtra(valorExtra);
        AluguelEntity aluguelFechado = aluguelRepository.save(aluguelAberto);
        desativarAluguelParaCiclista(aluguelFechado.getCiclista());

        String novoStatusBicicleta = "REPARO_SOLICITADO".equals(novoDevolucaoDTO.getAcao()) ? "EM_REPARO" : "NOVA";
        equipamentoClient.atualizarStatusBicicleta(aluguelFechado.getNumeroBicicleta(), novoStatusBicicleta);

        IntegrarBicicletaNaRedeDTO integrarBicicletaNaRede = new IntegrarBicicletaNaRedeDTO();
        integrarBicicletaNaRede.setIdBicicleta(aluguelFechado.getNumeroBicicleta());
        integrarBicicletaNaRede.setIdTranca(aluguelFechado.getTrancaFim());
        integraBicicleta(integrarBicicletaNaRede);

        String assunto = "Devolucao realizada com sucesso!";
        String email = ciclista.getEmail();
        String mensagem = construirMensagemDevolucaoRealizado(aluguelFechado, trancaFinal);
        EmailRealizadoEvent eventoEmail = EmailRealizadoEvent.of(this, email, assunto, mensagem);
        eventPublisher.publishEvent(eventoEmail);
        return responseDevolucao(aluguelFechado);
    }

    private TrancaDTO validarTranca(Integer idTranca) {
        try {
            ResponseEntity<TrancaDTO> response = equipamentoClient.buscarTrancaPorId(idTranca);
            if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
                return response.getBody();
            }
            throw new ResponseStatusException(response.getStatusCode(), "Tranca não encontrada ou serviço de equipamento retornou erro.");

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Serviço de equipamento indisponível.", e);
        }
    }

    private CobrancaDTO enviaCobranca(Integer idCiclista, Long valorExtra) {
        try {
            NovaCobranca novaCobranca = new NovaCobranca();
            novaCobranca.setValor(valorExtra);
            novaCobranca.setCiclista(idCiclista);
            ResponseEntity<CobrancaDTO> response = externoClient.realizarCobranca(novaCobranca);

            if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
                return response.getBody();
            }

            throw new ResponseStatusException(response.getStatusCode(), "Serviço de cobrança retornou erro.");

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Serviço de cobrança indisponível.", e);
        }
    }

    private void desativarAluguelParaCiclista(Integer idCiclista) {
        CiclistaEntity ciclista = ciclistaRepository.findById(idCiclista)
                .orElseThrow(() -> new EntityNotFoundException("Ciclista do aluguel não encontrado."));
        ciclista.setAluguelAtivo(false);
        ciclistaRepository.save(ciclista);
    }

    private DevolucaoDTO responseDevolucao(AluguelEntity entidade) {
        DevolucaoDTO dto = new DevolucaoDTO();
        dto.setBicicleta(entidade.getNumeroBicicleta());
        dto.setCiclista(entidade.getCiclista());
        dto.setCobranca(entidade.getCobranca());
        dto.setHoraInicio(entidade.getHoraInicio());
        dto.setHoraFim(entidade.getHoraFim());
        dto.setTrancaFim(entidade.getTrancaFim());
        return dto;
    }

    private CobrancaDTO buscarCobranca(Long idCobranca) {
        ResponseEntity<CobrancaDTO> response = externoClient.obterCobrancaPorId(idCobranca);
        if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
            return response.getBody();
        }
        throw new EntityNotFoundException("Cobrança com id " + idCobranca + " não encontrada no serviço externo.");
    }


    private String construirMensagemDevolucaoRealizado(AluguelEntity aluguel, TrancaDTO tranca) {
        CobrancaDTO cobranca = buscarCobranca(aluguel.getCobranca());

        DateTimeFormatter formatadorDeDataHora = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm:ss");
        String horaFormatada = aluguel.getHoraFim().format(formatadorDeDataHora);
        String horaCobrancaFormatada = cobranca.getHoraSolicitacao().format(formatadorDeDataHora);

        AtomicReference<StringBuilder> mensagem = new AtomicReference<>(new StringBuilder());

        mensagem.get().append("Olá, ").append(aluguel.getNomeTitular()).append("!\n\n");
        mensagem.get().append("Sua devolução de bicicleta foi confirmado com sucesso. Confira os detalhes abaixo:\n");
        mensagem.get().append("========================================\n\n");

        mensagem.get().append("DETALHES DA DEVOLUÇÃO\n");
        mensagem.get().append("---------------------------------\n");
        mensagem.get().append("• Bicicleta N.º: ").append(aluguel.getNumeroBicicleta()).append("\n");
        mensagem.get().append("• Tranca N.º: ").append(tranca.getNumero()).append("\n");
        mensagem.get().append("• Horário da Devolução: ").append(horaFormatada).append("\n");

        mensagem.get().append("DETALHES DO PAGAMENTO\n");
        mensagem.get().append("---------------------------------\n");
        if (aluguel.getValorExtra() > 0) {
            mensagem.get().append("• Horário da Cobrança:").append(horaCobrancaFormatada).append("\n");
            mensagem.get().append(String.format("• Valor-extra Cobrado: R$ %.2f\n", (double) aluguel.getValorExtra()));
            mensagem.get().append("• Pago com o cartão de final: ").append(aluguel.getFinalCartao()).append("\n\n");
        } else {
            mensagem.get().append("Não há valor extra á ser cobrado");
        }
        return mensagem.toString();
    }

    public void integraBicicleta(IntegrarBicicletaNaRedeDTO integraBicicletaDTO) {
        try {
            ResponseEntity<IntegrarBicicletaNaRedeDTO> response = equipamentoClient.integrarNaRede(integraBicicletaDTO);
            if (response.getStatusCode().is2xxSuccessful() && response.hasBody()){
                response.getBody();
                return;
            }
            throw new ResponseStatusException(response.getStatusCode(), "Serviço de equipamento retornou erro.");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Serviço de equipamento indisponível.", e);
        }

    }

}
