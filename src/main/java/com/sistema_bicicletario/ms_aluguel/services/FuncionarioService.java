package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.FuncionarioResponseDTO;
import com.sistema_bicicletario.ms_aluguel.dtos.NovoFuncionarioDTO;
import com.sistema_bicicletario.ms_aluguel.entities.funcionario.FuncionarioEntity;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessableEntityException;
import com.sistema_bicicletario.ms_aluguel.repositories.FuncionarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    public FuncionarioResponseDTO criaFuncionario(NovoFuncionarioDTO funcionarioDTO) {
        FuncionarioEntity funcionario = new FuncionarioEntity(
                funcionarioDTO.getNome(),
                funcionarioDTO.getSenha(),
                funcionarioDTO.getConfirmaSenha(),
                funcionarioDTO.getEmail(),
                funcionarioDTO.getIdade(),
                funcionarioDTO.getCpf(),
                funcionarioDTO.getFuncao()
        );
        if (funcionarioDTO.senhaValida()) {
            funcionarioRepository.save(funcionario);
            return new FuncionarioResponseDTO(funcionario);
        }
        throw new TrataUnprocessableEntityException("Senha Invalida");
    }

    public FuncionarioResponseDTO atualizaFuncionario(NovoFuncionarioDTO funcionarioDTO, Integer idFuncionario) {
        FuncionarioEntity funcionarioAtualizado = funcionarioRepository.findById(idFuncionario).orElseThrow(EntityNotFoundException::new);
                funcionarioAtualizado.setNome(funcionarioDTO.getNome());
                funcionarioAtualizado.setSenha(funcionarioDTO.getSenha());
                funcionarioAtualizado.setConfirmaSenha(funcionarioDTO.getConfirmaSenha());
                funcionarioAtualizado.setEmail(funcionarioDTO.getEmail());
                funcionarioAtualizado.setIdade(funcionarioDTO.getIdade());
                funcionarioAtualizado.setCpf(funcionarioAtualizado.getCpf());
                funcionarioAtualizado.setFuncao(funcionarioDTO.getFuncao());

            if (funcionarioDTO.senhaValida()) {
                funcionarioRepository.save(funcionarioAtualizado);
                return new FuncionarioResponseDTO(funcionarioAtualizado);
            }
                throw new TrataUnprocessableEntityException("Senha Invalida");
    }

    public void excluiFuncionario(Integer idFuncionario) {
       if (idFuncionario <= 0) {
           throw new TrataUnprocessableEntityException("O ID deve ser um número positivo.");
       }
       if (!funcionarioRepository.existsById(idFuncionario)) {
           throw new EntityNotFoundException("Funcionario não encontrado com id: " + idFuncionario);
       }
       funcionarioRepository.deleteById(idFuncionario);
    }

    public FuncionarioResponseDTO buscaFuncionarioPorId(Integer idFuncionario) {
        if (idFuncionario <= 0) {
            throw new TrataUnprocessableEntityException("O ID deve ser um número positivo.");
        }

        FuncionarioEntity funcionarioEntity = funcionarioRepository.findById(idFuncionario)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado"));
        return new FuncionarioResponseDTO(funcionarioEntity);
    }

    public List<FuncionarioEntity> buscaTodosFuncionario() {
        return funcionarioRepository.findAll();
    }
}
