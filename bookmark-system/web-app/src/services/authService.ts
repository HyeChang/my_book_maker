import api from './api';

export interface User {
  id: string;
  email: string;
  name: string;
  picture?: string;
}

const authService = {
  // Google OAuth 로그인 URL로 리다이렉트
  googleLogin: () => {
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  },

  // 현재 로그인한 사용자 정보 가져오기
  getCurrentUser: async (): Promise<User | null> => {
    try {
      const response = await api.get('/auth/user');
      return response.data;
    } catch (error) {
      return null;
    }
  },

  // 로그아웃
  logout: async (): Promise<void> => {
    await api.post('/auth/logout');
    window.location.href = '/login';
  },

  // Drive 초기화 상태 확인
  checkDriveInitialized: async (): Promise<boolean> => {
    try {
      const response = await api.get('/drive/init');
      return response.data.initialized;
    } catch (error) {
      return false;
    }
  },

  // Drive 초기화
  initializeDrive: async (): Promise<void> => {
    await api.post('/drive/init');
  },
};

export default authService;