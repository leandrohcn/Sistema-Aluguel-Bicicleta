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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CiclistaService {

    private final CiclistaRepository ciclistaRepository;

    public CiclistaService(CiclistaRepository ciclistaRepository) {
        this.ciclistaRepository = ciclistaRepository;
    }

    public ResponseEntity<CiclistaResponseDTO> atualizarCiclista(Integer id, AtualizaCiclistaDTO ciclistaDTO) {
        return ciclistaRepository.findById(id)
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
                    return ResponseEntity.ok(new CiclistaResponseDTO(ciclistaEntity));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Transactional
    public CiclistaResponseDTO cadastrarCiclista(NovoCiclistaDTO novoCiclistaDto) {
        // Constrói entidade do ciclista
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setNome(novoCiclistaDto.getNome());
        ciclista.setDataNascimento(novoCiclistaDto.getNascimento());
        ciclista.setCpf(novoCiclistaDto.getCpf());
        ciclista.setEmail(novoCiclistaDto.getEmail());
        ciclista.setNacionalidade(novoCiclistaDto.getNacionalidade());
        ciclista.setUrlFotoDocumento(novoCiclistaDto.getUrlFotoDocumento());
        ciclista.setSenha(novoCiclistaDto.getSenha());

        // Cria entidade de passaporte
        PassaporteEntity passaporte = new PassaporteEntity();
        passaporte.setNumeroPassaporte(novoCiclistaDto.getPassaporte().getNumero());
        passaporte.setValidadePassaporte(novoCiclistaDto.getPassaporte().getValidade());
        passaporte.setPais(novoCiclistaDto.getPassaporte().getPais());
        passaporte.setCiclista(ciclista); // RELAÇÃO BIDIRECIONAL
        ciclista.setPassaporteEntity(passaporte);

        // Cria entidade de cartão
        CartaoDeCreditoEntity cartao = new CartaoDeCreditoEntity();
        cartao.setNomeTitular(novoCiclistaDto.getNome());
        cartao.setNumero(novoCiclistaDto.getMeioDePagamento().getNumeroCartao());
        cartao.setCvv(novoCiclistaDto.getMeioDePagamento().getCvv());
        cartao.setValidade(novoCiclistaDto.getMeioDePagamento().getValidade());
        cartao.setCiclista(ciclista); // RELAÇÃO BIDIRECIONAL
        ciclista.setCartao(cartao);

        // Salva tudo em cascata
        CiclistaEntity salvo = ciclistaRepository.save(ciclista);
        return new CiclistaResponseDTO(salvo);
    }

    public ResponseEntity<CiclistaResponseDTO> ativarCiclista(Integer idCiclista) {
        Optional<CiclistaEntity> ciclistaEntity = ciclistaRepository.findById(idCiclista);
        if (ciclistaEntity.isPresent()) {
            CiclistaEntity ciclistaAtual = ciclistaEntity.get();
            ciclistaAtual.setStatus(Status.ATIVO);
            ciclistaRepository.save(ciclistaAtual);
            return ResponseEntity.ok(new CiclistaResponseDTO(ciclistaAtual));
        }
        return ResponseEntity.notFound().build();
    }

    public boolean existeEmail(String email) {
        return ciclistaRepository.existsByEmail(email);
    }
}

