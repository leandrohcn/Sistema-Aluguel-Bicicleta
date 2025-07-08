package com.sistema_bicicletario.ms_aluguel.controllers;

import com.sistema_bicicletario.ms_aluguel.dtos.*;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.CiclistaEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Nacionalidade;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.PassaporteEntity;
import com.sistema_bicicletario.ms_aluguel.entities.ciclista.Status;
import com.sistema_bicicletario.ms_aluguel.services.AluguelService;
import com.sistema_bicicletario.ms_aluguel.services.CiclistaService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CiclistaControllerTest {

    @Mock
    private AluguelService aluguelService;
    @Mock
    private CiclistaService ciclistaService;
    @InjectMocks
    private CiclistaController ciclistaController;

    private NovoCiclistaDTO novoCiclistaDTO;
    private AtualizaCiclistaDTO atualizaCiclistaDTO;
    private CiclistaEntity ciclistaEntity;
    private CiclistaResponseDTO ciclistaResponseDTO;



    @BeforeEach
    void setUp() {

        novoCiclistaDTO = new NovoCiclistaDTO();
        novoCiclistaDTO.setNome("João Silva");
        novoCiclistaDTO.setEmail("joao.silva@example.com");
        novoCiclistaDTO.setCpf("123.456.789-00");
        novoCiclistaDTO.setUrlFotoDocumento("example.com/foto_doc.jpg");
        novoCiclistaDTO.setNacionalidade(Nacionalidade.BRASILEIRO);
        novoCiclistaDTO.setSenha("123456");
        novoCiclistaDTO.setConfirmaSenha("123456");
        novoCiclistaDTO.setDataNascimento(LocalDate.of(2003, 11, 11));


        atualizaCiclistaDTO = new AtualizaCiclistaDTO();
        atualizaCiclistaDTO.setNome("Leandro Atualizado");
        atualizaCiclistaDTO.setUrlFotoDocumento("example.com/foto_doc_atualizado.jpg");
        atualizaCiclistaDTO.setSenha("1235678");
        atualizaCiclistaDTO.setConfirmaSenha("12345678");

        ciclistaEntity = new CiclistaEntity();
        ciclistaEntity.setId(1);
        ciclistaEntity.setStatus(Status.AGUARDANDO_CONFIRMACAO);
        PassaporteEntity passaporte2 = new PassaporteEntity();
        passaporte2.setNumeroPassaporte("");
        ciclistaEntity.setPassaporteEntity(passaporte2);

        ciclistaResponseDTO = new CiclistaResponseDTO(ciclistaEntity);

    }

    @Test
    void deveCadastrarCiclistaComSucesso() {
        when(ciclistaService.cadastrarCiclista(novoCiclistaDTO)).thenReturn(ciclistaResponseDTO);

        ResponseEntity<CiclistaResponseDTO> resposta = ciclistaController.cadastrarCiclista(novoCiclistaDTO);
        assertNotNull(resposta.getBody());
        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertEquals(ciclistaResponseDTO.getId(), resposta.getBody().getId());
        assertEquals(ciclistaResponseDTO.getNome(), resposta.getBody().getNome());
        assertEquals(ciclistaResponseDTO.getEmail(), resposta.getBody().getEmail());

        verify(ciclistaService, times(1)).cadastrarCiclista(novoCiclistaDTO);
    }

    @Test
    void deveBuscarCiclistaPorIdComSucesso() {
        when(ciclistaService.buscarCiclistaporId(1)).thenReturn(ciclistaEntity);

        ResponseEntity<CiclistaResponseDTO> resposta = ciclistaController.buscarCiclista(1);
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(ciclistaResponseDTO.getId(), resposta.getBody().getId());
        assertEquals(ciclistaResponseDTO.getNome(), resposta.getBody().getNome());

        verify(ciclistaService, times(1)).buscarCiclistaporId(1);
    }

    @Test
    void deveRetornarNotFoundAoBuscarCiclistaInexistente() {
        when(ciclistaService.buscarCiclistaporId(999)).thenThrow(new EntityNotFoundException("Ciclista não encontrado."));
        assertThrows(EntityNotFoundException.class, () -> ciclistaController.buscarCiclista(999));
        verify(ciclistaService, times(1)).buscarCiclistaporId(999);
    }

    @Test
    void deveAtualizarCiclistaComSucesso() {
        CiclistaResponseDTO respostaEsperada = new CiclistaResponseDTO(ciclistaEntity);
        ciclistaEntity.setId(3);
        ciclistaEntity.setNome("Leandro");
        ciclistaEntity.setUrlFotoDocumento("example.com/foto_doc_atualizado.jpg");

        when(ciclistaService.atualizarCiclista(3, atualizaCiclistaDTO)).thenReturn(respostaEsperada);
        ResponseEntity<CiclistaResponseDTO> response = ciclistaController.atualizarCiclista(3, atualizaCiclistaDTO);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(respostaEsperada.getId(), response.getBody().getId());
        assertEquals(respostaEsperada.getNome(), response.getBody().getNome());
        assertEquals(respostaEsperada.getUrlFotoDocumento(), response.getBody().getUrlFotoDocumento());

        verify(ciclistaService, times(1)).atualizarCiclista(3, atualizaCiclistaDTO);
    }

    @Test
    void deveRetornarNotFoundAoAtualizarCiclistaInexistente() {
        when(ciclistaService.atualizarCiclista(999, atualizaCiclistaDTO)).thenThrow(new EntityNotFoundException("Ciclista não encontrado para atualização."));
        assertThrows(EntityNotFoundException.class, () -> ciclistaController.atualizarCiclista(999, atualizaCiclistaDTO));
        verify(ciclistaService, times(1)).atualizarCiclista(999, atualizaCiclistaDTO);
    }

    @Test
    void deveAtivarCiclistaComSucesso() {
        CiclistaEntity ciclistaNaoAtivo = ciclistaEntity;
        ciclistaNaoAtivo.setStatus(Status.ATIVO);
        when(ciclistaService.ativarCiclista(1)).thenReturn(ciclistaNaoAtivo);

        ResponseEntity<CiclistaResponseDTO> response = ciclistaController.ativarCiclista(1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ATIVO", response.getBody().getStatus());

        verify(ciclistaService, times(1)).ativarCiclista(1);
    }

    @Test
    void deveRetornarNotFoundAoAtivarCiclistaInexistente() {
        when(ciclistaService.ativarCiclista(999)).thenThrow(new EntityNotFoundException("Ciclista não encontrado para ativar."));
        assertThrows(EntityNotFoundException.class, () -> ciclistaController.ativarCiclista(999));
        verify(ciclistaService, times(1)).ativarCiclista(999);
    }

    @Test
    void deveRetornarTrueSeEmailExiste() {
        when(ciclistaService.existeEmail("joao.silva@example.com")).thenReturn(true);

        ResponseEntity<Boolean> response = ciclistaController.existeEmail("joao.silva@example.com");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody());
        verify(ciclistaService, times(1)).existeEmail("joao.silva@example.com");
    }

    @Test
    void deveRetornarNotFoundSeEmailNaoExiste() {
        doThrow(new EntityNotFoundException("Email não encontrado.")).when(ciclistaService).existeEmail("nao.existe@example.com");
        assertThrows(EntityNotFoundException.class, () -> ciclistaController.existeEmail("nao.existe@example.com"));
        verify(ciclistaService, times(1)).existeEmail("nao.existe@example.com");
    }

    @Test
    void deveRetornarTrueSePermiteAluguel() {
        when(ciclistaService.permiteAluguel(1)).thenReturn(true);

        ResponseEntity<Boolean> response = ciclistaController.permiteAluguel(1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody());

        verify(ciclistaService, times(1)).permiteAluguel(1);
    }

    @Test
    void deveRetornarFalseSeNaoPermiteAluguel() {
        when(ciclistaService.permiteAluguel(1)).thenReturn(false);

        ResponseEntity<Boolean> response = ciclistaController.permiteAluguel(1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotEquals(Boolean.TRUE, response.getBody());

        verify(ciclistaService, times(1)).permiteAluguel(1);
    }

    @Test
    void deveRetornarBicicletaAlugadaComSucesso() {
        int ciclistaId = 1;
        int bicicletaId = 101;

        BicicletaDTO bicicletaEsperada = new BicicletaDTO();
        bicicletaEsperada.setIdBicicleta(bicicletaId);
        bicicletaEsperada.setStatus("EM_USO");
        bicicletaEsperada.setMarca("Caloi");
        bicicletaEsperada.setModelo("Urbana");


        when(aluguelService.buscarBicicletaDoAluguelAtivo(ciclistaId))
                .thenReturn(Optional.of(bicicletaEsperada));

        ResponseEntity<Optional<BicicletaDTO>> resposta = ciclistaController.bicicletaAlugada(ciclistaId);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody(), "O corpo da resposta não deveria ser nulo.");

        Optional<BicicletaDTO> body = resposta.getBody();
        assertTrue(body.isPresent(), "O corpo da resposta deveria conter uma bicicleta.");

        BicicletaDTO bicicletaRetornada = body.get();
        assertEquals(bicicletaId, bicicletaRetornada.getIdBicicleta());
        assertEquals("EM_USO", bicicletaRetornada.getStatus());
        assertEquals("Caloi", bicicletaRetornada.getMarca());


        verify(aluguelService, times(1)).buscarBicicletaDoAluguelAtivo(ciclistaId);

        verifyNoMoreInteractions(aluguelService);
    }

    @Test
    void deveRetornarVazioQuandoNaoHouverAluguelAtivo() { // Nome do teste mais genérico e correto
        int ciclistaId = 3;


        when(aluguelService.buscarBicicletaDoAluguelAtivo(ciclistaId)).thenReturn(Optional.empty());

        ResponseEntity<Optional<BicicletaDTO>> resposta = ciclistaController.bicicletaAlugada(ciclistaId);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertTrue(resposta.getBody().isEmpty());

        verify(aluguelService, times(1)).buscarBicicletaDoAluguelAtivo(ciclistaId);
    }

    @Test
    void deveRetornarVazioSeAluguelNaoEncontrado() {
        int ciclistaId = 3;


        when(aluguelService.buscarBicicletaDoAluguelAtivo(ciclistaId)).thenReturn(Optional.empty());

        ResponseEntity<Optional<BicicletaDTO>> resposta = ciclistaController.bicicletaAlugada(ciclistaId);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody(), "O corpo da resposta não deveria ser nulo.");
        assertTrue(resposta.getBody().isEmpty(), "O corpo da resposta deveria ser um Optional vazio.");

        verify(aluguelService, times(1)).buscarBicicletaDoAluguelAtivo(ciclistaId);
    }
}