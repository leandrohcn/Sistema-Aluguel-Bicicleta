package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.*;
import com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito.CartaoDeCreditoEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Nacionalidade;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.PassaporteEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Status;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessableEntityException;
import com.sistema_bicicletario.ms_aluguel.repositories.CiclistaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class CiclistaService {

    private final CiclistaRepository ciclistaRepository;

    private final CartaoService cartaoService;

    public CiclistaService(CiclistaRepository ciclistaRepository, CartaoService cartaoService) {
        this.ciclistaRepository = ciclistaRepository;
        this.cartaoService = cartaoService;
    }

    @Transactional
    public CiclistaResponseDTO cadastrarCiclista(NovoCiclistaDTO novoCiclistaDto) {
        verificarRegrasDeNegocioDeCadastro(novoCiclistaDto);

        CiclistaEntity novoCiclista = new CiclistaEntity(
                novoCiclistaDto.getNome(),
                novoCiclistaDto.getDataNascimento(),
                novoCiclistaDto.getEmail(),
                novoCiclistaDto.getNacionalidade(),
                novoCiclistaDto.getUrlFotoDocumento(),
                encripta(novoCiclistaDto.getSenha()),
                novoCiclistaDto.getConfirmaSenha()
        );

        if (novoCiclistaDto.getNacionalidade().equals(Nacionalidade.BRASILEIRO)) {
            if(ciclistaRepository.findByCpf(novoCiclistaDto.getCpf()).isPresent()){
                throw new IllegalArgumentException("CPF já existente");
            }
            novoCiclista.setCpf(novoCiclistaDto.getCpf());
        } else if (novoCiclistaDto.getNacionalidade().equals(Nacionalidade.ESTRANGEIRO)) {
            PassaporteDTO passaporteDto = novoCiclistaDto.getPassaporte();
            PassaporteEntity passaporte = new PassaporteEntity(
                    passaporteDto.getNumeroPassaporte(),
                    passaporteDto.getValidadePassaporte(),
                    passaporteDto.getPais()
            );
            novoCiclista.setPassaporteEntity(passaporte);
        } else {
            throw new EntityNotFoundException("Requisição mal formada");
        }

        processarMeioDePagamento(novoCiclistaDto, novoCiclista);
        novoCiclista.setStatus(Status.AGUARDANDO_CONFIRMACAO);
        CiclistaEntity ciclistaSalvo = ciclistaRepository.save(novoCiclista);
        return new CiclistaResponseDTO(ciclistaSalvo);
    }

    private void processarMeioDePagamento(NovoCiclistaDTO dto, CiclistaEntity ciclista) {
        String numeroCartao = dto.getMeioDePagamento().getNumeroCartao();

        if (cartaoService.cartaoExiste(numeroCartao)) {
            throw new IllegalArgumentException("Cartao já cadastrado em outro usuário");
        }

        if (validarCartao(dto.getMeioDePagamento())) {
            CartaoDeCreditoEntity cartao = new CartaoDeCreditoEntity(
                    dto.getNome(),
                    numeroCartao,
                    dto.getMeioDePagamento().getCvv(),
                    dto.getMeioDePagamento().getValidadeCartao(),
                    ciclista
            );
            ciclista.setCartao(cartao);
        } else {
            throw new IllegalArgumentException("Cartão recusado");
        }
    }

    private String encripta(String senha) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(senha);
    }

    @Transactional
    public CiclistaResponseDTO atualizarCiclista(Integer id, AtualizaCiclistaDTO ciclistaDTO) {
        CiclistaEntity ciclista = ciclistaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ciclista não encontrado com id: " +  id));

        regrasDeNegocioAtualiza(ciclistaDTO, ciclista);

        ciclista.setNome(!ciclistaDTO.getNome().isBlank() ? ciclistaDTO.getNome() : ciclista.getNome());
        ciclista.setCpf(!ciclistaDTO.getCpf().isBlank() ? ciclistaDTO.getCpf() : ciclista.getCpf());
        ciclista.setUrlFotoDocumento(!ciclistaDTO.getUrlFotoDocumento().isBlank() ? ciclistaDTO.getUrlFotoDocumento() : ciclista.getUrlFotoDocumento());
        ciclista.setNacionalidade(ciclistaDTO.getNacionalidade() != null ? ciclistaDTO.getNacionalidade() : ciclista.getNacionalidade());
        ciclista.setSenha(!ciclistaDTO.getSenha().isBlank() ? encripta(ciclistaDTO.getSenha()) : ciclista.getSenha());
        ciclista.setConfirmaSenha(!ciclistaDTO.getConfirmaSenha().isBlank() ? ciclistaDTO.getConfirmaSenha() : ciclista.getConfirmaSenha());

        PassaporteDTO passaporteDTO = ciclistaDTO.getPassaporte();
        if (ciclistaDTO.getPassaporte() != null){
            PassaporteEntity passaporte = new PassaporteEntity(
                    passaporteDTO.getNumeroPassaporte(),
                    passaporteDTO.getValidadePassaporte(),
                    passaporteDTO.getPais()
            );
            ciclista.setPassaporteEntity(passaporte);
        } else {
            ciclista.setPassaporteEntity(ciclista.getPassaporteEntity());
        }

        CiclistaEntity ciclistaAtualizado = ciclistaRepository.save(ciclista);
        return new CiclistaResponseDTO(ciclistaAtualizado);
    }

    private void regrasDeNegocioAtualiza(AtualizaCiclistaDTO ciclistaDto, CiclistaEntity ciclistaExistente) {
        String novoEmail = ciclistaDto.getEmail();
        if (novoEmail != null && !novoEmail.equalsIgnoreCase(ciclistaExistente.getEmail()) && existeEmail(novoEmail)) {

                throw new TrataUnprocessableEntityException("Email ja existente");

        }

        if (ciclistaDto.getSenha() != null) {
            if (!ciclistaDto.senhaValida()) {
                throw new TrataUnprocessableEntityException("Senhas diferentes");
            }
        }

        Nacionalidade nacionalidadeFinal = ciclistaDto.getNacionalidade() != null ? ciclistaDto.getNacionalidade() : ciclistaExistente.getNacionalidade();
        if (nacionalidadeFinal == null) {
            throw new IllegalArgumentException("Nacionalidade é um campo obrigatório e não pode ser removida.");
        }

        if (nacionalidadeFinal.equals(Nacionalidade.BRASILEIRO)) {
            String cpfFinal = ciclistaDto.getCpf() != null ? ciclistaDto.getCpf() : ciclistaExistente.getCpf();
            if (cpfFinal == null || cpfFinal.isBlank()) {
                throw new IllegalArgumentException("CPF é obrigatório para brasileiros");
            }
        } else if (nacionalidadeFinal.equals(Nacionalidade.ESTRANGEIRO)) {
            PassaporteDTO passaporteDto = ciclistaDto.getPassaporte();
            PassaporteEntity passaporteExistente = ciclistaExistente.getPassaporteEntity();

            if (passaporteDto != null) {
                if (passaporteDto.getNumeroPassaporte() == null || passaporteDto.getNumeroPassaporte().isBlank() ||
                        passaporteDto.getPais() == null || passaporteDto.getPais().isBlank() ||
                        passaporteDto.getValidadePassaporte() == null) {
                    throw new IllegalArgumentException("Ao atualizar, o passaporte completo é obrigatório para estrangeiros");
                }
            } else if (passaporteExistente == null) {
                throw new IllegalArgumentException("Passaporte completo é obrigatório para estrangeiros");
            }
        }
    }

    @Transactional
    public CiclistaEntity ativarCiclista(Integer idCiclista) {
        if (idCiclista <= 0) {
            throw new TrataUnprocessableEntityException("id invalido: " + idCiclista);
        }

        Optional<CiclistaEntity> ciclistaEntity = ciclistaRepository.findById(idCiclista);
        if (!ciclistaEntity.isPresent()) {
            throw new EntityNotFoundException("Ciclista não encontrado com id: " + idCiclista);
        }

        CiclistaEntity ciclistaAtual = ciclistaEntity.get();
        if (!ciclistaAtual.getStatus().equals(Status.AGUARDANDO_CONFIRMACAO)) {
            throw new TrataUnprocessableEntityException("Dados não correspondem a registro pendente");
        }
        ciclistaAtual.setHoraConfirmacaoEmail(LocalDateTime.now());
        ciclistaAtual.setStatus(Status.ATIVO);
        return ciclistaRepository.save(ciclistaAtual);

    }

    public boolean existeEmail(String email) {
        if (!email.contains("@")){
            throw new IllegalArgumentException("Email inválido");
        }

        return ciclistaRepository.existsByEmail(email);
    }

    public CiclistaEntity buscarCiclistaporId(Integer idCiclista) {
        if (idCiclista <= 0) {
            throw new TrataUnprocessableEntityException("id invalido: " + idCiclista);
        }

        return ciclistaRepository.findById(idCiclista)
                .orElseThrow((()-> new EntityNotFoundException("Ciclista não encontrado com ID: " + idCiclista)));
    }

    public boolean permiteAluguel(Integer idCiclista) {
        CiclistaEntity ciclista = ciclistaRepository.findById(idCiclista)
                .orElseThrow(() -> new EntityNotFoundException("Ciclista não encontrado"));

        return ciclista.getStatus().equals(Status.ATIVO) && !ciclista.isAluguelAtivo();
    }

    public Optional<BicicletaDTO> bicicletaAlugada(Integer idCiclista) {
        if (!ciclistaRepository.existsById(idCiclista)) {
            throw new EntityNotFoundException("Ciclista não encontrado com ID: " + idCiclista);
        }

        if (permiteAluguel(idCiclista)) {
            return Optional.empty();
        }

        return Optional.ofNullable(null);
    }

    private void verificarRegrasDeNegocioDeCadastro(NovoCiclistaDTO novoCiclistaDto) {
        if (!novoCiclistaDto.senhaValida()) {
            throw new TrataUnprocessableEntityException("Senhas diferentes");
        }

        if (existeEmail(novoCiclistaDto.getEmail())) {
            throw new TrataUnprocessableEntityException("Email ja existente");
        }

        if (novoCiclistaDto.getNacionalidade() == null) {
            throw new IllegalArgumentException("Nacionalidade é obrigatória");
        }

        if (novoCiclistaDto.getNacionalidade().equals(Nacionalidade.BRASILEIRO)) {
            if (novoCiclistaDto.getCpf() == null || novoCiclistaDto.getCpf().isBlank()) {
                throw new IllegalArgumentException("CPF é obrigatório para brasileiros");
            }
        }else if (novoCiclistaDto.getNacionalidade().equals(Nacionalidade.ESTRANGEIRO)) {
            if (novoCiclistaDto.getPassaporte() == null) {
                throw new IllegalArgumentException("Passaporte é obrigatório para estrangeiros");
            }

            if (novoCiclistaDto.getPassaporte().getPais() == null || novoCiclistaDto.getPassaporte().getPais().isBlank()) {
                throw new IllegalArgumentException("País do passaporte é obrigatório para estrangeiros");
            }

            if (novoCiclistaDto.getPassaporte().getValidadePassaporte() == null) {
                throw new IllegalArgumentException("Validade do passaporte é obrigatório para estrangeiros");
            }

            if (novoCiclistaDto.getPassaporte().getNumeroPassaporte() == null || novoCiclistaDto.getPassaporte().getNumeroPassaporte().isBlank()) {
                throw new IllegalArgumentException("Numero do passaporte é obrigatório para estrangeiros");
            }
        }
    }
    public boolean validarCartao(NovoCartaoDeCreditoDTO cartao) {
        log.info(cartao.toString());
        return true;
    }
}
