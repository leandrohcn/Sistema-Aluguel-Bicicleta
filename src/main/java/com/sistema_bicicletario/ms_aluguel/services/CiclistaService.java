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
        CiclistaEntity ciclistaSalvo = ciclistaRepository.save(novoCiclista);

        processarMeioDePagamento(novoCiclistaDto, novoCiclista);
        novoCiclista.setStatus(Status.AGUARDANDO_CONFIRMACAO);
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
        validarEmail(ciclistaDto, ciclistaExistente);
        validarSenha(ciclistaDto);
        validarNacionalidade(ciclistaDto, ciclistaExistente);
        validarDocumentoPorNacionalidade(ciclistaDto, ciclistaExistente);
    }

    private void validarEmail(AtualizaCiclistaDTO ciclistaDto, CiclistaEntity ciclistaExistente) {
        String novoEmail = ciclistaDto.getEmail();
        if (novoEmail != null && !novoEmail.equalsIgnoreCase(ciclistaExistente.getEmail()) && existeEmail(novoEmail)) {
            throw new TrataUnprocessableEntityException("Email já existente");
        }
    }

    private void validarSenha(AtualizaCiclistaDTO ciclistaDto) {
        if (ciclistaDto.getSenha() != null && !ciclistaDto.senhaValida()) {
            throw new TrataUnprocessableEntityException("Senhas diferentes");
        }
    }

    private void validarNacionalidade(AtualizaCiclistaDTO ciclistaDto, CiclistaEntity ciclistaExistente) {
        Nacionalidade nacionalidadeFinal = ciclistaDto.getNacionalidade() != null ? ciclistaDto.getNacionalidade() : ciclistaExistente.getNacionalidade();
        if (nacionalidadeFinal == null) {
            throw new IllegalArgumentException("Nacionalidade é um campo obrigatório e não pode ser removida.");
        }
    }

    private void validarDocumentoPorNacionalidade(AtualizaCiclistaDTO ciclistaDto, CiclistaEntity ciclistaExistente) {
        Nacionalidade nacionalidadeFinal = ciclistaDto.getNacionalidade() != null ? ciclistaDto.getNacionalidade() : ciclistaExistente.getNacionalidade();

        if (Nacionalidade.BRASILEIRO.equals(nacionalidadeFinal)) {
            String cpfFinal = ciclistaDto.getCpf() != null ? ciclistaDto.getCpf() : ciclistaExistente.getCpf();
            if (cpfFinal == null || cpfFinal.isBlank()) {
                throw new IllegalArgumentException("CPF é obrigatório para brasileiros");
            }
        } else if (Nacionalidade.ESTRANGEIRO.equals(nacionalidadeFinal)) {
            validarPassaporteEstrangeiro(ciclistaDto.getPassaporte(), ciclistaExistente.getPassaporteEntity());
        }
    }

    private void validarPassaporteEstrangeiro(PassaporteDTO passaporteDto, PassaporteEntity passaporteExistente) {
        if (passaporteDto != null) {
            if (passaporteDto.getNumeroPassaporte() == null || passaporteDto.getNumeroPassaporte().isBlank() ||
                    passaporteDto.getPais() == null || passaporteDto.getPais().isBlank() ||
                    passaporteDto.getValidadePassaporte() == null) {
                throw new IllegalArgumentException("Ao atualizar, o passaporte completo é obrigatório para estrangeiros");
            }
        } else if (passaporteExistente == null) {
            // Se não foi enviado passaporte no DTO e não existe no entity, é um erro.
            // Se o passaporteExiste != null, significa que ele já tinha passaporte e não está tentando remover.
            throw new IllegalArgumentException("Passaporte completo é obrigatório para estrangeiros");
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

//    public Optional<BicicletaDTO> bicicletaAlugada(Integer idCiclista) {
//        if (!ciclistaRepository.existsById(idCiclista)) {
//            throw new EntityNotFoundException("Ciclista não encontrado com ID: " + idCiclista);
//        }
//
//        if (permiteAluguel(idCiclista)) {
//            return Optional.empty();
//        }
//
//        return Optional.ofNullable(null);
//    }

    private void verificarRegrasDeNegocioDeCadastro(NovoCiclistaDTO novoCiclistaDto) {
        validarSenhaCadastro(novoCiclistaDto);
        validarEmailCadastro(novoCiclistaDto);
        validarNacionalidadeCadastro(novoCiclistaDto);
        validarDocumentoPorNacionalidadeCadastro(novoCiclistaDto);
    }

    private void validarSenhaCadastro(NovoCiclistaDTO novoCiclistaDto) {
        if (!novoCiclistaDto.senhaValida()) {
            throw new TrataUnprocessableEntityException("Senhas diferentes");
        }
    }

    private void validarEmailCadastro(NovoCiclistaDTO novoCiclistaDto) {
        if (existeEmail(novoCiclistaDto.getEmail())) {
            throw new TrataUnprocessableEntityException("Email já existente");
        }
    }

    private void validarNacionalidadeCadastro(NovoCiclistaDTO novoCiclistaDto) {
        if (novoCiclistaDto.getNacionalidade() == null) {
            throw new IllegalArgumentException("Nacionalidade é obrigatória");
        }
    }

    private void validarDocumentoPorNacionalidadeCadastro(NovoCiclistaDTO novoCiclistaDto) {
        if (Nacionalidade.BRASILEIRO.equals(novoCiclistaDto.getNacionalidade())) {
            if (novoCiclistaDto.getCpf() == null || novoCiclistaDto.getCpf().isBlank()) {
                throw new IllegalArgumentException("CPF é obrigatório para brasileiros");
            }
        } else if (Nacionalidade.ESTRANGEIRO.equals(novoCiclistaDto.getNacionalidade())) {
            validarPassaporteCadastro(novoCiclistaDto.getPassaporte());
        }
    }

    private void validarPassaporteCadastro(PassaporteDTO passaporteDto) {
        if (passaporteDto == null) {
            throw new IllegalArgumentException("Passaporte é obrigatório para estrangeiros");
        }
        if (passaporteDto.getPais() == null || passaporteDto.getPais().isBlank()) {
            throw new IllegalArgumentException("País do passaporte é obrigatório para estrangeiros");
        }
        if (passaporteDto.getValidadePassaporte() == null) {
            throw new IllegalArgumentException("Validade do passaporte é obrigatório para estrangeiros");
        }
        if (passaporteDto.getNumeroPassaporte() == null || passaporteDto.getNumeroPassaporte().isBlank()) {
            throw new IllegalArgumentException("Número do passaporte é obrigatório para estrangeiros");
        }
    }
    public boolean validarCartao(NovoCartaoDeCreditoDTO cartao) {
        log.info(cartao.toString());
        return true;
    }
}
