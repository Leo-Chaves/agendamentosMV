package com.projeto.agendamentosMV.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.projeto.agendamentosMV.entity.Agendamento;
import com.projeto.agendamentosMV.entity.AreaProfissional;
import com.projeto.agendamentosMV.entity.Paciente;
import com.projeto.agendamentosMV.entity.Profissional;
import com.projeto.agendamentosMV.entity.StatusAgendamento;
import com.projeto.agendamentosMV.entity.TipoAtendimento;
import com.projeto.agendamentosMV.repository.AgendamentoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final PacienteService pacienteService;
    private final ProfissionalService profissionalService;

    @Value("${agendamento.duracao-minutos:30}")
    private long duracaoMinutos = 30;

    public Agendamento agendar(Long pacienteId, Long profissionalId, LocalDateTime dataHora, TipoAtendimento tipoAtendimento) {
        Paciente paciente = pacienteService.buscarPorId(pacienteId);
        Profissional profissional = profissionalService.buscarPorId(profissionalId);

        validarPacienteAtivo(paciente);
        validarProfissionalAtivo(profissional);
        validarDataHora(dataHora);
        validarTipoAtendimento(profissional, tipoAtendimento);
        validarDisponibilidade(pacienteId, profissionalId, dataHora);

        Agendamento agendamento = new Agendamento();
        agendamento.setPaciente(paciente);
        agendamento.setProfissional(profissional);
        agendamento.setDataHora(dataHora);
        agendamento.setTipoAtendimento(tipoAtendimento);
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

    public Agendamento realizar(Long id) {
        Agendamento agendamento = buscarPorId(id);
        validarAgendamentoPodeSerRealizado(agendamento);

        agendamento.setStatus(StatusAgendamento.REALIZADO);
        return agendamentoRepository.save(agendamento);
    }

    private void validarDisponibilidade(Long pacienteId, Long profissionalId, LocalDateTime dataHora) {
        LocalDateTime inicioJanelaConflito = dataHora.minusMinutes(duracaoMinutos);
        LocalDateTime fimNovoAgendamento = dataHora.plusMinutes(duracaoMinutos);

        boolean pacienteOcupado = agendamentoRepository.existsByPacienteIdAndStatusAndDataHoraAfterAndDataHoraBefore(
                pacienteId,
                StatusAgendamento.AGENDADO,
                inicioJanelaConflito,
                fimNovoAgendamento);

        if (pacienteOcupado) {
            throw new IllegalArgumentException("Paciente já possui agendamento neste horário.");
        }

        boolean profissionalOcupado = agendamentoRepository
                .existsByProfissionalIdAndStatusAndDataHoraAfterAndDataHoraBefore(
                profissionalId,
                StatusAgendamento.AGENDADO,
                inicioJanelaConflito,
                fimNovoAgendamento);

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

    private void validarTipoAtendimento(Profissional profissional, TipoAtendimento tipoAtendimento) {
        if (tipoAtendimento == null) {
            throw new IllegalArgumentException("Tipo de atendimento deve ser informado.");
        }

        boolean profissionalRealizaExames = profissionalRealizaExames(profissional);

        if (tipoAtendimento == TipoAtendimento.EXAME && !profissionalRealizaExames) {
            throw new IllegalArgumentException("Exames devem ser agendados com profissional de biomedicina ou enfermagem.");
        }

        if (tipoAtendimento != TipoAtendimento.EXAME && profissionalRealizaExames) {
            throw new IllegalArgumentException("Consultas, retornos e avaliacoes devem ser agendados com as demais areas profissionais.");
        }
    }

    private boolean profissionalRealizaExames(Profissional profissional) {
        return profissional.getArea() == AreaProfissional.BIOMEDICINA
                || profissional.getArea() == AreaProfissional.ENFERMAGEM;
    }

    private void validarAgendamentoPodeSerRealizado(Agendamento agendamento) {
        if (agendamento.getStatus() != StatusAgendamento.AGENDADO) {
            throw new IllegalArgumentException("Apenas agendamentos agendados podem ser realizados.");
        }
    }

    private void validarPacienteAtivo(Paciente paciente) {
        if (Boolean.FALSE.equals(paciente.getAtivo())) {
            throw new IllegalArgumentException("Paciente inativo não pode receber agendamento.");
        }
    }

    private void validarProfissionalAtivo(Profissional profissional) {
        if (Boolean.FALSE.equals(profissional.getAtivo())) {
            throw new IllegalArgumentException("Profissional inativo não pode receber agendamento.");
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
