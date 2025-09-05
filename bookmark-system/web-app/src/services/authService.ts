import api from './api';

export interface User {
  id: string;
  email: string;
  name: string;
  picture?: string;
}

export const authService = {
  // Google OAuth 로그인 URL로 리다이렉트
  googleLogin: () => {
    // location.replace를 사용하여 현재 페이지를 히스토리에서 대체
    // 이렇게 하면 뒤로가기 시 OAuth URL이 아닌 이전 페이지로 이동
    window.location.replace('http://localhost:8080/api/oauth2/authorization/google');
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
    try {
      await api.post('/auth/logout');
      // 프론트엔드 로그인 페이지로 리다이렉트 (포트 3000)
      window.location.href = 'http://localhost:3000/login';
    } catch (error) {
      console.error('Logout failed:', error);
      // 에러가 발생해도 로그인 페이지로 이동
      window.location.href = 'http://localhost:3000/login';
    }
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