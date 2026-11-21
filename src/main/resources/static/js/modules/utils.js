/**
 * Módulo Utils: Formateo de fechas, conversión de turnos y seguridad HTML
 */

export const dateUtils = {
  // Convierte input HTML datetime-local (yyyy-MM-ddTHH:mm) a API backend (dd-MM-yyyyTHH:mm)
  toBackendDateTime(localIso) {
    if (!localIso) return '';
    const [datePart, timePart] = localIso.split('T');
    if (!datePart || !timePart) return localIso;
    const [year, month, day] = datePart.split('-');
    return `${day}-${month}-${year}T${timePart}`;
  },

  // Convierte string backend (dd-MM-yyyyTHH:mm) a input HTML datetime-local (yyyy-MM-ddTHH:mm)
  toLocalDateTime(backendStr) {
    if (!backendStr) return '';
    const [datePart, timePart] = backendStr.split('T');
    if (!datePart || !timePart) return backendStr;
    const [day, month, year] = datePart.split('-');
    return `${year}-${month}-${day}T${timePart}`;
  },

  // Convierte input HTML date (yyyy-MM-dd) a API backend (dd-MM-yyyy)
  toBackendDate(localIsoDate) {
    if (!localIsoDate) return '';
    const [year, month, day] = localIsoDate.split('-');
    return `${day}-${month}-${year}`;
  },

  // Convierte fecha backend (dd-MM-yyyy) a input HTML date (yyyy-MM-dd)
  toLocalDate(backendDate) {
    if (!backendDate) return '';
    const [day, month, year] = backendDate.split('-');
    return `${year}-${month}-${day}`;
  },

  // Calcula la hora de término de un turno sumando exactamente 120 minutos
  calculateEndTime(backendStr) {
    if (!backendStr) return '--:--';
    try {
      const [datePart, timePart] = backendStr.split('T');
      const [day, month, year] = datePart.split('-').map(Number);
      const [hour, minute] = timePart.split(':').map(Number);

      const date = new Date(year, month - 1, day, hour, minute);
      date.setMinutes(date.getMinutes() + 120);

      const pad = n => String(n).padStart(2, '0');
      return `${pad(date.getHours())}:${pad(date.getMinutes())}`;
    } catch {
      return '+2 horas';
    }
  },

  // Formato amigable para el usuario: dd-MM-yyyy HH:mm
  formatFriendly(backendStr) {
    if (!backendStr) return '-';
    const [d, t] = backendStr.split('T');
    return `${d} ${t || ''}`;
  }
};

export function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
