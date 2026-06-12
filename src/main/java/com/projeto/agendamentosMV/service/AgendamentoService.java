package com.projeto.agendamentosMV.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
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

    public List<Agendamento> listar(Long pacienteId, Long profissionalId, StatusAgendamento status) {
        Specification<Agendamento> filtros = Specification.allOf(
                pacienteIdIgualA(pacienteId),
                profissionalIdIgualA(profissionalId),
                statusIgualA(status));

        return agendamentoRepository.findAll(filtros);
    }

    public Agendamento buscarPorId(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado."));
    }

    public Agendamento cancelar(Long id, String motivoCancelamento) {
        validarMotivoCancelamento(motivoCancelamento);

        Agendamento agendamento = buscarPorId(id);
        agendamento.setStatus(StatusAgendamento.CANCELADO);
        agendamento.setMotivoCancelamento(motivoCancelamento.trim());
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

    private void validarMotivoCancelamento(String motivoCancelamento) {
        if (motivoCancelamento == null || motivoCancelamento.isBlank()) {
            throw new IllegalArgumentException("Motivo do cancelamento deve ser informado.");
        }
    }

    private Specification<Agendamento> pacienteIdIgualA(Long pacienteId) {
        return (root, query, criteriaBuilder) -> pacienteId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("paciente").get("id"), pacienteId);
    }

    private Specification<Agendamento> profissionalIdIgualA(Long profissionalId) {
        return (root, query, criteriaBuilder) -> profissionalId == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("profissional").get("id"), profissionalId);
    }

    private Specification<Agendamento> statusIgualA(StatusAgendamento status) {
        return (root, query, criteriaBuilder) -> status == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("status"), status);
    }
}
