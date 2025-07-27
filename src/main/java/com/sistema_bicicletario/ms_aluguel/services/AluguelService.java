package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.clients.EquipamentoClient;
import com.sistema_bicicletario.ms_aluguel.clients.ExternoClient;
import com.sistema_bicicletario.ms_aluguel.listeners.EmailRealizadoEvent;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class AluguelService {
    private final AluguelRepository aluguelRepository;
    private final CiclistaRepository ciclistaRepository;
    private final EquipamentoClient equipamentoClient;
    private final ExternoClient externoClient;
    private final ApplicationEventPublisher eventPublisher;
    private final CartaoRepository cartaoRepository;


    public AluguelService(AluguelRepository aluguelRepository, CiclistaRepository ciclistaRepository,
                          EquipamentoClient equipamentoClient, ExternoClient externoClient,
                          ApplicationEventPublisher eventPublisher, CartaoRepository cartaoRepository) {
        this.aluguelRepository = aluguelRepository;
        this.ciclistaRepository = ciclistaRepository;
        this.equipamentoClient = equipamentoClient;
        this.externoClient = externoClient;
        this.eventPublisher = eventPublisher;
        this.cartaoRepository = cartaoRepository;
    }

    @Transactional
    public AluguelDTO realizarAluguel(NovoAluguelDTO novoAluguel) {
        CiclistaEntity ciclista = ciclistaRepository.findById(novoAluguel.getCiclista())
                .orElseThrow(EntityNotFoundException::new);
        validarCiclistaAptoParaAluguel(ciclista.getId());

        TrancaDTO tranca = validarTranca(novoAluguel.getTrancaInicio());

        BicicletaDTO bicicleta = buscaBicicleta(tranca.getBicicleta());
        validarCondicoesDaBicicleta(bicicleta);

        CobrancaDTO cobranca = enviaCobranca(novoAluguel.getCiclista());

        if (!"PAGA".equals(cobranca.getStatus())) {
            throw new TrataUnprocessableEntityException("Pagamento recusado.");
        }

        TrancarDestrancarDTO dto = new TrancarDestrancarDTO();
        dto.setIdBicicleta(bicicleta.getNumero());

        AluguelEntity aluguelSalvo = registrarDadosDoAluguel(tranca, bicicleta, cobranca, ciclista);
        atualizarStatusDoCiclista(novoAluguel.getCiclista());
        equipamentoClient.liberaTranca(tranca.getNumero(), dto);
        equipamentoClient.atualizarStatusBicicleta(bicicleta.getNumero(), "EM_USO");

        String mensagem = construirMensagemAluguelRealizado(aluguelSalvo, cobranca);
        String emailDoCiclista = ciclista.getEmail();
        EmailRealizadoEvent eventoEmail = EmailRealizadoEvent.of(this, emailDoCiclista, "Confirmação do Aluguel", mensagem);
        eventPublisher.publishEvent(eventoEmail);

        return responseAluguel(aluguelSalvo);
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

    private BicicletaDTO buscaBicicleta(Integer idBicicleta) {
        try {
            log.info("Tentando buscar bicicleta com ID: {}", idBicicleta);
            ResponseEntity<BicicletaDTO> response = equipamentoClient.buscarBicicletaPorId(idBicicleta);
            if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
                return response.getBody();
            }
            throw new ResponseStatusException(response.getStatusCode(), "Bicicleta não encontrada ou serviço de equipamento retornou erro.");

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Serviço de equipamento indisponível.", e);
        }
    }

    private CobrancaDTO enviaCobranca(Integer idCiclista) {
        try {
            NovaCobranca novaCobranca = new NovaCobranca();
            novaCobranca.setCiclista(idCiclista);
            novaCobranca.setValor(1000L);

            ResponseEntity<CobrancaDTO> response = externoClient.realizarCobranca(novaCobranca);

            if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
                return response.getBody();
            }

            throw new ResponseStatusException(response.getStatusCode(), "Serviço de cobrança retornou erro.");

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Serviço de cobrança indisponível.", e);
        }
    }

    private String construirMensagemAluguelRealizado(AluguelEntity aluguel, CobrancaDTO cobranca) {
        DateTimeFormatter formatadorDeDataHora = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm:ss");
        String horaFormatada = aluguel.getHoraInicio().format(formatadorDeDataHora);

        AtomicReference<StringBuilder> mensagem = new AtomicReference<>(new StringBuilder());

        mensagem.get().append("Olá, ").append(aluguel.getNomeTitular()).append("!\n\n");
        mensagem.get().append("Seu aluguel de bicicleta foi confirmado com sucesso. Confira os detalhes abaixo:\n");
        mensagem.get().append("========================================\n\n");

        mensagem.get().append("DETALHES DA RETIRADA\n");
        mensagem.get().append("---------------------------------\n");
        mensagem.get().append("• Bicicleta N.º: ").append(aluguel.getNumeroBicicleta()).append("\n");
        mensagem.get().append((" Tranca N.º: ")).append(aluguel.getTrancaInicio()).append("\n");
        mensagem.get().append("• Horário da Retirada: ").append(horaFormatada).append("\n");

        mensagem.get().append("DETALHES DO PAGAMENTO\n");
        mensagem.get().append("---------------------------------\n");

        mensagem.get().append(String.format("• Valor Cobrado: R$ %.2f\n", (double) (cobranca.getValor()) / 100));
        mensagem.get().append("• Pago com o cartão de final: ").append(aluguel.getFinalCartao()).append("\n\n");

        return mensagem.toString();
    }

    private AluguelDTO responseAluguel(AluguelEntity entidade) {
        AluguelDTO dto = new AluguelDTO();
        dto.setCiclista(entidade.getCiclista());
        dto.setTrancaInicio(entidade.getTrancaInicio());
        dto.setIdBicicleta(entidade.getNumeroBicicleta());
        dto.setHoraInicio(entidade.getHoraInicio());
        dto.setCobranca(entidade.getCobranca());
        return dto;
    }

    private void validarCiclistaAptoParaAluguel(int idCiclista) {
        CiclistaEntity ciclista = ciclistaRepository.findById(idCiclista)
                .orElseThrow(() -> new EntityNotFoundException("Ciclista não encontrado."));

        Optional<AluguelEntity> aluguelExiste = aluguelRepository.findByCiclista(idCiclista);

        if (ciclista.isAluguelAtivo()){
           EnviaEmailDTO enviaEmail = new EnviaEmailDTO();
           enviaEmail.setMensagem(aluguelExiste.toString());
           enviaEmail.setEmail(ciclista.getEmail());
           enviaEmail.setAssunto("Aluguel Existente: ");
           externoClient.enviarEmail(enviaEmail);
           throw new TrataUnprocessableEntityException("Ciclista já possui um aluguel ativo.");
        }
        if (!ciclista.getStatus().equals(Status.ATIVO)){
            throw new TrataUnprocessableEntityException("Ciclista não está ativo no sistema.");
        }
    }

    private void validarCondicoesDaBicicleta(BicicletaDTO bicicleta) {
        if ("EM_REPARO".equals(bicicleta.getStatus()) || "EM_USO".equals(bicicleta.getStatus())) {
            throw new TrataUnprocessableEntityException("Esta bicicleta não pode ser alugada");
        }
    }

    private AluguelEntity registrarDadosDoAluguel(TrancaDTO tranca, BicicletaDTO bicicleta, CobrancaDTO cobranca, CiclistaEntity ciclista) {
        CartaoDeCreditoEntity cartao = cartaoRepository.findByCiclistaId(ciclista.getId())
                .orElseThrow(EntityNotFoundException::new);

        String numeroCartao = cartao.getNumero();
        String finalCartao = numeroCartao.substring(numeroCartao.length() - 4);
        AluguelEntity novoAluguel = new AluguelEntity();
        novoAluguel.setCiclista(ciclista.getId());
        novoAluguel.setTrancaInicio(tranca.getNumero());
        novoAluguel.setNumeroBicicleta(bicicleta.getNumero());
        novoAluguel.setHoraInicio(LocalDateTime.now());
        novoAluguel.setCobranca(cobranca.getId());
        novoAluguel.setNomeTitular(ciclista.getNome());
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

        if (ciclista.isAluguelAtivo()) {
            Optional<AluguelEntity> aluguelOpt = aluguelRepository.findByCiclista(idCiclista);
            if (aluguelOpt.isPresent()) {
                AluguelEntity aluguel = aluguelOpt.get();
                try {
                    ResponseEntity<BicicletaDTO> respostaBicicleta = equipamentoClient.buscarBicicletaPorId(aluguel.getNumeroBicicleta());
                    if (respostaBicicleta.getStatusCode().is2xxSuccessful() && respostaBicicleta.hasBody()) {
                        return Optional.of(Objects.requireNonNull(respostaBicicleta.getBody()));
                    }
                } catch (Exception e) {
                    return Optional.empty();
                }
            }
        }

        return Optional.empty();
    }
}
