package com.projeto.agendamentosMV.entity;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String crm;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AreaProfissional area;

    @Column(nullable = false)
    private Boolean ativo = true;

    @JsonIgnore
    @OneToMany(mappedBy = "profissional", cascade = CascadeType.ALL)
    private List<Agendamento> agendamentos = new ArrayList<>();

    public Profissional(Long id, String nome, String crm, AreaProfissional area, List<Agendamento> agendamentos) {
        this.id = id;
        this.nome = nome;
        this.crm = crm;
        this.area = area;
        this.ativo = true;
        this.agendamentos = agendamentos;
    }
}
