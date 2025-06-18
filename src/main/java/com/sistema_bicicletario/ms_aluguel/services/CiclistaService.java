package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.AtualizaCiclistaDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.NovoCiclistaDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.CiclistaResponseDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.PassaporteDTO;
import com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito.CartaoDeCreditoEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Nacionalidade;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.PassaporteEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Status;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessabeEntity;
import com.sistema_bicicletario.ms_aluguel.repositories.CiclistaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CiclistaService {

    private final CiclistaRepository ciclistaRepository;

    public CiclistaService(CiclistaRepository ciclistaRepository) {
        this.ciclistaRepository = ciclistaRepository;
    }

    @Transactional
    public CiclistaResponseDTO cadastrarCiclista(NovoCiclistaDTO novoCiclistaDto) {

        regrasDeNegocioCadastro(novoCiclistaDto);
        CiclistaEntity ciclista = new CiclistaEntity(
                novoCiclistaDto.getNome(),
                novoCiclistaDto.getNascimento(),
                novoCiclistaDto.getCpf(),
                novoCiclistaDto.getEmail(),
                novoCiclistaDto.getNacionalidade(),
                novoCiclistaDto.getUrlFotoDocumento(),
                novoCiclistaDto.getSenha(),
                novoCiclistaDto.getConfirmaSenha()
        );

        PassaporteEntity passaporte = new PassaporteEntity(
                novoCiclistaDto.getPassaporte().getNumeroPassaporte(),
                novoCiclistaDto.getPassaporte().getValidadePassaporte(),
                novoCiclistaDto.getPassaporte().getPais()
        );
        ciclista.setPassaporteEntity(passaporte);

        CartaoDeCreditoEntity cartao = new CartaoDeCreditoEntity(
                novoCiclistaDto.getNome(),
                novoCiclistaDto.getMeioDePagamento().getNumeroCartao(),
                novoCiclistaDto.getMeioDePagamento().getCvv(),
                novoCiclistaDto.getMeioDePagamento().getValidadeCartao(),
                ciclista
        );
        ciclista.setCartao(cartao);

        CiclistaEntity salvo = ciclistaRepository.save(ciclista);
        return new CiclistaResponseDTO(salvo);
    }

    public CiclistaEntity atualizarCiclista(Integer id, AtualizaCiclistaDTO ciclistaDTO) {
        return ciclistaRepository.findById(id)
                .map(ciclistaEntity -> {
                    ciclistaEntity.setNome(ciclistaDTO.getNome() != null ? ciclistaDTO.getNome() : ciclistaEntity.getNome());
                    ciclistaEntity.setCpf(ciclistaDTO.getCpf() != null ? ciclistaDTO.getCpf() : ciclistaEntity.getCpf());
                    ciclistaEntity.setDataNascimento(ciclistaDTO.getDataNascimento() != null ? ciclistaDTO.getDataNascimento() : ciclistaEntity.getDataNascimento());
                    ciclistaEntity.setNacionalidade(ciclistaDTO.getNacionalidade() != null ? ciclistaDTO.getNacionalidade() : ciclistaEntity.getNacionalidade());
                    ciclistaEntity.setEmail(ciclistaDTO.getEmail() != null ? ciclistaDTO.getEmail() : ciclistaEntity.getEmail());
                    ciclistaEntity.setUrlFotoDocumento(ciclistaDTO.getUrlFotoDocumento() != null ? ciclistaDTO.getUrlFotoDocumento() : ciclistaEntity.getUrlFotoDocumento());

                    if (ciclistaDTO.getPassaporte() != null) {
                        PassaporteEntity passaporteAtual = ciclistaEntity.getPassaporteEntity();
                        PassaporteDTO passaporteDto = ciclistaDTO.getPassaporte();

                        passaporteAtual.setNumeroPassaporte(passaporteDto.getNumeroPassaporte() != null ? passaporteDto.getNumeroPassaporte() : passaporteAtual.getNumeroPassaporte());
                        passaporteAtual.setValidadePassaporte(passaporteDto.getValidadePassaporte() != null ? passaporteDto.getValidadePassaporte() : passaporteAtual.getValidadePassaporte());
                        passaporteAtual.setPais(passaporteDto.getPais() != null ? passaporteDto.getPais() : passaporteAtual.getPais());

                        ciclistaEntity.setPassaporteEntity(passaporteAtual);
                    }

                    return ciclistaRepository.save(ciclistaEntity);
                })
                .orElseThrow(() -> new RuntimeException("Ciclista não encontrado com ID: " + id));
    }

    public CiclistaResponseDTO ativarCiclista(Integer idCiclista) {
        Optional<CiclistaEntity> ciclistaEntity = ciclistaRepository.findById(idCiclista);
        if (ciclistaEntity.isPresent()) {
            CiclistaEntity ciclistaAtual = ciclistaEntity.get();
            ciclistaAtual.setStatus(Status.ATIVO);
            ciclistaRepository.save(ciclistaAtual);
        }
        return null;
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

    public Optional<CiclistaEntity> buscarCiclistaporId(Integer idCiclista) {
        return ciclistaRepository.findById(idCiclista);
    }

    private void regrasDeNegocioCadastro(NovoCiclistaDTO novoCiclistaDto) {
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
}
