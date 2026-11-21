/**
 * Sabor Gourmet - Orquestador Principal
 * Arquitectura modular con componentes reutilizables
 */

import { api } from './modules/api.js';
import { dateUtils, escapeHtml } from './modules/utils.js';
import {
  createBadge,
  createEmptyState,
  createActionButtons,
  showToast,
  openModal,
  closeModal,
  showConfirmDialog,
  renderIcons
} from './modules/components.js';

// ================= ESTADO CENTRALIZADO =================
const state = {
  customers: [],
  tables: [],
  reservations: [],
  currentReservationFilter: 'current',
  selectedFilterDate: ''
};

// ================= NOTAS Y MENSAJES DE RESERVAS =================
export function getStoredNotes() {
  try {
    return JSON.parse(localStorage.getItem('sg_reservation_notes') || '{}');
  } catch {
    return {};
  }
}

export function saveStoredNote(resId, text, custId = null, email = null) {
  try {
    const map = getStoredNotes();
    if (text) {
      map[resId] = text;
      if (custId) map[`cust_${custId}`] = text;
      if (email) map[`email_${email.toLowerCase()}`] = text;
    } else {
      delete map[resId];
    }
    localStorage.setItem('sg_reservation_notes', JSON.stringify(map));
  } catch (e) {
    console.warn('Error saving note', e);
  }
}

// ================= SINCRONIZACIÓN Y RELOJ =================
function startSantiagoClock() {
  const clockEl = document.getElementById('liveClock');
  if (!clockEl) return;
  const update = () => {
    try {
      clockEl.textContent = new Intl.DateTimeFormat('es-CL', {
        timeZone: 'America/Santiago',
        hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false
      }).format(new Date());
    } catch {
      clockEl.textContent = new Date().toLocaleTimeString();
    }
  };
  setInterval(update, 1000);
  update();
}

// ================= CARGA DE DATOS (API) =================
export async function refreshAll() {
  try {
    await Promise.all([fetchCustomers(), fetchTables(), fetchReservations()]);
    updateKpis();
    checkTableLimit();
  } catch (err) {
    showToast('Error sincronizando datos: ' + err.message, 'error');
  }
}

async function fetchCustomers() {
  state.customers = (await api.get('/customers')) || [];
  renderCustomers();
  populateCustomerSelect();
  document.getElementById('badgeCustomersCount').textContent = state.customers.length;
}

async function fetchTables() {
  state.tables = (await api.get('/tables')) || [];
  renderTables();
  populateTableSelect();
  document.getElementById('badgeTablesCount').textContent = `${state.tables.length}/10`;
}

async function fetchReservations() {
  let url = '/reservations';
  if (state.currentReservationFilter === 'byDate' && state.selectedFilterDate) {
    url += `?date=${encodeURIComponent(dateUtils.toBackendDate(state.selectedFilterDate))}`;
  }
  state.reservations = (await api.get(url)) || [];
  renderReservations();
  document.getElementById('badgeReservationsCount').textContent = state.reservations.length;
}

async function updateKpis() {
  document.getElementById('kpiTotalTables').textContent = state.tables.length;
  document.getElementById('kpiTotalCustomers').textContent = state.customers.length;
  document.getElementById('kpiActiveReservations').textContent = state.reservations.filter(r => r.status === 'CONFIRMED').length;

  try {
    const avail = await api.get('/tables/available');
    document.getElementById('kpiAvailableToday').textContent = Array.isArray(avail) ? avail.length : 0;
  } catch {
    document.getElementById('kpiAvailableToday').textContent = '-';
  }
}

function checkTableLimit() {
  const isFull = state.tables.length >= 10;
  document.getElementById('tableLimitAlert').classList.toggle('hidden', !isFull);
  const btn = document.getElementById('btnOpenNewTableModal');
  btn.disabled = isFull;
  btn.classList.toggle('opacity-50', isFull);
  btn.classList.toggle('cursor-not-allowed', isFull);
}

// ================= RENDERIZADO CON COMPONENTES REUTILIZABLES =================

// 1. Reservas
function renderReservations(list = state.reservations) {
  const tbody = document.getElementById('reservationsTableBody');
  if (!list.length) {
    tbody.innerHTML = `<tr><td colspan="7">${createEmptyState({
      icon: 'calendar-x',
      message: 'No hay reservas registradas para este criterio.',
      actionText: 'Crear nueva reserva',
      actionFn: "openModal('modalNewReservation')"
    })}</td></tr>`;
    renderIcons();
    return;
  }

  tbody.innerHTML = list.map(res => {
    const cust = state.customers.find(c => c.id === res.customerId);
    const tbl = state.tables.find(t => t.id === res.tableId);
    const isConfirmed = res.status === 'CONFIRMED';
    const endTime = dateUtils.calculateEndTime(res.startTime);
    const notesMap = getStoredNotes();
    const resNote = res.notes || notesMap[res.id] || (cust ? (notesMap[`cust_${cust.id}`] || notesMap[`email_${cust.email?.toLowerCase()}`]) : '');

    return `
      <tr class="hover:bg-slate-800/40 transition-colors">
        <td class="py-3 px-4 font-mono font-semibold text-slate-400">#${res.id}</td>
        <td class="py-3 px-4">
          <div class="font-semibold text-slate-100">${escapeHtml(cust?.name || `Cliente #${res.customerId}`)}</div>
          <div class="text-[11px] text-slate-400 font-mono">${escapeHtml(cust?.phone || '-')}</div>
          ${resNote ? `
            <div class="mt-2 flex items-start space-x-1.5 text-xs text-amber-300 bg-amber-500/10 border border-amber-500/30 px-2.5 py-1.5 rounded-lg max-w-sm">
              <i data-lucide="message-square" class="w-3.5 h-3.5 shrink-0 mt-0.5 text-amber-400"></i>
              <div>
                <span class="text-[9px] uppercase font-bold text-amber-400 block tracking-wider">Mensaje / Notas:</span>
                <span class="italic text-slate-200">${escapeHtml(resNote)}</span>
              </div>
            </div>
          ` : ''}
        </td>
        <td class="py-3 px-4">
          <span class="inline-flex items-center space-x-1 px-2.5 py-1 rounded bg-slate-800 text-slate-200">
            <i data-lucide="table-2" class="w-3.5 h-3.5 text-amber-400"></i>
            <span>Mesa ${tbl?.number || res.tableId}</span>
            <span class="text-[10px] text-slate-400 font-mono">(Cap. ${tbl?.capacity || '?'})</span>
          </span>
        </td>
        <td class="py-3 px-4 font-mono">
          <div class="text-slate-200">${dateUtils.formatFriendly(res.startTime)}</div>
          <div class="text-[11px] text-slate-400">Hasta las <strong>${endTime}</strong> (120 min)</div>
        </td>
        <td class="py-3 px-4 text-center font-mono font-semibold text-slate-300">${res.people} pers.</td>
        <td class="py-3 px-4 text-center">
          ${createBadge(isConfirmed ? 'Confirmada' : 'Cancelada', isConfirmed ? 'success' : 'neutral')}
        </td>
        <td class="py-3 px-4 text-right">
          ${isConfirmed ? createActionButtons({
      onEdit: `editReservation(${res.id})`,
      onDelete: `confirmCancelReservation(${res.id})`,
      deleteTitle: 'Cancelar reserva'
    }) : '<span class="text-[11px] text-slate-600 italic">Sin acciones</span>'}
        </td>
      </tr>
    `;
  }).join('');
  renderIcons();
}

// 2. Mesas (Grid y Tabla)
function renderTables() {
  const grid = document.getElementById('tablesVisualGrid');
  const tbody = document.getElementById('tablesTableBody');
  const sorted = [...state.tables].sort((a, b) => a.number - b.number);

  // Plano del salón
  grid.innerHTML = sorted.map(t => `
    <div class="bg-slate-950 border ${t.available ? 'border-slate-800 hover:border-amber-500/50' : 'border-rose-900/40 opacity-70'} rounded-xl p-4 flex flex-col justify-between">
      <div class="flex items-center justify-between mb-2">
        <span class="font-serif font-bold text-base text-slate-100">Mesa <span class="text-amber-400 font-mono">#${t.number}</span></span>
        <span class="w-2.5 h-2.5 rounded-full ${t.available ? 'bg-emerald-500' : 'bg-rose-500'}"></span>
      </div>
      <div class="space-y-1 my-2 text-xs">
        <div class="text-slate-400">Capacidad: <strong class="text-slate-200 font-mono">${t.capacity}</strong> pers.</div>
        <div class="font-mono ${t.available ? 'text-emerald-400' : 'text-rose-400'} text-[11px]">${t.available ? 'Habilitada' : 'Inactiva'}</div>
      </div>
      <div class="pt-2 border-t border-slate-900 flex justify-end">
        ${createActionButtons({ onEdit: `editTable(${t.id})`, onDelete: `confirmDeleteTable(${t.id})` })}
      </div>
    </div>
  `).join('');

  // Tabla
  tbody.innerHTML = sorted.map(t => `
    <tr class="hover:bg-slate-800/40 transition-colors">
      <td class="py-3 px-4 font-mono text-slate-400">#${t.id}</td>
      <td class="py-3 px-4 font-semibold text-slate-100">Mesa ${t.number}</td>
      <td class="py-3 px-4 font-mono">${t.capacity} personas</td>
      <td class="py-3 px-4 text-center">${createBadge(t.available ? 'Habilitada' : 'Deshabilitada', t.available ? 'success' : 'danger')}</td>
      <td class="py-3 px-4 text-right">${createActionButtons({ onEdit: `editTable(${t.id})`, onDelete: `confirmDeleteTable(${t.id})` })}</td>
    </tr>
  `).join('');
  renderIcons();
}

// 3. Clientes
function renderCustomers(list = state.customers) {
  const tbody = document.getElementById('customersTableBody');
  if (!list.length) {
    tbody.innerHTML = `<tr><td colspan="6">${createEmptyState({ icon: 'users', message: 'No hay clientes registrados.' })}</td></tr>`;
    renderIcons();
    return;
  }

  tbody.innerHTML = list.map(c => {
    const resCount = state.reservations.filter(r => r.customerId === c.id).length;
    return `
      <tr class="hover:bg-slate-800/40 transition-colors">
        <td class="py-3 px-4 font-mono text-slate-400">#${c.id}</td>
        <td class="py-3 px-4 font-semibold text-slate-100">${escapeHtml(c.name)}</td>
        <td class="py-3 px-4 font-mono text-slate-300">${escapeHtml(c.email)}</td>
        <td class="py-3 px-4 font-mono text-slate-300">${escapeHtml(c.phone)}</td>
        <td class="py-3 px-4 text-center font-mono">${createBadge(`${resCount} res.`, resCount ? 'warning' : 'neutral')}</td>
        <td class="py-3 px-4 text-right">${createActionButtons({ onEdit: `editCustomer(${c.id})`, onDelete: `confirmDeleteCustomer(${c.id})` })}</td>
      </tr>
    `;
  }).join('');
  renderIcons();
}

// ================= DROPDOWNS REUTILIZABLES =================
function populateCustomerSelect() {
  const select = document.getElementById('resCustomerId');
  if (!select) return;
  const current = select.value;
  select.innerHTML = '<option value="">-- Seleccionar cliente --</option>' +
    state.customers.map(c => `<option value="${c.id}">${escapeHtml(c.name)} (${escapeHtml(c.phone)})</option>`).join('');
  if (current) select.value = current;
}

function populateTableSelect() {
  const select = document.getElementById('resTableId');
  if (!select) return;
  const people = parseInt(document.getElementById('resPeople')?.value) || 1;
  const eligible = state.tables.filter(t => t.available && t.capacity >= people);
  select.innerHTML = '<option value="">-- Seleccionar mesa asignada --</option>' +
    eligible.map(t => `<option value="${t.id}">Mesa ${t.number} — Capacidad ${t.capacity} pers.</option>`).join('');
}

// ================= GESTORES DE FORMULARIOS =================

// 1. Submit Reserva
export async function handleReservationSubmit(e) {
  e.preventDefault();
  const id = document.getElementById('editReservationId').value;
  const resNotes = document.getElementById('resNotes')?.value.trim() || '';
  const payload = {
    customerId: Number(document.getElementById('resCustomerId').value),
    tableId: Number(document.getElementById('resTableId').value),
    startTime: dateUtils.toBackendDateTime(document.getElementById('resStartTime').value),
    people: Number(document.getElementById('resPeople').value)
  };

  try {
    if (id) {
      await api.put(`/reservations/${id}`, payload);
      saveStoredNote(id, resNotes, payload.customerId);
      showToast(`Reserva #${id} actualizada con éxito`);
    } else {
      const created = await api.post('/reservations', payload);
      saveStoredNote(created.id, resNotes, payload.customerId);
      showToast(`Reserva #${created.id} creada con éxito`);
    }
    closeModal('modalNewReservation');
    await refreshAll();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// 2. Submit Cliente
export async function handleCustomerSubmit(e) {
  e.preventDefault();
  const id = document.getElementById('editCustomerId').value;
  const payload = {
    name: document.getElementById('custName').value.trim(),
    email: document.getElementById('custEmail').value.trim(),
    phone: document.getElementById('custPhone').value.trim()
  };

  try {
    if (id) {
      await api.put(`/customers/${id}`, payload);
      showToast('Cliente actualizado');
    } else {
      const created = await api.post('/customers', payload);
      showToast(`Cliente #${created.id} registrado`);
    }
    closeModal('modalNewCustomer');
    await fetchCustomers();
    updateKpis();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// 3. Submit Mesa
export async function handleTableSubmit(e) {
  e.preventDefault();
  const id = document.getElementById('editTableId').value;
  const payload = {
    number: Number(document.getElementById('tblNumber').value),
    capacity: Number(document.getElementById('tblCapacity').value),
    available: document.getElementById('tblAvailable').checked
  };

  try {
    if (id) {
      await api.put(`/tables/${id}`, payload);
      showToast('Mesa actualizada');
    } else {
      await api.post('/tables', payload);
      showToast('Mesa creada exitosamente');
    }
    closeModal('modalNewTable');
    await fetchTables();
    checkTableLimit();
    updateKpis();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// ================= ACCIONES DE EDICIÓN Y CANCELACIÓN =================
export function editReservation(id) {
  const res = state.reservations.find(r => r.id === id);
  if (!res) return;

  document.getElementById('editReservationId').value = res.id;
  document.getElementById('resCustomerId').value = res.customerId;
  document.getElementById('resStartTime').value = dateUtils.toLocalDateTime(res.startTime);
  document.getElementById('resPeople').value = res.people;

  const notesMap = getStoredNotes();
  const cust = state.customers.find(c => c.id === res.customerId);
  const noteVal = res.notes || notesMap[res.id] || (cust ? (notesMap[`cust_${cust.id}`] || notesMap[`email_${cust.email?.toLowerCase()}`]) : '') || '';
  const notesEl = document.getElementById('resNotes');
  if (notesEl) notesEl.value = noteVal;

  populateTableSelect();
  document.getElementById('resTableId').value = res.tableId;
  document.getElementById('modalReservationTitle').innerHTML = `<i data-lucide="edit" class="w-5 h-5"></i><span>Editar Reserva #${res.id}</span>`;
  document.getElementById('btnSubmitReservation').textContent = 'Guardar Cambios';

  onReservationDateTimeChange();
  openModal('modalNewReservation');
}

export function openNewReservationModal() {
  document.getElementById('editReservationId').value = '';
  document.getElementById('formReservation').reset();
  const notesEl = document.getElementById('resNotes');
  if (notesEl) notesEl.value = '';
  document.getElementById('modalReservationTitle').innerHTML = `<i data-lucide="calendar-plus" class="w-5 h-5"></i><span>Registrar Nueva Reserva</span>`;
  document.getElementById('btnSubmitReservation').textContent = 'Confirmar Reserva';
  openModal('modalNewReservation');
}

export function confirmCancelReservation(id) {
  showConfirmDialog({
    title: `¿Cancelar la Reserva #${id}?`,
    description: 'La reserva pasará a estado CANCELLED, conservando el registro histórico y liberando la mesa.',
    confirmText: 'Cancelar Reserva',
    isDanger: true,
    onConfirm: async () => {
      try {
        await api.delete(`/reservations/${id}`);
        showToast(`Reserva #${id} cancelada. Mesa liberada.`);
        closeModal('modalConfirm');
        await refreshAll();
      } catch (err) {
        showToast(err.message, 'error');
      }
    }
  });
}

export function editCustomer(id) {
  const cust = state.customers.find(c => c.id === id);
  if (!cust) return;
  document.getElementById('editCustomerId').value = cust.id;
  document.getElementById('custName').value = cust.name;
  document.getElementById('custEmail').value = cust.email;
  document.getElementById('custPhone').value = cust.phone;
  openModal('modalNewCustomer');
}

export function confirmDeleteCustomer(id) {
  showConfirmDialog({
    title: `¿Eliminar Cliente #${id}?`,
    description: 'Solo puede ser eliminado si no tiene reservas vinculadas.',
    confirmText: 'Eliminar Cliente',
    isDanger: true,
    onConfirm: async () => {
      try {
        await api.delete(`/customers/${id}`);
        showToast('Cliente eliminado');
        closeModal('modalConfirm');
        await fetchCustomers();
        updateKpis();
      } catch (err) {
        showToast(err.message, 'error');
      }
    }
  });
}

export function editTable(id) {
  const tbl = state.tables.find(t => t.id === id);
  if (!tbl) return;
  document.getElementById('editTableId').value = tbl.id;
  document.getElementById('tblNumber').value = tbl.number;
  document.getElementById('tblCapacity').value = tbl.capacity;
  document.getElementById('tblAvailable').checked = tbl.available;
  openModal('modalNewTable');
}

export function confirmDeleteTable(id) {
  showConfirmDialog({
    title: `¿Eliminar Mesa #${id}?`,
    description: 'Solo puede ser eliminada si no posee reservas históricas asociadas.',
    confirmText: 'Eliminar Mesa',
    isDanger: true,
    onConfirm: async () => {
      try {
        await api.delete(`/tables/${id}`);
        showToast('Mesa eliminada');
        closeModal('modalConfirm');
        await fetchTables();
        checkTableLimit();
        updateKpis();
      } catch (err) {
        showToast(err.message, 'error');
      }
    }
  });
}

// ================= DISPONIBILIDAD EN VIVO =================
export async function handleAvailabilitySubmit(e) {
  e.preventDefault();
  const mode = document.querySelector('input[name="availMode"]:checked')?.value || 'slot';
  const people = document.getElementById('availPeople').value;

  const params = new URLSearchParams();
  let searchSummary = '';
  if (mode === 'slot') {
    const raw = document.getElementById('availSlotDateTime').value;
    if (!raw) return showToast('Selecciona fecha y hora', 'info');
    const backendDt = dateUtils.toBackendDateTime(raw);
    params.set('startTime', backendDt);
    searchSummary = `Turno: ${dateUtils.formatFriendly(backendDt)} (hasta las ${dateUtils.calculateEndTime(backendDt)})`;
  } else {
    const raw = document.getElementById('availDayDate').value;
    if (!raw) return showToast('Selecciona fecha', 'info');
    const backendD = dateUtils.toBackendDate(raw);
    params.set('date', backendD);
    searchSummary = `Día completo: ${backendD}`;
  }
  if (people && Number(people) >= 1) {
    params.set('people', people);
    searchSummary += ` · Mínimo ${people} personas`;
  }

  const submitBtn = e.target.querySelector('button[type="submit"]');
  const originalBtnHtml = submitBtn ? submitBtn.innerHTML : '';
  const resultsContainer = document.getElementById('availabilityResultsContainer');
  const subHeader = document.getElementById('availResultSubHeader');

  if (submitBtn) {
    submitBtn.disabled = true;
    submitBtn.innerHTML = `<i data-lucide="loader-2" class="w-4 h-4 animate-spin"></i><span>Consultando...</span>`;
    renderIcons();
  }

  if (resultsContainer) resultsContainer.classList.remove('hidden');
  if (subHeader) subHeader.textContent = 'Buscando disponibilidad en tiempo real...';

  try {
    const qs = params.toString() ? `?${params.toString()}` : '';
    const [avail, reserved] = await Promise.all([
      api.get(`/tables/available${qs}`),
      api.get(`/tables/reserved${qs}`)
    ]);

    if (subHeader) {
      subHeader.textContent = `${searchSummary} — Consulta completada con éxito`;
    }
    document.getElementById('availFreeCount').textContent = `${avail.length} Disponibles`;
    document.getElementById('availReservedCount').textContent = `${reserved.length} Reservadas`;

    const freeList = document.getElementById('availFreeList');
    freeList.innerHTML = avail.length ? avail.map(t => `
      <div class="flex items-center justify-between p-3 rounded-lg bg-slate-950 border border-emerald-500/20">
        <div>
          <div class="font-semibold text-slate-100 text-xs">Mesa ${t.number}</div>
          <div class="text-[11px] text-slate-400">Capacidad: ${t.capacity} personas</div>
        </div>
        <button onclick="bookThisTable(${t.id})" class="px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs">Reservar</button>
      </div>
    `).join('') : '<p class="text-xs text-slate-500 text-center py-4">Sin mesas disponibles</p>';

    const resList = document.getElementById('availReservedList');
    resList.innerHTML = reserved.length ? reserved.map(t => `
      <div class="flex items-center justify-between p-3 rounded-lg bg-slate-950 border border-rose-500/20 text-slate-400">
        <div>
          <div class="font-semibold text-slate-300 text-xs">Mesa ${t.number}</div>
          <div class="text-[11px] text-slate-500">Capacidad: ${t.capacity} personas</div>
        </div>
        <span class="text-xs text-rose-400 font-medium">Ocupada</span>
      </div>
    `).join('') : '<p class="text-xs text-slate-500 text-center py-4">Sin mesas ocupadas</p>';

    renderIcons();
  } catch (err) {
    if (subHeader) {
      subHeader.textContent = 'Error al consultar disponibilidad: ' + err.message;
    }
    showToast(err.message, 'error');
  } finally {
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.innerHTML = originalBtnHtml;
      renderIcons();
    }
  }
}

export function bookThisTable(tableId) {
  openModal('modalNewReservation');
  document.getElementById('resTableId').value = tableId;
  const slot = document.getElementById('availSlotDateTime')?.value;
  if (slot) {
    document.getElementById('resStartTime').value = slot;
    onReservationDateTimeChange();
  }
}

// ================= AUXILIARES DE UI =================
export function navigateTab(tabName) {
  document.querySelectorAll('.tab-button').forEach(b => {
    b.classList.remove('active-tab', 'border-amber-500', 'text-amber-400');
    b.classList.add('border-transparent', 'text-slate-400');
  });
  const activeBtn = document.getElementById(`tabBtn-${tabName}`);
  if (activeBtn) activeBtn.classList.add('active-tab', 'border-amber-500', 'text-amber-400');

  document.querySelectorAll('.tab-content').forEach(s => s.classList.add('hidden'));
  document.getElementById(`tab-${tabName}`)?.classList.remove('hidden');
  renderIcons();
}

export function setReservationFilter(mode) {
  state.currentReservationFilter = mode;
  const isCurrent = mode === 'current';
  document.getElementById('btnFilterCurrent').className = `px-3 py-1.5 text-xs font-medium rounded-md ${isCurrent ? 'bg-amber-500/20 text-amber-300' : 'text-slate-400'}`;
  document.getElementById('btnFilterByDate').className = `px-3 py-1.5 text-xs font-medium rounded-md ${!isCurrent ? 'bg-amber-500/20 text-amber-300' : 'text-slate-400'}`;
  document.getElementById('filterDateContainer').classList.toggle('hidden', isCurrent);
  document.getElementById('filterDateContainer').classList.toggle('flex', !isCurrent);

  if (!isCurrent && !document.getElementById('filterReservationDate').value) {
    document.getElementById('filterReservationDate').value = new Date().toISOString().split('T')[0];
  }
  state.selectedFilterDate = document.getElementById('filterReservationDate').value;
  fetchReservations();
}

export function onReservationDateTimeChange() {
  const val = document.getElementById('resStartTime').value;
  const display = document.getElementById('resEndTimeDisplay');
  if (!val) {
    display.textContent = 'Duración: 120 minutos (El turno termina automáticamente 2 horas después).';
    return;
  }
  const backendStr = dateUtils.toBackendDateTime(val);
  display.innerHTML = `Turno: <strong class="text-white">${dateUtils.formatFriendly(backendStr)}</strong> hasta <strong class="text-white">${dateUtils.calculateEndTime(backendStr)}</strong> (120 min).`;
}

export function filterReservationsLocal() {
  const q = document.getElementById('searchReservationsInput').value.toLowerCase().trim();
  if (!q) return renderReservations();
  renderReservations(state.reservations.filter(r => {
    const c = state.customers.find(x => x.id === r.customerId)?.name.toLowerCase() || '';
    return c.includes(q) || r.startTime.includes(q) || String(r.id).includes(q);
  }));
}

export function filterCustomersLocal() {
  const q = document.getElementById('searchCustomersInput').value.toLowerCase().trim();
  if (!q) return renderCustomers();
  renderCustomers(state.customers.filter(c => c.name.toLowerCase().includes(q) || c.email.toLowerCase().includes(q) || c.phone.includes(q)));
}

export function toggleAvailMode(mode) {
  document.getElementById('availSlotInputGroup').classList.toggle('hidden', mode !== 'slot');
  document.getElementById('availDayInputGroup').classList.toggle('hidden', mode === 'slot');
}

// ================= EXPOSICIÓN GLOBAL A WINDOW =================
Object.assign(window, {
  navigateTab,
  openModal,
  closeModal,
  refreshAll,
  setReservationFilter,
  filterReservationsLocal,
  filterCustomersLocal,
  handleReservationSubmit,
  handleCustomerSubmit,
  handleTableSubmit,
  editReservation,
  confirmCancelReservation,
  editCustomer,
  confirmDeleteCustomer,
  editTable,
  confirmDeleteTable,
  toggleAvailMode,
  handleAvailabilitySubmit,
  bookThisTable,
  quickCreateCustomer: () => openModal('modalNewCustomer'),
  onReservationPeopleChange: populateTableSelect,
  onReservationDateTimeChange
});

// Inicialización
window.addEventListener('DOMContentLoaded', () => {
  startSantiagoClock();
  renderIcons();

  const now = new Date();
  const pad = n => String(n).padStart(2, '0');
  const isoDay = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
  const slotEl = document.getElementById('availSlotDateTime');
  const dayEl = document.getElementById('availDayDate');
  if (slotEl) slotEl.value = `${isoDay}T20:00`;
  if (dayEl) dayEl.value = isoDay;

  refreshAll();
});
