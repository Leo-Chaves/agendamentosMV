package com.projeto.agendamentosMV.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.agendamentosMV.entity.Agendamento;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
}
