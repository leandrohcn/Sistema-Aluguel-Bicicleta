//package com.sistema_bicicletario.ms_aluguel.services;
//
//import com.sistema_bicicletario.ms_aluguel.dtos.BicicletaDTO;
//import com.sistema_bicicletario.ms_aluguel.dtos.CobrancaDTO;
//import com.sistema_bicicletario.ms_aluguel.dtos.TrancaDTO;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//
//import java.util.concurrent.atomic.AtomicInteger;
//
//@Service
//public class ExternoSimulacao {
//
//    private static final AtomicInteger cobrancaIdCounter = new AtomicInteger(1);
//
//    public Optional<TrancaDTO> getTranca(Integer idTranca) {
//        System.out.println("Buscando tranca com ID " + idTranca);
//
//        if (idTranca == 999) {
//            return Optional.empty();
//        }
//
//        TrancaDTO tranca = new TrancaDTO();
//        tranca.setIdTranca(idTranca);
//        tranca.setIdBicicleta(idTranca == 100 ? 2 : 1);
//        return Optional.of(tranca);
//    }
//
//    public Optional<BicicletaDTO> getBicicleta(Integer idBicicleta) {
//        BicicletaDTO bicicleta = new BicicletaDTO();
//        bicicleta.setIdBicicleta(idBicicleta);
//        bicicleta.setStatus(idBicicleta == 2 ? "EM_REPARO" : "DISPONIVEL");
//        return Optional.of(bicicleta);
//    }
//
//    public CobrancaDTO realizarCobranca(Integer idCiclista, Double valor) {
//        CobrancaDTO cobranca = new CobrancaDTO();
//        cobranca.setId((cobrancaIdCounter.getAndIncrement()));
//        cobranca.setCiclistaId(idCiclista);
//        cobranca.setValor(valor);
//        cobranca.setStatus(idCiclista == 1 ? "FALHOU" : "PAGO");
//        return cobranca;
//    }
//
//    public void destrancarBicicleta(Integer idTranca) {
//        if (idTranca == 500) {
//            throw new RuntimeException("Simulação de falha de comunicação com a tranca.");
//        }
//    }
//
//    public void enviarEmail(String tipo, String dados) {
//        System.out.println("Tipo de E-mail: " + tipo);
//        System.out.println("Dados Enviados: " + dados);
//    }
//
//    public void alterarStatusBicicleta(Integer idBicicleta, String novoStatus) {
//        System.out.println("Alterando status da bicicleta " + idBicicleta + " para " + novoStatus);
//    }
//
//    public void trancarBicicletaNaTranca(Integer idTranca, Integer idBicicleta) {
//        System.out.println("Trancando bicicleta " + idBicicleta + " na tranca " + idTranca + " e alterando status para OCUPADA.");
//    }
//
//    public void aberturaDeTranca(Integer idTranca, String novoStatus) {
//        System.out.println("A tranca: " + idTranca + "está" + novoStatus);
//    }
//
//}
