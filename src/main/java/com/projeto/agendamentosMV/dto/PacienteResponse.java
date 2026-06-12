package com.projeto.agendamentosMV.dto;

import com.projeto.agendamentosMV.entity.Paciente;

public record PacienteResponse(
        Long id,
        String nome,
        String cpf,
        Integer idade,
        String sexo,
        String endereco,
        Boolean ativo) {

    public static PacienteResponse from(Paciente paciente) {
        return new PacienteResponse(
                paciente.getId(),
                paciente.getNome(),
                paciente.getCpf(),
                paciente.getIdade(),
                paciente.getSexo(),
                paciente.getEndereco(),
                paciente.getAtivo());
    }
}
