import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';


const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, 
});


api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwt_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    console.log('🚀 API Request:', config.method.toUpperCase(), config.url);
    return config;
  },
  (error) => {
    console.error('❌ Request Error:', error);
    return Promise.reject(error);
  }
);


api.interceptors.response.use(
  (response) => {
    console.log('✅ API Response:', response.status, response.config.url);
    return response;
  },
  (error) => {
    console.error('❌ API Error:', error.response?.status, error.response?.data);
    
    if (error.response?.status === 401) {

      localStorage.removeItem('jwt_token');

      console.warn('🔒 Unauthorized - token removed');
    }
    return Promise.reject(error);
  }
);

// API endpoints 
export const hotelAPI = {
  // Auth endpoints
  auth: {
    register: (userData) => api.post('/auth/register', userData),
    login: (credentials) => api.post('/auth/login', credentials),
    verify: (verificationData) => api.post('/auth/verify', verificationData),
    resend: (email) => api.post('/auth/resend', { email }),
  },

  // User endpoints
  users: {
    getMe: () => api.get('/users/me'),
    getMyReservations: () => api.get('/users/me/reservations'),
    getAllUsers: () => api.get('/users'), // Admin only
    getUser: (userId) => api.get(`/users/${userId}`), // Admin only
    getUserReservations: (userId) => api.get(`/users/${userId}/reservations`), // Admin only
  },

  // Room endpoints
  rooms: {
    search: (params) => {

      const cleanParams = Object.fromEntries(
        Object.entries(params || {}).filter(([_, value]) => value !== '' && value != null)
      );
      return api.get('/rooms', { params: cleanParams });
    },
    getAll: () => api.get('/rooms'), 
    create: (roomData) => api.post('/rooms', roomData), // Admin only
    update: (roomId, roomData) => api.patch(`/rooms/${roomId}`, roomData), // Admin only
    delete: (roomId) => api.delete(`/rooms/${roomId}`), // Admin only
  },


  reservations: {
    create: (reservationData) => api.post('/reservations', reservationData),
    confirm: (reservationId) => api.patch(`/reservations/${reservationId}/confirm`),
    cancel: (reservationId) => api.patch(`/reservations/${reservationId}/cancel`),
  },
};

// Helper functions
export const apiHelpers = {

  isLoggedIn: () => {
    return !!localStorage.getItem('jwt_token');
  },


  getToken: () => {
    return localStorage.getItem('jwt_token');
  },


  setToken: (token) => {
    localStorage.setItem('jwt_token', token);
  },


  removeToken: () => {
    localStorage.removeItem('jwt_token');
  },


  healthCheck: async () => {
    try {
      const response = await api.get('/actuator/health').catch(() => {
        return api.get('/rooms', { params: {} });
      });
      return response.status === 200;
    } catch (error) {
      return false;
    }
  }
};

export default api;