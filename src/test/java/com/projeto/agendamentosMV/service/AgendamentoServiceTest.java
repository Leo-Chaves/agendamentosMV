package com.projeto.agendamentosMV.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.jpa.domain.Specification;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.projeto.agendamentosMV.entity.Agendamento;
import com.projeto.agendamentosMV.entity.AreaProfissional;
import com.projeto.agendamentosMV.entity.Paciente;
import com.projeto.agendamentosMV.entity.Profissional;
import com.projeto.agendamentosMV.entity.Sexo;
import com.projeto.agendamentosMV.entity.StatusAgendamento;
import com.projeto.agendamentosMV.repository.AgendamentoRepository;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private PacienteService pacienteService;

    @Mock
    private ProfissionalService profissionalService;

    @InjectMocks
    private AgendamentoService agendamentoService;

    @Test
    void deveCriarAgendamentoQuandoHorarioEstiverDisponivel() {
        LocalDateTime dataHora = LocalDateTime.of(2026, 7, 10, 14, 0);
        Paciente paciente = new Paciente(1L, "Maria Silva", "12345678900", LocalDate.of(1996, 1, 1), Sexo.FEMININO, "Rua A", List.of());
        Profissional profissional = new Profissional(1L, "Ana Costa", "CRM12345", AreaProfissional.CARDIOLOGIA, List.of());

        when(pacienteService.buscarPorId(1L)).thenReturn(paciente);
        when(profissionalService.buscarPorId(1L)).thenReturn(profissional);
        when(agendamentoRepository.existsByPacienteIdAndStatusAndDataHoraAfterAndDataHoraBefore(
                1L,
                StatusAgendamento.AGENDADO,
                dataHora.minusMinutes(30),
                dataHora.plusMinutes(30)))
                .thenReturn(false);
        when(agendamentoRepository.existsByProfissionalIdAndStatusAndDataHoraAfterAndDataHoraBefore(
                1L,
                StatusAgendamento.AGENDADO,
                dataHora.minusMinutes(30),
                dataHora.plusMinutes(30))).thenReturn(false);
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocation -> {
            Agendamento agendamento = invocation.getArgument(0);
            agendamento.setId(1L);
            return agendamento;
        });

        Agendamento resultado = agendamentoService.agendar(1L, 1L, dataHora);

        assertEquals(1L, resultado.getId());
        assertEquals(dataHora, resultado.getDataHora());
        assertSame(paciente, resultado.getPaciente());
        assertSame(profissional, resultado.getProfissional());
        assertEquals(StatusAgendamento.AGENDADO, resultado.getStatus());
        verify(agendamentoRepository).save(any(Agendamento.class));
    }

    @Test
    void deveLancarErroQuandoPacienteJaTiverAgendamentoNoMesmoHorario() {
        LocalDateTime dataHora = LocalDateTime.of(2026, 7, 10, 14, 0);
        Paciente paciente = new Paciente(1L, "Maria Silva", "12345678900", LocalDate.of(1996, 1, 1), Sexo.FEMININO, "Rua A", List.of());
        Profissional profissional = new Profissional(1L, "Ana Costa", "CRM12345", AreaProfissional.CARDIOLOGIA, List.of());

        when(pacienteService.buscarPorId(1L)).thenReturn(paciente);
        when(profissionalService.buscarPorId(1L)).thenReturn(profissional);
        when(agendamentoRepository.existsByPacienteIdAndStatusAndDataHoraAfterAndDataHoraBefore(
                1L,
                StatusAgendamento.AGENDADO,
                dataHora.minusMinutes(30),
                dataHora.plusMinutes(30)))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agendamentoService.agendar(1L, 1L, dataHora));

        assertEquals("Paciente já possui agendamento neste horário.", exception.getMessage());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    void deveLancarErroQuandoPacienteTiverAgendamentoSobreposto() {
        LocalDateTime dataHora = LocalDateTime.of(2026, 7, 10, 10, 15);
        Paciente paciente = new Paciente(1L, "Maria Silva", "12345678900", LocalDate.of(1996, 1, 1), Sexo.FEMININO, "Rua A", List.of());
        Profissional profissional = new Profissional(2L, "Ana Costa", "CRM12345", AreaProfissional.CARDIOLOGIA, List.of());

        when(pacienteService.buscarPorId(1L)).thenReturn(paciente);
        when(profissionalService.buscarPorId(2L)).thenReturn(profissional);
        when(agendamentoRepository.existsByPacienteIdAndStatusAndDataHoraAfterAndDataHoraBefore(
                1L,
                StatusAgendamento.AGENDADO,
                dataHora.minusMinutes(30),
                dataHora.plusMinutes(30)))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agendamentoService.agendar(1L, 2L, dataHora));

        assertEquals("Paciente já possui agendamento neste horário.", exception.getMessage());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    void deveLancarErroQuandoDataHoraForPassada() {
        LocalDateTime dataHora = LocalDateTime.now().minusDays(1);
        Paciente paciente = new Paciente(1L, "Maria Silva", "12345678900", LocalDate.of(1996, 1, 1), Sexo.FEMININO, "Rua A", List.of());
        Profissional profissional = new Profissional(1L, "Ana Costa", "CRM12345", AreaProfissional.CARDIOLOGIA, List.of());

        when(pacienteService.buscarPorId(1L)).thenReturn(paciente);
        when(profissionalService.buscarPorId(1L)).thenReturn(profissional);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agendamentoService.agendar(1L, 1L, dataHora));

        assertEquals("Não é possível agendar para uma data passada.", exception.getMessage());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    void deveLancarErroQuandoProfissionalJaTiverAgendamentoNoMesmoHorario() {
        LocalDateTime dataHora = LocalDateTime.of(2026, 7, 10, 14, 0);
        Paciente paciente = new Paciente(1L, "Maria Silva", "12345678900", LocalDate.of(1996, 1, 1), Sexo.FEMININO, "Rua A", List.of());
        Profissional profissional = new Profissional(1L, "Ana Costa", "CRM12345", AreaProfissional.CARDIOLOGIA, List.of());

        when(pacienteService.buscarPorId(1L)).thenReturn(paciente);
        when(profissionalService.buscarPorId(1L)).thenReturn(profissional);
        when(agendamentoRepository.existsByPacienteIdAndStatusAndDataHoraAfterAndDataHoraBefore(
                1L,
                StatusAgendamento.AGENDADO,
                dataHora.minusMinutes(30),
                dataHora.plusMinutes(30)))
                .thenReturn(false);
        when(agendamentoRepository.existsByProfissionalIdAndStatusAndDataHoraAfterAndDataHoraBefore(
                1L,
                StatusAgendamento.AGENDADO,
                dataHora.minusMinutes(30),
                dataHora.plusMinutes(30))).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agendamentoService.agendar(1L, 1L, dataHora));

        assertEquals("Profissional já possui agendamento neste horário.", exception.getMessage());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    void deveLancarErroQuandoProfissionalTiverAgendamentoSobreposto() {
        LocalDateTime dataHora = LocalDateTime.of(2026, 7, 10, 10, 0);
        Paciente paciente = new Paciente(1L, "Maria Silva", "12345678900", LocalDate.of(1996, 1, 1), Sexo.FEMININO, "Rua A", List.of());
        Profissional profissional = new Profissional(1L, "Ana Costa", "CRM12345", AreaProfissional.CARDIOLOGIA, List.of());

        when(pacienteService.buscarPorId(1L)).thenReturn(paciente);
        when(profissionalService.buscarPorId(1L)).thenReturn(profissional);
        when(agendamentoRepository.existsByPacienteIdAndStatusAndDataHoraAfterAndDataHoraBefore(
                1L,
                StatusAgendamento.AGENDADO,
                dataHora.minusMinutes(30),
                dataHora.plusMinutes(30)))
                .thenReturn(false);
        when(agendamentoRepository.existsByProfissionalIdAndStatusAndDataHoraAfterAndDataHoraBefore(
                1L,
                StatusAgendamento.AGENDADO,
                dataHora.minusMinutes(30),
                dataHora.plusMinutes(30))).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agendamentoService.agendar(1L, 1L, dataHora));

        assertEquals("Profissional já possui agendamento neste horário.", exception.getMessage());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    void deveLancarErroQuandoPacienteEstiverInativo() {
        LocalDateTime dataHora = LocalDateTime.of(2026, 7, 10, 14, 0);
        Paciente paciente = new Paciente(1L, "Maria Silva", "12345678900", LocalDate.of(1996, 1, 1), Sexo.FEMININO, "Rua A", List.of());
        Profissional profissional = new Profissional(1L, "Ana Costa", "CRM12345", AreaProfissional.CARDIOLOGIA, List.of());
        paciente.setAtivo(false);

        when(pacienteService.buscarPorId(1L)).thenReturn(paciente);
        when(profissionalService.buscarPorId(1L)).thenReturn(profissional);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agendamentoService.agendar(1L, 1L, dataHora));

        assertEquals("Paciente inativo não pode receber agendamento.", exception.getMessage());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    void deveLancarErroQuandoProfissionalEstiverInativo() {
        LocalDateTime dataHora = LocalDateTime.of(2026, 7, 10, 14, 0);
        Paciente paciente = new Paciente(1L, "Maria Silva", "12345678900", LocalDate.of(1996, 1, 1), Sexo.FEMININO, "Rua A", List.of());
        Profissional profissional = new Profissional(1L, "Ana Costa", "CRM12345", AreaProfissional.CARDIOLOGIA, List.of());
        profissional.setAtivo(false);

        when(pacienteService.buscarPorId(1L)).thenReturn(paciente);
        when(profissionalService.buscarPorId(1L)).thenReturn(profissional);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agendamentoService.agendar(1L, 1L, dataHora));

        assertEquals("Profissional inativo não pode receber agendamento.", exception.getMessage());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    void deveListarAgendamentos() {
        List<Agendamento> agendamentos = List.of(new Agendamento());

        when(agendamentoRepository.findAll()).thenReturn(agendamentos);

        List<Agendamento> resultado = agendamentoService.listar();

        assertEquals(1, resultado.size());
        assertSame(agendamentos, resultado);
        verify(agendamentoRepository).findAll();
    }

    @Test
    void deveListarAgendamentosComFiltroPorPacienteProfissionalOuStatus() {
        List<Agendamento> agendamentos = List.of(new Agendamento());

        when(agendamentoRepository.findAll(any(Specification.class))).thenReturn(agendamentos);

        List<Agendamento> resultado = agendamentoService.listar(1L, 2L, StatusAgendamento.AGENDADO);

        assertSame(agendamentos, resultado);
        verify(agendamentoRepository).findAll(any(Specification.class));
    }

    @Test
    void deveBuscarAgendamentoPorId() {
        Agendamento agendamento = new Agendamento();
        agendamento.setId(1L);

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

        Agendamento resultado = agendamentoService.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
        verify(agendamentoRepository).findById(1L);
    }

    @Test
    void deveLancarErroQuandoAgendamentoNaoForEncontrado() {
        when(agendamentoRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agendamentoService.buscarPorId(99L));

        assertEquals("Agendamento não encontrado.", exception.getMessage());
        verify(agendamentoRepository).findById(99L);
    }

    @Test
    void deveCancelarAgendamento() {
        Agendamento agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setStatus(StatusAgendamento.AGENDADO);

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);

        Agendamento resultado = agendamentoService.cancelar(1L, "Paciente solicitou cancelamento.");

        assertEquals(StatusAgendamento.CANCELADO, resultado.getStatus());
        assertEquals("Paciente solicitou cancelamento.", resultado.getMotivoCancelamento());
        verify(agendamentoRepository).save(agendamento);
    }

    @Test
    void deveMarcarAgendamentoComoRealizado() {
        Agendamento agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setStatus(StatusAgendamento.AGENDADO);

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.save(agendamento)).thenReturn(agendamento);

        Agendamento resultado = agendamentoService.realizar(1L);

        assertEquals(StatusAgendamento.REALIZADO, resultado.getStatus());
        verify(agendamentoRepository).save(agendamento);
    }

    @Test
    void deveLancarErroQuandoAgendamentoCanceladoForMarcadoComoRealizado() {
        Agendamento agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setStatus(StatusAgendamento.CANCELADO);

        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agendamentoService.realizar(1L));

        assertEquals("Apenas agendamentos agendados podem ser realizados.", exception.getMessage());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    void deveLancarErroQuandoMotivoCancelamentoNaoForInformado() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agendamentoService.cancelar(1L, " "));

        assertEquals("Motivo do cancelamento deve ser informado.", exception.getMessage());
        verify(agendamentoRepository, never()).findById(1L);
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }
}


