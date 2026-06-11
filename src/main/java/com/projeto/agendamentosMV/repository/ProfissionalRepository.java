package com.projeto.agendamentosMV.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.agendamentosMV.entity.Profissional;

public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {
}
