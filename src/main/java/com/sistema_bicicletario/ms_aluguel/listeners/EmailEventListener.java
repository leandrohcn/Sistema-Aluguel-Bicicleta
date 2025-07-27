package com.sistema_bicicletario.ms_aluguel.listeners;

import com.sistema_bicicletario.ms_aluguel.clients.ExternoClient;
import com.sistema_bicicletario.ms_aluguel.dtos.EnviaEmailDTO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EmailEventListener {

    private final ExternoClient externoClient;

    public EmailEventListener(ExternoClient externoClient) {
        this.externoClient = externoClient;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEnvioDeEmail(EmailRealizadoEvent event) {

        System.out.println("Transação confirmada. Preparando para enviar e-mail para: " + event.getEmailDestino());

        EnviaEmailDTO emailDto = new EnviaEmailDTO(
                event.getEmailDestino(),
                event.getAssunto(),
                event.getMensagem()
        );

        try {
            externoClient.enviarEmail(emailDto);
            System.out.println("Solicitação de envio de e-mail para " + event.getEmailDestino() + " enviada com sucesso.");

        } catch (Exception e) {
            System.err.println("FALHA AO SOLICITAR ENVIO DE E-MAIL para " + event.getEmailDestino() + ". Assunto: " + event.getAssunto() + ". Erro: " + e.getMessage());
        }
    }
}
