package com.sistema_bicicletario.ms_aluguel.listeners;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class EmailRealizadoEvent extends ApplicationEvent {
    private final String emailDestino;
    private final String assunto;
    private final String mensagem;

    private EmailRealizadoEvent(Object source, String emailDestino, String assunto, String mensagem) {
        super(source);
        this.emailDestino = emailDestino;
        this.assunto = assunto;
        this.mensagem = mensagem;
    }

    public static EmailRealizadoEvent of(Object source, String emailDestino, String assunto, String mensagem) {
        return new EmailRealizadoEvent(source, emailDestino, assunto, mensagem);
    }

}
