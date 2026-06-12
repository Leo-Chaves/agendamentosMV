package com.projeto.agendamentosMV.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.projeto.agendamentosMV.entity.Paciente;
import com.projeto.agendamentosMV.entity.Sexo;
import com.projeto.agendamentosMV.repository.PacienteRepository;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private PacienteService pacienteService;

    @Test
    void deveSalvarPaciente() {
        Paciente paciente = new Paciente(null, "Maria Silva", "12345678900", LocalDate.of(1996, 1, 1), Sexo.FEMININO, "Rua A", List.of());
        Paciente pacienteSalvo = new Paciente(1L, "Maria Silva", "12345678900", LocalDate.of(1996, 1, 1), Sexo.FEMININO, "Rua A", List.of());

        when(pacienteRepository.save(paciente)).thenReturn(pacienteSalvo);

        Paciente resultado = pacienteService.salvar(paciente);

        assertEquals(1L, resultado.getId());
        assertEquals("Maria Silva", resultado.getNome());
        assertEquals("12345678900", resultado.getCpf());
        verify(pacienteRepository).save(paciente);
    }

    @Test
    void deveAtualizarPacienteMantendoStatusAtual() {
        Paciente paciente = new Paciente(1L, "Maria Silva", "12345678900", LocalDate.of(1996, 1, 1), Sexo.FEMININO, "Rua A", List.of());
        paciente.setAtivo(false);
        Paciente dadosAtualizados = new Paciente(null, "Maria Oliveira", "11122233344", LocalDate.of(1995, 1, 1), Sexo.FEMININO, "Rua B",
                List.of());

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(pacienteRepository.save(paciente)).thenReturn(paciente);

        Paciente resultado = pacienteService.atualizar(1L, dadosAtualizados);

        assertEquals("Maria Oliveira", resultado.getNome());
        assertEquals("11122233344", resultado.getCpf());
        assertEquals(LocalDate.of(1995, 1, 1), resultado.getDataNascimento());
        assertEquals("Rua B", resultado.getEndereco());
        assertFalse(resultado.getAtivo());
        verify(pacienteRepository).save(paciente);
    }

    @Test
    void deveListarPacientes() {
        List<Paciente> pacientes = List.of(
                new Paciente(1L, "Maria Silva", "12345678900", LocalDate.of(1996, 1, 1), Sexo.FEMININO, "Rua A", List.of()),
                new Paciente(2L, "Joao Souza", "98765432100", LocalDate.of(1985, 1, 1), Sexo.MASCULINO, "Rua B", List.of()));

        when(pacienteRepository.findAll()).thenReturn(pacientes);

        List<Paciente> resultado = pacienteService.listar();

        assertEquals(2, resultado.size());
        assertSame(pacientes, resultado);
        verify(pacienteRepository).findAll();
    }

    @Test
    void deveBuscarPacientePorId() {
        Paciente paciente = new Paciente(1L, "Maria Silva", "12345678900", LocalDate.of(1996, 1, 1), Sexo.FEMININO, "Rua A", List.of());

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        Paciente resultado = pacienteService.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("Maria Silva", resultado.getNome());
        verify(pacienteRepository).findById(1L);
    }

    @Test
    void deveLancarErroQuandoPacienteNaoForEncontrado() {
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pacienteService.buscarPorId(99L));

        assertEquals("Paciente não encontrado.", exception.getMessage());
        verify(pacienteRepository).findById(99L);
    }

    @Test
    void deveInativarPacienteSemRemoverRegistro() {
        Paciente paciente = new Paciente(1L, "Maria Silva", "12345678900", LocalDate.of(1996, 1, 1), Sexo.FEMININO, "Rua A", List.of());

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(pacienteRepository.save(paciente)).thenReturn(paciente);

        Paciente resultado = pacienteService.inativar(1L);

        assertFalse(resultado.getAtivo());
        verify(pacienteRepository).save(paciente);
    }

    @Test
    void deveAtivarPacienteInativo() {
        Paciente paciente = new Paciente(1L, "Maria Silva", "12345678900", LocalDate.of(1996, 1, 1), Sexo.FEMININO, "Rua A", List.of());
        paciente.setAtivo(false);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(pacienteRepository.save(paciente)).thenReturn(paciente);

        Paciente resultado = pacienteService.ativar(1L);

        assertTrue(resultado.getAtivo());
        verify(pacienteRepository).save(paciente);
    }
}


