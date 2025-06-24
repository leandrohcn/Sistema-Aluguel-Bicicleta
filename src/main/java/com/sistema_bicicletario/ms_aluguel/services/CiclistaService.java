package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.*;
import com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito.CartaoDeCreditoEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Nacionalidade;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.PassaporteEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Status;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessabeEntity;
import com.sistema_bicicletario.ms_aluguel.repositories.CiclistaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CiclistaService {

    private final CiclistaRepository ciclistaRepository;

    public CiclistaService(CiclistaRepository ciclistaRepository) {
        this.ciclistaRepository = ciclistaRepository;
    }

    @Transactional
    public CiclistaEntity cadastrarCiclista(NovoCiclistaDTO novoCiclistaDto) {

        regrasDeNegocioCadastra(novoCiclistaDto);

        CiclistaEntity ciclista = new CiclistaEntity(
                novoCiclistaDto.getNome(),
                novoCiclistaDto.getDataNascimento(),
                novoCiclistaDto.getCpf(),
                novoCiclistaDto.getEmail(),
                novoCiclistaDto.getNacionalidade(),
                novoCiclistaDto.getUrlFotoDocumento(),
                novoCiclistaDto.getSenha(),
                novoCiclistaDto.getConfirmaSenha()
        );

        if (novoCiclistaDto.getPassaporte() != null) {
            PassaporteDTO dto = novoCiclistaDto.getPassaporte();
            PassaporteEntity passaporte = new PassaporteEntity(
                    dto.getNumeroPassaporte(),
                    dto.getValidadePassaporte(),
                    dto.getPais()
            );
            ciclista.setPassaporteEntity(passaporte);
        }

        CartaoDeCreditoEntity cartao = new CartaoDeCreditoEntity(
                novoCiclistaDto.getNome(),
                novoCiclistaDto.getMeioDePagamento().getNumeroCartao(),
                novoCiclistaDto.getMeioDePagamento().getCvv(),
                novoCiclistaDto.getMeioDePagamento().getValidadeCartao(),
                ciclista
        );
        ciclista.setCartao(cartao);
        ciclista.setStatus(Status.AGUARDANDO_CONFIRMACAO);
        return ciclistaRepository.save(ciclista);
    }

    @Transactional
    public CiclistaEntity atualizarCiclista(Integer id, AtualizaCiclistaDTO ciclistaDTO) {
        return ciclistaRepository.findById(id).map(ciclista -> {
            ciclista.setNome(!ciclistaDTO.getNome().isBlank() ? ciclistaDTO.getNome() : ciclista.getNome());
            ciclista.setDataNascimento(ciclistaDTO.getDataNascimento() != null ? ciclistaDTO.getDataNascimento() : ciclista.getDataNascimento());
            ciclista.setNacionalidade(ciclistaDTO.getNacionalidade() != null ? ciclistaDTO.getNacionalidade() : ciclista.getNacionalidade());
            ciclista.setUrlFotoDocumento(!ciclistaDTO.getUrlFotoDocumento().isBlank() ? ciclistaDTO.getUrlFotoDocumento() : ciclista.getUrlFotoDocumento());
            ciclista.setCpf(!ciclistaDTO.getCpf().isBlank() ? ciclistaDTO.getCpf() : ciclista.getCpf());

            if (ciclistaDTO.getSenha() != null && !ciclistaDTO.getSenha().isBlank()) {
                if (!ciclistaDTO.senhaValida()) {
                    throw new TrataUnprocessabeEntity("Senhas diferentes");
                }
                ciclista.setSenha(ciclistaDTO.getSenha());
                ciclista.setConfirmaSenha(ciclistaDTO.getConfirmaSenha());
            }

            if (ciclistaDTO.getEmail() != null && !ciclistaDTO.getEmail().isBlank()) {
                if (!ciclistaDTO.getEmail().equals(ciclista.getEmail()) && ciclistaRepository.existsByEmail(ciclistaDTO.getEmail())) {
                    throw new TrataUnprocessabeEntity("Email já existente");
                }
                ciclista.setEmail(ciclistaDTO.getEmail());
            } else {
                ciclista.setEmail(ciclista.getEmail());
            }

            if (ciclistaDTO.getPassaporte() != null) {
                PassaporteEntity passaporte = ciclista.getPassaporteEntity() != null ? ciclista.getPassaporteEntity() : new PassaporteEntity();

                if (ciclistaDTO.getPassaporte().getNumeroPassaporte() != null && !ciclistaDTO.getPassaporte().getNumeroPassaporte().isBlank()) {
                    passaporte.setNumeroPassaporte(ciclistaDTO.getPassaporte().getNumeroPassaporte());
                }

                if (ciclistaDTO.getPassaporte().getPais() != null && !ciclistaDTO.getPassaporte().getPais().isBlank()) {
                    passaporte.setPais(ciclistaDTO.getPassaporte().getPais());
                }

                if (ciclistaDTO.getPassaporte().getValidadePassaporte() != null) {
                    passaporte.setValidadePassaporte(ciclistaDTO.getPassaporte().getValidadePassaporte());
                }

                ciclista.setPassaporteEntity(passaporte);
            }

            regrasDeNegocioAtualiza(ciclista);

            return ciclistaRepository.save(ciclista);
        }).orElseThrow(() -> new EntityNotFoundException("Ciclista não encontrado com ID: " + id));
    }

    public CiclistaEntity ativarCiclista(Integer idCiclista) {
        if (idCiclista <= 0) {
            throw new TrataUnprocessabeEntity("id invalido: " + idCiclista);
        }

        Optional<CiclistaEntity> ciclistaEntity = ciclistaRepository.findById(idCiclista);
        if (!ciclistaEntity.isPresent()) {
            throw new EntityNotFoundException("Ciclista não encontrado com id: " + idCiclista);
        }
        if (confirmaEmail()) {
            CiclistaEntity ciclistaAtual = ciclistaEntity.get();
            if (!ciclistaAtual.getStatus().equals(Status.AGUARDANDO_CONFIRMACAO)) {
                throw new TrataUnprocessabeEntity("Dados não correspondem a registro pendente");
            }
            if(ciclistaAtual.getConfirmaEmail() == null){
                ConfirmaEmailDTO confirmaEmailDTO = new ConfirmaEmailDTO();
                ciclistaAtual.setConfirmaEmail(confirmaEmailDTO);
            }
            ciclistaAtual.getConfirmaEmail().setHoraConfirmacao(LocalDateTime.now());
            System.out.println(ciclistaAtual.getConfirmaEmail().getHoraConfirmacao());
            ciclistaAtual.setStatus(Status.ATIVO);
            ciclistaRepository.save(ciclistaAtual);
        }
        return ciclistaEntity.get();
    }

    public boolean confirmaEmail(){
        return true;
    }

    public void existeEmail(String email) {
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Email não enviado como parametro");
        }

        if (!ciclistaRepository.existsByEmail(email)) {
            throw new TrataUnprocessabeEntity("Dados inválidos");
        }

        ciclistaRepository.existsByEmail(email);
    }

    public CiclistaEntity buscarCiclistaporId(Integer idCiclista) {
        if (idCiclista <= 0) {
            throw new TrataUnprocessabeEntity("id invalido: " + idCiclista);
        }

        if (idCiclista.toString().isBlank()){
            throw new IllegalArgumentException("id invalido");
        }

        return ciclistaRepository.findById(idCiclista)
                .orElseThrow((()-> new EntityNotFoundException("Funcionário não encontrado com ID: " + idCiclista)));
    }

    private void regrasDeNegocioCadastra(NovoCiclistaDTO novoCiclistaDto) {
        if (!novoCiclistaDto.senhaValida()) {
            throw new TrataUnprocessabeEntity("Senhas diferentes");
        }

        if (ciclistaRepository.existsByEmail(novoCiclistaDto.getEmail())) {
            throw new TrataUnprocessabeEntity("Email ja existente");
        }

        if (novoCiclistaDto.getNacionalidade() == null) {
            throw new TrataUnprocessabeEntity("Nacionalidade é obrigatória");
        }

        if (novoCiclistaDto.getNacionalidade().equals(Nacionalidade.BRASILEIRO)) {
            if (novoCiclistaDto.getCpf() == null || novoCiclistaDto.getCpf().isBlank()) {
                throw new TrataUnprocessabeEntity("CPF é obrigatório para brasileiros");
            }
        }else if (novoCiclistaDto.getNacionalidade().equals(Nacionalidade.ESTRANGEIRO)) {
            if (novoCiclistaDto.getPassaporte() == null) {
                throw new TrataUnprocessabeEntity("Passaporte é obrigatório para estrangeiros");
            }

            if (novoCiclistaDto.getPassaporte().getPais() == null || novoCiclistaDto.getPassaporte().getPais().isBlank()) {
                throw new TrataUnprocessabeEntity("País do passaporte é obrigatório para estrangeiros");
            }

            if (novoCiclistaDto.getPassaporte().getValidadePassaporte() == null) {
                throw new TrataUnprocessabeEntity("Validade do passaporte é obrigatório para estrangeiros");
            }

            if (novoCiclistaDto.getPassaporte().getNumeroPassaporte() == null || novoCiclistaDto.getPassaporte().getNumeroPassaporte().isBlank()) {
                throw new TrataUnprocessabeEntity("Numero do passaporte é obrigatório para estrangeiros");
            }
        }
    }

    private void regrasDeNegocioAtualiza(CiclistaEntity ciclista) {

        if (ciclista.getNacionalidade().equals(Nacionalidade.BRASILEIRO)) {
            if (ciclista.getCpf() == null || ciclista.getCpf().isBlank()) {
                throw new TrataUnprocessabeEntity("CPF é obrigatório para brasileiros");
            }
        }

        if (ciclista.getNacionalidade().equals(Nacionalidade.ESTRANGEIRO)) {
            PassaporteEntity passaporte = ciclista.getPassaporteEntity();
            if (passaporte == null ||
                    passaporte.getNumeroPassaporte() == null || passaporte.getNumeroPassaporte().isBlank() ||
                    passaporte.getPais() == null || passaporte.getPais().isBlank() ||
                    passaporte.getValidadePassaporte() == null) {
                throw new TrataUnprocessabeEntity("Passaporte completo é obrigatório para estrangeiros");
            }
        }
    }
}
