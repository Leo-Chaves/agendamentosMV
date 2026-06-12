package com.projeto.agendamentosMV.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.projeto.agendamentosMV.entity.Agendamento;
import com.projeto.agendamentosMV.entity.AreaProfissional;
import com.projeto.agendamentosMV.entity.Paciente;
import com.projeto.agendamentosMV.entity.Profissional;
import com.projeto.agendamentosMV.entity.Sexo;
import com.projeto.agendamentosMV.entity.StatusAgendamento;
import com.projeto.agendamentosMV.entity.TipoAtendimento;

import jakarta.persistence.EntityManager;

@DataJpaTest
class AgendamentoRepositoryTest {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void deveEncontrarConflitoQuandoPacienteTiverAgendamentoSobreposto() {
        Paciente paciente = salvarPaciente("12345678900");
        Profissional profissional = salvarProfissional("CRM12345");
        salvarAgendamento(paciente, profissional, LocalDateTime.of(2026, 7, 10, 10, 0),
                StatusAgendamento.AGENDADO);

        LocalDateTime novoInicio = LocalDateTime.of(2026, 7, 10, 10, 15);

        boolean existeConflito = agendamentoRepository.existsByPacienteIdAndStatusAndDataHoraAfterAndDataHoraBefore(
                paciente.getId(),
                StatusAgendamento.AGENDADO,
                novoInicio.minusMinutes(30),
                novoInicio.plusMinutes(30));

        assertTrue(existeConflito);
    }

    @Test
    void deveEncontrarConflitoQuandoProfissionalTiverAgendamentoSobrepostoAntesDoNovoInicio() {
        Paciente paciente = salvarPaciente("12345678901");
        Profissional profissional = salvarProfissional("CRM12346");
        salvarAgendamento(paciente, profissional, LocalDateTime.of(2026, 7, 10, 9, 45),
                StatusAgendamento.AGENDADO);

        LocalDateTime novoInicio = LocalDateTime.of(2026, 7, 10, 10, 0);

        boolean existeConflito =
                agendamentoRepository.existsByProfissionalIdAndStatusAndDataHoraAfterAndDataHoraBefore(
                        profissional.getId(),
                        StatusAgendamento.AGENDADO,
                        novoInicio.minusMinutes(30),
                        novoInicio.plusMinutes(30));

        assertTrue(existeConflito);
    }

    @Test
    void naoDeveEncontrarConflitoQuandoAgendamentoEncostarNoFimDoAnterior() {
        Paciente paciente = salvarPaciente("12345678902");
        Profissional profissional = salvarProfissional("CRM12347");
        salvarAgendamento(paciente, profissional, LocalDateTime.of(2026, 7, 10, 10, 0),
                StatusAgendamento.AGENDADO);

        LocalDateTime novoInicio = LocalDateTime.of(2026, 7, 10, 10, 30);

        boolean existeConflito = agendamentoRepository.existsByPacienteIdAndStatusAndDataHoraAfterAndDataHoraBefore(
                paciente.getId(),
                StatusAgendamento.AGENDADO,
                novoInicio.minusMinutes(30),
                novoInicio.plusMinutes(30));

        assertFalse(existeConflito);
    }

    @Test
    void deveIgnorarAgendamentoCanceladoNaValidacaoDeConflito() {
        Paciente paciente = salvarPaciente("12345678903");
        Profissional profissional = salvarProfissional("CRM12348");
        salvarAgendamento(paciente, profissional, LocalDateTime.of(2026, 7, 10, 10, 0),
                StatusAgendamento.CANCELADO);

        LocalDateTime novoInicio = LocalDateTime.of(2026, 7, 10, 10, 15);

        boolean existeConflito = agendamentoRepository.existsByPacienteIdAndStatusAndDataHoraAfterAndDataHoraBefore(
                paciente.getId(),
                StatusAgendamento.AGENDADO,
                novoInicio.minusMinutes(30),
                novoInicio.plusMinutes(30));

        assertFalse(existeConflito);
    }

    private Paciente salvarPaciente(String cpf) {
        Paciente paciente = new Paciente(null, "Maria Silva", cpf, LocalDate.of(1996, 1, 1), Sexo.FEMININO, "Rua A", List.of());
        entityManager.persist(paciente);
        return paciente;
    }

    private Profissional salvarProfissional(String crm) {
        Profissional profissional = new Profissional(null, "Ana Costa", crm, AreaProfissional.CARDIOLOGIA, List.of());
        entityManager.persist(profissional);
        return profissional;
    }

    private void salvarAgendamento(
            Paciente paciente,
            Profissional profissional,
            LocalDateTime dataHora,
            StatusAgendamento status) {
        Agendamento agendamento = new Agendamento();
        agendamento.setPaciente(paciente);
        agendamento.setProfissional(profissional);
        agendamento.setDataHora(dataHora);
        agendamento.setTipoAtendimento(TipoAtendimento.CONSULTA);
        agendamento.setStatus(status);
        entityManager.persist(agendamento);
        entityManager.flush();
    }
}


