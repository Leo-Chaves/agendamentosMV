package com.projeto.agendamentosMV.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.agendamentosMV.dto.ProfissionalRequest;
import com.projeto.agendamentosMV.dto.ProfissionalResponse;
import com.projeto.agendamentosMV.entity.Profissional;
import com.projeto.agendamentosMV.service.ProfissionalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/profissionais")
@RequiredArgsConstructor
public class ProfissionalController {

    private final ProfissionalService profissionalService;

    @PostMapping
    public ResponseEntity<ProfissionalResponse> salvar(@Valid @RequestBody ProfissionalRequest request) {
        Profissional profissionalSalvo = profissionalService.salvar(request.toEntity());

        return ResponseEntity
                .created(URI.create("/profissionais/" + profissionalSalvo.getId()))
                .body(ProfissionalResponse.from(profissionalSalvo));
    }

    @GetMapping
    public List<ProfissionalResponse> listar() {
        return profissionalService.listar()
                .stream()
                .map(ProfissionalResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ProfissionalResponse buscarPorId(@PathVariable Long id) {
        return ProfissionalResponse.from(profissionalService.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        profissionalService.inativar(id);
        return ResponseEntity.noContent().build();
    }
}
