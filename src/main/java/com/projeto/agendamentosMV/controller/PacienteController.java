package com.projeto.agendamentosMV.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.agendamentosMV.dto.PacienteRequest;
import com.projeto.agendamentosMV.dto.PacienteResponse;
import com.projeto.agendamentosMV.entity.Paciente;
import com.projeto.agendamentosMV.service.PacienteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    @PostMapping
    public ResponseEntity<PacienteResponse> salvar(@Valid @RequestBody PacienteRequest request) {
        Paciente pacienteSalvo = pacienteService.salvar(request.toEntity());

        return ResponseEntity
                .created(URI.create("/pacientes/" + pacienteSalvo.getId()))
                .body(PacienteResponse.from(pacienteSalvo));
    }

    @GetMapping
    public List<PacienteResponse> listar() {
        return pacienteService.listar()
                .stream()
                .map(PacienteResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public PacienteResponse buscarPorId(@PathVariable Long id) {
        return PacienteResponse.from(pacienteService.buscarPorId(id));
    }
}
