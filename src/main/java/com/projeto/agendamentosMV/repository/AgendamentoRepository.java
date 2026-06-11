package com.projeto.agendamentosMV.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projeto.agendamentosMV.entity.Agendamento;
import com.projeto.agendamentosMV.entity.StatusAgendamento;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    boolean existsByPacienteIdAndDataHoraAndStatus(Long pacienteId, LocalDateTime dataHora, StatusAgendamento status);

    boolean existsByProfissionalIdAndDataHoraAndStatus(Long profissionalId, LocalDateTime dataHora,
            StatusAgendamento status);
}
