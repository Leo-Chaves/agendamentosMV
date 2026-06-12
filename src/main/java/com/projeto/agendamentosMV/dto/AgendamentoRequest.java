package com.projeto.agendamentosMV.dto;

import java.time.LocalDateTime;

import com.projeto.agendamentosMV.entity.TipoAtendimento;

import jakarta.validation.constraints.NotNull;

public record AgendamentoRequest(
        @NotNull Long pacienteId,
        @NotNull Long profissionalId,
        @NotNull LocalDateTime dataHora,
        @NotNull TipoAtendimento tipoAtendimento) {
}
