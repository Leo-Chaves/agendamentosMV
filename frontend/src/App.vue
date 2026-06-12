<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  Ban,
  CalendarCheck,
  CalendarClock,
  CheckCircle2,
  Filter,
  LoaderCircle,
  Pencil,
  Plus,
  RefreshCw,
  Save,
  Search,
  Stethoscope,
  UserRound,
  XCircle,
} from '@lucide/vue'

const tabs = [
  { key: 'agendamentos', label: 'Agendamentos', icon: CalendarClock },
  { key: 'pacientes', label: 'Pacientes', icon: UserRound },
  { key: 'profissionais', label: 'Profissionais', icon: Stethoscope },
]

const sexos = [
  { value: 'MASCULINO', label: 'Masculino' },
  { value: 'FEMININO', label: 'Feminino' },
  { value: 'OUTRO', label: 'Outro' },
  { value: 'NAO_INFORMADO', label: 'Não informado' },
]

const areasProfissionais = [
  { value: 'CLINICO_GERAL', label: 'Clínico geral' },
  { value: 'CARDIOLOGIA', label: 'Cardiologia' },
  { value: 'ORTOPEDIA', label: 'Ortopedia' },
  { value: 'PSICOLOGIA', label: 'Psicologia' },
  { value: 'PEDIATRIA', label: 'Pediatria' },
  { value: 'FISIOTERAPIA', label: 'Fisioterapia' },
]

const activeTab = ref('agendamentos')
const loading = ref(false)
const notice = ref('')
const error = ref('')

const pacientes = ref([])
const profissionais = ref([])
const agendamentos = ref([])

const pacienteForm = reactive({
  nome: '',
  cpf: '',
  dataNascimento: '',
  sexo: '',
  endereco: '',
})

const profissionalForm = reactive({
  nome: '',
  crm: '',
  area: '',
})

const pacienteEmEdicaoId = ref(null)
const profissionalEmEdicaoId = ref(null)
const pacienteModalAberto = ref(false)
const profissionalModalAberto = ref(false)

const agendamentoForm = reactive({
  pacienteId: '',
  profissionalId: '',
  dataHora: '',
})

const agendamentoModalAberto = ref(false)
const cancelamentoModalAberto = ref(false)

const filtros = reactive({
  pacienteId: '',
  profissionalId: '',
  status: '',
  texto: '',
  areaProfissional: '',
  periodo: '',
})

const filtrosPacientes = reactive({
  texto: '',
  status: '',
})

const filtrosProfissionais = reactive({
  texto: '',
  area: '',
  status: '',
})

const cancelamento = reactive({
  agendamentoId: '',
  motivo: '',
})

const pacientesAtivos = computed(() => pacientes.value.filter((paciente) => paciente.ativo))
const profissionaisAtivos = computed(() => profissionais.value.filter((profissional) => profissional.ativo))

const pacientesFiltrados = computed(() => {
  const texto = normalizar(filtrosPacientes.texto)

  return pacientes.value.filter((paciente) => {
    const correspondeAoTexto =
      !texto ||
      [
        paciente.nome,
        paciente.cpf,
        paciente.idade,
        rotuloSexo(paciente.sexo),
        paciente.ativo ? 'ativo' : 'inativo',
      ].some((valor) => normalizar(valor).includes(texto))

    const correspondeAoStatus =
      !filtrosPacientes.status ||
      (filtrosPacientes.status === 'ATIVO' ? paciente.ativo : !paciente.ativo)

    return correspondeAoTexto && correspondeAoStatus
  })
})

const profissionaisFiltrados = computed(() => {
  const texto = normalizar(filtrosProfissionais.texto)

  return profissionais.value.filter((profissional) => {
    const correspondeAoTexto =
      !texto ||
      [
        profissional.nome,
        profissional.crm,
        profissional.area,
        rotuloArea(profissional.area),
        profissional.ativo ? 'ativo' : 'inativo',
      ].some((valor) => normalizar(valor).includes(texto))

    const correspondeAArea = !filtrosProfissionais.area || profissional.area === filtrosProfissionais.area
    const correspondeAoStatus =
      !filtrosProfissionais.status ||
      (filtrosProfissionais.status === 'ATIVO' ? profissional.ativo : !profissional.ativo)

    return correspondeAoTexto && correspondeAArea && correspondeAoStatus
  })
})

const agendamentosFiltrados = computed(() => {
  const texto = normalizar(filtros.texto)

  return agendamentos.value.filter((agendamento) => {
    const profissional = profissionalPorId(agendamento.profissionalId)
    const areaProfissional = profissional?.area || ''

    const correspondeAoTexto =
      !texto ||
      [
        agendamento.id,
        agendamento.pacienteNome,
        agendamento.profissionalNome,
        agendamento.status,
        agendamento.motivoCancelamento,
        formatarData(agendamento.dataHora),
        areaProfissional,
        rotuloArea(areaProfissional),
      ].some((valor) => normalizar(valor).includes(texto))

    const correspondeAArea = !filtros.areaProfissional || areaProfissional === filtros.areaProfissional
    const correspondeAoPeriodo = filtrarPorPeriodo(agendamento.dataHora, filtros.periodo)

    return correspondeAoTexto && correspondeAArea && correspondeAoPeriodo
  })
})

async function api(path, options = {}) {
  const response = await fetch(`/api${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
    ...options,
  })

  const text = await response.text()
  const data = text ? JSON.parse(text) : null

  if (!response.ok) {
    throw new Error(data?.mensagem || 'Não foi possível completar a operação.')
  }

  return data
}

async function run(action, successMessage) {
  loading.value = true
  error.value = ''
  notice.value = ''

  try {
    await action()
    if (successMessage) {
      notice.value = successMessage
    }
  } catch (exception) {
    error.value = exception.message
  } finally {
    loading.value = false
  }
}

async function carregarDados() {
  await run(async () => {
    const [pacientesData, profissionaisData, agendamentosData] = await Promise.all([
      api('/pacientes'),
      api('/profissionais'),
      api('/agendamentos'),
    ])

    pacientes.value = pacientesData
    profissionais.value = profissionaisData
    agendamentos.value = agendamentosData
  }, '')
}

async function salvarPaciente() {
  const editando = Boolean(pacienteEmEdicaoId.value)
  await run(async () => {
    await api(editando ? `/pacientes/${pacienteEmEdicaoId.value}` : '/pacientes', {
      method: editando ? 'PUT' : 'POST',
      body: JSON.stringify(pacienteForm),
    })
    fecharModalPaciente()
    await carregarDadosSilencioso()
  }, editando ? 'Paciente atualizado.' : 'Paciente cadastrado.')
}

async function salvarProfissional() {
  const editando = Boolean(profissionalEmEdicaoId.value)
  await run(async () => {
    await api(editando ? `/profissionais/${profissionalEmEdicaoId.value}` : '/profissionais', {
      method: editando ? 'PUT' : 'POST',
      body: JSON.stringify(profissionalForm),
    })
    fecharModalProfissional()
    await carregarDadosSilencioso()
  }, editando ? 'Profissional atualizado.' : 'Profissional cadastrado.')
}

function abrirModalPaciente() {
  limparFormularioPaciente()
  pacienteModalAberto.value = true
}

function editarPaciente(paciente) {
  pacienteEmEdicaoId.value = paciente.id
  Object.assign(pacienteForm, {
    nome: paciente.nome,
    cpf: paciente.cpf,
    dataNascimento: paciente.dataNascimento,
    sexo: paciente.sexo,
    endereco: paciente.endereco,
  })
  pacienteModalAberto.value = true
}

function limparFormularioPaciente() {
  pacienteEmEdicaoId.value = null
  Object.assign(pacienteForm, { nome: '', cpf: '', dataNascimento: '', sexo: '', endereco: '' })
}

function fecharModalPaciente() {
  pacienteModalAberto.value = false
  limparFormularioPaciente()
}

function abrirModalProfissional() {
  limparFormularioProfissional()
  profissionalModalAberto.value = true
}

function editarProfissional(profissional) {
  profissionalEmEdicaoId.value = profissional.id
  Object.assign(profissionalForm, {
    nome: profissional.nome,
    crm: profissional.crm,
    area: profissional.area,
  })
  profissionalModalAberto.value = true
}

function limparFormularioProfissional() {
  profissionalEmEdicaoId.value = null
  Object.assign(profissionalForm, { nome: '', crm: '', area: '' })
}

function fecharModalProfissional() {
  profissionalModalAberto.value = false
  limparFormularioProfissional()
}

async function salvarAgendamento() {
  await run(async () => {
    await api('/agendamentos', {
      method: 'POST',
      body: JSON.stringify({
        pacienteId: Number(agendamentoForm.pacienteId),
        profissionalId: Number(agendamentoForm.profissionalId),
        dataHora: agendamentoForm.dataHora,
      }),
    })
    fecharModalAgendamento()
    await buscarAgendamentos()
  }, 'Agendamento criado.')
}

function abrirModalAgendamento() {
  Object.assign(agendamentoForm, { pacienteId: '', profissionalId: '', dataHora: '' })
  agendamentoModalAberto.value = true
}

function fecharModalAgendamento() {
  agendamentoModalAberto.value = false
  Object.assign(agendamentoForm, { pacienteId: '', profissionalId: '', dataHora: '' })
}

async function buscarAgendamentos() {
  const params = new URLSearchParams()
  if (filtros.pacienteId) params.append('pacienteId', filtros.pacienteId)
  if (filtros.profissionalId) params.append('profissionalId', filtros.profissionalId)
  if (filtros.status) params.append('status', filtros.status)

  const query = params.toString()
  agendamentos.value = await api(`/agendamentos${query ? `?${query}` : ''}`)
}

async function filtrarAgendamentos() {
  await run(buscarAgendamentos, 'Agendamentos filtrados.')
}

async function limparFiltros() {
  Object.assign(filtros, {
    pacienteId: '',
    profissionalId: '',
    status: '',
    texto: '',
    areaProfissional: '',
    periodo: '',
  })
  await run(buscarAgendamentos, 'Filtros limpos.')
}

function limparFiltrosPacientes() {
  Object.assign(filtrosPacientes, { texto: '', status: '' })
}

function limparFiltrosProfissionais() {
  Object.assign(filtrosProfissionais, { texto: '', area: '', status: '' })
}

async function cancelarAgendamento() {
  await run(async () => {
    await api(`/agendamentos/${cancelamento.agendamentoId}/cancelar`, {
      method: 'PATCH',
      body: JSON.stringify({ motivo: cancelamento.motivo }),
    })
    fecharModalCancelamento()
    await buscarAgendamentos()
  }, 'Agendamento cancelado.')
}

function abrirModalCancelamento(agendamento) {
  Object.assign(cancelamento, { agendamentoId: agendamento.id, motivo: '' })
  cancelamentoModalAberto.value = true
}

function fecharModalCancelamento() {
  cancelamentoModalAberto.value = false
  Object.assign(cancelamento, { agendamentoId: '', motivo: '' })
}

async function realizarAgendamento(id) {
  await run(async () => {
    await api(`/agendamentos/${id}/realizar`, { method: 'PATCH' })
    await buscarAgendamentos()
  }, 'Agendamento marcado como realizado.')
}

async function inativarPaciente(id) {
  await run(async () => {
    await api(`/pacientes/${id}`, { method: 'DELETE' })
    await carregarDadosSilencioso()
  }, 'Paciente inativado.')
}

async function ativarPaciente(id) {
  await run(async () => {
    await api(`/pacientes/${id}/ativar`, { method: 'PATCH' })
    await carregarDadosSilencioso()
  }, 'Paciente ativado.')
}

async function inativarProfissional(id) {
  await run(async () => {
    await api(`/profissionais/${id}`, { method: 'DELETE' })
    await carregarDadosSilencioso()
  }, 'Profissional inativado.')
}

async function ativarProfissional(id) {
  await run(async () => {
    await api(`/profissionais/${id}/ativar`, { method: 'PATCH' })
    await carregarDadosSilencioso()
  }, 'Profissional ativado.')
}

async function carregarDadosSilencioso() {
  const [pacientesData, profissionaisData, agendamentosData] = await Promise.all([
    api('/pacientes'),
    api('/profissionais'),
    api('/agendamentos'),
  ])

  pacientes.value = pacientesData
  profissionais.value = profissionaisData
  agendamentos.value = agendamentosData
}

function formatarData(dataHora) {
  if (!dataHora) return ''
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(dataHora))
}

function rotuloSexo(valor) {
  return sexos.find((sexo) => sexo.value === valor)?.label || valor
}

function rotuloArea(valor) {
  return areasProfissionais.find((area) => area.value === valor)?.label || valor
}

function normalizar(valor) {
  return String(valor ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim()
}

function profissionalPorId(id) {
  return profissionais.value.find((profissional) => profissional.id === id)
}

function filtrarPorPeriodo(dataHora, periodo) {
  if (!periodo) return true

  const dataAgendamento = new Date(dataHora)
  const hoje = new Date()

  if (periodo === 'HOJE') {
    return mesmaData(dataAgendamento, hoje)
  }

  if (periodo === 'PROXIMOS_7_DIAS') {
    const inicio = inicioDoDia(hoje)
    const fim = inicioDoDia(hoje)
    fim.setDate(fim.getDate() + 7)
    return dataAgendamento >= inicio && dataAgendamento < fim
  }

  return true
}

function mesmaData(primeiraData, segundaData) {
  return primeiraData.getFullYear() === segundaData.getFullYear()
    && primeiraData.getMonth() === segundaData.getMonth()
    && primeiraData.getDate() === segundaData.getDate()
}

function inicioDoDia(data) {
  return new Date(data.getFullYear(), data.getMonth(), data.getDate())
}

onMounted(carregarDados)
</script>

<template>
  <main class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark" aria-hidden="true">MV</div>
        <div>
          <strong>Agendamentos MV</strong>
          <span>Gestão de atendimentos</span>
        </div>
      </div>

      <nav class="tabs" aria-label="Navegação principal">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-button"
          :class="{ active: activeTab === tab.key }"
          type="button"
          @click="activeTab = tab.key"
        >
          <component :is="tab.icon" :size="18" aria-hidden="true" />
          <span>{{ tab.label }}</span>
        </button>
      </nav>

      <button class="ghost-button" type="button" :disabled="loading" @click="carregarDados">
        <LoaderCircle v-if="loading" :size="18" class="spin" aria-hidden="true" />
        <RefreshCw v-else :size="18" aria-hidden="true" />
        <span>Atualizar</span>
      </button>
    </aside>

    <section class="workspace">
      <header class="topbar">
        <div>
          <p class="eyebrow">Rotina de atendimento</p>
          <h1>{{ tabs.find((tab) => tab.key === activeTab)?.label }}</h1>
        </div>
        <div class="status-strip">
          <span>{{ pacientesAtivos.length }} pacientes ativos</span>
          <span>{{ profissionaisAtivos.length }} profissionais ativos</span>
          <span>{{ agendamentos.length }} agendamentos</span>
        </div>
      </header>

      <p v-if="notice" class="alert success">{{ notice }}</p>
      <p v-if="error" class="alert danger">{{ error }}</p>

      <section v-if="activeTab === 'agendamentos'" class="content-grid appointments-grid">
        <form class="panel appointment-filter-panel span-all" @submit.prevent="filtrarAgendamentos">
          <div class="filter-header">
            <h2>Filtros</h2>
            <button class="primary-button" type="button" :disabled="loading" @click="abrirModalAgendamento">
              <Plus :size="18" aria-hidden="true" />
              <span>Novo agendamento</span>
            </button>
          </div>
          <label>
            <span>Busca</span>
            <input
              v-model="filtros.texto"
              placeholder="Paciente, profissional, status ou área"
              type="search"
            />
          </label>
          <label>
            <span>Paciente</span>
            <select v-model="filtros.pacienteId">
              <option value="">Todos</option>
              <option v-for="paciente in pacientes" :key="paciente.id" :value="paciente.id">
                {{ paciente.nome }}
              </option>
            </select>
          </label>
          <label>
            <span>Profissional</span>
            <select v-model="filtros.profissionalId">
              <option value="">Todos</option>
              <option v-for="profissional in profissionais" :key="profissional.id" :value="profissional.id">
                {{ profissional.nome }}
              </option>
            </select>
          </label>
          <label>
            <span>Status</span>
            <select v-model="filtros.status">
              <option value="">Todos</option>
              <option value="AGENDADO">AGENDADO</option>
              <option value="CANCELADO">CANCELADO</option>
              <option value="REALIZADO">REALIZADO</option>
            </select>
          </label>
          <label>
            <span>Período</span>
            <select v-model="filtros.periodo">
              <option value="">Todos</option>
              <option value="HOJE">Hoje</option>
              <option value="PROXIMOS_7_DIAS">Próximos 7 dias</option>
            </select>
          </label>
          <label>
            <span>Área profissional</span>
            <select v-model="filtros.areaProfissional">
              <option value="">Todas</option>
              <option v-for="area in areasProfissionais" :key="area.value" :value="area.value">
                {{ area.label }}
              </option>
            </select>
          </label>
          <div class="filter-actions">
            <button class="secondary-button" type="submit" :disabled="loading">
              <Filter :size="18" aria-hidden="true" />
              <span>Filtrar</span>
            </button>
            <button class="icon-button" type="button" title="Limpar filtros" @click="limparFiltros">
              <XCircle :size="18" aria-hidden="true" />
            </button>
          </div>
        </form>

        <section class="panel table-panel span-all">
          <div class="section-heading">
            <h2>Agenda</h2>
            <CalendarCheck :size="20" aria-hidden="true" />
          </div>
          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Data</th>
                  <th>Paciente</th>
                  <th>Profissional</th>
                  <th>Status</th>
                  <th>Motivo</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="agendamento in agendamentosFiltrados" :key="agendamento.id">
                  <td>#{{ agendamento.id }}</td>
                  <td>{{ formatarData(agendamento.dataHora) }}</td>
                  <td>{{ agendamento.pacienteNome }}</td>
                  <td>{{ agendamento.profissionalNome }}</td>
                  <td>
                    <span class="badge" :class="agendamento.status.toLowerCase()">
                      {{ agendamento.status }}
                    </span>
                  </td>
                  <td>{{ agendamento.motivoCancelamento || '-' }}</td>
                  <td class="actions">
                    <div v-if="agendamento.status === 'AGENDADO'" class="action-buttons">
                      <button
                        class="icon-button success-icon"
                        type="button"
                        title="Marcar como realizado"
                        :disabled="loading"
                        @click="realizarAgendamento(agendamento.id)"
                      >
                        <CheckCircle2 :size="18" aria-hidden="true" />
                      </button>
                      <button
                        class="icon-button danger-icon"
                        type="button"
                        title="Cancelar agendamento"
                        :disabled="loading"
                        @click="abrirModalCancelamento(agendamento)"
                      >
                        <Ban :size="18" aria-hidden="true" />
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </section>

      <section v-if="activeTab === 'pacientes'" class="content-grid">
        <div class="entity-toolbar span-all">
          <button class="primary-button" type="button" :disabled="loading" @click="abrirModalPaciente">
            <Plus :size="18" aria-hidden="true" />
            <span>Novo paciente</span>
          </button>
        </div>

        <form class="panel search-panel span-all" @submit.prevent>
          <label>
            <Search :size="18" aria-hidden="true" />
            <input v-model="filtrosPacientes.texto" placeholder="Buscar por nome, CPF ou sexo" type="search" />
          </label>
          <select v-model="filtrosPacientes.status" aria-label="Status do paciente">
            <option value="">Todos os status</option>
            <option value="ATIVO">Ativos</option>
            <option value="INATIVO">Inativos</option>
          </select>
          <button class="icon-button" type="button" title="Limpar busca" @click="limparFiltrosPacientes">
            <XCircle :size="18" aria-hidden="true" />
          </button>
        </form>

        <section class="panel table-panel span-all">
          <div class="section-heading">
            <h2>Pacientes</h2>
            <Search :size="20" aria-hidden="true" />
          </div>
          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Nome</th>
                  <th>CPF</th>
                  <th>Idade</th>
                  <th>Sexo</th>
                  <th>Status</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="paciente in pacientesFiltrados" :key="paciente.id">
                  <td>{{ paciente.nome }}</td>
                  <td>{{ paciente.cpf }}</td>
                  <td>{{ paciente.idade }}</td>
                  <td>{{ rotuloSexo(paciente.sexo) }}</td>
                  <td><span class="badge" :class="paciente.ativo ? 'ativo' : 'inativo'">{{ paciente.ativo ? 'ATIVO' : 'INATIVO' }}</span></td>
                  <td class="actions">
                    <div class="action-buttons">
                      <button
                        class="icon-button"
                        type="button"
                        title="Editar paciente"
                        @click="editarPaciente(paciente)"
                      >
                        <Pencil :size="18" aria-hidden="true" />
                      </button>
                      <button
                        v-if="paciente.ativo"
                        class="icon-button danger-icon"
                        type="button"
                        title="Inativar paciente"
                        @click="inativarPaciente(paciente.id)"
                      >
                        <Ban :size="18" aria-hidden="true" />
                      </button>
                      <button
                        v-else
                        class="icon-button success-icon"
                        type="button"
                        title="Ativar paciente"
                        @click="ativarPaciente(paciente.id)"
                      >
                        <CheckCircle2 :size="18" aria-hidden="true" />
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </section>

      <section v-if="activeTab === 'profissionais'" class="content-grid">
        <div class="entity-toolbar span-all">
          <button class="primary-button" type="button" :disabled="loading" @click="abrirModalProfissional">
            <Plus :size="18" aria-hidden="true" />
            <span>Novo profissional</span>
          </button>
        </div>

        <form class="panel search-panel span-all" @submit.prevent>
          <label>
            <Search :size="18" aria-hidden="true" />
            <input v-model="filtrosProfissionais.texto" placeholder="Buscar por nome, CRM ou área" type="search" />
          </label>
          <select v-model="filtrosProfissionais.area" aria-label="Área profissional">
            <option value="">Todas as áreas</option>
            <option v-for="area in areasProfissionais" :key="area.value" :value="area.value">
              {{ area.label }}
            </option>
          </select>
          <select v-model="filtrosProfissionais.status" aria-label="Status do profissional">
            <option value="">Todos os status</option>
            <option value="ATIVO">Ativos</option>
            <option value="INATIVO">Inativos</option>
          </select>
          <button class="icon-button" type="button" title="Limpar busca" @click="limparFiltrosProfissionais">
            <XCircle :size="18" aria-hidden="true" />
          </button>
        </form>

        <section class="panel table-panel span-all">
          <div class="section-heading">
            <h2>Profissionais</h2>
            <Search :size="20" aria-hidden="true" />
          </div>
          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Nome</th>
                  <th>CRM</th>
                  <th>Área</th>
                  <th>Status</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="profissional in profissionaisFiltrados" :key="profissional.id">
                  <td>{{ profissional.nome }}</td>
                  <td>{{ profissional.crm }}</td>
                  <td>{{ rotuloArea(profissional.area) }}</td>
                  <td><span class="badge" :class="profissional.ativo ? 'ativo' : 'inativo'">{{ profissional.ativo ? 'ATIVO' : 'INATIVO' }}</span></td>
                  <td class="actions">
                    <div class="action-buttons">
                      <button
                        class="icon-button"
                        type="button"
                        title="Editar profissional"
                        @click="editarProfissional(profissional)"
                      >
                        <Pencil :size="18" aria-hidden="true" />
                      </button>
                      <button
                        v-if="profissional.ativo"
                        class="icon-button danger-icon"
                        type="button"
                        title="Inativar profissional"
                        @click="inativarProfissional(profissional.id)"
                      >
                        <Ban :size="18" aria-hidden="true" />
                      </button>
                      <button
                        v-else
                        class="icon-button success-icon"
                        type="button"
                        title="Ativar profissional"
                        @click="ativarProfissional(profissional.id)"
                      >
                        <CheckCircle2 :size="18" aria-hidden="true" />
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </section>
    </section>

    <div
      v-if="agendamentoModalAberto"
      class="modal-backdrop"
      role="presentation"
      @click.self="fecharModalAgendamento"
    >
      <section class="modal-panel" role="dialog" aria-modal="true" aria-labelledby="agendamento-modal-titulo">
        <header class="modal-header">
          <h2 id="agendamento-modal-titulo">Novo agendamento</h2>
          <button class="icon-button" type="button" title="Fechar" @click="fecharModalAgendamento">
            <XCircle :size="18" aria-hidden="true" />
          </button>
        </header>

        <form class="modal-form" @submit.prevent="salvarAgendamento">
          <label>
            <span>Paciente</span>
            <select v-model="agendamentoForm.pacienteId" required>
              <option value="">Selecione</option>
              <option v-for="paciente in pacientesAtivos" :key="paciente.id" :value="paciente.id">
                {{ paciente.nome }}
              </option>
            </select>
          </label>
          <label>
            <span>Profissional</span>
            <select v-model="agendamentoForm.profissionalId" required>
              <option value="">Selecione</option>
              <option
                v-for="profissional in profissionaisAtivos"
                :key="profissional.id"
                :value="profissional.id"
              >
                {{ profissional.nome }} - {{ rotuloArea(profissional.area) }}
              </option>
            </select>
          </label>
          <label>
            <span>Data e hora</span>
            <input v-model="agendamentoForm.dataHora" type="datetime-local" required />
          </label>
          <div class="button-row">
            <button class="primary-button" type="submit" :disabled="loading">
              <Plus :size="18" aria-hidden="true" />
              <span>Agendar</span>
            </button>
            <button class="secondary-button" type="button" @click="fecharModalAgendamento">
              <XCircle :size="18" aria-hidden="true" />
              <span>Cancelar</span>
            </button>
          </div>
        </form>
      </section>
    </div>

    <div
      v-if="cancelamentoModalAberto"
      class="modal-backdrop"
      role="presentation"
      @click.self="fecharModalCancelamento"
    >
      <section class="modal-panel" role="dialog" aria-modal="true" aria-labelledby="cancelamento-modal-titulo">
        <header class="modal-header">
          <h2 id="cancelamento-modal-titulo">Cancelar agendamento</h2>
          <button class="icon-button" type="button" title="Fechar" @click="fecharModalCancelamento">
            <XCircle :size="18" aria-hidden="true" />
          </button>
        </header>

        <form class="modal-form" @submit.prevent="cancelarAgendamento">
          <p class="modal-summary">Agendamento #{{ cancelamento.agendamentoId }}</p>
          <label>
            <span>Motivo</span>
            <textarea v-model="cancelamento.motivo" rows="4" required />
          </label>
          <div class="button-row">
            <button class="danger-button" type="submit" :disabled="loading">
              <Ban :size="18" aria-hidden="true" />
              <span>Cancelar agendamento</span>
            </button>
            <button class="secondary-button" type="button" @click="fecharModalCancelamento">
              <XCircle :size="18" aria-hidden="true" />
              <span>Voltar</span>
            </button>
          </div>
        </form>
      </section>
    </div>

    <div
      v-if="pacienteModalAberto"
      class="modal-backdrop"
      role="presentation"
      @click.self="fecharModalPaciente"
    >
      <section class="modal-panel" role="dialog" aria-modal="true" aria-labelledby="paciente-modal-titulo">
        <header class="modal-header">
          <h2 id="paciente-modal-titulo">{{ pacienteEmEdicaoId ? 'Editar paciente' : 'Novo paciente' }}</h2>
          <button class="icon-button" type="button" title="Fechar" @click="fecharModalPaciente">
            <XCircle :size="18" aria-hidden="true" />
          </button>
        </header>

        <form class="modal-form" @submit.prevent="salvarPaciente">
          <label><span>Nome</span><input v-model="pacienteForm.nome" required /></label>
          <label><span>CPF</span><input v-model="pacienteForm.cpf" required /></label>
          <label><span>Data de nascimento</span><input v-model="pacienteForm.dataNascimento" type="date" required /></label>
          <label>
            <span>Sexo</span>
            <select v-model="pacienteForm.sexo" required>
              <option value="">Selecione</option>
              <option v-for="sexo in sexos" :key="sexo.value" :value="sexo.value">
                {{ sexo.label }}
              </option>
            </select>
          </label>
          <label><span>Endereço</span><input v-model="pacienteForm.endereco" required /></label>
          <div class="button-row">
            <button class="primary-button" type="submit" :disabled="loading">
              <Save v-if="pacienteEmEdicaoId" :size="18" aria-hidden="true" />
              <Plus v-else :size="18" aria-hidden="true" />
              <span>{{ pacienteEmEdicaoId ? 'Salvar' : 'Cadastrar' }}</span>
            </button>
            <button class="secondary-button" type="button" @click="fecharModalPaciente">
              <XCircle :size="18" aria-hidden="true" />
              <span>Cancelar</span>
            </button>
          </div>
        </form>
      </section>
    </div>

    <div
      v-if="profissionalModalAberto"
      class="modal-backdrop"
      role="presentation"
      @click.self="fecharModalProfissional"
    >
      <section class="modal-panel" role="dialog" aria-modal="true" aria-labelledby="profissional-modal-titulo">
        <header class="modal-header">
          <h2 id="profissional-modal-titulo">
            {{ profissionalEmEdicaoId ? 'Editar profissional' : 'Novo profissional' }}
          </h2>
          <button class="icon-button" type="button" title="Fechar" @click="fecharModalProfissional">
            <XCircle :size="18" aria-hidden="true" />
          </button>
        </header>

        <form class="modal-form" @submit.prevent="salvarProfissional">
          <label><span>Nome</span><input v-model="profissionalForm.nome" required /></label>
          <label><span>CRM</span><input v-model="profissionalForm.crm" required /></label>
          <label>
            <span>Área</span>
            <select v-model="profissionalForm.area" required>
              <option value="">Selecione</option>
              <option v-for="area in areasProfissionais" :key="area.value" :value="area.value">
                {{ area.label }}
              </option>
            </select>
          </label>
          <div class="button-row">
            <button class="primary-button" type="submit" :disabled="loading">
              <Save v-if="profissionalEmEdicaoId" :size="18" aria-hidden="true" />
              <Plus v-else :size="18" aria-hidden="true" />
              <span>{{ profissionalEmEdicaoId ? 'Salvar' : 'Cadastrar' }}</span>
            </button>
            <button class="secondary-button" type="button" @click="fecharModalProfissional">
              <XCircle :size="18" aria-hidden="true" />
              <span>Cancelar</span>
            </button>
          </div>
        </form>
      </section>
    </div>
  </main>
</template>
