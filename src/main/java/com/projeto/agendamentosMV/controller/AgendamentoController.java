package com.projeto.agendamentosMV.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.agendamentosMV.dto.AgendamentoRequest;
import com.projeto.agendamentosMV.dto.AgendamentoResponse;
import com.projeto.agendamentosMV.dto.CancelamentoAgendamentoRequest;
import com.projeto.agendamentosMV.entity.Agendamento;
import com.projeto.agendamentosMV.entity.StatusAgendamento;
import com.projeto.agendamentosMV.service.AgendamentoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/agendamentos")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    @PostMapping
    public ResponseEntity<AgendamentoResponse> agendar(@Valid @RequestBody AgendamentoRequest request) {
        Agendamento agendamento = agendamentoService.agendar(
                request.pacienteId(),
                request.profissionalId(),
                request.dataHora());

        return ResponseEntity
                .created(URI.create("/agendamentos/" + agendamento.getId()))
                .body(AgendamentoResponse.from(agendamento));
    }

    @GetMapping
    public List<AgendamentoResponse> listar(
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) Long profissionalId,
            @RequestParam(required = false) StatusAgendamento status) {

        return agendamentoService.listar(pacienteId, profissionalId, status)
                .stream()
                .map(AgendamentoResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public AgendamentoResponse buscarPorId(@PathVariable Long id) {
        return AgendamentoResponse.from(agendamentoService.buscarPorId(id));
    }

    @PatchMapping("/{id}/cancelar")
    public AgendamentoResponse cancelar(
            @PathVariable Long id,
            @Valid @RequestBody CancelamentoAgendamentoRequest request) {

        return AgendamentoResponse.from(agendamentoService.cancelar(id, request.motivo()));
    }

    @PatchMapping("/{id}/realizar")
    public AgendamentoResponse realizar(@PathVariable Long id) {
        return AgendamentoResponse.from(agendamentoService.realizar(id));
    }
}
