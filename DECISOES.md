# Decisões Técnicas

## Banco de dados

Para o desenvolvimento inicial, foi escolhido o banco H2 em memória.

Motivos:

- facilita a execução local do projeto;
- não exige instalação de Docker ou PostgreSQL no primeiro momento;
- acelera os testes durante o desenvolvimento;
- combina bem com Spring Boot e Spring Data JPA.

PostgreSQL com Docker pode ser adicionado posteriormente como melhoria, caso o escopo do teste exija ou se houver tempo.

## Perfil de administrador e autenticação

JWT e perfil não serão implementados no início.

Motivo:

- o foco principal do projeto é o fluxo de agendamento;
- não identifiquei como requisito explícito no enunciado.

## Estrutura de pacotes

O projeto segue uma organização simples em camadas:

- `controller`: recebe as requisições HTTP;
- `service`: concentra as regras de negócio;
- `repository`: acessa o banco de dados;
- `entity`: representa as entidades persistidas;
- `dto`: representa dados de entrada e saída da API;
- `exception`: concentra tratamento de erros.

## DTOs e Controllers

A API REST não expõe as entidades JPA diretamente.

Foram criados DTOs de request e response para:

- controlar quais campos entram e saem pela API;
- evitar acoplamento direto entre contrato HTTP e modelo de persistência;
- facilitar validações com Bean Validation;
- reduzir risco de problemas com relacionamentos bidirecionais na serialização JSON.

Os controllers delegam regras de negócio para os services e ficam responsáveis apenas por:

- receber requisições HTTP;
- validar payloads de entrada;
- converter DTOs em entidades ou parâmetros de service;
- devolver responses com status HTTP adequado.

Erros de regra de negócio lançados como `IllegalArgumentException` são tratados por um `RestControllerAdvice`, retornando `400 Bad Request` com mensagem simples.

## Entidades principais

As entidades iniciais do domínio são:

- `Paciente`;
- `Profissional`;
- `Agendamento`.

## Identificadores únicos

Foram adicionados identificadores únicos para evitar cadastros duplicados das entidades principais.

O `Paciente` possui `cpf`.

O `Profissional` possui `crm`.

Esses campos são obrigatórios e únicos no banco de dados.

## Relacionamentos

Um paciente pode ter vários agendamentos.

Um profissional pode ter vários agendamentos.

Cada agendamento pertence a um único paciente e a um único profissional.

## Status do agendamento

O status do agendamento foi modelado com o enum `StatusAgendamento`.

Valores iniciais:

- `AGENDADO`;
- `CANCELADO`;
- `REALIZADO`.

## Desenvolvimento orientado a testes

O projeto seguirá uma abordagem orientada a testes (TDD) nas regras de negócio.

O auxílio de IA será usado para apoiar a escrita dos testes, revisar decisões técnicas e sugerir melhorias.

## Cobertura inicial de testes

Os primeiros testes unitários foram criados para a camada de `service`, usando mocks dos repositories.

O `PacienteServiceTest` cobre:

- salvar paciente;
- listar pacientes;
- buscar paciente por id;
- lançar erro quando o paciente não for encontrado.

O `ProfissionalServiceTest` cobre:

- salvar profissional;
- listar profissionais;
- buscar profissional por id;
- lançar erro quando o profissional não for encontrado.

O `AgendamentoServiceTest` cobre:

- criar agendamento quando o horário estiver disponível;
- impedir agendamento em data/hora passada;
- impedir agendamento quando o paciente já tiver agendamento ativo no mesmo horário;
- impedir agendamento quando o profissional já tiver agendamento ativo no mesmo horário;
- listar agendamentos;
- listar agendamentos com filtros por paciente, profissional ou status;
- buscar agendamento por id;
- lançar erro quando o agendamento não for encontrado;
- cancelar agendamento registrando motivo;
- impedir cancelamento sem motivo.

## Ideia

Percebi que seria interessante algumas partes do sistema terem identificadores únicos além do ID interno. Por isso, escolhi CPF para pacientes e CRM para profissionais.

## Regras de agendamento

O agendamento deve ser criado a partir de um paciente existente, um profissional existente e uma data/hora.

Regras iniciais:

- não é permitido criar agendamento para data/hora passada;
- um paciente não pode ter dois agendamentos ativos no mesmo horário;
- um profissional não pode ter dois agendamentos ativos no mesmo horário;
- todo novo agendamento começa com status `AGENDADO`;
- agendamentos cancelados recebem o status `CANCELADO`;
- todo cancelamento deve registrar um motivo;
- o registro do agendamento deve ser mantido após o cancelamento;
- a listagem de agendamentos deve permitir filtros opcionais por paciente, profissional ou status.

As validações de conflito consideram apenas agendamentos com status `AGENDADO`.

Agendamentos com status `CANCELADO` ou `REALIZADO` são tratados como histórico e não vão bloquear novos agendamentos no mesmo horário.
