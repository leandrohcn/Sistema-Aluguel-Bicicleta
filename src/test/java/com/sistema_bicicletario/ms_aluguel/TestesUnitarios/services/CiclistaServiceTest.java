package com.sistema_bicicletario.ms_aluguel.TestesUnitarios.services;

import com.sistema_bicicletario.ms_aluguel.dtos.*;
import com.sistema_bicicletario.ms_aluguel.entities.cartao_de_credito.CartaoDeCreditoEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.*;
import com.sistema_bicicletario.ms_aluguel.exceptions.TrataUnprocessableEntityException;
import com.sistema_bicicletario.ms_aluguel.listeners.EmailRealizadoEvent;
import com.sistema_bicicletario.ms_aluguel.repositories.CartaoRepository;
import com.sistema_bicicletario.ms_aluguel.repositories.CiclistaRepository;
import com.sistema_bicicletario.ms_aluguel.services.CiclistaService;
import com.sistema_bicicletario.ms_aluguel.services.CartaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CiclistaServiceTest {

    @Mock private CiclistaRepository ciclistaRepository;
    @Mock private CartaoService cartaoService;
    @Mock private CartaoRepository cartaoRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks private CiclistaService ciclistaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- Helpers para criação de DTOs e entidades válidas

    private NovoCartaoDeCreditoDTO novoCartaoValido() {
        return new NovoCartaoDeCreditoDTO("Titular Teste", "124", LocalDate.of(2025,12,12), "1234567890123456");
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

    // --- Testes ---

    @Test
    void deveCadastrarCiclistaBrasileiroComSucesso() {
        NovoCiclistaDTO dto = ciclistaBrasileiroValido();

        when(ciclistaRepository.findByCpf(dto.getCpf())).thenReturn(Optional.empty());
        when(ciclistaRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(cartaoService.cartaoExiste(dto.getMeioDePagamento().getNumero())).thenReturn(false);
        when(cartaoService.validarCartao(dto.getMeioDePagamento())).thenReturn(true);
        when(bCryptPasswordEncoder.encode(dto.getSenha())).thenReturn("senhaCodificada");

        // Simula salvar ciclista e retornar a entidade com ID
        CiclistaEntity ciclistaSalvo = new CiclistaEntity();
        ciclistaSalvo.setId(1);
        ciclistaSalvo.setNome(dto.getNome());
        ciclistaSalvo.setNacionalidade(dto.getNacionalidade());
        ciclistaSalvo.setEmail(dto.getEmail());
        ciclistaSalvo.setStatus(Status.AGUARDANDO_CONFIRMACAO);
        when(ciclistaRepository.save(any())).thenReturn(ciclistaSalvo);

        CiclistaResponseDTO response = ciclistaService.cadastrarCiclista(dto);

        assertNotNull(response);
        assertEquals("João Silva", response.getNome());
        assertEquals(Nacionalidade.BRASILEIRO, response.getNacionalidade());
        assertEquals("AGUARDANDO_CONFIRMACAO", response.getStatus());

        verify(eventPublisher, times(1)).publishEvent(any(EmailRealizadoEvent.class));
        verify(cartaoRepository, times(1)).save(any(CartaoDeCreditoEntity.class));
    }

    @Test
    void deveCadastrarCiclistaEstrangeiroComSucesso() {
        NovoCiclistaDTO dto = ciclistaEstrangeiroValido();

        when(ciclistaRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(cartaoService.cartaoExiste(dto.getMeioDePagamento().getNumero())).thenReturn(false);
        when(cartaoService.validarCartao(dto.getMeioDePagamento())).thenReturn(true);
        when(bCryptPasswordEncoder.encode(dto.getSenha())).thenReturn("senhaCodificada");

        CiclistaEntity ciclistaSalvo = new CiclistaEntity();
        ciclistaSalvo.setId(2);
        ciclistaSalvo.setNome(dto.getNome());
        ciclistaSalvo.setNacionalidade(dto.getNacionalidade());
        ciclistaSalvo.setEmail(dto.getEmail());
        ciclistaSalvo.setStatus(Status.AGUARDANDO_CONFIRMACAO);
        when(ciclistaRepository.save(any())).thenReturn(ciclistaSalvo);

        CiclistaResponseDTO response = ciclistaService.cadastrarCiclista(dto);

        assertNotNull(response);
        assertEquals("Anna Müller", response.getNome());
        assertEquals(Nacionalidade.ESTRANGEIRO, response.getNacionalidade());

        verify(eventPublisher, times(1)).publishEvent(any(EmailRealizadoEvent.class));
        verify(cartaoRepository, times(1)).save(any(CartaoDeCreditoEntity.class));
    }

    @Test
    void deveLancarErroAoCadastrarBrasileiroComCpfDuplicado() {
        NovoCiclistaDTO dto = ciclistaBrasileiroValido();

        when(ciclistaRepository.findByCpf(dto.getCpf())).thenReturn(Optional.of(new CiclistaEntity()));

        Exception ex = assertThrows(IllegalArgumentException.class, () -> ciclistaService.cadastrarCiclista(dto));
        assertTrue(ex.getMessage().contains("CPF já existente"));

        verify(ciclistaRepository, never()).save(any());
    }

    @Test
    void deveLancarErroAoCadastrarComEmailDuplicado() {
        NovoCiclistaDTO dto = ciclistaBrasileiroValido();

        when(ciclistaRepository.findByCpf(dto.getCpf())).thenReturn(Optional.empty());
        when(ciclistaRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        Exception ex = assertThrows(TrataUnprocessableEntityException.class, () -> ciclistaService.cadastrarCiclista(dto));
        assertTrue(ex.getMessage().contains("Email já existente"));

        verify(ciclistaRepository, never()).save(any());
    }

    @Test
    void deveLancarErroAoCadastrarComSenhaInvalida() {
        NovoCiclistaDTO dto = ciclistaBrasileiroValido();
        dto.setConfirmaSenha("diferente");

        Exception ex = assertThrows(TrataUnprocessableEntityException.class, () -> ciclistaService.cadastrarCiclista(dto));
        assertTrue(ex.getMessage().contains("Senhas diferentes"));

        verify(ciclistaRepository, never()).save(any());
    }

    @Test
    void deveLancarErroAoCadastrarEstrangeiroSemPassaporte() {
        NovoCiclistaDTO dto = ciclistaEstrangeiroValido();
        dto.setPassaporte(null);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> ciclistaService.cadastrarCiclista(dto));
        assertTrue(ex.getMessage().contains("Passaporte é obrigatório"));
    }

    @Test
    void deveAtualizarCiclistaComSucesso() {
        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        dto.setNome("Novo Nome");
        dto.setCpf("12345678901");
        dto.setSenha("123456");
        dto.setConfirmaSenha("123456");
        dto.setNacionalidade(Nacionalidade.BRASILEIRO);
        dto.setUrlFotoDocumento("novaFoto.jpg");

        CiclistaEntity existente = new CiclistaEntity();
        existente.setId(1);
        existente.setNome("Nome Antigo");
        existente.setCpf("12345678901");
        existente.setEmail("email@teste.com");
        existente.setNacionalidade(Nacionalidade.BRASILEIRO);
        existente.setUrlFotoDocumento("velhaFoto.jpg");
        existente.setSenha("senhaAntiga");
        existente.setConfirmaSenha("senhaAntiga");

        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(existente));
        when(ciclistaRepository.existsByEmail("email@teste.com")).thenReturn(false);
        when(ciclistaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CiclistaResponseDTO resposta = ciclistaService.atualizarCiclista(1, dto);

        assertEquals("Novo Nome", resposta.getNome());
        verify(eventPublisher, times(1)).publishEvent(any(EmailRealizadoEvent.class));
    }

    @Test
    void deveLancarErroAoAtualizarQuandoEmailExistente() {
        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        dto.setEmail("email@teste.com");

        CiclistaEntity existente = new CiclistaEntity();
        existente.setId(1);
        existente.setEmail("outro@email.com");

        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(existente));
        when(ciclistaRepository.existsByEmail("email@teste.com")).thenReturn(true);

        Exception ex = assertThrows(TrataUnprocessableEntityException.class, () -> ciclistaService.atualizarCiclista(1, dto));
        assertTrue(ex.getMessage().contains("Email já existente"));
    }

    @Test
    void deveAtivarCiclistaComSucesso() {
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setId(1);
        ciclista.setStatus(Status.AGUARDANDO_CONFIRMACAO);

        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(ciclista));
        when(ciclistaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CiclistaEntity ativado = ciclistaService.ativarCiclista(1);

        assertEquals(Status.ATIVO, ativado.getStatus());
        assertNotNull(ativado.getHoraConfirmacaoEmail());
    }

    @Test
    void deveLancarErroAoAtivarCiclistaInvalido() {
        Exception ex = assertThrows(TrataUnprocessableEntityException.class, () -> ciclistaService.ativarCiclista(0));
        assertTrue(ex.getMessage().contains("id invalido"));
    }

    @Test
    void deveRetornarFalsoSeNaoPermitirAluguel() {
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setId(1);
        ciclista.setStatus(Status.AGUARDANDO_CONFIRMACAO);
        ciclista.setAluguelAtivo(false);

        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(ciclista));

        boolean permite = ciclistaService.permiteAluguel(1);

        assertFalse(permite);
    }

    @Test
    void deveRetornarVerdadeiroSePermitirAluguel() {
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setId(1);
        ciclista.setStatus(Status.ATIVO);
        ciclista.setAluguelAtivo(false);

        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(ciclista));

        boolean permite = ciclistaService.permiteAluguel(1);

        assertTrue(permite);
    }

    @Test
    void deveLancarErroSeBuscarCiclistaComIdInvalido() {
        Exception ex = assertThrows(TrataUnprocessableEntityException.class, () -> ciclistaService.buscarCiclistaporId(0));
        assertTrue(ex.getMessage().contains("id invalido"));
    }

    @Test
    void deveBuscarCiclistaPorIdComSucesso() {
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setId(1);

        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(ciclista));

        CiclistaEntity encontrado = ciclistaService.buscarCiclistaporId(1);

        assertNotNull(encontrado);
        assertEquals(1, encontrado.getId());
    }
}
