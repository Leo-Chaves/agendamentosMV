package com.projeto.agendamentosMV.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String cpf;

    @NotNull
    @Past
    private LocalDate dataNascimento;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Sexo sexo;

    @NotBlank
    private String endereco;

    @Column(nullable = false)
    private Boolean ativo = true;

    @JsonIgnore
    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL)
    private List<Agendamento> agendamentos = new ArrayList<>();

    public Paciente(Long id, String nome, String cpf, LocalDate dataNascimento, Sexo sexo, String endereco,
            List<Agendamento> agendamentos) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.sexo = sexo;
        this.endereco = endereco;
        this.ativo = true;
        this.agendamentos = agendamentos;
    }
}
