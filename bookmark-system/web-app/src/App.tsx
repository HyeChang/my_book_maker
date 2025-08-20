import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';
import { AuthProvider } from './contexts/AuthContext';
import Layout from './components/Layout';
import PrivateRoute from './components/PrivateRoute';
import LoginPage from './pages/LoginPage';
import BookmarksPage from './pages/BookmarksPage';
import BookmarkFormPage from './pages/BookmarkFormPage';
import FoldersPage from './pages/FoldersPage';
import TagsPage from './pages/TagsPage';
import SearchPage from './pages/SearchPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <AuthProvider>
          <Router>
            <Routes>
              <Route path="/login" element={<LoginPage />} />
              <Route
                path="/"
                element={
                  <PrivateRoute>
                    <Layout />
                  </PrivateRoute>
                }
              >
                <Route index element={<BookmarksPage />} />
                <Route path="bookmarks/new" element={<BookmarkFormPage />} />
                <Route path="bookmarks/edit/:id" element={<BookmarkFormPage />} />
                <Route path="folders" element={<FoldersPage />} />
                <Route path="tags" element={<TagsPage />} />
                <Route path="settings" element={<div>설정 페이지 (개발 중)</div>} />
                <Route path="search" element={<SearchPage />} />
              </Route>
            </Routes>
          </Router>
        </AuthProvider>
      </ThemeProvider>
    </QueryClientProvider>
  );
}

export default App
