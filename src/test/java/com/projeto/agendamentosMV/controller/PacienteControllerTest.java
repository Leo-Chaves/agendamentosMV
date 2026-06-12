package com.projeto.agendamentosMV.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
        Paciente pacienteSalvo = new Paciente(1L, "Maria Silva", "12345678900", LocalDate.of(1996, 1, 1), "Feminino", "Rua A",
                List.of());

        when(pacienteService.salvar(any(Paciente.class))).thenReturn(pacienteSalvo);

        mockMvc.perform(post("/pacientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "Maria Silva",
                          "cpf": "12345678900",
                          "dataNascimento": "1996-01-01",
                          "sexo": "Feminino",
                          "endereco": "Rua A"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Maria Silva"))
                .andExpect(jsonPath("$.cpf").value("12345678900"))
                .andExpect(jsonPath("$.dataNascimento").value("1996-01-01"))
                .andExpect(jsonPath("$.idade").value(30));

        verify(pacienteService).salvar(any(Paciente.class));
    }

    @Test
    void deveListarPacientes() throws Exception {
        List<Paciente> pacientes = List.of(
                new Paciente(1L, "Maria Silva", "12345678900", LocalDate.of(1996, 1, 1), "Feminino", "Rua A", List.of()),
                new Paciente(2L, "Joao Souza", "98765432100", LocalDate.of(1985, 1, 1), "Masculino", "Rua B", List.of()));

        when(pacienteService.listar()).thenReturn(pacientes);

        mockMvc.perform(get("/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Maria Silva"))
                .andExpect(jsonPath("$[1].nome").value("Joao Souza"));
    }

    @Test
    void deveBuscarPacientePorId() throws Exception {
        Paciente paciente = new Paciente(1L, "Maria Silva", "12345678900", LocalDate.of(1996, 1, 1), "Feminino", "Rua A", List.of());

        when(pacienteService.buscarPorId(1L)).thenReturn(paciente);

        mockMvc.perform(get("/pacientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Maria Silva"));
    }

    @Test
    void deveAtualizarPaciente() throws Exception {
        Paciente pacienteAtualizado = new Paciente(1L, "Maria Oliveira", "11122233344", LocalDate.of(1995, 1, 1), "Feminino", "Rua B",
                List.of());

        when(pacienteService.atualizar(eq(1L), any(Paciente.class))).thenReturn(pacienteAtualizado);

        mockMvc.perform(put("/pacientes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "Maria Oliveira",
                          "cpf": "11122233344",
                          "dataNascimento": "1995-01-01",
                          "sexo": "Feminino",
                          "endereco": "Rua B"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Maria Oliveira"))
                .andExpect(jsonPath("$.cpf").value("11122233344"))
                .andExpect(jsonPath("$.dataNascimento").value("1995-01-01"))
                .andExpect(jsonPath("$.idade").value(31));

        verify(pacienteService).atualizar(eq(1L), any(Paciente.class));
    }

    @Test
    void deveRetornarBadRequestQuandoPacienteNaoForEncontrado() throws Exception {
        when(pacienteService.buscarPorId(99L)).thenThrow(new IllegalArgumentException("Paciente não encontrado."));

        mockMvc.perform(get("/pacientes/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Paciente não encontrado."));
    }

    @Test
    void deveRetornarBadRequestQuandoPayloadForInvalido() throws Exception {
        mockMvc.perform(post("/pacientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "",
                          "cpf": "",
                          "dataNascimento": "2999-01-01",
                          "sexo": "",
                          "endereco": ""
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos."));

        verify(pacienteService, never()).salvar(any(Paciente.class));
    }

    @Test
    void deveInativarPaciente() throws Exception {
        Paciente paciente = new Paciente(1L, "Maria Silva", "12345678900", LocalDate.of(1996, 1, 1), "Feminino", "Rua A", List.of());
        paciente.setAtivo(false);

        when(pacienteService.inativar(1L)).thenReturn(paciente);

        mockMvc.perform(delete("/pacientes/1"))
                .andExpect(status().isNoContent());

        verify(pacienteService).inativar(1L);
    }

    @Test
    void deveAtivarPaciente() throws Exception {
        Paciente paciente = new Paciente(1L, "Maria Silva", "12345678900", LocalDate.of(1996, 1, 1), "Feminino", "Rua A", List.of());

        when(pacienteService.ativar(1L)).thenReturn(paciente);

        mockMvc.perform(patch("/pacientes/1/ativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));

        verify(pacienteService).ativar(1L);
    }
}

