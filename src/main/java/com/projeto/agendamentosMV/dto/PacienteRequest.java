package com.projeto.agendamentosMV.dto;

import java.time.LocalDate;

import com.projeto.agendamentosMV.entity.Paciente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

public record PacienteRequest(
        @NotBlank String nome,
        @NotBlank String cpf,
        @NotNull @Past LocalDate dataNascimento,
        @NotBlank String sexo,
        @NotBlank String endereco) {

    public Paciente toEntity() {
        Paciente paciente = new Paciente();
        paciente.setNome(nome);
        paciente.setCpf(cpf);
        paciente.setDataNascimento(dataNascimento);
        paciente.setSexo(sexo);
        paciente.setEndereco(endereco);
        return paciente;
    }
}
