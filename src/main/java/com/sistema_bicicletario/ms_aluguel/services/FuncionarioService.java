package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.NovoFuncionarioDTO;
import com.sistema_bicicletario.ms_aluguel.entitys.funcionario.FuncionarioEntity;
import com.sistema_bicicletario.ms_aluguel.repositorys.FuncionarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    public FuncionarioService(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    public FuncionarioEntity criaFuncionario(NovoFuncionarioDTO funcionarioDTO) {
        FuncionarioEntity funcionario = new FuncionarioEntity();
        funcionario.setNome(funcionarioDTO.getNome());
        funcionario.setCpf(funcionarioDTO.getCpf());
        funcionario.setEmail(funcionarioDTO.getEmail());
        funcionario.setSenha(funcionarioDTO.getSenha());
        funcionario.setConfirmaSenha(funcionarioDTO.getConfirmaSenha());
        funcionario.setIdade(funcionarioDTO.getIdade());
        funcionario.setFuncao(funcionarioDTO.getFuncao());
        FuncionarioEntity funcionarioNovo = funcionarioRepository.save(funcionario);

        return new FuncionarioEntity(funcionarioNovo);
    }

    public void atualizaFuncionario(NovoFuncionarioDTO funcionarioDTO,
                                    Long idFuncionario) {

        funcionarioRepository.findById(idFuncionario).map(funcionarioEntity -> {
            funcionarioEntity.setCpf(funcionarioDTO.getCpf() != null ? funcionarioDTO.getCpf() : funcionarioEntity.getCpf());
            funcionarioEntity.setNome(funcionarioDTO.getNome() != null ? funcionarioDTO.getNome() : funcionarioEntity.getNome());
            funcionarioEntity.setEmail(funcionarioDTO.getEmail() != null ? funcionarioDTO.getEmail() : funcionarioEntity.getEmail());
            funcionarioEntity.setSenha(funcionarioDTO.getSenha() != null ? funcionarioDTO.getSenha() : funcionarioEntity.getSenha());
            funcionarioEntity.setConfirmaSenha(funcionarioDTO.getConfirmaSenha() != null ? funcionarioDTO.getConfirmaSenha() : funcionarioEntity.getConfirmaSenha());
            funcionarioEntity.setIdade(funcionarioDTO.getIdade() > 0 ? funcionarioDTO.getIdade() : funcionarioEntity.getIdade());

            funcionarioRepository.save(funcionarioEntity);
            return funcionarioEntity;
        });
    }

    public void excluiFuncionario(Long idFuncionario) {
        if (funcionarioRepository.existsById(idFuncionario)) {
            funcionarioRepository.deleteById(idFuncionario);
        }
    }

}
