# Decisões Técnicas

## Banco de dados

Para o desenvolvimento inicial, foi escolhido o banco H2 em memória.

Motivos:

- facilita a execução local do projeto;
- não exige instalação de Docker ou PostgreSQL no primeiro momento;
- acelera os testes durante o desenvolvimento;
- combina bem com Spring Boot e Spring Data JPA.

## Compatibilidade com Oracle

O H2 permanece como banco padrão para facilitar execução, testes e avaliação local.

Foi adicionada uma configuração opcional para Oracle usando:

- `docker-compose.yml` com Oracle Free;
- profile Spring `oracle`;
- driver JDBC `ojdbc11`.

## Dados iniciais

Foi criado um carregamento inicial de dados com `CommandLineRunner`.

Ele cadastra pacientes, profissionais e agendamentos de exemplo quando o banco está vazio.

Motivos:

- facilitar testes manuais no frontend e no Postman;
- permitir que a tela abra com dados para navegação;
- evitar duplicidade em bancos persistentes, como Oracle, verificando se já existem registros antes de inserir;
- manter compatibilidade entre H2 e Oracle sem depender de diferenças de sintaxe SQL.

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

A API REST não pode expor as entidades JPA diretamente.

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

## Interface frontend

Foi adicionada uma interface simples em Vue para consumir a API REST.

Motivos:

- atender ao diferencial de interface para consumo da API;
- facilitar validação manual dos fluxos principais;
- manter o frontend separado do backend, com proxy de desenvolvimento via Vite;
- evitar acoplamento entre a aplicação Java e a camada de apresentação.

A interface cobre cadastro, edição, listagem, filtros, cancelamento, inativação e ativação.

Auxilio de IA para criação das telas 

## Pesquisa inteligente no frontend

Foram adicionados filtros locais no frontend para melhorar a usabilidade das listagens.

A busca considera textos relacionados ao registro, sem exigir que o usuário escolha exatamente a coluna.

Filtros disponíveis:

- agendamentos: busca por paciente, profissional, status, data, motivo ou área profissional;
- pacientes: busca por nome, CPF, sexo ou status;
- profissionais: busca por nome, CRM, área ou status;
- profissionais e agendamentos também possuem filtro por área profissional.

O filtro por tipo de atendimento não foi implementado porque esse campo ainda não existe no modelo do sistema. Caso o domínio passe a armazenar esse dado, ele pode ser incluído na mesma estratégia de busca.

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

## Data de nascimento do paciente

O paciente armazena `dataNascimento` em vez de armazenar `idade` diretamente.

Motivos:

- idade muda com o tempo e poderia ficar inconsistente no banco;
- data de nascimento é um dado estável do cadastro;
- a idade pode ser calculada na resposta da API e exibida no frontend quando necessário.

## Enums para sexo e área profissional

Os campos `sexo` e `area` foram modelados como enums.

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
- `FISIOTERAPIA`.

Motivos:

- evitar variações de digitação;
- padronizar os dados gravados no banco;
- facilitar validação no backend e seleção por lista no frontend.

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

Agendamentos não mudam automaticamente para `REALIZADO` apenas porque a data/hora passou.

A conclusão do atendimento é uma ação explícita do usuário, pois um horário passado pode representar atendimento realizado, falta do paciente ou falha operacional.

Somente agendamentos com status `AGENDADO` podem ser marcados como `REALIZADO`.

## Desenvolvimento orientado a testes

O projeto seguirá uma abordagem orientada a testes (TDD) nas regras de negócio.

O auxílio de IA será usado para apoiar a escrita dos testes, revisar decisões técnicas e sugerir melhorias, mas a arquitetura do monolito e deicões tcnicsas serão criadas/tomadas pro mim!

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
- impedir agendamento quando o paciente já tiver agendamento ativo no mesmo horário ou em horário sobreposto;
- impedir agendamento quando o profissional já tiver agendamento ativo no mesmo horário ou em horário sobreposto;
- listar agendamentos;
- listar agendamentos com filtros por paciente, profissional ou status;
- buscar agendamento por id;
- lançar erro quando o agendamento não for encontrado;
- cancelar agendamento registrando motivo;
- impedir cancelamento sem motivo;
- marcar agendamento como realizado;
- impedir marcar como realizado um agendamento que não esteja `AGENDADO`.

## Ideia

Percebi que seria interessante algumas partes do sistema terem identificadores únicos além do ID interno. Por isso, escolhi CPF para pacientes e CRM para profissionais.

## Regras de agendamento

O agendamento deve ser criado a partir de um paciente existente, um profissional existente e uma data/hora.

Regras iniciais:

- não é permitido criar agendamento para data/hora passada;
- um paciente não pode ter dois agendamentos ativos no mesmo horário;
- um profissional não pode ter dois agendamentos ativos no mesmo horário;
- cada agendamento possui duração padrão de 30 minutos;
- conflitos são validados por sobreposição de intervalo de tempo, não apenas por horário inicial igual;
- a duração do agendamento pode ser alterada pela propriedade `agendamento.duracao-minutos`;
- todo novo agendamento começa com status `AGENDADO`;
- agendamentos cancelados recebem o status `CANCELADO`;
- todo cancelamento deve registrar um motivo;
- agendamentos agendados podem ser marcados como `REALIZADO`;
- agendamentos cancelados não podem ser marcados como realizados;
- o registro do agendamento deve ser mantido após o cancelamento;
- a listagem de agendamentos deve permitir filtros opcionais por paciente, profissional ou status.

As validações de conflito consideram apenas agendamentos com status `AGENDADO`.

A janela de conflito considera o horário inicial do novo agendamento e a duração configurada.

Exemplo com duração de 30 minutos:

- um agendamento das 10:00 às 10:30 bloqueia outro às 10:15;
- um agendamento das 09:45 às 10:15 bloqueia outro às 10:00;
- um agendamento das 10:00 às 10:30 não bloqueia outro às 10:30.

Agendamentos com status `CANCELADO` ou `REALIZADO` são tratados como histórico e não bloqueiam novos agendamentos no mesmo intervalo.

## Ativação e inativação de pacientes e profissionais

Pacientes e profissionais não são removidos fisicamente pelo sistema.

O endpoint de exclusão faz uma inativação lógica, alterando o campo `ativo` para `false`.

Motivos:

- preservar o histórico de agendamentos;
- evitar perda de rastreabilidade;
- impedir inconsistência em registros antigos;
- permitir que os dados continuem disponíveis para consulta.

Pacientes ou profissionais inativos não podem receber novos agendamentos.

Também foi adicionado endpoint para ativar novamente pacientes e profissionais inativos, alterando o campo `ativo` para `true`.

## Edição de pacientes e profissionais

Pacientes e profissionais podem ter seus dados cadastrais atualizados por endpoints `PUT`.

A edição altera apenas os dados principais do cadastro e preserva o status atual do registro.

Motivos:

- corrigir dados digitados incorretamente;
- manter o histórico de agendamentos ligado ao mesmo registro;
- evitar exclusão e recriação de cadastros para ajustes simples.

## Percebi que não tinha um tempo de agendamento, então era possivel marcar agendamentos seguidos

- A partirde agora cada agendamento tem um tempo minimo, os sistema entende que cada agendamento dura em torno de 30 minutos e bloqueia o proficional e usuario 

- Optei por ser um atributo do sistema, ou seja para alterar so ajustando o codigo. Achei melhor fazer dessa formar ao inves de agendamentos terem tempo diferente(menos poluição no front), mas caso fosse um sistema para multi-empresas podesse ser melhor 

## Achei melhor armazenar a data de nascimento do que a apenas a idade 

## Não irei colocar validador de CPF para avaliadores testaerem de forma mais pratica 


