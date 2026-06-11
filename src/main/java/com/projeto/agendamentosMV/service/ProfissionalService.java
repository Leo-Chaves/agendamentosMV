package com.projeto.agendamentosMV.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.projeto.agendamentosMV.entity.Profissional;
import com.projeto.agendamentosMV.repository.ProfissionalRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;

    public Profissional salvar(Profissional profissional) {
        return profissionalRepository.save(profissional);
    }

    public List<Profissional> listar() {
        return profissionalRepository.findAll();
    }

    public Profissional buscarPorId(Long id) {
        return profissionalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado."));
    }
}
