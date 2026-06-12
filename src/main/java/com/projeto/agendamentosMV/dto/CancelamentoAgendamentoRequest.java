package com.projeto.agendamentosMV.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelamentoAgendamentoRequest(@NotBlank String motivo) {
}
