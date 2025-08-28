import React, { useState } from 'react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline, Box, CircularProgress, Typography } from '@mui/material';
import { AuthProvider, useAuth } from '../context/AuthContext';
import Login from '../components/Login';

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
    background: {
      default: '#f5f5f5',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
  },
});

const AppContent = () => {
  const { user, loading, isAuthenticated, logout } = useAuth();
  const [currentView, setCurrentView] = useState('login');

  if (loading) {
    return (
      <Box 
        display="flex" 
        flexDirection="column"
        justifyContent="center" 
        alignItems="center" 
        minHeight="100vh"
        gap={2}
      >
        <CircularProgress size={60} />
        <Typography variant="h6">Loading...</Typography>
      </Box>
    );
  }

  if (!isAuthenticated) {
    return (
      <Login 
        onLoginSuccess={(userData, token) => {
          console.log('Login successful:', userData);
        }}
        switchToRegister={() => setCurrentView('register')}
      />
    );
  }

  return (
    <Box sx={{ minHeight: '100vh', backgroundColor: 'background.default' }}>
      <Box 
        sx={{ 
          backgroundColor: 'primary.main', 
          color: 'white', 
          p: 2,
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}
      >
        <Typography variant="h5" component="h1">
          Hotel Manager
        </Typography>
        
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Typography variant="body1">
            Welcome, {user?.firstName || user?.name || user?.email}!
          </Typography>
          
          <button 
            onClick={logout}
            style={{
              padding: '8px 16px',
              backgroundColor: 'transparent',
              color: 'white',
              border: '1px solid white',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Logout
          </button>
        </Box>
      </Box>

      <Box sx={{ p: 3 }}>
        <Typography variant="h4" component="h2" sx={{ mb: 3 }}>
          Dashboard
        </Typography>
        
        <Box sx={{ 
          backgroundColor: 'white', 
          p: 3, 
          borderRadius: 2, 
          boxShadow: 1,
          mb: 3
        }}>
          <Typography variant="h6" sx={{ mb: 2 }}>
            User information:
          </Typography>
          <pre style={{ 
            backgroundColor: '#f5f5f5', 
            padding: '10px', 
            borderRadius: '4px',
            fontSize: '14px'
          }}>
            {JSON.stringify(user, null, 2)}
          </pre>
        </Box>

        <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
          <button 
            style={{
              padding: '12px 24px',
              backgroundColor: '#1976d2',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '16px'
            }}
          >
            My Reservations
          </button>
          
          <button 
            style={{
              padding: '12px 24px',
              backgroundColor: '#2e7d32',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '16px'
            }}
          >
            Browse Rooms
          </button>
          
          {user?.role === 'ADMIN' && (
            <button 
              style={{
                padding: '12px 24px',
                backgroundColor: '#ed6c02',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '16px'
              }}
            >
              Admin Panel
            </button>
          )}
        </Box>

        <Box sx={{ 
          mt: 4, 
          p: 2, 
          backgroundColor: '#e8f5e8', 
          borderRadius: 1,
          border: '1px solid #4caf50'
        }}>
          <Typography variant="body2" color="success.main">
            Connection to backend API working correctly!
          </Typography>
          <Typography variant="caption" display="block" sx={{ mt: 1 }}>
            Backend: http://localhost:8080 | Frontend: http://localhost:5173
          </Typography>
        </Box>
      </Box>
    </Box>
  );
};

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;