package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.AtualizaCiclistaDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.NovoCiclistaDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.CiclistaResponseDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.PassaporteDTO;
import com.sistema_bicicletario.ms_aluguel.entitys.cartao_de_credito.CartaoDeCreditoEntity;
import com.sistema_bicicletario.ms_aluguel.entitys.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.entitys.ciclista.PassaporteEntity;
import com.sistema_bicicletario.ms_aluguel.entitys.ciclista.Status;
import com.sistema_bicicletario.ms_aluguel.repositorys.CiclistaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CiclistaService {

    private final CiclistaRepository ciclistaRepository;

    public CiclistaService(CiclistaRepository ciclistaRepository) {
        this.ciclistaRepository = ciclistaRepository;
    }

    public CiclistaEntity atualizarCiclista(Integer id, AtualizaCiclistaDTO ciclistaDTO) {
                ciclistaRepository.findById(id)
                .map(ciclistaEntity -> {
                    ciclistaEntity.setNome(ciclistaDTO.getNome() != null ? ciclistaDTO.getNome() : ciclistaEntity.getNome());
                    ciclistaEntity.setCpf(ciclistaDTO.getCpf() != null ? ciclistaDTO.getCpf() : ciclistaEntity.getCpf());
                    ciclistaEntity.setDataNascimento(ciclistaDTO.getNascimento() != null ? ciclistaDTO.getNascimento() : ciclistaEntity.getDataNascimento());
                    ciclistaEntity.setNacionalidade(ciclistaDTO.getNacionalidade() != null ? ciclistaDTO.getNacionalidade() : ciclistaEntity.getNacionalidade());
                    ciclistaEntity.setEmail(ciclistaDTO.getEmail() != null ? ciclistaDTO.getEmail() : ciclistaEntity.getEmail());
                    ciclistaEntity.setUrlFotoDocumento(ciclistaDTO.getUrlFotoDocumento() != null ? ciclistaDTO.getUrlFotoDocumento() : ciclistaEntity.getUrlFotoDocumento());

                    if (ciclistaDTO.getPassaporte() != null) {
                        PassaporteEntity passaporteAtual = ciclistaEntity.getPassaporteEntity();
                        PassaporteDTO passaporteDto = ciclistaDTO.getPassaporte();

                        passaporteAtual.setNumeroPassaporte(passaporteDto.getNumero() != null ? passaporteDto.getNumero() : passaporteAtual.getNumeroPassaporte());
                        passaporteAtual.setValidadePassaporte(passaporteDto.getValidade() != null ? passaporteDto.getValidade() : passaporteAtual.getValidadePassaporte());
                        passaporteAtual.setPais(passaporteDto.getPais() != null ? passaporteDto.getPais() : passaporteAtual.getPais());

                        ciclistaEntity.setPassaporteEntity(passaporteAtual);
                    }

                    ciclistaRepository.save(ciclistaEntity);
                    return ciclistaEntity;
                });
                return null;
    }

    @Transactional
    public CiclistaResponseDTO cadastrarCiclista(NovoCiclistaDTO novoCiclistaDto) {
        CiclistaEntity ciclista = new CiclistaEntity(
                novoCiclistaDto.getNome(),
                novoCiclistaDto.getNascimento(),
                novoCiclistaDto.getCpf(),
                novoCiclistaDto.getEmail(),
                novoCiclistaDto.getNacionalidade(),
                novoCiclistaDto.getUrlFotoDocumento(),
                novoCiclistaDto.getSenha()
        );

        PassaporteEntity passaporte = new PassaporteEntity(
                novoCiclistaDto.getPassaporte().getNumero(),
                novoCiclistaDto.getPassaporte().getValidade(),
                novoCiclistaDto.getPassaporte().getPais()
        );
        ciclista.setPassaporteEntity(passaporte);

        CartaoDeCreditoEntity cartao = new CartaoDeCreditoEntity(
                novoCiclistaDto.getNome(),
                novoCiclistaDto.getMeioDePagamento().getNumeroCartao(),
                novoCiclistaDto.getMeioDePagamento().getCvv(),
                novoCiclistaDto.getMeioDePagamento().getValidade(),
                ciclista
        );
        ciclista.setCartao(cartao);

        CiclistaEntity salvo = ciclistaRepository.save(ciclista);
        return new CiclistaResponseDTO(salvo);
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

    public boolean existeEmail(String email) {
        return ciclistaRepository.existsByEmail(email);
    }

    public Optional<CiclistaEntity> buscaCiclistaporId(Integer idCiclista) {
        return ciclistaRepository.findById(idCiclista);
    }

}

