package com.sistema_bicicletario.ms_aluguel.services;

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

    public FuncionarioEntity criaFuncionario(NovoFuncionarioDTO funcionarioDTO) {
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
            return funcionarioRepository.save(funcionario);
        }
        throw new TrataUnprocessableEntityException("Senha Invalida");
    }

    public FuncionarioEntity atualizaFuncionario(NovoFuncionarioDTO funcionarioDTO, Integer idFuncionario) {

        return funcionarioRepository.findById(idFuncionario).map(funcionarioAtualizado -> {
            funcionarioAtualizado.setNome(funcionarioDTO.getNome());
            funcionarioAtualizado.setSenha(funcionarioDTO.getSenha());
            funcionarioAtualizado.setConfirmaSenha(funcionarioDTO.getConfirmaSenha());
            funcionarioAtualizado.setEmail(funcionarioDTO.getEmail());
            funcionarioAtualizado.setIdade(funcionarioDTO.getIdade());
            funcionarioAtualizado.setCpf(funcionarioAtualizado.getCpf());
            funcionarioAtualizado.setFuncao(funcionarioDTO.getFuncao());

            if (funcionarioDTO.senhaValida()) {
                return funcionarioRepository.save(funcionarioAtualizado);
            }
                throw new TrataUnprocessableEntityException("Senha Invalida");
        }).orElseThrow(() -> new EntityNotFoundException("Funcionario não encontrado com id: " + idFuncionario));
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

    public FuncionarioEntity buscaFuncionarioPorId(Integer idFuncionario) {
        if (idFuncionario <= 0) {
            throw new TrataUnprocessableEntityException("O ID deve ser um número positivo.");
        }

        return funcionarioRepository.findById(idFuncionario)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado com ID: " + idFuncionario));
    }

    public List<FuncionarioEntity> buscaTodosFuncionario() {
        return funcionarioRepository.findAll();
    }
}
