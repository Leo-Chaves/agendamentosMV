package com.projeto.agendamentosMV.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.projeto.agendamentosMV.entity.Agendamento;
import com.projeto.agendamentosMV.entity.AreaProfissional;
import com.projeto.agendamentosMV.entity.Paciente;
import com.projeto.agendamentosMV.entity.Profissional;
import com.projeto.agendamentosMV.entity.Sexo;
import com.projeto.agendamentosMV.entity.StatusAgendamento;
import com.projeto.agendamentosMV.repository.AgendamentoRepository;
import com.projeto.agendamentosMV.repository.PacienteRepository;
import com.projeto.agendamentosMV.repository.ProfissionalRepository;

@Configuration
public class DadosIniciaisConfig {

    @Bean
    CommandLineRunner carregarDadosIniciais(
            PacienteRepository pacienteRepository,
            ProfissionalRepository profissionalRepository,
            AgendamentoRepository agendamentoRepository) {
        return args -> {
            boolean bancoJaPossuiDados = pacienteRepository.count() > 0
                    || profissionalRepository.count() > 0
                    || agendamentoRepository.count() > 0;

            if (bancoJaPossuiDados) {
                return;
            }

            Paciente ana = novoPaciente("Ana Souza", "11122233344", LocalDate.of(1994, 3, 12), Sexo.FEMININO,
                    "Rua das Flores, 100");
            Paciente bruno = novoPaciente("Bruno Lima", "22233344455", LocalDate.of(1988, 7, 25), Sexo.MASCULINO,
                    "Avenida Central, 230");
            Paciente carla = novoPaciente("Carla Mendes", "33344455566", LocalDate.of(2001, 11, 8), Sexo.FEMININO,
                    "Rua Primavera, 45");
            Paciente diego = novoPaciente("Diego Rocha", "44455566677", LocalDate.of(1979, 1, 30), Sexo.MASCULINO,
                    "Travessa Norte, 12");

            pacienteRepository.saveAll(List.of(ana, bruno, carla, diego));

            Profissional lara = novoProfissional("Dra. Lara Costa", "CRM1001", AreaProfissional.CARDIOLOGIA);
            Profissional marcos = novoProfissional("Dr. Marcos Vieira", "CRM1002", AreaProfissional.CLINICO_GERAL);
            Profissional helena = novoProfissional("Dra. Helena Prado", "CRM1003", AreaProfissional.PEDIATRIA);
            Profissional rafael = novoProfissional("Dr. Rafael Nunes", "CRM1004", AreaProfissional.FISIOTERAPIA);

            profissionalRepository.saveAll(List.of(lara, marcos, helena, rafael));

            LocalDateTime amanha = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);

            Agendamento agendamentoAna = novoAgendamento(ana, lara, amanha.withHour(9).withMinute(0),
                    StatusAgendamento.AGENDADO, null);
            Agendamento agendamentoBruno = novoAgendamento(bruno, marcos, amanha.withHour(10).withMinute(0),
                    StatusAgendamento.AGENDADO, null);
            Agendamento agendamentoCarla = novoAgendamento(carla, helena, amanha.plusDays(1).withHour(11).withMinute(0),
                    StatusAgendamento.AGENDADO, null);
            Agendamento agendamentoDiego = novoAgendamento(diego, rafael, amanha.plusDays(2).withHour(14).withMinute(0),
                    StatusAgendamento.CANCELADO, "Paciente solicitou remarcacao.");

            agendamentoRepository.saveAll(List.of(
                    agendamentoAna,
                    agendamentoBruno,
                    agendamentoCarla,
                    agendamentoDiego));
        };
    }

    private Paciente novoPaciente(String nome, String cpf, LocalDate dataNascimento, Sexo sexo, String endereco) {
        Paciente paciente = new Paciente();
        paciente.setNome(nome);
        paciente.setCpf(cpf);
        paciente.setDataNascimento(dataNascimento);
        paciente.setSexo(sexo);
        paciente.setEndereco(endereco);
        paciente.setAtivo(true);
        return paciente;
    }

    private Profissional novoProfissional(String nome, String crm, AreaProfissional area) {
        Profissional profissional = new Profissional();
        profissional.setNome(nome);
        profissional.setCrm(crm);
        profissional.setArea(area);
        profissional.setAtivo(true);
        return profissional;
    }

    private Agendamento novoAgendamento(
            Paciente paciente,
            Profissional profissional,
            LocalDateTime dataHora,
            StatusAgendamento status,
            String motivoCancelamento) {
        Agendamento agendamento = new Agendamento();
        agendamento.setPaciente(paciente);
        agendamento.setProfissional(profissional);
        agendamento.setDataHora(dataHora);
        agendamento.setStatus(status);
        agendamento.setMotivoCancelamento(motivoCancelamento);
        return agendamento;
    }
}
