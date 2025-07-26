import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:3000';

const api = axios.create({
  baseURL: `${API_BASE_URL}/api`,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    console.log(`Making ${config.method?.toUpperCase()} request to ${config.url}`);
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    console.error('API Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

// Device API
export const deviceAPI = {
  getAll: () => api.get('/devices'),
  register: (deviceData) => api.post('/devices/register', deviceData),
  getSettings: (deviceId) => api.get(`/devices/${deviceId}/settings`),
  updateSettings: (deviceId, settings) => api.put(`/devices/${deviceId}/settings`, { settings }),
  updateStatus: (deviceId, isActive) => api.patch(`/devices/${deviceId}/status`, { isActive }),
  updateSync: (deviceId, dataType) => api.post(`/devices/${deviceId}/sync/${dataType}`)
};

// Contacts API
export const contactsAPI = {
  sync: (deviceId, contacts) => api.post('/contacts/sync', { deviceId, contacts }),
  getAll: (deviceId, params = {}) => api.get(`/contacts/${deviceId}`, { params }),
  getById: (deviceId, contactId) => api.get(`/contacts/${deviceId}/${contactId}`),
  delete: (deviceId, contactId) => api.delete(`/contacts/${deviceId}/${contactId}`)
};

// Call Logs API
export const callLogsAPI = {
  sync: (deviceId, callLogs) => api.post('/call-logs/sync', { deviceId, callLogs }),
  getAll: (deviceId, params = {}) => api.get(`/call-logs/${deviceId}`, { params }),
  getStats: (deviceId) => api.get(`/call-logs/${deviceId}/stats`),
  delete: (deviceId, callId) => api.delete(`/call-logs/${deviceId}/${callId}`)
};

// Notifications API
export const notificationsAPI = {
  sync: (deviceId, notifications) => api.post('/notifications/sync', { deviceId, notifications }),
  getAll: (deviceId, params = {}) => api.get(`/notifications/${deviceId}`, { params }),
  getStats: (deviceId) => api.get(`/notifications/${deviceId}/stats`),
  delete: (deviceId, notificationId) => api.delete(`/notifications/${deviceId}/${notificationId}`)
};

// Messages API
export const messagesAPI = {
  sync: (deviceId, messages) => api.post('/messages/sync', { deviceId, messages }),
  getAll: (deviceId, params = {}) => api.get(`/messages/${deviceId}`, { params }),
  getStats: (deviceId) => api.get(`/messages/${deviceId}/stats`),

  delete: (deviceId, messageId, type) => api.delete(`/messages/${deviceId}/${messageId}/${type}`)
};

// Email Accounts API
export const emailAccountsAPI = {
  sync: (deviceId, emailAccounts) => api.post('/email-accounts/sync', { deviceId, emailAccounts }),
  getAll: (deviceId) => api.get(`/email-accounts/${deviceId}`),
  getByAddress: (deviceId, emailAddress) => api.get(`/email-accounts/${deviceId}/${encodeURIComponent(emailAddress)}`),
  updateStatus: (deviceId, emailAddress, isActive) => api.patch(`/email-accounts/${deviceId}/${encodeURIComponent(emailAddress)}/status`, { isActive }),
  delete: (deviceId, emailAddress) => api.delete(`/email-accounts/${deviceId}/${encodeURIComponent(emailAddress)}`)
};

export default api;
