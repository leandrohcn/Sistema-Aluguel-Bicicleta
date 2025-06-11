package com.sistema_bicicletario.ms_aluguel.dtos;


import com.sistema_bicicletario.ms_aluguel.entitys.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.entitys.ciclista.Nacionalidade;
import com.sistema_bicicletario.ms_aluguel.entitys.ciclista.Status;
import lombok.Getter;
import java.util.Date;

@Getter
public class CiclistaResponseDTO {
    private final int id;
    private final String status;
    private final String nome;
    private final Date nascimento;
    private final String cpf;
    private final Nacionalidade nacionalidade;
    private final String email;
    private final String urlFotoDocumento;

    private final PassaporteDTO passaporte;

    public CiclistaResponseDTO(CiclistaEntity ciclista) {
        this.id = ciclista.getId();
        this.status = (ciclista.getStatus() != null) ? ciclista.getStatus().name() : String.valueOf(Status.AGUARDANDO_CONFIRMACAO);
        this.nome = ciclista.getNome();
        this.nascimento = ciclista.getDataNascimento();
        this.cpf = ciclista.getCpf();
        this.nacionalidade = ciclista.getNacionalidade();
        this.email = ciclista.getEmail();
        this.urlFotoDocumento = ciclista.getUrlFotoDocumento();
        this.passaporte = new PassaporteDTO(
                ciclista.getPassaporteEntity().getNumeroPassaporte(),
                ciclista.getPassaporteEntity().getPais(),
                ciclista.getPassaporteEntity().getValidadePassaporte()
        );
    }

}