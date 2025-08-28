import React, { createContext, useContext, useState, useEffect } from 'react';
import { hotelAPI, apiHelpers } from '../services/api';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    const initializeAuth = async () => {
      const token = apiHelpers.getToken();
      
      if (token) {
        try {
          const response = await hotelAPI.users.getMe();
          setUser(response.data);
          setIsAuthenticated(true);
          console.log('User authenticated:', response.data);
        } catch (error) {
          console.error('Token invalid:', error);
          apiHelpers.removeToken();
          setUser(null);
          setIsAuthenticated(false);
        }
      }
      
      setLoading(false);
    };

    initializeAuth();
  }, []);

  const login = async (email, password) => {
    try {
      const response = await hotelAPI.auth.login({ email, password });
      const { token, expiresIn } = response.data;
      
      if (token) {
        apiHelpers.setToken(token);
        
        const userResponse = await hotelAPI.users.getMe();
        setUser(userResponse.data);
        setIsAuthenticated(true);
        return { success: true, user: userResponse.data };
      }
      
      return { success: false, error: 'No token in response' };
    } catch (error) {
      console.error('Login error:', error);
      return { 
        success: false, 
        error: error.response?.data?.message || 'Login error' 
      };
    }
  };

  const register = async (userData) => {
    try {
      const response = await hotelAPI.auth.register(userData);
      return { success: true, data: response.data };
    } catch (error) {
      console.error('Register error:', error);
      return { 
        success: false, 
        error: error.response?.data?.message || 'Registration error' 
      };
    }
  };

  const logout = () => {
    apiHelpers.removeToken();
    setUser(null);
    setIsAuthenticated(false);
    console.log('User logged out');
  };

  const verifyEmail = async (verificationData) => {
    try {
      const response = await hotelAPI.auth.verify(verificationData);
      return { success: true, data: response.data };
    } catch (error) {
      console.error('Verify error:', error);
      return { 
        success: false, 
        error: error.response?.data?.message || 'Verification error' 
      };
    }
  };

  const resendVerification = async (email) => {
    try {
      const response = await hotelAPI.auth.resend(email);
      return { success: true, data: response.data };
    } catch (error) {
      console.error('Resend error:', error);
      return { 
        success: false, 
        error: error.response?.data?.message || 'Resend error' 
      };
    }
  };

  const isAdmin = () => {
    return user?.role === 'ADMIN' || user?.roles?.includes('ADMIN');
  };

  const refreshUser = async () => {
    if (!isAuthenticated) return;
    
    try {
      const response = await hotelAPI.users.getMe();
      setUser(response.data);
    } catch (error) {
      console.error('Refresh user error:', error);
      if (error.response?.status === 401) {
        logout();
      }
    }
  };

  const value = {
    user,
    loading,
    isAuthenticated,
    login,
    register,
    logout,
    verifyEmail,
    resendVerification,
    refreshUser,
    isAdmin,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export default AuthContext;