package com.projeto.agendamentosMV.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

import com.projeto.agendamentosMV.entity.Profissional;
import com.projeto.agendamentosMV.service.ProfissionalService;

@WebMvcTest(ProfissionalController.class)
class ProfissionalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfissionalService profissionalService;

    @Test
    void deveCadastrarProfissional() throws Exception {
        Profissional profissionalSalvo = new Profissional(1L, "Ana Costa", "CRM12345", "Cardiologia", List.of());

        when(profissionalService.salvar(any(Profissional.class))).thenReturn(profissionalSalvo);

        mockMvc.perform(post("/profissionais")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "Ana Costa",
                          "crm": "CRM12345",
                          "area": "Cardiologia"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana Costa"))
                .andExpect(jsonPath("$.crm").value("CRM12345"))
                .andExpect(jsonPath("$.area").value("Cardiologia"));

        verify(profissionalService).salvar(any(Profissional.class));
    }

    @Test
    void deveListarProfissionais() throws Exception {
        List<Profissional> profissionais = List.of(
                new Profissional(1L, "Ana Costa", "CRM12345", "Cardiologia", List.of()),
                new Profissional(2L, "Bruno Lima", "CRM67890", "Ortopedia", List.of()));

        when(profissionalService.listar()).thenReturn(profissionais);

        mockMvc.perform(get("/profissionais"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Ana Costa"))
                .andExpect(jsonPath("$[1].nome").value("Bruno Lima"));
    }

    @Test
    void deveBuscarProfissionalPorId() throws Exception {
        Profissional profissional = new Profissional(1L, "Ana Costa", "CRM12345", "Cardiologia", List.of());

        when(profissionalService.buscarPorId(1L)).thenReturn(profissional);

        mockMvc.perform(get("/profissionais/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana Costa"));
    }

    @Test
    void deveRetornarBadRequestQuandoProfissionalNaoForEncontrado() throws Exception {
        when(profissionalService.buscarPorId(99L)).thenThrow(new IllegalArgumentException("Profissional não encontrado."));

        mockMvc.perform(get("/profissionais/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Profissional não encontrado."));
    }

    @Test
    void deveRetornarBadRequestQuandoPayloadForInvalido() throws Exception {
        mockMvc.perform(post("/profissionais")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "",
                          "crm": "",
                          "area": ""
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos."));

        verify(profissionalService, never()).salvar(any(Profissional.class));
    }

    @Test
    void deveInativarProfissional() throws Exception {
        Profissional profissional = new Profissional(1L, "Ana Costa", "CRM12345", "Cardiologia", List.of());
        profissional.setAtivo(false);

        when(profissionalService.inativar(1L)).thenReturn(profissional);

        mockMvc.perform(delete("/profissionais/1"))
                .andExpect(status().isNoContent());

        verify(profissionalService).inativar(1L);
    }
}
