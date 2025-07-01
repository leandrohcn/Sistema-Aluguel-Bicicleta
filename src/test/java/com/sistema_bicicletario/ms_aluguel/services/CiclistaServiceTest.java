package com.sistema_bicicletario.ms_aluguel.services;

import com.sistema_bicicletario.ms_aluguel.dtos.*;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.*;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessableEntityException;
import com.sistema_bicicletario.ms_aluguel.repositories.CiclistaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        cartao.setNumeroCartao("1234123412341234");
        cartao.setCvv("123");
        cartao.setValidadeCartao(String.valueOf(LocalDate.of(2030, 1, 1)));
        dto.setMeioDePagamento(cartao);

        when(ciclistaRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(ciclistaRepository.save(any(CiclistaEntity.class))).thenAnswer(invocation -> {
            CiclistaEntity entity = invocation.getArgument(0);
            entity.setId(1);
            return entity;
        });

        ArgumentCaptor<CiclistaEntity> ciclistaCaptor = ArgumentCaptor.forClass(CiclistaEntity.class);
        CiclistaResponseDTO response = ciclistaService.cadastrarCiclista(dto);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals(dto.getNome(), response.getNome());

        verify(ciclistaRepository).save(ciclistaCaptor.capture());
        CiclistaEntity ciclistaSalvo = ciclistaCaptor.getValue();
        assertEquals(dto.getCpf(), ciclistaSalvo.getCpf());
        assertNull(ciclistaSalvo.getPassaporteEntity());
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
        cartao.setNumeroCartao("1111222233334444");
        cartao.setCvv("321");
        cartao.setValidadeCartao(String.valueOf(LocalDate.of(2031, 6, 1)));
        dto.setMeioDePagamento(cartao);
        dto.setPassaporte(passaporte);

        when(ciclistaRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(ciclistaRepository.save(any(CiclistaEntity.class))).thenAnswer(invocation -> {
            CiclistaEntity entity = invocation.getArgument(0);
            entity.setId(2);
            return entity;
        });

        ArgumentCaptor<CiclistaEntity> ciclistaCaptor = ArgumentCaptor.forClass(CiclistaEntity.class);

        CiclistaResponseDTO response = ciclistaService.cadastrarCiclista(dto);
        assertNotNull(response);
        assertEquals(2, response.getId());

        verify(ciclistaRepository).save(ciclistaCaptor.capture());
        CiclistaEntity ciclistaSalvo = ciclistaCaptor.getValue();
        assertNull(ciclistaSalvo.getCpf());
        assertNotNull(ciclistaSalvo.getPassaporteEntity());
        assertEquals("123456", ciclistaSalvo.getPassaporteEntity().getNumeroPassaporte());
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
        cartao.setNumeroCartao("1234567890123456");
        cartao.setCvv("999");
        cartao.setValidadeCartao(String.valueOf(LocalDate.of(2028, 12, 1)));
        dto.setMeioDePagamento(cartao);

        when(ciclistaRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> ciclistaService.cadastrarCiclista(dto));
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
        cartao.setNumeroCartao("0000111122223333");
        cartao.setCvv("111");
        cartao.setValidadeCartao(String.valueOf(LocalDate.of(2027, 7, 1)));
        dto.setMeioDePagamento(cartao);
        dto.setPassaporte(passaporte);

        when(ciclistaRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> ciclistaService.cadastrarCiclista(dto));
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

        assertThrows(TrataUnprocessableEntityException.class, () -> ciclistaService.atualizarCiclista(id, dto));
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

        assertThrows(TrataUnprocessableEntityException.class, () -> ciclistaService.atualizarCiclista(id, dto));
    }

    @Test
    void deveAtivarCiclistaComSucesso() {
        Integer idCiclista = 1;
        CiclistaEntity ciclistaPendente = new CiclistaEntity();
        ciclistaPendente.setId(idCiclista);
        ciclistaPendente.setStatus(Status.AGUARDANDO_CONFIRMACAO);
        ciclistaPendente.setNome("Ciclista Teste");

        when(ciclistaRepository.findById(idCiclista)).thenReturn(Optional.of(ciclistaPendente));
        doReturn(true).when(ciclistaService).confirmaEmail();
        when(ciclistaRepository.save(any(CiclistaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CiclistaEntity ciclistaAtivado = ciclistaService.ativarCiclista(idCiclista);
        assertNotNull(ciclistaAtivado);
        assertEquals(Status.ATIVO, ciclistaAtivado.getStatus());
        assertNotNull(ciclistaAtivado.getHoraConfirmacaoEmail());
        assertEquals(idCiclista, ciclistaAtivado.getId());

        ArgumentCaptor<CiclistaEntity> ciclistaCaptor = ArgumentCaptor.forClass(CiclistaEntity.class);
        verify(ciclistaRepository).save(ciclistaCaptor.capture());

        CiclistaEntity savedCiclista = ciclistaCaptor.getValue();
        assertEquals(Status.ATIVO, savedCiclista.getStatus());
        assertTrue(savedCiclista.getHoraConfirmacaoEmail().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void deveLancarErroSeConfirmaEmailRetornarFalse() {
        Integer idCiclista = 3;
        CiclistaEntity ciclistaPendente = new CiclistaEntity();
        ciclistaPendente.setId(idCiclista);
        ciclistaPendente.setStatus(Status.AGUARDANDO_CONFIRMACAO);

        when(ciclistaRepository.findById(idCiclista)).thenReturn(Optional.of(ciclistaPendente));
        doReturn(false).when(ciclistaService).confirmaEmail();
        TrataUnprocessableEntityException exception = assertThrows(TrataUnprocessableEntityException.class, () -> ciclistaService.ativarCiclista(idCiclista));
        assertEquals("Email não foi confirmado", exception.getMessage());
        verify(ciclistaRepository, never()).save(any());
    }

    @Test
    void deveLancarErroAoAtivarCiclistaComIdInvalido() {
        assertThrows(TrataUnprocessableEntityException.class, () -> ciclistaService.ativarCiclista(-1));
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

        assertThrows(TrataUnprocessableEntityException.class, () -> ciclistaService.ativarCiclista(id));
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
        assertThrows(TrataUnprocessableEntityException.class, () -> ciclistaService.buscarCiclistaporId(0));
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
    void deveRetornarBicicletaQuandoCiclistaTemBicicletaAlugada() {
        Integer idCiclista = 10;

        when(ciclistaRepository.existsById(idCiclista)).thenReturn(true);
        doReturn(false).when(ciclistaService).permiteAluguel(idCiclista);

        BicicletaDTO bicicleta = new BicicletaDTO(1, "Caloi", "Elite", "2024", "123", "ALUGADA");
        doReturn(Optional.of(bicicleta)).when(ciclistaService).bicicletaAlugada(idCiclista);
        Optional<BicicletaDTO> resultado = ciclistaService.bicicletaAlugada(idCiclista);

        assertTrue(resultado.isPresent());
        assertEquals("Caloi", resultado.get().getMarca());
    }

    @Test
    void deveLancarExcecaoQuandoCiclistaNaoExiste() {
        Integer idInvalido = 999;
        when(ciclistaRepository.existsById(idInvalido)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> ciclistaService.bicicletaAlugada(idInvalido));
    }

}
