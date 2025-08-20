import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Google OAuth 인증을 위해 필요
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    // 필요시 토큰 추가 로직
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
    if (error.response?.status === 401) {
      // 인증 에러 처리
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;