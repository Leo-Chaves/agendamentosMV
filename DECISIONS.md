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

## Entidades principais

As entidades iniciais do domínio são:

- `Paciente`;
- `Profissional`;
- `Agendamento`.

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

O projeto seguirá uma abordagem orientada a testes(TDD) nas regras de negócio.

O auxílio de IA será usado para a escrita de testes 
