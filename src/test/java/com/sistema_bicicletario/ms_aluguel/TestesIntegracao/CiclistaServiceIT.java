package com.sistema_bicicletario.ms_aluguel.TestesIntegracao;

import com.sistema_bicicletario.ms_aluguel.clients.ExternoClient;
import com.sistema_bicicletario.ms_aluguel.dtos.*;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Nacionalidade;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Status;
import com.sistema_bicicletario.ms_aluguel.repositories.CartaoRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.CiclistaRepository;
import com.sistema_bicicletario.ms_aluguel.services.CiclistaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@Transactional
class CiclistaServiceIT {

    @Autowired
    private CiclistaService ciclistaService;

    @Autowired
    private CiclistaRepository ciclistaRepository;

    @Autowired
    private CartaoRepository cartaoRepository;

    @MockBean
    private ExternoClient externoClient; // simula validação de cartão externo

    @MockBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @BeforeEach
    void setUp() {
        Mockito.when(bCryptPasswordEncoder.encode(Mockito.any())).thenReturn("senhaCodificada");
        cartaoRepository.deleteAll();
        ciclistaRepository.deleteAll();
    }

    private NovoCartaoDeCreditoDTO novoCartaoValido() {
        return new NovoCartaoDeCreditoDTO("Titular Teste", "124", LocalDate.of(2025,12,12), "123532553243234333");
    }

    private PassaporteDTO passaporteValido() {
        PassaporteDTO passaporte = new PassaporteDTO();
        passaporte.setNumeroPassaporte("XP123456");
        passaporte.setValidadePassaporte(String.valueOf(LocalDate.now().plusYears(2)));
        passaporte.setPais("FR");
        return passaporte;
    }

    private NovoCiclistaDTO ciclistaBrasileiroValido() {
        NovoCiclistaDTO dto = new NovoCiclistaDTO();
        dto.setNome("João Silva");
        dto.setDataNascimento(LocalDate.of(1990, 5, 20));
        dto.setCpf("12345678901");
        dto.setEmail("joao@email.com");
        dto.setSenha("123456");
        dto.setConfirmaSenha("123456");
        dto.setNacionalidade(Nacionalidade.BRASILEIRO);
        dto.setUrlFotoDocumento("foto.com/doc.jpg");
        dto.setMeioDePagamento(novoCartaoValido());
        return dto;
    }

    private NovoCiclistaDTO ciclistaEstrangeiroValido() {
        NovoCiclistaDTO dto = new NovoCiclistaDTO();
        dto.setNome("Anna Müller");
        dto.setDataNascimento(LocalDate.of(1985, 7, 12));
        dto.setEmail("anna@email.com");
        dto.setSenha("abcdef");
        dto.setConfirmaSenha("abcdef");
        dto.setNacionalidade(Nacionalidade.ESTRANGEIRO);
        dto.setUrlFotoDocumento("foto.com/estrangeira.jpg");
        dto.setPassaporte(passaporteValido());
        dto.setMeioDePagamento(novoCartaoValido());
        return dto;
    }

    @Test
    void deveCadastrarCiclistaBrasileiroComSucesso() {
        Mockito.doAnswer(invocation -> null).when(externoClient).validarCartaoDeCredito(Mockito.any());
        doReturn(ResponseEntity.ok().build()).when(externoClient).enviarEmail(any(EnviaEmailDTO.class));
        CiclistaResponseDTO response = ciclistaService.cadastrarCiclista(ciclistaBrasileiroValido());

        assertNotNull(response);
        assertEquals("João Silva", response.getNome());
        assertEquals(Nacionalidade.BRASILEIRO, response.getNacionalidade());
        assertEquals("AGUARDANDO_CONFIRMACAO", response.getStatus());
        assertNotNull(cartaoRepository.findByCiclistaId(response.getId()));
    }

    @Test
    void deveCadastrarCiclistaEstrangeiroComSucesso() {
        Mockito.doAnswer(invocation -> null).when(externoClient).validarCartaoDeCredito(Mockito.any());
        doReturn(ResponseEntity.ok().build()).when(externoClient).enviarEmail(any(EnviaEmailDTO.class));
        CiclistaResponseDTO response = ciclistaService.cadastrarCiclista(ciclistaEstrangeiroValido());

        assertNotNull(response);
        assertEquals("Anna Müller", response.getNome());
        assertEquals(Nacionalidade.ESTRANGEIRO, response.getNacionalidade());
        assertNotNull(response.getPassaporte());
    }

    @Test
    void deveAtualizarNomeCiclistaComSucesso() {
        Mockito.doAnswer(invocation -> null).when(externoClient).validarCartaoDeCredito(Mockito.any());
        doReturn(ResponseEntity.ok().build()).when(externoClient).enviarEmail(any(EnviaEmailDTO.class));
        CiclistaResponseDTO cadastrado = ciclistaService.cadastrarCiclista(ciclistaBrasileiroValido());

        AtualizaCiclistaDTO atualizaDTO = new AtualizaCiclistaDTO();
        atualizaDTO.setNome("João Atualizado");
        atualizaDTO.setCpf("12345678901");
        atualizaDTO.setSenha("123456");
        atualizaDTO.setConfirmaSenha("123456");
        atualizaDTO.setNacionalidade(Nacionalidade.BRASILEIRO);
        atualizaDTO.setUrlFotoDocumento("foto.com/doc.jpg");

        CiclistaResponseDTO atualizado = ciclistaService.atualizarCiclista(cadastrado.getId(), atualizaDTO);

        assertEquals("João Atualizado", atualizado.getNome());
    }

    @Test
    void deveAtivarCiclistaComSucesso() {
        Mockito.doAnswer(invocation -> null).when(externoClient).validarCartaoDeCredito(Mockito.any());
        CiclistaResponseDTO cadastrado = ciclistaService.cadastrarCiclista(ciclistaBrasileiroValido());

        CiclistaEntity ativado = ciclistaService.ativarCiclista(cadastrado.getId());

        assertEquals(Status.ATIVO, ativado.getStatus());
        assertNotNull(ativado.getHoraConfirmacaoEmail());
    }

    @Test
    void naoDeveCadastrarCiclistaComEmailDuplicado() {
        Mockito.doAnswer(invocation -> null).when(externoClient).validarCartaoDeCredito(Mockito.any());
        ciclistaService.cadastrarCiclista(ciclistaBrasileiroValido());

        NovoCiclistaDTO duplicado = ciclistaBrasileiroValido();
        duplicado.setCpf("99999999999");

        Exception e = assertThrows(RuntimeException.class, () -> ciclistaService.cadastrarCiclista(duplicado));
        assertTrue(e.getMessage().toLowerCase().contains("email"));
    }

    @Test
    void naoPermiteAluguelQuandoStatusInvalidoOuComAluguelAtivo() {
        Mockito.doAnswer(invocation -> null).when(externoClient).validarCartaoDeCredito(Mockito.any());
        CiclistaResponseDTO ciclista = ciclistaService.cadastrarCiclista(ciclistaBrasileiroValido());
        assertFalse(ciclistaService.permiteAluguel(ciclista.getId()));

        ciclistaService.ativarCiclista(ciclista.getId());

        CiclistaEntity entity = ciclistaRepository.findById(ciclista.getId()).get();
        entity.setAluguelAtivo(true);
        ciclistaRepository.save(entity);

        assertFalse(ciclistaService.permiteAluguel(ciclista.getId()));
    }
}
