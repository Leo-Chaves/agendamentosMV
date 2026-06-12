package com.projeto.agendamentosMV.dto;

import com.projeto.agendamentosMV.entity.Paciente;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PacienteRequest(
        @NotBlank String nome,
        @NotBlank String cpf,
        @Min(0) Integer idade,
        @NotBlank String sexo,
        @NotBlank String endereco) {

    public Paciente toEntity() {
        Paciente paciente = new Paciente();
        paciente.setNome(nome);
        paciente.setCpf(cpf);
        paciente.setIdade(idade);
        paciente.setSexo(sexo);
        paciente.setEndereco(endereco);
        return paciente;
    }
}
