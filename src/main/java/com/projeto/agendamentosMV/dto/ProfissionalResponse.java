package com.projeto.agendamentosMV.dto;

import com.projeto.agendamentosMV.entity.AreaProfissional;
import com.projeto.agendamentosMV.entity.Profissional;

public record ProfissionalResponse(
        Long id,
        String nome,
        String crm,
        AreaProfissional area,
        Boolean ativo) {

    public static ProfissionalResponse from(Profissional profissional) {
        return new ProfissionalResponse(
                profissional.getId(),
                profissional.getNome(),
                profissional.getCrm(),
                profissional.getArea(),
                profissional.getAtivo());
    }
}
