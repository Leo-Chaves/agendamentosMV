package com.projeto.agendamentosMV.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.agendamentosMV.entity.Agendamento;
import com.projeto.agendamentosMV.entity.StatusAgendamento;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long>, JpaSpecificationExecutor<Agendamento> {

    boolean existsByPacienteIdAndStatusAndDataHoraAfterAndDataHoraBefore(
            Long pacienteId,
            StatusAgendamento status,
            LocalDateTime inicioJanela,
            LocalDateTime fim);

    boolean existsByProfissionalIdAndStatusAndDataHoraAfterAndDataHoraBefore(
            Long profissionalId,
            StatusAgendamento status,
            LocalDateTime inicioJanela,
            LocalDateTime fim);
}
