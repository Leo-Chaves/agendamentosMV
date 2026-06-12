package com.projeto.agendamentosMV.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record AgendamentoRequest(
        @NotNull Long pacienteId,
        @NotNull Long profissionalId,
        @NotNull LocalDateTime dataHora) {
}
