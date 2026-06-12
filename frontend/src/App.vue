<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  Ban,
  CalendarCheck,
  CalendarClock,
  Filter,
  LoaderCircle,
  Plus,
  RefreshCw,
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
  idade: 30,
  sexo: '',
  endereco: '',
})

const profissionalForm = reactive({
  nome: '',
  crm: '',
  area: '',
})

const agendamentoForm = reactive({
  pacienteId: '',
  profissionalId: '',
  dataHora: '',
})

const filtros = reactive({
  pacienteId: '',
  profissionalId: '',
  status: '',
})

const cancelamento = reactive({
  agendamentoId: '',
  motivo: '',
})

const pacientesAtivos = computed(() => pacientes.value.filter((paciente) => paciente.ativo))
const profissionaisAtivos = computed(() => profissionais.value.filter((profissional) => profissional.ativo))

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
  await run(async () => {
    await api('/pacientes', {
      method: 'POST',
      body: JSON.stringify({
        ...pacienteForm,
        idade: Number(pacienteForm.idade),
      }),
    })
    Object.assign(pacienteForm, { nome: '', cpf: '', idade: 30, sexo: '', endereco: '' })
    await carregarDadosSilencioso()
  }, 'Paciente cadastrado.')
}

async function salvarProfissional() {
  await run(async () => {
    await api('/profissionais', {
      method: 'POST',
      body: JSON.stringify(profissionalForm),
    })
    Object.assign(profissionalForm, { nome: '', crm: '', area: '' })
    await carregarDadosSilencioso()
  }, 'Profissional cadastrado.')
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
    Object.assign(agendamentoForm, { pacienteId: '', profissionalId: '', dataHora: '' })
    await buscarAgendamentos()
  }, 'Agendamento criado.')
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
  Object.assign(filtros, { pacienteId: '', profissionalId: '', status: '' })
  await run(buscarAgendamentos, 'Filtros limpos.')
}

async function cancelarAgendamento() {
  await run(async () => {
    await api(`/agendamentos/${cancelamento.agendamentoId}/cancelar`, {
      method: 'PATCH',
      body: JSON.stringify({ motivo: cancelamento.motivo }),
    })
    Object.assign(cancelamento, { agendamentoId: '', motivo: '' })
    await buscarAgendamentos()
  }, 'Agendamento cancelado.')
}

async function inativarPaciente(id) {
  await run(async () => {
    await api(`/pacientes/${id}`, { method: 'DELETE' })
    await carregarDadosSilencioso()
  }, 'Paciente inativado.')
}

async function inativarProfissional(id) {
  await run(async () => {
    await api(`/profissionais/${id}`, { method: 'DELETE' })
    await carregarDadosSilencioso()
  }, 'Profissional inativado.')
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
        <form class="panel form-panel" @submit.prevent="salvarAgendamento">
          <h2>Novo agendamento</h2>
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
                {{ profissional.nome }} - {{ profissional.area }}
              </option>
            </select>
          </label>
          <label>
            <span>Data e hora</span>
            <input v-model="agendamentoForm.dataHora" type="datetime-local" required />
          </label>
          <button class="primary-button" type="submit" :disabled="loading">
            <Plus :size="18" aria-hidden="true" />
            <span>Agendar</span>
          </button>
        </form>

        <form class="panel form-panel" @submit.prevent="filtrarAgendamentos">
          <h2>Filtros</h2>
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
          <div class="button-row">
            <button class="secondary-button" type="submit" :disabled="loading">
              <Filter :size="18" aria-hidden="true" />
              <span>Filtrar</span>
            </button>
            <button class="icon-button" type="button" title="Limpar filtros" @click="limparFiltros">
              <XCircle :size="18" aria-hidden="true" />
            </button>
          </div>
        </form>

        <form class="panel form-panel" @submit.prevent="cancelarAgendamento">
          <h2>Cancelar</h2>
          <label>
            <span>Agendamento</span>
            <select v-model="cancelamento.agendamentoId" required>
              <option value="">Selecione</option>
              <option
                v-for="agendamento in agendamentos.filter((item) => item.status === 'AGENDADO')"
                :key="agendamento.id"
                :value="agendamento.id"
              >
                #{{ agendamento.id }} - {{ formatarData(agendamento.dataHora) }}
              </option>
            </select>
          </label>
          <label>
            <span>Motivo</span>
            <textarea v-model="cancelamento.motivo" rows="4" required />
          </label>
          <button class="danger-button" type="submit" :disabled="loading">
            <Ban :size="18" aria-hidden="true" />
            <span>Cancelar</span>
          </button>
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
                </tr>
              </thead>
              <tbody>
                <tr v-for="agendamento in agendamentos" :key="agendamento.id">
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
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </section>

      <section v-if="activeTab === 'pacientes'" class="content-grid">
        <form class="panel form-panel" @submit.prevent="salvarPaciente">
          <h2>Novo paciente</h2>
          <label><span>Nome</span><input v-model="pacienteForm.nome" required /></label>
          <label><span>CPF</span><input v-model="pacienteForm.cpf" required /></label>
          <label><span>Idade</span><input v-model.number="pacienteForm.idade" min="0" type="number" required /></label>
          <label><span>Sexo</span><input v-model="pacienteForm.sexo" required /></label>
          <label><span>Endereço</span><input v-model="pacienteForm.endereco" required /></label>
          <button class="primary-button" type="submit" :disabled="loading">
            <Plus :size="18" aria-hidden="true" />
            <span>Cadastrar</span>
          </button>
        </form>

        <section class="panel table-panel wide">
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
                  <th>Status</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="paciente in pacientes" :key="paciente.id">
                  <td>{{ paciente.nome }}</td>
                  <td>{{ paciente.cpf }}</td>
                  <td>{{ paciente.idade }}</td>
                  <td><span class="badge" :class="paciente.ativo ? 'ativo' : 'inativo'">{{ paciente.ativo ? 'ATIVO' : 'INATIVO' }}</span></td>
                  <td class="actions">
                    <button
                      class="icon-button danger-icon"
                      type="button"
                      title="Inativar paciente"
                      :disabled="!paciente.ativo"
                      @click="inativarPaciente(paciente.id)"
                    >
                      <Ban :size="18" aria-hidden="true" />
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </section>

      <section v-if="activeTab === 'profissionais'" class="content-grid">
        <form class="panel form-panel" @submit.prevent="salvarProfissional">
          <h2>Novo profissional</h2>
          <label><span>Nome</span><input v-model="profissionalForm.nome" required /></label>
          <label><span>CRM</span><input v-model="profissionalForm.crm" required /></label>
          <label><span>Área</span><input v-model="profissionalForm.area" required /></label>
          <button class="primary-button" type="submit" :disabled="loading">
            <Plus :size="18" aria-hidden="true" />
            <span>Cadastrar</span>
          </button>
        </form>

        <section class="panel table-panel wide">
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
                <tr v-for="profissional in profissionais" :key="profissional.id">
                  <td>{{ profissional.nome }}</td>
                  <td>{{ profissional.crm }}</td>
                  <td>{{ profissional.area }}</td>
                  <td><span class="badge" :class="profissional.ativo ? 'ativo' : 'inativo'">{{ profissional.ativo ? 'ATIVO' : 'INATIVO' }}</span></td>
                  <td class="actions">
                    <button
                      class="icon-button danger-icon"
                      type="button"
                      title="Inativar profissional"
                      :disabled="!profissional.ativo"
                      @click="inativarProfissional(profissional.id)"
                    >
                      <Ban :size="18" aria-hidden="true" />
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </section>
    </section>
  </main>
</template>
