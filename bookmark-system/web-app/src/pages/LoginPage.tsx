import React from 'react';
import { Navigate } from 'react-router-dom';
import {
  Box,
  Button,
  Card,
  CardContent,
  Typography,
  Container,
} from '@mui/material';
import { Google as GoogleIcon } from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';

const LoginPage: React.FC = () => {
  const { user, login, loading } = useAuth();

  if (loading) {
    return null;
  }

  if (user) {
    return <Navigate to="/" replace />;
  }

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          alignItems: 'center',
        }}
      >
        <Card sx={{ width: '100%', p: 3 }}>
          <CardContent>
            <Typography variant="h4" component="h1" gutterBottom textAlign="center">
              북마크 관리 시스템
            </Typography>
            <Typography variant="body1" color="text.secondary" textAlign="center" sx={{ mb: 4 }}>
              Google Drive와 연동된 개인 북마크 관리 서비스
            </Typography>
            
            <Box sx={{ display: 'flex', justifyContent: 'center' }}>
              <Button
                variant="contained"
                size="large"
                startIcon={<GoogleIcon />}
                onClick={login}
                sx={{
                  backgroundColor: '#4285f4',
                  '&:hover': {
                    backgroundColor: '#357ae8',
                  },
                  textTransform: 'none',
                  px: 4,
                  py: 1.5,
                }}
              >
                Google 계정으로 로그인
              </Button>
            </Box>

            <Typography variant="body2" color="text.secondary" textAlign="center" sx={{ mt: 4 }}>
              로그인하면 Google Drive에 북마크 데이터가 자동으로 저장됩니다.
            </Typography>
          </CardContent>
        </Card>
      </Box>
    </Container>
  );
};

export default LoginPage;