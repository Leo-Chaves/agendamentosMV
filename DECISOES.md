# Decisões Técnicas  

## 1. Desenvolvimento orientado a testes

O desenvolvimento foi conduzido com foco em TDD, principalmente nas regras de negócio da camada de `service`.

Antes de evoluir novas regras, foram criados testes automatizados para validar os principais comportamentos esperados do sistema, como:

- impedir agendamentos em data/hora passada;
- impedir conflito de horário para profissional;
- impedir conflito de horário para paciente;
- validar sobreposição de horários considerando duração padrão de 30 minutos;
- cancelar agendamento registrando motivo;
- manter o registro do agendamento após cancelamento;
- filtrar agendamentos por paciente, profissional e status;
- validar compatibilidade entre tipo de atendimento e área profissional;
- marcar agendamentos como realizados somente quando estiverem com status `AGENDADO`.

A escolha por TDD ajudou a garantir que as regras obrigatórias do teste fossem implementadas com mais segurança, além de facilitar alterações durante o desenvolvimento.

---

Obs.: Também foi criado o arquivo `DIARIO.md`, contendo as decisões tomadas durante o desenvolvimento de forma cronológica e mais natural.

## 2. Principais decisões técnicas

Foi utilizado Java com Spring Boot para construção da API REST, seguindo uma organização simples em camadas.

A estrutura principal do backend foi dividida em:

- `controller`: recebe as requisições HTTP;
- `service`: concentra as regras de negócio;
- `repository`: realiza o acesso ao banco de dados;
- `entity`: representa as entidades persistidas;
- `dto`: representa os dados de entrada e saída da API;
- `exception`: concentra o tratamento de erros.

As regras de negócio foram concentradas na camada de `service`, evitando que os controllers fiquem responsáveis por validações ou decisões do domínio.

Também foram utilizados DTOs de request e response para evitar a exposição direta das entidades JPA na API.

Essa decisão foi tomada para:

- controlar melhor quais dados entram e saem pela API;
- reduzir acoplamento entre contrato HTTP e modelo de persistência;
- facilitar validações com Bean Validation;
- evitar problemas de serialização JSON em relacionamentos entre entidades.

---

## 3. Banco de dados

Para o desenvolvimento inicial, foi escolhido o banco H2 em memória.

Essa escolha foi feita porque o H2:

- facilita a execução local do projeto;
- não exige instalação obrigatória de Docker ou banco externo;
- acelera os testes durante o desenvolvimento;
- combina bem com Spring Boot e Spring Data JPA;
- facilita a avaliação inicial do projeto.

Além disso, foi adicionada uma configuração opcional para Oracle, utilizando:

- `docker-compose.yml` com Oracle Free;
- profile Spring `oracle`;
- driver JDBC `ojdbc11`.

O H2 permanece como banco padrão para facilitar execução, testes e avaliação local. A configuração com Oracle foi adicionada como diferencial técnico e para demonstrar compatibilidade com outro banco relacional.

---

## 4. Modelagem das entidades

As principais entidades do domínio são:

- `Paciente`;
- `Profissional`;
- `Agendamento`.

Um paciente pode possuir vários agendamentos.

Um profissional pode possuir vários agendamentos.

Cada agendamento pertence a um único paciente e a um único profissional.

Foram adicionados identificadores únicos para evitar cadastros duplicados:

- `cpf` para pacientes;
- `crm` para profissionais.

Esses campos são obrigatórios e únicos no banco de dados.

---

## 5. Data de nascimento em vez de idade

O paciente armazena `dataNascimento` em vez de armazenar a idade diretamente.

Essa decisão foi tomada porque a idade muda com o tempo e poderia ficar inconsistente no banco de dados.

A data de nascimento é um dado mais estável, e a idade pode ser calculada quando necessário na resposta da API ou exibida no frontend.

---

## 6. Enums utilizados

Alguns campos foram modelados como enums para evitar variações de digitação e padronizar os dados gravados no banco.

Foram criados enums para:

- sexo;
- área profissional;
- tipo de atendimento;
- status do agendamento.

Valores de `Sexo`:

- `MASCULINO`;
- `FEMININO`;
- `OUTRO`;
- `NAO_INFORMADO`.

Valores de `AreaProfissional`:

- `CLINICO_GERAL`;
- `CARDIOLOGIA`;
- `ORTOPEDIA`;
- `PSICOLOGIA`;
- `PEDIATRIA`;
- `FISIOTERAPIA`;
- `BIOMEDICINA`;
- `ENFERMAGEM`.

Valores de `TipoAtendimento`:

- `CONSULTA`;
- `RETORNO`;
- `EXAME`;
- `AVALIACAO`.

Valores de `StatusAgendamento`:

- `AGENDADO`;
- `CANCELADO`;
- `REALIZADO`.

O uso de enums facilita a validação no backend, melhora a consistência dos dados e permite que o frontend trabalhe com listas controladas de opções.

---

## 7. Regras de agendamento

O agendamento deve ser criado a partir de:

- um paciente existente;
- um profissional existente;
- uma data e hora;
- um tipo de atendimento.

As principais regras implementadas foram:

- não permitir agendamento em data/hora passada;
- todo agendamento deve informar um tipo de atendimento;
- todo novo agendamento começa com status `AGENDADO`;
- um profissional não pode ter dois agendamentos ativos no mesmo horário;
- um paciente não pode ter dois agendamentos ativos no mesmo horário;
- o cancelamento deve registrar motivo;
- ao cancelar, o status muda para `CANCELADO`;
- o registro do agendamento é mantido após cancelamento;
- a listagem permite filtro por paciente, profissional ou status.

Além das regras obrigatórias, também foi adicionada validação por sobreposição de horário.

Cada agendamento possui duração padrão de 30 minutos.

Com isso, o sistema não valida apenas se dois agendamentos começam exatamente no mesmo horário. Ele também verifica se os intervalos de tempo se cruzam.

Exemplo com duração de 30 minutos:

- um agendamento das 10:00 às 10:30 bloqueia outro às 10:15;
- um agendamento das 09:45 às 10:15 bloqueia outro às 10:00;
- um agendamento das 10:00 às 10:30 não bloqueia outro às 10:30.

As validações de conflito consideram apenas agendamentos com status `AGENDADO`.

Agendamentos com status `CANCELADO` ou `REALIZADO` são tratados como histórico e não bloqueiam novos agendamentos no mesmo intervalo.

A duração padrão pode ser alterada pela propriedade:

```properties
agendamento.duracao-minutos=30
```

## Uso de IA

A IA foi utilizada como ferramenta de apoio durante o desenvolvimento do projeto.

Ela auxiliou principalmente em:

- revisão de decisões técnicas;
- sugestão de melhorias na organização do código;
- apoio na escrita inicial de alguns testes automatizados;
- apoio na criação inicial da interface frontend;
- revisão de textos de documentação.

As principais decisões de arquitetura, modelagem das entidades e regras de negócio foram definidas por mim.

Todo código gerado ou sugerido com auxílio de IA foi revisado, ajustado e validado manualmente.

A validação foi feita por meio de:

- execução local da aplicação;
- 56 testes automatizados;
- testes manuais no frontend;
- testes manuais via Postman;
- revisão das regras obrigatórias do enunciado.

A IA foi utilizada como assistente de desenvolvimento, não como substituta da validação técnica.