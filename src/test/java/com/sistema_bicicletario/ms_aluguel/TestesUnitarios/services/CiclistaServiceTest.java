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
import jakarta.persistence.EntityNotFoundException;
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

    // --- Testes de Cadastro ---

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
        when(ciclistaRepository.save(any(CiclistaEntity.class))).thenReturn(ciclistaSalvo);
        when(cartaoRepository.save(any(CartaoDeCreditoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));


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
        ciclistaSalvo.setPassaporteEntity(new PassaporteEntity()); // Mockando o passaporte salvo
        when(ciclistaRepository.save(any(CiclistaEntity.class))).thenReturn(ciclistaSalvo);
        when(cartaoRepository.save(any(CartaoDeCreditoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));


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
        verifyNoInteractions(cartaoService, cartaoRepository, eventPublisher, bCryptPasswordEncoder);
    }

    @Test
    void deveLancarErroAoCadastrarComSenhaInvalida() {
        NovoCiclistaDTO dto = ciclistaBrasileiroValido();
        dto.setConfirmaSenha("diferente"); // Senha inválida

        Exception ex = assertThrows(TrataUnprocessableEntityException.class, () -> ciclistaService.cadastrarCiclista(dto));
        assertTrue(ex.getMessage().contains("Senhas diferentes"));

        verify(ciclistaRepository, never()).save(any());
        verifyNoInteractions(cartaoService, cartaoRepository, eventPublisher, bCryptPasswordEncoder);
    }

    @Test
    void deveLancarErroAoCadastrarComNacionalidadeNula() {
        NovoCiclistaDTO dto = ciclistaBrasileiroValido();
        dto.setNacionalidade(null); // Nacionalidade nula

        Exception ex = assertThrows(IllegalArgumentException.class, () -> ciclistaService.cadastrarCiclista(dto));
        assertTrue(ex.getMessage().contains("Nacionalidade é obrigatória"));

        verify(ciclistaRepository, never()).save(any());
        verifyNoInteractions(cartaoService, cartaoRepository, eventPublisher, bCryptPasswordEncoder);
    }

    @Test
    void deveLancarErroAoCadastrarEstrangeiroSemPassaporte() {
        NovoCiclistaDTO dto = ciclistaEstrangeiroValido();
        dto.setPassaporte(null); // Passaporte nulo

        Exception ex = assertThrows(IllegalArgumentException.class, () -> ciclistaService.cadastrarCiclista(dto));
        assertTrue(ex.getMessage().contains("Passaporte é obrigatório para estrangeiros"));

        verify(ciclistaRepository, never()).save(any());
        verifyNoInteractions(cartaoService, cartaoRepository, eventPublisher, bCryptPasswordEncoder);
    }

    @Test
    void deveLancarErroAoCadastrarEstrangeiroComPassaporteNumeroNuloOuVazio() {
        NovoCiclistaDTO dto = ciclistaEstrangeiroValido();
        dto.getPassaporte().setNumeroPassaporte(null); // Número de passaporte nulo

        Exception ex = assertThrows(IllegalArgumentException.class, () -> ciclistaService.cadastrarCiclista(dto));
        assertTrue(ex.getMessage().contains("Número do passaporte é obrigatório para estrangeiros"));

        dto.getPassaporte().setNumeroPassaporte(""); // Número de passaporte vazio
        ex = assertThrows(IllegalArgumentException.class, () -> ciclistaService.cadastrarCiclista(dto));
        assertTrue(ex.getMessage().contains("Número do passaporte é obrigatório para estrangeiros"));

        verify(ciclistaRepository, never()).save(any());
        verifyNoInteractions(cartaoService, cartaoRepository, eventPublisher, bCryptPasswordEncoder);
    }

    @Test
    void deveLancarErroAoCadastrarEstrangeiroComPassaportePaisNuloOuVazio() {
        NovoCiclistaDTO dto = ciclistaEstrangeiroValido();
        dto.getPassaporte().setPais(null); // País nulo

        Exception ex = assertThrows(IllegalArgumentException.class, () -> ciclistaService.cadastrarCiclista(dto));
        assertTrue(ex.getMessage().contains("País do passaporte é obrigatório para estrangeiros"));

        dto.getPassaporte().setPais(""); // País vazio
        ex = assertThrows(IllegalArgumentException.class, () -> ciclistaService.cadastrarCiclista(dto));
        assertTrue(ex.getMessage().contains("País do passaporte é obrigatório para estrangeiros"));

        verify(ciclistaRepository, never()).save(any());
        verifyNoInteractions(cartaoService, cartaoRepository, eventPublisher, bCryptPasswordEncoder);
    }

    @Test
    void deveLancarErroAoCadastrarEstrangeiroComPassaporteValidadeNula() {
        NovoCiclistaDTO dto = ciclistaEstrangeiroValido();
        dto.getPassaporte().setValidadePassaporte(null); // Validade nula

        Exception ex = assertThrows(IllegalArgumentException.class, () -> ciclistaService.cadastrarCiclista(dto));
        assertTrue(ex.getMessage().contains("Validade do passaporte é obrigatório para estrangeiros"));

        verify(ciclistaRepository, never()).save(any());
        verifyNoInteractions(cartaoService, cartaoRepository, eventPublisher, bCryptPasswordEncoder);
    }

    @Test
    void deveLancarErroAoCadastrarComCartaoJaCadastrado() {
        NovoCiclistaDTO dto = ciclistaBrasileiroValido();

        when(ciclistaRepository.findByCpf(dto.getCpf())).thenReturn(Optional.empty());
        when(ciclistaRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(cartaoService.cartaoExiste(dto.getMeioDePagamento().getNumero())).thenReturn(true); // Cartão já existe

        Exception ex = assertThrows(IllegalArgumentException.class, () -> ciclistaService.cadastrarCiclista(dto));
        assertTrue(ex.getMessage().contains("Cartao já cadastrado em outro usuário"));

        verify(cartaoRepository, never()).save(any());
    }

    @Test
    void deveLancarErroAoCadastrarComCartaoRecusado() {
        NovoCiclistaDTO dto = ciclistaBrasileiroValido();

        when(ciclistaRepository.findByCpf(dto.getCpf())).thenReturn(Optional.empty());
        when(ciclistaRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(cartaoService.cartaoExiste(dto.getMeioDePagamento().getNumero())).thenReturn(false);
        when(cartaoService.validarCartao(dto.getMeioDePagamento())).thenReturn(false); // Cartão recusado

        Exception ex = assertThrows(IllegalArgumentException.class, () -> ciclistaService.cadastrarCiclista(dto));
        assertTrue(ex.getMessage().contains("Cartão recusado"));

        verify(cartaoRepository, never()).save(any());
    }

    // --- Testes de Atualização ---

    @Test
    void deveAtualizarCiclistaComSucesso() {
        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        dto.setNome("Novo Nome");
        dto.setCpf("12345678901");
        dto.setSenha("123456");
        dto.setConfirmaSenha("123456");
        dto.setNacionalidade(Nacionalidade.BRASILEIRO);
        dto.setUrlFotoDocumento("novaFoto.jpg");
        dto.setEmail("email@teste.com"); // Adicionado email para evitar erro de email já existente se o email for alterado

        CiclistaEntity existente = new CiclistaEntity();
        existente.setId(1);
        existente.setNome("Nome Antigo");
        existente.setCpf("12345678901"); // CPF igual para não disparar erro de CPF duplicado
        existente.setEmail("email@teste.com");
        existente.setNacionalidade(Nacionalidade.BRASILEIRO);
        existente.setUrlFotoDocumento("velhaFoto.jpg");
        existente.setSenha("senhaAntiga");
        existente.setConfirmaSenha("senhaAntiga");

        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(existente));
        when(ciclistaRepository.existsByEmail(dto.getEmail())).thenReturn(false); // Email não existe (ou é o mesmo)
        when(bCryptPasswordEncoder.encode(dto.getSenha())).thenReturn("senhaCodificada");
        when(ciclistaRepository.save(any(CiclistaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CiclistaResponseDTO resposta = ciclistaService.atualizarCiclista(1, dto);

        assertEquals("Novo Nome", resposta.getNome());
        assertEquals("12345678901", resposta.getCpf());
        assertEquals("novaFoto.jpg", resposta.getUrlFotoDocumento());
        assertEquals(Nacionalidade.BRASILEIRO, resposta.getNacionalidade());
        verify(eventPublisher, times(1)).publishEvent(any(EmailRealizadoEvent.class));
        verify(bCryptPasswordEncoder, times(1)).encode(dto.getSenha());
        verify(ciclistaRepository, times(1)).save(any(CiclistaEntity.class));
    }

    @Test
    void deveLancarErroAoAtualizarCiclistaNaoEncontrado() {
        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        when(ciclistaRepository.findById(anyInt())).thenReturn(Optional.empty());

        Exception ex = assertThrows(EntityNotFoundException.class, () -> ciclistaService.atualizarCiclista(99, dto));
        assertTrue(ex.getMessage().contains("Ciclista não encontrado com id: 99"));

        verify(ciclistaRepository, times(1)).findById(99);
        verify(ciclistaRepository, never()).save(any());
        verifyNoInteractions(eventPublisher, bCryptPasswordEncoder);
    }

    @Test
    void deveLancarErroAoAtualizarQuandoEmailExistente() {
        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        dto.setEmail("email@teste.com");

        CiclistaEntity existente = new CiclistaEntity();
        existente.setId(1);
        existente.setEmail("outro@email.com"); // Email diferente do DTO

        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(existente));
        when(ciclistaRepository.existsByEmail("email@teste.com")).thenReturn(true); // Novo email já existe

        Exception ex = assertThrows(TrataUnprocessableEntityException.class, () -> ciclistaService.atualizarCiclista(1, dto));
        assertTrue(ex.getMessage().contains("Email já existente"));

        verify(ciclistaRepository, times(1)).findById(1);
        verify(ciclistaRepository, times(1)).existsByEmail("email@teste.com");
        verify(ciclistaRepository, never()).save(any());
        verifyNoInteractions(eventPublisher, bCryptPasswordEncoder);
    }

    @Test
    void deveLancarErroAoAtualizarComSenhaInvalida() {
        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        dto.setSenha("novaSenha");
        dto.setConfirmaSenha("senhaDiferente"); // Senhas não batem

        CiclistaEntity existente = new CiclistaEntity();
        existente.setId(1);
        existente.setEmail("email@teste.com"); // Necessário para passar na validação de email
        existente.setUrlFotoDocumento("foto.jpg"); // Necessário para passar na validação de foto

        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(existente));
        when(ciclistaRepository.existsByEmail(anyString())).thenReturn(false); // Simula email não existente

        Exception ex = assertThrows(TrataUnprocessableEntityException.class, () -> ciclistaService.atualizarCiclista(1, dto));
        assertTrue(ex.getMessage().contains("Senhas diferentes"));

        verify(ciclistaRepository, times(1)).findById(1);
        verify(ciclistaRepository, never()).save(any());
        verifyNoInteractions(eventPublisher, bCryptPasswordEncoder);
    }

    @Test
    void deveLancarErroAoAtualizarComNacionalidadeNula() {
        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        dto.setNacionalidade(null); // Nacionalidade nula
        dto.setEmail("email@teste.com");
        dto.setSenha("123456");
        dto.setConfirmaSenha("123456");
        dto.setUrlFotoDocumento("foto.jpg");

        CiclistaEntity existente = new CiclistaEntity();
        existente.setId(1);
        existente.setEmail("email@teste.com");
        existente.setNacionalidade(null); // Nacionalidade existente também nula
        existente.setUrlFotoDocumento("foto.jpg");

        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(existente));
        when(ciclistaRepository.existsByEmail(anyString())).thenReturn(false);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> ciclistaService.atualizarCiclista(1, dto));
        assertTrue(ex.getMessage().contains("Nacionalidade é um campo obrigatório e não pode ser removida."));

        verify(ciclistaRepository, times(1)).findById(1);
        verify(ciclistaRepository, never()).save(any());
        verifyNoInteractions(eventPublisher, bCryptPasswordEncoder);
    }

    @Test
    void deveLancarErroAoAtualizarBrasileiroComCpfNuloOuVazio() {
        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        dto.setNacionalidade(Nacionalidade.BRASILEIRO);
        dto.setCpf(null); // CPF nulo
        dto.setEmail("email@teste.com");
        dto.setSenha("123456");
        dto.setConfirmaSenha("123456");
        dto.setUrlFotoDocumento("foto.jpg");


        CiclistaEntity existente = new CiclistaEntity();
        existente.setId(1);
        existente.setEmail("email@teste.com");
        existente.setNacionalidade(Nacionalidade.BRASILEIRO);
        existente.setCpf(null); // CPF existente também nulo
        existente.setUrlFotoDocumento("foto.jpg");

        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(existente));
        when(ciclistaRepository.existsByEmail(anyString())).thenReturn(false);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> ciclistaService.atualizarCiclista(1, dto));
        assertTrue(ex.getMessage().contains("CPF é obrigatório para brasileiros"));

        dto.setCpf(""); // CPF vazio
        ex = assertThrows(IllegalArgumentException.class, () -> ciclistaService.atualizarCiclista(1, dto));
        assertTrue(ex.getMessage().contains("CPF é obrigatório para brasileiros"));

        verify(ciclistaRepository, times(2)).findById(1); // Chamado duas vezes para os dois casos de teste
        verify(ciclistaRepository, never()).save(any());
        verifyNoInteractions(eventPublisher, bCryptPasswordEncoder);
    }

    @Test
    void deveLancarErroAoAtualizarEstrangeiroComPassaporteIncompleto() {
        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        dto.setNacionalidade(Nacionalidade.ESTRANGEIRO);
        dto.setEmail("email@teste.com");
        dto.setSenha("123456");
        dto.setConfirmaSenha("123456");
        dto.setUrlFotoDocumento("foto.jpg");

        PassaporteDTO passaporteIncompleto = new PassaporteDTO();
        passaporteIncompleto.setNumeroPassaporte("123"); // Faltando país e validade
        dto.setPassaporte(passaporteIncompleto);

        CiclistaEntity existente = new CiclistaEntity();
        existente.setId(1);
        existente.setEmail("email@teste.com");
        existente.setNacionalidade(Nacionalidade.ESTRANGEIRO);
        existente.setUrlFotoDocumento("foto.jpg");
        existente.setPassaporteEntity(null); // Não tem passaporte existente

        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(existente));
        when(ciclistaRepository.existsByEmail(anyString())).thenReturn(false);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> ciclistaService.atualizarCiclista(1, dto));
        assertTrue(ex.getMessage().contains("Ao atualizar, o passaporte completo é obrigatório para estrangeiros"));

        // Teste para passaporte nulo quando não existe passaporte existente
        AtualizaCiclistaDTO dtoSemPassaporte = new AtualizaCiclistaDTO();
        dtoSemPassaporte.setNacionalidade(Nacionalidade.ESTRANGEIRO);
        dtoSemPassaporte.setEmail("email@teste.com");
        dtoSemPassaporte.setSenha("123456");
        dtoSemPassaporte.setConfirmaSenha("123456");
        dtoSemPassaporte.setUrlFotoDocumento("foto.jpg");
        dtoSemPassaporte.setPassaporte(null); // Passaporte DTO nulo

        ex = assertThrows(IllegalArgumentException.class, () -> ciclistaService.atualizarCiclista(1, dtoSemPassaporte));
        assertTrue(ex.getMessage().contains("Passaporte completo é obrigatório para estrangeiros"));

        verify(ciclistaRepository, times(2)).findById(1); // 2 chamadas para findById
        verify(ciclistaRepository, never()).save(any());
        verifyNoInteractions(eventPublisher, bCryptPasswordEncoder);
    }

    @Test
    void deveManterPassaporteExistenteSeNaoInformadoNoUpdate() {
        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        dto.setNacionalidade(Nacionalidade.ESTRANGEIRO);
        dto.setNome("asfdasf");
        dto.setCpf("");
        dto.setEmail("email@teste.com");
        dto.setSenha("123456");
        dto.setConfirmaSenha("123456");
        dto.setUrlFotoDocumento("foto.jpg");
        dto.setPassaporte(null); // Não informa passaporte no DTO

        PassaporteEntity passaporteExistente = new PassaporteEntity("OLD123", "12/29", "BR");
        CiclistaEntity existente = new CiclistaEntity();
        existente.setNome("DRANKA");
        existente.setId(1);
        existente.setEmail("email@teste.com");
        existente.setNacionalidade(Nacionalidade.ESTRANGEIRO);
        existente.setUrlFotoDocumento("foto.jpg");
        existente.setPassaporteEntity(passaporteExistente); // Tem passaporte existente

        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(existente));
        when(ciclistaRepository.existsByEmail(anyString())).thenReturn(false);
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(ciclistaRepository.save(any(CiclistaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CiclistaResponseDTO resposta = ciclistaService.atualizarCiclista(1, dto);

        assertNotNull(resposta);
        assertEquals(passaporteExistente.getNumeroPassaporte(), resposta.getPassaporte().getNumeroPassaporte());
        assertEquals(passaporteExistente.getPais(), resposta.getPassaporte().getPais());
        assertEquals(passaporteExistente.getValidadePassaporte(), resposta.getPassaporte().getValidadePassaporte());

        verify(ciclistaRepository, times(1)).save(any(CiclistaEntity.class));
        verify(eventPublisher, times(1)).publishEvent(any(EmailRealizadoEvent.class));
    }

    @Test
    void deveLancarErroAoAtualizarComUrlFotoDocumentoNula() {
        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        dto.setUrlFotoDocumento(null); // URL da foto nula
        dto.setEmail("email@teste.com");
        dto.setSenha("123456");
        dto.setConfirmaSenha("123456");
        dto.setNacionalidade(Nacionalidade.BRASILEIRO);
        dto.setCpf("12345678901");


        CiclistaEntity existente = new CiclistaEntity();
        existente.setId(1);
        existente.setEmail("email@teste.com");
        existente.setUrlFotoDocumento(null); // URL existente também nula
        existente.setNacionalidade(Nacionalidade.BRASILEIRO);
        existente.setCpf("12345678901");


        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(existente));
        when(ciclistaRepository.existsByEmail(anyString())).thenReturn(false);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> ciclistaService.atualizarCiclista(1, dto));
        assertTrue(ex.getMessage().contains("Url não pode ser removida"));

        verify(ciclistaRepository, times(1)).findById(1);
        verify(ciclistaRepository, never()).save(any());
        verifyNoInteractions(eventPublisher, bCryptPasswordEncoder);
    }


    // --- Testes de Ativação ---

    @Test
    void deveAtivarCiclistaComSucesso() {
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setId(1);
        ciclista.setStatus(Status.AGUARDANDO_CONFIRMACAO);

        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(ciclista));
        when(ciclistaRepository.save(any(CiclistaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CiclistaEntity ativado = ciclistaService.ativarCiclista(1);

        assertEquals(Status.ATIVO, ativado.getStatus());
        assertNotNull(ativado.getHoraConfirmacaoEmail());
        verify(ciclistaRepository, times(1)).save(any(CiclistaEntity.class));
    }

    @Test
    void deveLancarErroAoAtivarCiclistaInvalido() {
        Exception ex = assertThrows(TrataUnprocessableEntityException.class, () -> ciclistaService.ativarCiclista(0));
        assertTrue(ex.getMessage().contains("id invalido"));

        verify(ciclistaRepository, never()).findById(any());
        verify(ciclistaRepository, never()).save(any());
    }

    @Test
    void deveLancarErroAoAtivarCiclistaNaoEncontrado() {
        when(ciclistaRepository.findById(anyInt())).thenReturn(Optional.empty());

        Exception ex = assertThrows(EntityNotFoundException.class, () -> ciclistaService.ativarCiclista(99));
        assertTrue(ex.getMessage().contains("Ciclista não encontrado com id: 99"));

        verify(ciclistaRepository, times(1)).findById(99);
        verify(ciclistaRepository, never()).save(any());
    }

    @Test
    void deveLancarErroAoAtivarCiclistaComStatusInvalido() {
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setId(1);
        ciclista.setStatus(Status.ATIVO); // Status já ATIVO, não AGUARDANDO_CONFIRMACAO

        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(ciclista));

        Exception ex = assertThrows(TrataUnprocessableEntityException.class, () -> ciclistaService.ativarCiclista(1));
        assertTrue(ex.getMessage().contains("Dados não correspondem a registro pendente"));

        verify(ciclistaRepository, times(1)).findById(1);
        verify(ciclistaRepository, never()).save(any());
    }


    // --- Testes de Consulta ---

    @Test
    void deveRetornarFalsoSeNaoPermitirAluguel() {
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setId(1);
        ciclista.setStatus(Status.AGUARDANDO_CONFIRMACAO); // Status não ATIVO
        ciclista.setAluguelAtivo(false);

        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(ciclista));

        boolean permite = ciclistaService.permiteAluguel(1);

        assertFalse(permite);
        verify(ciclistaRepository, times(1)).findById(1);
    }

    @Test
    void deveRetornarFalsoSeCiclistaJaPossuiAluguelAtivo() {
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setId(1);
        ciclista.setStatus(Status.ATIVO);
        ciclista.setAluguelAtivo(true); // Já possui aluguel ativo

        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(ciclista));

        boolean permite = ciclistaService.permiteAluguel(1);

        assertFalse(permite);
        verify(ciclistaRepository, times(1)).findById(1);
    }

    @Test
    void deveRetornarVerdadeiroSePermitirAluguel() {
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setId(1);
        ciclista.setStatus(Status.ATIVO);
        ciclista.setAluguelAtivo(false); // Ativo e sem aluguel

        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(ciclista));

        boolean permite = ciclistaService.permiteAluguel(1);

        assertTrue(permite);
        verify(ciclistaRepository, times(1)).findById(1);
    }

    @Test
    void deveLancarErroAoPermitirAluguelParaCiclistaNaoEncontrado() {
        when(ciclistaRepository.findById(anyInt())).thenReturn(Optional.empty());

        Exception ex = assertThrows(EntityNotFoundException.class, () -> ciclistaService.permiteAluguel(99));
        assertTrue(ex.getMessage().contains("Ciclista não encontrado"));

        verify(ciclistaRepository, times(1)).findById(99);
    }

    @Test
    void deveLancarErroSeBuscarCiclistaComIdInvalido() {
        Exception ex = assertThrows(TrataUnprocessableEntityException.class, () -> ciclistaService.buscarCiclistaporId(0));
        assertTrue(ex.getMessage().contains("id invalido"));

        verify(ciclistaRepository, never()).findById(any());
    }

    @Test
    void deveBuscarCiclistaPorIdComSucesso() {
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setId(1);

        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(ciclista));

        CiclistaEntity encontrado = ciclistaService.buscarCiclistaporId(1);

        assertNotNull(encontrado);
        assertEquals(1, encontrado.getId());
        verify(ciclistaRepository, times(1)).findById(1);
    }

    @Test
    void deveLancarErroAoBuscarCiclistaPorIdNaoEncontrado() {
        when(ciclistaRepository.findById(anyInt())).thenReturn(Optional.empty());

        Exception ex = assertThrows(EntityNotFoundException.class, () -> ciclistaService.buscarCiclistaporId(99));
        assertTrue(ex.getMessage().contains("Ciclista não encontrado com ID: 99"));

        verify(ciclistaRepository, times(1)).findById(99);
    }

    @Test
    void deveLancarErroAoExisteEmailComEmailInvalido() {
        String emailInvalido = "emailsemarroba.com";
        Exception ex = assertThrows(IllegalArgumentException.class, () -> ciclistaService.existeEmail(emailInvalido));
        assertTrue(ex.getMessage().contains("Email inválido"));

        verify(ciclistaRepository, never()).existsByEmail(anyString());
    }
}
