package com.projeto.agendamentosMV.dto;

import com.projeto.agendamentosMV.entity.AreaProfissional;
import com.projeto.agendamentosMV.entity.Profissional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProfissionalRequest(
        @NotBlank String nome,
        @NotBlank String crm,
        @NotNull AreaProfissional area) {

    public Profissional toEntity() {
        Profissional profissional = new Profissional();
        profissional.setNome(nome);
        profissional.setCrm(crm);
        profissional.setArea(area);
        return profissional;
    }
}
