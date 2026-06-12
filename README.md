# Agendamentos MV

API REST para cadastro de pacientes, profissionais e controle de agendamentos.

O projeto foi desenvolvido com Spring Boot, Spring Data JPA, Bean Validation, H2 em memória e testes automatizados com JUnit, Mockito e MockMvc.

## Requisitos

- Java 17+
- Maven Wrapper incluso no projeto

## Como executar

```powershell
.\mvnw.cmd spring-boot:run
```

A API fica disponível em:

```text
http://localhost:8080
```

## Como rodar os testes

```powershell
.\mvnw.cmd test
```

## Banco H2

Console:

```text
http://localhost:8080/h2-console
```

Dados de conexão:

```text
JDBC URL: jdbc:h2:mem:agendamentosmv
User: sa
Password:
```

## Principais endpoints

### Pacientes

Criar paciente:

```http
POST /pacientes
```

```json
{
  "nome": "Maria Silva",
  "cpf": "12345678900",
  "idade": 30,
  "sexo": "Feminino",
  "endereco": "Rua A"
}
```

Listar pacientes:

```http
GET /pacientes
```

Buscar paciente:

```http
GET /pacientes/{id}
```

Inativar paciente:

```http
DELETE /pacientes/{id}
```

### Profissionais

Criar profissional:

```http
POST /profissionais
```

```json
{
  "nome": "Ana Costa",
  "crm": "CRM12345",
  "area": "Cardiologia"
}
```

Listar profissionais:

```http
GET /profissionais
```

Buscar profissional:

```http
GET /profissionais/{id}
```

Inativar profissional:

```http
DELETE /profissionais/{id}
```

### Agendamentos

Criar agendamento:

```http
POST /agendamentos
```

```json
{
  "pacienteId": 1,
  "profissionalId": 1,
  "dataHora": "2026-07-10T14:00:00"
}
```

Listar agendamentos:

```http
GET /agendamentos
```

Filtros opcionais:

```http
GET /agendamentos?pacienteId=1&profissionalId=1&status=AGENDADO
```

Buscar agendamento:

```http
GET /agendamentos/{id}
```

Cancelar agendamento:

```http
PATCH /agendamentos/{id}/cancelar
```

```json
{
  "motivo": "Paciente solicitou cancelamento."
}
```

## Regras de negócio

- Um profissional não pode ter dois agendamentos ativos no mesmo horário.
- Um paciente não pode ter dois agendamentos ativos no mesmo horário.
- Não é permitido criar agendamento para data/hora passada.
- Todo novo agendamento começa com status `AGENDADO`.
- Cancelamentos registram motivo e mudam o status para `CANCELADO`.
- Agendamentos cancelados ou realizados são mantidos como histórico.
- A listagem de agendamentos permite filtro por paciente, profissional ou status.
- Pacientes e profissionais são inativados logicamente, sem remoção física.
- Pacientes ou profissionais inativos não podem receber novos agendamentos.

## Decisões técnicas

As decisões do projeto estão documentadas em [DECISOES.md](DECISOES.md).

## Testes

O projeto segue uma abordagem orientada a testes nas regras de negócio e nos controllers.

Coberturas principais:

- services de paciente, profissional e agendamento;
- regras de conflito de horário;
- cancelamento com motivo;
- filtros de agendamento;
- inativação lógica;
- controllers REST;
- validação de payloads inválidos;
- respostas de erro da API.
