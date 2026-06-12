package com.projeto.agendamentosMV.dto;

import java.time.LocalDateTime;

import com.projeto.agendamentosMV.entity.Agendamento;
import com.projeto.agendamentosMV.entity.StatusAgendamento;

public record AgendamentoResponse(
        Long id,
        LocalDateTime dataHora,
        Long pacienteId,
        String pacienteNome,
        Long profissionalId,
        String profissionalNome,
        StatusAgendamento status,
        String motivoCancelamento) {

    public static AgendamentoResponse from(Agendamento agendamento) {
        return new AgendamentoResponse(
                agendamento.getId(),
                agendamento.getDataHora(),
                agendamento.getPaciente().getId(),
                agendamento.getPaciente().getNome(),
                agendamento.getProfissional().getId(),
                agendamento.getProfissional().getNome(),
                agendamento.getStatus(),
                agendamento.getMotivoCancelamento());
    }
}
