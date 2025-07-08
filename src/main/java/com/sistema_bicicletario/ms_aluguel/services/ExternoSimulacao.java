package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.BicicletaDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.CobrancaDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.TrancaDTO;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ExternoSimulacao {

    private static final AtomicInteger cobrancaIdCounter = new AtomicInteger(1);
    private static final AtomicInteger bicicletaIdCounter = new AtomicInteger(1);
    private static final List<String> MARCAS = Arrays.asList("Caloi", "Sense", "Oggi", "Specialized", "Trek", "Cannondale");
    private static final List<String> MODELOS = Arrays.asList("Explorer", "Impact", "Venture", "Rockhopper", "Marlin", "Trail");
    private static final SecureRandom random = new SecureRandom();

    public Optional<TrancaDTO> getTranca(Integer idTranca) {
        System.out.println("Buscando tranca com ID " + idTranca);

        if (idTranca == 999) {
            return Optional.empty();
        }

        TrancaDTO tranca = new TrancaDTO();
        tranca.setIdTranca(idTranca);
        tranca.setIdBicicleta(bicicletaIdCounter.getAndIncrement());
        return Optional.of(tranca);
    }

    public Optional<BicicletaDTO> getBicicleta(Integer idBicicleta) {
        BicicletaDTO bicicleta = new BicicletaDTO();
        bicicleta.setIdBicicleta(idBicicleta);
        bicicleta.setStatus(idBicicleta == 3 ? "EM_REPARO" : "DISPONIVEL");
        String marcaAleatoria = MARCAS.get(random.nextInt(MARCAS.size()));
        String modeloAleatorio = MODELOS.get(random.nextInt(MODELOS.size()));

        //Gera um ano aleatório (por exemplo, entre 2020 e 2025)
        int anoAleatorio = 2020 + random.nextInt(6);

        //Gera um número de série único e aleatório
        String numeroSerieAleatorio = "BR-" + idBicicleta + "-" + random.nextInt(10000);

        bicicleta.setMarca(marcaAleatoria);
        bicicleta.setModelo(modeloAleatorio);
        bicicleta.setAno(String.valueOf(anoAleatorio));
        bicicleta.setNumero(numeroSerieAleatorio);
        return Optional.of(bicicleta);
    }

    public CobrancaDTO realizarCobranca(Integer idCiclista, Double valor) {
        CobrancaDTO cobranca = new CobrancaDTO();
        cobranca.setId((cobrancaIdCounter.getAndIncrement()));
        cobranca.setCiclistaId(idCiclista);
        cobranca.setValor(valor);
        cobranca.setStatus(idCiclista == 3 ? "FALHOU" : "PAGO");
        return cobranca;
    }

    public void destrancarBicicleta(Integer idTranca) {
        if (idTranca == 500) {
            throw new RuntimeException("Simulação de falha de comunicação com a tranca.");
        }
    }

    public void enviarEmail(String tipo, String dados) {
        System.out.println("Tipo de E-mail: " + tipo);
        System.out.println("Dados Enviados: " + dados);
    }

    public void alterarStatusBicicleta(Integer idBicicleta, String novoStatus) {
        System.out.println("Alterando status da bicicleta " + idBicicleta + " para " + novoStatus);
    }

    public void trancarBicicletaNaTranca(Integer idTranca, Integer idBicicleta) {
        System.out.println("Trancando bicicleta " + idBicicleta + " na tranca " + idTranca + " e alterando status para OCUPADA.");
    }

    public void alterarStatusTranca(Integer idTranca, String novoStatus) {
        System.out.println("A tranca: " + idTranca + "está" + novoStatus);
    }

    public void resetCounters() {
        bicicletaIdCounter.set(1);
    }
}
