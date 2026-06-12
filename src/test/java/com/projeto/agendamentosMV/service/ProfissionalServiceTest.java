package com.projeto.agendamentosMV.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.projeto.agendamentosMV.entity.Profissional;
import com.projeto.agendamentosMV.repository.ProfissionalRepository;

@ExtendWith(MockitoExtension.class)
class ProfissionalServiceTest {

    @Mock
    private ProfissionalRepository profissionalRepository;

    @InjectMocks
    private ProfissionalService profissionalService;

    @Test
    void deveSalvarProfissional() {
        Profissional profissional = new Profissional(null, "Ana Costa", "CRM12345", "Cardiologia", List.of());
        Profissional profissionalSalvo = new Profissional(1L, "Ana Costa", "CRM12345", "Cardiologia", List.of());

        when(profissionalRepository.save(profissional)).thenReturn(profissionalSalvo);

        Profissional resultado = profissionalService.salvar(profissional);

        assertEquals(1L, resultado.getId());
        assertEquals("Ana Costa", resultado.getNome());
        assertEquals("CRM12345", resultado.getCrm());
        assertEquals("Cardiologia", resultado.getArea());
        verify(profissionalRepository).save(profissional);
    }

    @Test
    void deveAtualizarProfissionalMantendoStatusAtual() {
        Profissional profissional = new Profissional(1L, "Ana Costa", "CRM12345", "Cardiologia", List.of());
        profissional.setAtivo(false);
        Profissional dadosAtualizados = new Profissional(null, "Ana Lima", "CRM54321", "Pediatria", List.of());

        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(profissionalRepository.save(profissional)).thenReturn(profissional);

        Profissional resultado = profissionalService.atualizar(1L, dadosAtualizados);

        assertEquals("Ana Lima", resultado.getNome());
        assertEquals("CRM54321", resultado.getCrm());
        assertEquals("Pediatria", resultado.getArea());
        assertFalse(resultado.getAtivo());
        verify(profissionalRepository).save(profissional);
    }

    @Test
    void deveListarProfissionais() {
        List<Profissional> profissionais = List.of(
                new Profissional(1L, "Ana Costa", "CRM12345", "Cardiologia", List.of()),
                new Profissional(2L, "Bruno Lima", "CRM67890", "Ortopedia", List.of()));

        when(profissionalRepository.findAll()).thenReturn(profissionais);

        List<Profissional> resultado = profissionalService.listar();

        assertEquals(2, resultado.size());
        assertSame(profissionais, resultado);
        verify(profissionalRepository).findAll();
    }

    @Test
    void deveBuscarProfissionalPorId() {
        Profissional profissional = new Profissional(1L, "Ana Costa", "CRM12345", "Cardiologia", List.of());

        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));

        Profissional resultado = profissionalService.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("Ana Costa", resultado.getNome());
        verify(profissionalRepository).findById(1L);
    }

    @Test
    void deveLancarErroQuandoProfissionalNaoForEncontrado() {
        when(profissionalRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> profissionalService.buscarPorId(99L));

        assertEquals("Profissional não encontrado.", exception.getMessage());
        verify(profissionalRepository).findById(99L);
    }

    @Test
    void deveInativarProfissionalSemRemoverRegistro() {
        Profissional profissional = new Profissional(1L, "Ana Costa", "CRM12345", "Cardiologia", List.of());

        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(profissionalRepository.save(profissional)).thenReturn(profissional);

        Profissional resultado = profissionalService.inativar(1L);

        assertFalse(resultado.getAtivo());
        verify(profissionalRepository).save(profissional);
    }

    @Test
    void deveAtivarProfissionalInativo() {
        Profissional profissional = new Profissional(1L, "Ana Costa", "CRM12345", "Cardiologia", List.of());
        profissional.setAtivo(false);

        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(profissionalRepository.save(profissional)).thenReturn(profissional);

        Profissional resultado = profissionalService.ativar(1L);

        assertTrue(resultado.getAtivo());
        verify(profissionalRepository).save(profissional);
    }
}
