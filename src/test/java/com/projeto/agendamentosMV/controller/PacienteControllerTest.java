package com.projeto.agendamentosMV.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.projeto.agendamentosMV.entity.Paciente;
import com.projeto.agendamentosMV.service.PacienteService;

@WebMvcTest(PacienteController.class)
class PacienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PacienteService pacienteService;

    @Test
    void deveCadastrarPaciente() throws Exception {
        Paciente pacienteSalvo = new Paciente(1L, "Maria Silva", "12345678900", 30, "Feminino", "Rua A",
                List.of());

        when(pacienteService.salvar(any(Paciente.class))).thenReturn(pacienteSalvo);

        mockMvc.perform(post("/pacientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "Maria Silva",
                          "cpf": "12345678900",
                          "idade": 30,
                          "sexo": "Feminino",
                          "endereco": "Rua A"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Maria Silva"))
                .andExpect(jsonPath("$.cpf").value("12345678900"));

        verify(pacienteService).salvar(any(Paciente.class));
    }

    @Test
    void deveListarPacientes() throws Exception {
        List<Paciente> pacientes = List.of(
                new Paciente(1L, "Maria Silva", "12345678900", 30, "Feminino", "Rua A", List.of()),
                new Paciente(2L, "Joao Souza", "98765432100", 41, "Masculino", "Rua B", List.of()));

        when(pacienteService.listar()).thenReturn(pacientes);

        mockMvc.perform(get("/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Maria Silva"))
                .andExpect(jsonPath("$[1].nome").value("Joao Souza"));
    }

    @Test
    void deveBuscarPacientePorId() throws Exception {
        Paciente paciente = new Paciente(1L, "Maria Silva", "12345678900", 30, "Feminino", "Rua A", List.of());

        when(pacienteService.buscarPorId(1L)).thenReturn(paciente);

        mockMvc.perform(get("/pacientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Maria Silva"));
    }
}
