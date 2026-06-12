package com.projeto.agendamentosMV.dto;

import com.projeto.agendamentosMV.entity.Profissional;

import jakarta.validation.constraints.NotBlank;

public record ProfissionalRequest(
        @NotBlank String nome,
        @NotBlank String crm,
        @NotBlank String area) {

    public Profissional toEntity() {
        Profissional profissional = new Profissional();
        profissional.setNome(nome);
        profissional.setCrm(crm);
        profissional.setArea(area);
        return profissional;
    }
}
