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

    @Mock CartaoService cartaoService;

    @InjectMocks
    @Spy
    private CiclistaService ciclistaService;
    private NovoCiclistaDTO novoCiclistaDTO;
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        novoCiclistaDTO = new NovoCiclistaDTO();
        novoCiclistaDTO.setNome("João");
        novoCiclistaDTO.setDataNascimento(LocalDate.of(2000, 1, 1));
        novoCiclistaDTO.setEmail("joao@email.com");
        novoCiclistaDTO.setNacionalidade(Nacionalidade.BRASILEIRO);
        novoCiclistaDTO.setCpf("12345678900");
        novoCiclistaDTO.setUrlFotoDocumento("url");
        novoCiclistaDTO.setSenha("senha123");
        novoCiclistaDTO.setConfirmaSenha("senha123");

        NovoCartaoDeCreditoDTO cartao = new NovoCartaoDeCreditoDTO();
        cartao.setNumeroCartao("1234123412341234");
        cartao.setCvv("123");
        cartao.setValidadeCartao("12/30");
        novoCiclistaDTO.setMeioDePagamento(cartao);
    }

    @Test
    void deveCadastrarCiclistaBrasileiroComSucesso() {
        novoCiclistaDTO = new NovoCiclistaDTO();
        novoCiclistaDTO.setNome("João");
        novoCiclistaDTO.setDataNascimento(LocalDate.of(2000, 1, 1));
        novoCiclistaDTO.setCpf("12345678900");
        novoCiclistaDTO.setEmail("joao@email.com");
        novoCiclistaDTO.setNacionalidade(Nacionalidade.BRASILEIRO);
        novoCiclistaDTO.setUrlFotoDocumento("url");
        novoCiclistaDTO.setSenha("123");
        novoCiclistaDTO.setConfirmaSenha("123");

        NovoCartaoDeCreditoDTO cartao = new NovoCartaoDeCreditoDTO();
        cartao.setNumeroCartao("1234123412341234");
        cartao.setCvv("123");
        cartao.setValidadeCartao(String.valueOf(LocalDate.of(2030, 1, 1)));
        novoCiclistaDTO.setMeioDePagamento(cartao);

        when(ciclistaRepository.existsByEmail(novoCiclistaDTO.getEmail())).thenReturn(false);
        when(ciclistaRepository.save(any(CiclistaEntity.class))).thenAnswer(invocation -> {
            CiclistaEntity entity = invocation.getArgument(0);
            entity.setId(1);
            return entity;
        });

        when(cartaoService.cartaoExiste(any())).thenReturn(false);

        ArgumentCaptor<CiclistaEntity> ciclistaCaptor = ArgumentCaptor.forClass(CiclistaEntity.class);
        CiclistaResponseDTO response = ciclistaService.cadastrarCiclista(novoCiclistaDTO);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals(novoCiclistaDTO.getNome(), response.getNome());

        verify(ciclistaRepository).save(ciclistaCaptor.capture());
        CiclistaEntity ciclistaSalvo = ciclistaCaptor.getValue();
        assertEquals(novoCiclistaDTO.getCpf(), ciclistaSalvo.getCpf());
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

    @Test
    void deveLancarErroAoCadastrarComEmailExistente() {
        when(ciclistaRepository.existsByEmail("joao@email.com")).thenReturn(true);

        var exception = assertThrows(TrataUnprocessableEntityException.class, () -> ciclistaService.cadastrarCiclista(novoCiclistaDTO));
        assertEquals("Email já existente", exception.getMessage());
        verify(ciclistaRepository, never()).save(any());
    }

    @Test
    void deveLancarErroAoCadastrarBrasileiroComCpfExistente() {
        when(ciclistaRepository.existsByEmail(anyString())).thenReturn(false);
        when(ciclistaRepository.findByCpf(novoCiclistaDTO.getCpf())).thenReturn(Optional.of(new CiclistaEntity()));

        var exception = assertThrows(IllegalArgumentException.class, () -> ciclistaService.cadastrarCiclista(novoCiclistaDTO));
        assertEquals("CPF já existente", exception.getMessage());
    }

    @Test
    void deveLancarErroAoCadastrarComSenhasDiferentes() {
        novoCiclistaDTO.setConfirmaSenha("senha_diferente");

        var exception = assertThrows(TrataUnprocessableEntityException.class, () -> ciclistaService.cadastrarCiclista(novoCiclistaDTO));
        assertEquals("Senhas diferentes", exception.getMessage());
    }

    @Test
    void deveLancarErroAoCadastrarComNacionalidadeNula() {
        novoCiclistaDTO.setNacionalidade(null);

        var exception = assertThrows(IllegalArgumentException.class, () -> ciclistaService.cadastrarCiclista(novoCiclistaDTO));
        assertEquals("Nacionalidade é obrigatória", exception.getMessage());
    }

    @Test
    void deveLancarErroAoCadastrarComCartaoJaExistente() {
        when(ciclistaRepository.existsByEmail(anyString())).thenReturn(false);
        when(ciclistaRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(cartaoService.cartaoExiste(anyString())).thenReturn(true);

        var exception = assertThrows(IllegalArgumentException.class, () -> ciclistaService.cadastrarCiclista(novoCiclistaDTO));
        assertEquals("Cartao já cadastrado em outro usuário", exception.getMessage());
    }

    @Test
    void deveAtualizarCiclistaComSucesso() {
        Integer id = 1;
        CiclistaEntity ciclistaExistente = new CiclistaEntity("Antigo", LocalDate.now(),
                "antigo@email.com", Nacionalidade.BRASILEIRO, "url", "senha", "senha");
        ciclistaExistente.setId(id);
        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        dto.setNome("Novo Nome");
        dto.setCpf("12345678910");
        dto.setSenha("senha");
        dto.setConfirmaSenha("senha");
        dto.setUrlFotoDocumento("sdflasdf");

        when(ciclistaRepository.findById(id)).thenReturn(Optional.of(ciclistaExistente));
        when(ciclistaRepository.existsByEmail("novo@email.com")).thenReturn(false);
        when(ciclistaRepository.save(any(CiclistaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CiclistaResponseDTO response = ciclistaService.atualizarCiclista(id, dto);

        assertNotNull(response);
        assertEquals("Novo Nome", response.getNome());
        assertEquals("12345678910", response.getCpf());
        verify(ciclistaRepository).save(any(CiclistaEntity.class));
    }

    @Test
    void deveLancarErroAoAtualizarCiclistaInexistente() {
        Integer id = 99;
        when(ciclistaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> ciclistaService.atualizarCiclista(id, new AtualizaCiclistaDTO()));
    }

    @Test
    void deveLancarErroAoAtualizarParaNacionalidadeNula() {
        Integer id = 1;
        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        dto.setNacionalidade(null);

        CiclistaEntity ciclistaExistente = new CiclistaEntity();
        ciclistaExistente.setNacionalidade(null);

        when(ciclistaRepository.findById(id)).thenReturn(Optional.of(ciclistaExistente));
        var exception = assertThrows(IllegalArgumentException.class, () -> ciclistaService.atualizarCiclista(id, dto));
        assertEquals("Nacionalidade é um campo obrigatório e não pode ser removida.", exception.getMessage());
    }

    @Test
    void devePermitirAluguelQuandoCiclistaAtivoComAluguelAtivo() {
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setStatus(Status.ATIVO);
        ciclista.setAluguelAtivo(true);
        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(ciclista));

        boolean podeAlugar = ciclistaService.permiteAluguel(1);

        assertFalse(podeAlugar);
    }

    @Test
    void naoDevePermitirAluguelQuandoCiclistaAtivoSemAluguelAtivo() {
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setStatus(Status.ATIVO);
        ciclista.setAluguelAtivo(false);
        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(ciclista));

        boolean naoPodeAlugar = ciclistaService.permiteAluguel(1);

        assertTrue(naoPodeAlugar);
    }

    @Test
    void naoDevePermitirAluguelQuandoCiclistaNaoEstaAtivo() {
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setStatus(Status.INATIVO);
        ciclista.setAluguelAtivo(false);
        when(ciclistaRepository.findById(1)).thenReturn(Optional.of(ciclista));

        boolean podeAlugar = ciclistaService.permiteAluguel(1);

        assertFalse(podeAlugar);
    }

    @Test
    void deveLancarExcecaoEmPermiteAluguelQuandoCiclistaNaoEncontrado() {
        when(ciclistaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> ciclistaService.permiteAluguel(99));
    }

    @Test
    void deveLancarErroAoCadastrarComCartaoRecusado() {
        when(ciclistaRepository.existsByEmail(anyString())).thenReturn(false);
        when(ciclistaRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(cartaoService.cartaoExiste(anyString())).thenReturn(false);
        doReturn(false).when(ciclistaService).validarCartao(any(NovoCartaoDeCreditoDTO.class));
        var exception = assertThrows(IllegalArgumentException.class, () -> ciclistaService.cadastrarCiclista(novoCiclistaDTO));

        assertEquals("Cartão recusado", exception.getMessage());
    }

    @Test
    void deveLancarErroAoAtualizarBrasileiroRemovendoCpf() {
        Integer id = 1;
        CiclistaEntity ciclistaExistente = new CiclistaEntity("Nome", LocalDate.now(), "email@email.com", Nacionalidade.BRASILEIRO, "url", "senha", "senha");
        ciclistaExistente.setCpf("134343535");
        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        dto.setCpf("");

        when(ciclistaRepository.findById(id)).thenReturn(Optional.of(ciclistaExistente));
        var exception = assertThrows(IllegalArgumentException.class, () -> ciclistaService.atualizarCiclista(id, dto));

        assertEquals("CPF é obrigatório para brasileiros", exception.getMessage());
    }

    @Test
    void deveLancarErroAoAtualizarEstrangeiroComPassaporteIncompleto() {
        Integer id = 1;
        CiclistaEntity ciclistaExistente = new CiclistaEntity("Nome", LocalDate.now(), "email@email.com", Nacionalidade.ESTRANGEIRO, "url", "senha", "senha");

        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        PassaporteDTO passaporteIncompleto = new PassaporteDTO();
        passaporteIncompleto.setNumeroPassaporte(null);
        passaporteIncompleto.setValidadePassaporte("12/33");
        passaporteIncompleto.setPais("BR");
        dto.setPassaporte(passaporteIncompleto);
        when(ciclistaRepository.findById(id)).thenReturn(Optional.of(ciclistaExistente));

        var exception = assertThrows(IllegalArgumentException.class, () -> ciclistaService.atualizarCiclista(id, dto));

        assertEquals("Ao atualizar, o passaporte completo é obrigatório para estrangeiros", exception.getMessage());
    }

    @Test
    void deveAtualizarCiclistaAdicionandoPassaporte() {
        Integer id = 1;
        CiclistaEntity ciclistaExistente = new CiclistaEntity("Nome Antigo", LocalDate.now(), "email@email.com", Nacionalidade.ESTRANGEIRO, "url antiga", "senha antiga", "senha antiga");
        ciclistaExistente.setId(id);

        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        PassaporteDTO novoPassaporte = new PassaporteDTO();
        novoPassaporte.setPais("US");
        novoPassaporte.setValidadePassaporte("12/33");
        novoPassaporte.setNumeroPassaporte("PASS123");
        dto.setPassaporte(novoPassaporte);
        dto.setNome("");
        dto.setCpf("");
        dto.setUrlFotoDocumento("");
        dto.setSenha("");
        dto.setConfirmaSenha("");

        when(ciclistaRepository.findById(id)).thenReturn(Optional.of(ciclistaExistente));
        when(ciclistaRepository.save(any(CiclistaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<CiclistaEntity> captor = ArgumentCaptor.forClass(CiclistaEntity.class);
        ciclistaService.atualizarCiclista(id, dto);
        verify(ciclistaRepository).save(captor.capture());

        CiclistaEntity ciclistaSalvo = captor.getValue();
        assertNotNull(ciclistaSalvo.getPassaporteEntity());
        assertEquals("PASS123", ciclistaSalvo.getPassaporteEntity().getNumeroPassaporte());
        assertEquals("Nome Antigo", ciclistaSalvo.getNome());
    }
    @Test
    void deveLancarExcecaoAoAtualizarEstrangeiroQueNaoTemPassaporte() {
        Integer id = 1;
        CiclistaEntity ciclistaExistente = new CiclistaEntity("Nome", LocalDate.now(), "email@email.com", Nacionalidade.ESTRANGEIRO, "url", "senha", "senha");

        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        dto.setNome("Novo Nome");

        when(ciclistaRepository.findById(id)).thenReturn(Optional.of(ciclistaExistente));

        var exception = assertThrows(IllegalArgumentException.class, () -> ciclistaService.atualizarCiclista(id, dto));

        assertEquals("Passaporte completo é obrigatório para estrangeiros", exception.getMessage());
    }

    @Test
    void deveManterPassaporteAntigoSeNenhumForEnviadoNaAtualizacao() {
        Integer id = 1;
        CiclistaEntity ciclistaExistente = new CiclistaEntity("Nome", LocalDate.now(), "email@email.com", Nacionalidade.ESTRANGEIRO, "url", "senha", "senha");
        ciclistaExistente.setId(id);
        PassaporteEntity passaporteAntigo = new PassaporteEntity("ANTIGO123", "01/01/2030", "PT");
        ciclistaExistente.setPassaporteEntity(passaporteAntigo);

        AtualizaCiclistaDTO dto = new AtualizaCiclistaDTO();
        dto.setNome("Novo Nome");
        dto.setCpf("");
        dto.setUrlFotoDocumento("");
        dto.setSenha("");
        dto.setConfirmaSenha("");

        when(ciclistaRepository.findById(id)).thenReturn(Optional.of(ciclistaExistente));
        when(ciclistaRepository.save(any(CiclistaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<CiclistaEntity> captor = ArgumentCaptor.forClass(CiclistaEntity.class);

        ciclistaService.atualizarCiclista(id, dto);

        verify(ciclistaRepository).save(captor.capture());
        CiclistaEntity ciclistaSalvo = captor.getValue();

        assertNotNull(ciclistaSalvo.getPassaporteEntity());
        assertEquals("ANTIGO123", ciclistaSalvo.getPassaporteEntity().getNumeroPassaporte());
        assertEquals("Novo Nome", ciclistaSalvo.getNome());
    }

    @Test
    void deveRetornarOptionalEmptyParaBicicletaAlugada() {
        Integer idCiclista = 10;
        CiclistaEntity ciclista = new CiclistaEntity();
        ciclista.setStatus(Status.ATIVO);
        ciclista.setAluguelAtivo(false);

        when(ciclistaRepository.existsById(idCiclista)).thenReturn(true);
        when(ciclistaRepository.findById(idCiclista)).thenReturn(Optional.of(ciclista));

        Optional<BicicletaDTO> resultado = ciclistaService.bicicletaAlugada(idCiclista);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveLancarExcecaoAoCadastrarComSenhaNula() {

        NovoCiclistaDTO dtoComSenhaNula = new NovoCiclistaDTO();
        dtoComSenhaNula.setSenha("senha123");
        dtoComSenhaNula.setConfirmaSenha(null);
        dtoComSenhaNula.setEmail("teste.null@email.com");
        dtoComSenhaNula.setNacionalidade(Nacionalidade.BRASILEIRO);
        dtoComSenhaNula.setCpf("09876543210");

        when(ciclistaService.existeEmail("teste.null@email.com")).thenReturn(false);
        var exception = assertThrows(TrataUnprocessableEntityException.class, () -> ciclistaService.cadastrarCiclista(dtoComSenhaNula));
        assertEquals("Senhas diferentes", exception.getMessage());
    }
}
