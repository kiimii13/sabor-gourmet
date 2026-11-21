/**
 * Módulo Components: Componentes de interfaz gráfica reutilizables
 */

import { escapeHtml } from './utils.js';

// Componente: Badge de Estado Reutilizable
export function createBadge(label, variant = 'success') {
  const styles = {
    success: 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400',
    danger: 'bg-rose-500/10 border-rose-500/30 text-rose-400',
    warning: 'bg-amber-500/10 border-amber-500/30 text-amber-300',
    neutral: 'bg-slate-800 border-slate-700 text-slate-400'
  };

  const dots = {
    success: 'bg-emerald-500',
    danger: 'bg-rose-500',
    warning: 'bg-amber-400',
    neutral: 'bg-slate-500'
  };

  return `
    <span class="inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-full text-xs font-medium border ${styles[variant] || styles.neutral}">
      <span class="w-1.5 h-1.5 rounded-full ${dots[variant] || dots.neutral}"></span>
      <span>${escapeHtml(label)}</span>
    </span>
  `;
}

// Componente: Estado Vacío Reutilizable
export function createEmptyState({ icon = 'inbox', message, actionText = '', actionFn = '' }) {
  return `
    <div class="flex flex-col items-center justify-center py-12 text-slate-500 space-y-2">
      <i data-lucide="${icon}" class="w-8 h-8 text-slate-600"></i>
      <p class="text-xs font-medium">${escapeHtml(message)}</p>
      ${actionText && actionFn ? `<button onclick="${actionFn}" class="text-amber-400 hover:underline text-xs">+ ${escapeHtml(actionText)}</button>` : ''}
    </div>
  `;
}

// Componente: Botones de Acción Reutilizables (Editar / Eliminar)
export function createActionButtons({ onEdit, onDelete, editTitle = 'Editar', deleteTitle = 'Eliminar' }) {
  return `
    <div class="inline-flex items-center space-x-1">
      ${onEdit ? `
        <button onclick="${onEdit}" title="${editTitle}" class="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-amber-400 hover:text-amber-300 transition-colors">
          <i data-lucide="edit-3" class="w-3.5 h-3.5"></i>
        </button>
      ` : ''}
      ${onDelete ? `
        <button onclick="${onDelete}" title="${deleteTitle}" class="p-1.5 rounded-lg bg-slate-800 hover:bg-rose-950/40 text-rose-400 hover:text-rose-300 transition-colors">
          <i data-lucide="trash-2" class="w-3.5 h-3.5"></i>
        </button>
      ` : ''}
    </div>
  `;
}

// Componente: Toast Notifications
export function showToast(message, type = 'success') {
  const container = document.getElementById('toastContainer');
  if (!container) return;

  const toast = document.createElement('div');
  const typeStyles = {
    success: 'bg-emerald-950/90 border-emerald-500/50 text-emerald-200',
    error: 'bg-rose-950/90 border-rose-500/50 text-rose-200',
    info: 'bg-amber-950/90 border-amber-500/50 text-amber-200'
  };

  const icons = {
    success: 'check-circle',
    error: 'alert-triangle',
    info: 'info'
  };

  toast.className = `pointer-events-auto flex items-center space-x-3 px-4 py-3 rounded-xl border shadow-xl text-xs font-medium animate-fadeIn transition-all transform ${typeStyles[type] || typeStyles.info}`;
  toast.innerHTML = `
    <i data-lucide="${icons[type] || 'info'}" class="w-4 h-4 flex-shrink-0"></i>
    <span class="flex-1">${escapeHtml(message)}</span>
    <button onclick="this.parentElement.remove()" class="text-slate-400 hover:text-white ml-2 text-base leading-none">&times;</button>
  `;

  container.appendChild(toast);
  renderIcons();

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// Componente: Modales
export function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.remove('hidden');
  renderIcons();
}

export function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.add('hidden');
}

// Componente: Diálogo de Confirmación Universal
let confirmCallback = null;

export function showConfirmDialog({ title, description, confirmText = 'Confirmar', isDanger = true, onConfirm }) {
  const modal = document.getElementById('modalConfirm');
  if (!modal) return;

  document.getElementById('confirmModalTitle').textContent = title;
  document.getElementById('confirmModalDesc').textContent = description;
  
  const actionBtn = document.getElementById('confirmModalActionBtn');
  actionBtn.textContent = confirmText;
  actionBtn.className = isDanger
    ? 'px-5 py-2 rounded-lg bg-rose-600 hover:bg-rose-500 text-white font-bold text-xs transition-all'
    : 'px-5 py-2 rounded-lg bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold text-xs transition-all';

  confirmCallback = onConfirm;
  actionBtn.onclick = async () => {
    if (confirmCallback) {
      await confirmCallback();
      confirmCallback = null;
    }
  };

  openModal('modalConfirm');
}

// Refrescar Iconos SVG Lucide
export function renderIcons() {
  if (window.lucide && typeof window.lucide.createIcons === 'function') {
    window.lucide.createIcons();
  }
}
