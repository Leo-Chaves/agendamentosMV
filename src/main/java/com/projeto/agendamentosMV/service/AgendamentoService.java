package com.projeto.agendamentosMV.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.projeto.agendamentosMV.entity.Agendamento;
import com.projeto.agendamentosMV.entity.Paciente;
import com.projeto.agendamentosMV.entity.Profissional;
import com.projeto.agendamentosMV.entity.StatusAgendamento;
import com.projeto.agendamentosMV.repository.AgendamentoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final PacienteService pacienteService;
    private final ProfissionalService profissionalService;

    public Agendamento agendar(Long pacienteId, Long profissionalId, LocalDateTime dataHora) {
        Paciente paciente = pacienteService.buscarPorId(pacienteId);
        Profissional profissional = profissionalService.buscarPorId(profissionalId);

        validarDataHora(dataHora);
        validarDisponibilidade(pacienteId, profissionalId, dataHora);

        Agendamento agendamento = new Agendamento();
        agendamento.setPaciente(paciente);
        agendamento.setProfissional(profissional);
        agendamento.setDataHora(dataHora);
        agendamento.setStatus(StatusAgendamento.AGENDADO);

        return agendamentoRepository.save(agendamento);
    }

    public List<Agendamento> listar() {
        return agendamentoRepository.findAll();
    }

    public Agendamento buscarPorId(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado."));
    }

    public Agendamento cancelar(Long id) {
        Agendamento agendamento = buscarPorId(id);
        agendamento.setStatus(StatusAgendamento.CANCELADO);
        return agendamentoRepository.save(agendamento);
    }

    private void validarDisponibilidade(Long pacienteId, Long profissionalId, LocalDateTime dataHora) {
        boolean pacienteOcupado = agendamentoRepository.existsByPacienteIdAndDataHoraAndStatus(
                pacienteId,
                dataHora,
                StatusAgendamento.AGENDADO);

        if (pacienteOcupado) {
            throw new IllegalArgumentException("Paciente já possui agendamento neste horário.");
        }

        boolean profissionalOcupado = agendamentoRepository.existsByProfissionalIdAndDataHoraAndStatus(
                profissionalId,
                dataHora,
                StatusAgendamento.AGENDADO);

        if (profissionalOcupado) {
            throw new IllegalArgumentException("Profissional já possui agendamento neste horário.");
        }
    }

    private void validarDataHora(LocalDateTime dataHora) {
        if (dataHora.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Não é possível agendar para uma data passada.");
        }
    }
}
