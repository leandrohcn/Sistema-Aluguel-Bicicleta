package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.*;
import com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito.CartaoDeCreditoEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Nacionalidade;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.PassaporteEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Status;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessableEntity;
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
        // nao sei qual minha opiniao sobre isso mas ok
        verificarRegrasDeNegocioDeCadastro(novoCiclistaDto);

        CiclistaEntity ciclista = new CiclistaEntity(
                novoCiclistaDto.getNome(),
                novoCiclistaDto.getDataNascimento(),
                novoCiclistaDto.getCpf(),
                novoCiclistaDto.getEmail(),
                novoCiclistaDto.getNacionalidade(),
                novoCiclistaDto.getUrlFotoDocumento(),
                //tem q criptografar a senha
                novoCiclistaDto.getSenha(),
                novoCiclistaDto.getConfirmaSenha()
        );

        // isso aqui nao po, eu posso criar um ciclista nacional q tenha passaporte != null
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
        // po aqui poderia ja retornar o dto ao ivnes da entidade ne
        return ciclistaRepository.save(ciclista);
    }

    @Transactional
    public CiclistaEntity atualizarCiclista(Integer id, AtualizaCiclistaDTO ciclistaDTO) {

        // mas que isso é o maior return do mundo
        // acho mais legal tirar desse map, atribuir a uma entidade e depois salvar ela de novo
        return ciclistaRepository.findById(id).map(ciclista -> {
            ciclista.setNome(ciclistaDTO.getNome() != null ? ciclistaDTO.getNome() : ciclista.getNome());
            ciclista.setDataNascimento(ciclistaDTO.getDataNascimento() != null ? ciclistaDTO.getDataNascimento() : ciclista.getDataNascimento());
            ciclista.setNacionalidade(ciclistaDTO.getNacionalidade() != null ? ciclistaDTO.getNacionalidade() : ciclista.getNacionalidade());
            ciclista.setUrlFotoDocumento(ciclistaDTO.getUrlFotoDocumento() != null ? ciclistaDTO.getUrlFotoDocumento() : ciclista.getUrlFotoDocumento());
            ciclista.setCpf(ciclistaDTO.getCpf() != null ? ciclistaDTO.getCpf() : ciclista.getCpf());

            if (ciclistaDTO.getSenha() != null && !ciclistaDTO.getSenha().isBlank()) {
                if (!ciclistaDTO.senhaValida()) {
                    throw new TrataUnprocessableEntity("Senhas diferentes");
                }
                ciclista.setSenha(ciclistaDTO.getSenha());
                ciclista.setConfirmaSenha(ciclistaDTO.getConfirmaSenha());
            }

            if (ciclistaDTO.getEmail() != null && !ciclistaDTO.getEmail().isBlank()) {
                if (!ciclistaDTO.getEmail().equals(ciclista.getEmail()) && ciclistaRepository.existsByEmail(ciclistaDTO.getEmail())) {
                    throw new TrataUnprocessableEntity("Email já existente");
                }
                ciclista.setEmail(ciclistaDTO.getEmail());
            } else {
                ciclista.setEmail(ciclista.getEmail());
            }
            //isso aq vai ser resolvido com a atualizacao do UC01
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
            throw new TrataUnprocessableEntity("id invalido: " + idCiclista);
        }

        Optional<CiclistaEntity> ciclistaEntity = ciclistaRepository.findById(idCiclista);
        if (!ciclistaEntity.isPresent()) {
            throw new EntityNotFoundException("Ciclista não encontrado com id: " + idCiclista);
        }
        if (confirmaEmail()) {
            CiclistaEntity ciclistaAtual = ciclistaEntity.get();
            if (!ciclistaAtual.getStatus().equals(Status.AGUARDANDO_CONFIRMACAO)) {
                throw new TrataUnprocessableEntity("Dados não correspondem a registro pendente");
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

    //essa função era pra ser boolean
    public void existeEmail(String email) {
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Email enviado é inválido");
        }
        // quando nao tem o email, nao era pra dar erro era pra retornar false
//        if (!ciclistaRepository.existsByEmail(email)) {
//            throw new TrataUnprocessabeEntity("Dados inválidos");
//        }



        ciclistaRepository.existsByEmail(email);
    }

    public CiclistaEntity buscarCiclistaporId(Integer idCiclista) {
        if (idCiclista <= 0) {
            throw new TrataUnprocessableEntity("id invalido: " + idCiclista);
        }

        if (idCiclista.toString().isBlank()){
            throw new IllegalArgumentException("id invalido");
        }

        return ciclistaRepository.findById(idCiclista)
                .orElseThrow((()-> new EntityNotFoundException("Ciclista não encontrado com ID: " + idCiclista)));
    }

    public boolean permiteAluguel(Integer idCiclista) {
        if (!ciclistaRepository.existsById(idCiclista)) {
            throw new EntityNotFoundException("Ciclista não encontrado com ID: " + idCiclista);
        }
        return true;
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
        //eu n acho q seja tudo unprocessabe entity nao

        if (!novoCiclistaDto.senhaValida()) {
            throw new TrataUnprocessableEntity("Senhas diferentes");
        }

        if (ciclistaRepository.existsByEmail(novoCiclistaDto.getEmail())) {
            throw new TrataUnprocessableEntity("Email ja existente");
        }

        if (novoCiclistaDto.getNacionalidade() == null) {
            throw new TrataUnprocessableEntity("Nacionalidade é obrigatória");
        }

        if (novoCiclistaDto.getNacionalidade().equals(Nacionalidade.BRASILEIRO)) {
            if (novoCiclistaDto.getCpf() == null || novoCiclistaDto.getCpf().isBlank()) {
                throw new TrataUnprocessableEntity("CPF é obrigatório para brasileiros");
            }
        }else if (novoCiclistaDto.getNacionalidade().equals(Nacionalidade.ESTRANGEIRO)) {
            if (novoCiclistaDto.getPassaporte() == null) {
                throw new TrataUnprocessableEntity("Passaporte é obrigatório para estrangeiros");
            }

            if (novoCiclistaDto.getPassaporte().getPais() == null || novoCiclistaDto.getPassaporte().getPais().isBlank()) {
                throw new TrataUnprocessableEntity("País do passaporte é obrigatório para estrangeiros");
            }

            if (novoCiclistaDto.getPassaporte().getValidadePassaporte() == null) {
                throw new TrataUnprocessableEntity("Validade do passaporte é obrigatório para estrangeiros");
            }

            if (novoCiclistaDto.getPassaporte().getNumeroPassaporte() == null || novoCiclistaDto.getPassaporte().getNumeroPassaporte().isBlank()) {
                throw new TrataUnprocessableEntity("Numero do passaporte é obrigatório para estrangeiros");
            }
        }
    }

    private void regrasDeNegocioAtualiza(CiclistaEntity ciclista) {

        if (ciclista.getNacionalidade().equals(Nacionalidade.BRASILEIRO)) {
            if (ciclista.getCpf() == null || ciclista.getCpf().isBlank()) {
                throw new TrataUnprocessableEntity("CPF é obrigatório para brasileiros");
            }
        }

        if (ciclista.getNacionalidade().equals(Nacionalidade.ESTRANGEIRO)) {
            PassaporteEntity passaporte = ciclista.getPassaporteEntity();
            if (passaporte == null ||
                    passaporte.getNumeroPassaporte() == null || passaporte.getNumeroPassaporte().isBlank() ||
                    passaporte.getPais() == null || passaporte.getPais().isBlank() ||
                    passaporte.getValidadePassaporte() == null) {
                throw new TrataUnprocessableEntity("Passaporte completo é obrigatório para estrangeiros");
            }
        }
    }
}
