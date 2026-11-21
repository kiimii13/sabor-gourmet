/**
 * Módulo API: Cliente HTTP centralizado y reutilizable
 */

export async function apiRequest(endpoint, options = {}) {
  const config = {
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    },
    ...options
  };

  const res = await fetch(endpoint, config);

  if (res.status === 204) {
    return null;
  }

  const data = await res.json().catch(() => null);

  if (!res.ok) {
    const errorMsg = data && data.message ? data.message : `Error ${res.status}: ${res.statusText}`;
    throw new Error(errorMsg);
  }

  return data;
}

export const api = {
  get: (url) => apiRequest(url, { method: 'GET' }),
  post: (url, body) => apiRequest(url, { method: 'POST', body: JSON.stringify(body) }),
  put: (url, body) => apiRequest(url, { method: 'PUT', body: JSON.stringify(body) }),
  delete: (url) => apiRequest(url, { method: 'DELETE' })
};
