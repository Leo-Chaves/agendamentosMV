package com.projeto.agendamentosMV.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.projeto.agendamentosMV.entity.Agendamento;
import com.projeto.agendamentosMV.entity.AreaProfissional;
import com.projeto.agendamentosMV.entity.Paciente;
import com.projeto.agendamentosMV.entity.Profissional;
import com.projeto.agendamentosMV.entity.Sexo;
import com.projeto.agendamentosMV.entity.StatusAgendamento;
import com.projeto.agendamentosMV.service.AgendamentoService;

@WebMvcTest(AgendamentoController.class)
class AgendamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgendamentoService agendamentoService;

    @Test
    void deveCriarAgendamento() throws Exception {
        LocalDateTime dataHora = LocalDateTime.of(2026, 7, 10, 14, 0);
        Agendamento agendamento = agendamento(dataHora, StatusAgendamento.AGENDADO, null);

        when(agendamentoService.agendar(1L, 2L, dataHora)).thenReturn(agendamento);

        mockMvc.perform(post("/agendamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "pacienteId": 1,
                          "profissionalId": 2,
                          "dataHora": "2026-07-10T14:00:00"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.pacienteId").value(1))
                .andExpect(jsonPath("$.profissionalId").value(2))
                .andExpect(jsonPath("$.status").value("AGENDADO"));
    }

    @Test
    void deveListarAgendamentosComFiltros() throws Exception {
        LocalDateTime dataHora = LocalDateTime.of(2026, 7, 10, 14, 0);
        List<Agendamento> agendamentos = List.of(agendamento(dataHora, StatusAgendamento.AGENDADO, null));

        when(agendamentoService.listar(1L, 2L, StatusAgendamento.AGENDADO)).thenReturn(agendamentos);

        mockMvc.perform(get("/agendamentos")
                .param("pacienteId", "1")
                .param("profissionalId", "2")
                .param("status", "AGENDADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].status").value("AGENDADO"));

        verify(agendamentoService).listar(1L, 2L, StatusAgendamento.AGENDADO);
    }

    @Test
    void deveBuscarAgendamentoPorId() throws Exception {
        LocalDateTime dataHora = LocalDateTime.of(2026, 7, 10, 14, 0);
        Agendamento agendamento = agendamento(dataHora, StatusAgendamento.AGENDADO, null);

        when(agendamentoService.buscarPorId(10L)).thenReturn(agendamento);

        mockMvc.perform(get("/agendamentos/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.pacienteNome").value("Maria Silva"))
                .andExpect(jsonPath("$.profissionalNome").value("Ana Costa"));
    }

    @Test
    void deveCancelarAgendamentoComMotivo() throws Exception {
        LocalDateTime dataHora = LocalDateTime.of(2026, 7, 10, 14, 0);
        Agendamento agendamento = agendamento(dataHora, StatusAgendamento.CANCELADO, "Paciente solicitou.");

        when(agendamentoService.cancelar(10L, "Paciente solicitou.")).thenReturn(agendamento);

        mockMvc.perform(patch("/agendamentos/10/cancelar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "motivo": "Paciente solicitou."
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"))
                .andExpect(jsonPath("$.motivoCancelamento").value("Paciente solicitou."));
    }

    @Test
    void deveRetornarBadRequestQuandoCriacaoTiverPayloadInvalido() throws Exception {
        mockMvc.perform(post("/agendamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "pacienteId": null,
                          "profissionalId": null,
                          "dataHora": null
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos."));

        verify(agendamentoService, never()).agendar(null, null, null);
    }

    @Test
    void deveRetornarBadRequestQuandoCancelamentoNaoTiverMotivo() throws Exception {
        mockMvc.perform(patch("/agendamentos/10/cancelar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "motivo": ""
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos."));

        verify(agendamentoService, never()).cancelar(10L, "");
    }

    private Agendamento agendamento(LocalDateTime dataHora, StatusAgendamento status, String motivoCancelamento) {
        Paciente paciente = new Paciente(1L, "Maria Silva", "12345678900", LocalDate.of(1996, 1, 1), Sexo.FEMININO, "Rua A", List.of());
        Profissional profissional = new Profissional(2L, "Ana Costa", "CRM12345", AreaProfissional.CARDIOLOGIA, List.of());

        Agendamento agendamento = new Agendamento();
        agendamento.setId(10L);
        agendamento.setPaciente(paciente);
        agendamento.setProfissional(profissional);
        agendamento.setDataHora(dataHora);
        agendamento.setStatus(status);
        agendamento.setMotivoCancelamento(motivoCancelamento);

        return agendamento;
    }
}


