package com.projeto.agendamentosMV.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.agendamentosMV.entity.Paciente;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
}
