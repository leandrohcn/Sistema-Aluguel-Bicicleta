package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.*;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.*;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessabeEntity;
import com.sistema_bicicletario.ms_aluguel.repositories.CiclistaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CiclistaServiceTest {

    @Mock
    private CiclistaRepository ciclistaRepository;

    @InjectMocks
    @Spy
    private CiclistaService ciclistaService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveCadastrarCiclistaBrasileiroComSucesso() {
        NovoCiclistaDTO dto = new NovoCiclistaDTO();
        dto.setNome("João");
        dto.setDataNascimento(LocalDate.of(2000, 1, 1));
        dto.setCpf("12345678900");
        dto.setEmail("joao@email.com");
        dto.setNacionalidade(Nacionalidade.BRASILEIRO);
        dto.setUrlFotoDocumento("url");
        dto.setSenha("123");
        dto.setConfirmaSenha("123");

        NovoCartaoDeCreditoDTO cartao = new NovoCartaoDeCreditoDTO();
        cartao.setNumeroCartao(Long.parseLong("1234123412341234"));
        cartao.setCvv(Integer.parseInt("123"));
        cartao.setValidadeCartao(String.valueOf(LocalDate.of(2030, 1, 1)));
        dto.setMeioDePagamento(cartao);

        when(ciclistaRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(ciclistaRepository.save(any(CiclistaEntity.class))).thenAnswer(i -> i.getArgument(0));

        CiclistaEntity response = ciclistaService.cadastrarCiclista(dto);
        assertEquals("João", response.getNome());
    }

    @Test
    void deveCadastrarCiclistaEstrangeiroComSucesso() {
        PassaporteDTO passaporte = new PassaporteDTO();
        passaporte.setNumeroPassaporte("123456");
        passaporte.setValidadePassaporte(String.valueOf(LocalDate.of(2030, 1, 1)));
        passaporte.setPais("US");

        NovoCiclistaDTO dto = new NovoCiclistaDTO();
        dto.setNome("Ana");
        dto.setDataNascimento(LocalDate.of(1990, 5, 10));
        dto.setEmail("ana@email.com");
        dto.setNacionalidade(Nacionalidade.ESTRANGEIRO);
        dto.setUrlFotoDocumento("url");
        dto.setSenha("senha123");
        dto.setConfirmaSenha("senha123");

        NovoCartaoDeCreditoDTO cartao = new NovoCartaoDeCreditoDTO();
        cartao.setNumeroCartao(Long.parseLong("1111222233334444"));
        cartao.setCvv(Integer.parseInt("321"));
        cartao.setValidadeCartao(String.valueOf(LocalDate.of(2031, 6, 1)));
        dto.setMeioDePagamento(cartao);
        dto.setPassaporte(passaporte);

        when(ciclistaRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(ciclistaRepository.save(any(CiclistaEntity.class))).thenAnswer(i -> i.getArgument(0));

        CiclistaEntity c = ciclistaService.cadastrarCiclista(dto);
        assertEquals("Ana", c.getNome());
    }

    @Test
    void deveLancarErroAoCadastrarBrasileiroSemCpf() {
        NovoCiclistaDTO dto = new NovoCiclistaDTO();
        dto.setNome("Carlos");
        dto.setDataNascimento(LocalDate.of(1985, 3, 15));
        dto.setCpf("");
        dto.setEmail("carlos@email.com");
        dto.setNacionalidade(Nacionalidade.BRASILEIRO);
        dto.setUrlFotoDocumento("url");
        dto.setSenha("abc");
        dto.setConfirmaSenha("abc");

        NovoCartaoDeCreditoDTO cartao = new NovoCartaoDeCreditoDTO();
        cartao.setNumeroCartao(Long.parseLong("1234567890123456"));
        cartao.setCvv(Integer.parseInt("999"));
        cartao.setValidadeCartao(String.valueOf(LocalDate.of(2028, 12, 1)));
        dto.setMeioDePagamento(cartao);

        when(ciclistaRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        assertThrows(TrataUnprocessabeEntity.class, () -> ciclistaService.cadastrarCiclista(dto));
    }

    @Test
    void deveLancarErroAoCadastrarEstrangeiroComPassaporteIncompleto() {
        PassaporteDTO passaporte = new PassaporteDTO();
        passaporte.setNumeroPassaporte(null);
        passaporte.setValidadePassaporte(null);
        passaporte.setPais("");

        NovoCiclistaDTO dto = new NovoCiclistaDTO();
        dto.setNome("Miguel");
        dto.setDataNascimento(LocalDate.of(1992, 8, 20));
        dto.setEmail("miguel@email.com");
        dto.setNacionalidade(Nacionalidade.ESTRANGEIRO);
        dto.setUrlFotoDocumento("url");
        dto.setSenha("senha");
        dto.setConfirmaSenha("senha");

        NovoCartaoDeCreditoDTO cartao = new NovoCartaoDeCreditoDTO();
        cartao.setNumeroCartao(Long.parseLong("0000111122223333"));
        cartao.setCvv(Integer.parseInt("111"));
        cartao.setValidadeCartao(String.valueOf(LocalDate.of(2027, 7, 1)));
        dto.setMeioDePagamento(cartao);
        dto.setPassaporte(passaporte);

        when(ciclistaRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        assertThrows(TrataUnprocessabeEntity.class, () -> ciclistaService.cadastrarCiclista(dto));
    }

    @Test
    void deveAtualizarEmailComSucesso() {
        Integer id = 1;
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setId(id);
        ciclista.setEmail("velho@email.com");
        ciclista.setNacionalidade(Nacionalidade.BRASILEIRO);
        ciclista.setCpf("12345678900");

        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        dto.setEmail("novo@email.com");

        when(ciclistaRepository.findById(id)).thenReturn(Optional.of(ciclista));
        when(ciclistaRepository.existsByEmail("novo@email.com")).thenReturn(false);
        when(ciclistaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CiclistaEntity atualizado = ciclistaService.atualizarCiclista(id, dto);
        assertEquals("novo@email.com", atualizado.getEmail());
    }

    @Test
    void deveLancarErroAoAtualizarEmailDuplicado() {
        Integer id = 1;
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setEmail("original@email.com");
        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        dto.setEmail("duplicado@email.com");

        when(ciclistaRepository.findById(id)).thenReturn(Optional.of(ciclista));
        when(ciclistaRepository.existsByEmail("duplicado@email.com")).thenReturn(true);

        assertThrows(TrataUnprocessabeEntity.class, () -> ciclistaService.atualizarCiclista(id, dto));
    }

    @Test
    void deveAtualizarSenhaComSucesso() {
        Integer id = 2;
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setId(id);
        ciclista.setNacionalidade(Nacionalidade.BRASILEIRO);
        ciclista.setCpf("12345678900");
        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        dto.setSenha("novaSenha");
        dto.setConfirmaSenha("novaSenha");

        when(ciclistaRepository.findById(id)).thenReturn(Optional.of(ciclista));
        when(ciclistaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CiclistaEntity atualizado = ciclistaService.atualizarCiclista(id, dto);
        assertEquals("novaSenha", atualizado.getSenha());
    }

    @Test
    void deveLancarErroAoAtualizarSenhaInvalida() {
        Integer id = 2;
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setId(id);
        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        dto.setSenha("senha1");
        dto.setConfirmaSenha("senha2");

        when(ciclistaRepository.findById(id)).thenReturn(Optional.of(ciclista));

        assertThrows(TrataUnprocessabeEntity.class, () -> ciclistaService.atualizarCiclista(id, dto));
    }

    @Test
    void deveAtivarCiclistaComSucesso() {
        Integer id = 10;
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setStatus(Status.AGUARDANDO_CONFIRMACAO);

        when(ciclistaRepository.findById(id)).thenReturn(Optional.of(ciclista));
        doReturn(true).when(ciclistaService).confirmaEmail();

        CiclistaEntity ativado = ciclistaService.ativarCiclista(id);

        assertEquals(Status.ATIVO, ativado.getStatus());
        assertNotNull(ativado.getConfirmaEmail());
        assertNotNull(ativado.getConfirmaEmail().getHoraConfirmacao());
    }

    @Test
    void deveLancarErroSeConfirmaEmailRetornarFalse() {
        Integer id = 11;
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setStatus(Status.AGUARDANDO_CONFIRMACAO);

        when(ciclistaRepository.findById(id)).thenReturn(Optional.of(ciclista));
        doReturn(false).when(ciclistaService).confirmaEmail();

        CiclistaEntity naoAtivado = ciclistaService.ativarCiclista(id);
        assertEquals(Status.AGUARDANDO_CONFIRMACAO, naoAtivado.getStatus());
        assertNull(naoAtivado.getConfirmaEmail());
    }

    @Test
    void deveLancarErroAoAtivarCiclistaComIdInvalido() {
        assertThrows(TrataUnprocessabeEntity.class, () -> ciclistaService.ativarCiclista(-1));
    }

    @Test
    void deveLancarErroSeCiclistaNaoExistirAoAtivar() {
        when(ciclistaRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> ciclistaService.ativarCiclista(99));
    }

    @Test
    void deveLancarErroSeCiclistaJaEstiverAtivo() {
        Integer id = 12;
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setStatus(Status.ATIVO);

        when(ciclistaRepository.findById(id)).thenReturn(Optional.of(ciclista));
        doReturn(true).when(ciclistaService).confirmaEmail();

        assertThrows(TrataUnprocessabeEntity.class, () -> ciclistaService.ativarCiclista(id));
    }

    @Test
    void deveBuscarCiclistaPorIdComSucesso() {
        Integer id = 5;
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setNome("Leandro");

        when(ciclistaRepository.findById(id)).thenReturn(Optional.of(ciclista));
        CiclistaEntity resultado = ciclistaService.buscarCiclistaporId(id);
        assertEquals("Leandro", resultado.getNome());
    }

    @Test
    void deveLancarErroSeBuscarCiclistaComIdInvalido() {
        assertThrows(TrataUnprocessabeEntity.class, () -> ciclistaService.buscarCiclistaporId(0));
    }

    @Test
    void deveLancarErroSeCiclistaNaoForEncontrado() {
        when(ciclistaRepository.findById(123)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> ciclistaService.buscarCiclistaporId(123));
    }

    @Test
    void deveConfirmarEmailComSucesso() {
        when(ciclistaRepository.existsByEmail("teste@email.com")).thenReturn(true);
        assertDoesNotThrow(() -> ciclistaService.existeEmail("teste@email.com"));
    }

    @Test
    void deveLancarErroEmailSemArroba() {
        assertThrows(IllegalArgumentException.class, () -> ciclistaService.existeEmail("testeemail.com"));
    }

    @Test
    void deveLancarErroEmailInexistente() {
        when(ciclistaRepository.existsByEmail("naoexiste@email.com")).thenReturn(false);
        assertThrows(TrataUnprocessabeEntity.class, () -> ciclistaService.existeEmail("naoexiste@email.com"));
    }
}
