package com.projeto.agendamentosMV.dto;

import java.time.LocalDate;
import java.time.Period;

import com.projeto.agendamentosMV.entity.Paciente;
import com.projeto.agendamentosMV.entity.Sexo;

public record PacienteResponse(
        Long id,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        Integer idade,
        Sexo sexo,
        String endereco,
        Boolean ativo) {

    public static PacienteResponse from(Paciente paciente) {
        return new PacienteResponse(
                paciente.getId(),
                paciente.getNome(),
                paciente.getCpf(),
                paciente.getDataNascimento(),
                calcularIdade(paciente.getDataNascimento()),
                paciente.getSexo(),
                paciente.getEndereco(),
                paciente.getAtivo());
    }

    private static Integer calcularIdade(LocalDate dataNascimento) {
        return dataNascimento == null ? null : Period.between(dataNascimento, LocalDate.now()).getYears();
    }
}
