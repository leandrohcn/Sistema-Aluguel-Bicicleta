package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.NovoFuncionarioDTO;
import com.sistema_bicicletario.ms_aluguel.entitys.funcionario.FuncionarioEntity;
import com.sistema_bicicletario.ms_aluguel.repositorys.FuncionarioRepository;
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
                funcionarioDTO.getEmail(),
                funcionarioDTO.getIdade(),
                funcionarioDTO.getCpf(),
                funcionarioDTO.getFuncao()
        );
        return funcionarioRepository.save(funcionario);
    }

    public FuncionarioEntity atualizaFuncionario(NovoFuncionarioDTO funcionarioDTO,
                                    Long idFuncionario) {

        return funcionarioRepository.findById(idFuncionario).map(funcionarioEntity -> {
            funcionarioEntity.setCpf(funcionarioDTO.getCpf() != null ? funcionarioDTO.getCpf() : funcionarioEntity.getCpf());
            funcionarioEntity.setNome(funcionarioDTO.getNome() != null ? funcionarioDTO.getNome() : funcionarioEntity.getNome());
            funcionarioEntity.setEmail(funcionarioDTO.getEmail() != null ? funcionarioDTO.getEmail() : funcionarioEntity.getEmail());
            funcionarioEntity.setSenha(funcionarioDTO.getSenha() != null ? funcionarioDTO.getSenha() : funcionarioEntity.getSenha());
            funcionarioEntity.setConfirmaSenha(funcionarioDTO.getConfirmaSenha() != null ? funcionarioDTO.getConfirmaSenha() : funcionarioEntity.getConfirmaSenha());
            funcionarioEntity.setIdade(funcionarioDTO.getIdade() > 0 ? funcionarioDTO.getIdade() : funcionarioEntity.getIdade());

           return funcionarioRepository.save(funcionarioEntity);

        }).orElseThrow(() -> new RuntimeException("Funcionario não encontrado com ID: " + idFuncionario));
    }

    public void excluiFuncionario(Long idFuncionario) {
        if (funcionarioRepository.existsById(idFuncionario)) {
            funcionarioRepository.deleteById(idFuncionario);
        }
    }

    public FuncionarioEntity buscaFuncionarioPorId(Long idFuncionario) {
        return funcionarioRepository.findById(idFuncionario).orElse(null);
    }

    public List<FuncionarioEntity> buscaTodosFuncionario() {
        if (funcionarioRepository.findAll().isEmpty()) {
            return null;
        }
        return funcionarioRepository.findAll();
    }
}
