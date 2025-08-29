import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { hotelAPI, apiHelpers } from '../services/api';
import {
  Box,
  Paper,
  TextField,
  Button,
  Typography,
  Alert,
  InputAdornment,
  IconButton,
  CircularProgress,
  Stepper,
  Step,
  StepLabel
} from '@mui/material';
import { 
  Visibility, 
  VisibilityOff, 
  Email, 
  Lock, 
  Hotel, 
  CheckCircle 
} from '@mui/icons-material';

const Register = () => {
  const navigate = useNavigate();
  const [activeStep, setActiveStep] = useState(0);
  const [formData, setFormData] = useState({ 
    email: '', 
    password: '', 
    confirmPassword: '' 
  });
  const [verificationCode, setVerificationCode] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const steps = ['Account Details', 'Email Verification'];

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (error) setError('');
  };

  const validateForm = () => {
    if (!formData.email || !formData.password || !formData.confirmPassword) {
      setError('Please fill all fields');
      return false;
    }

    if (!formData.email.includes('@')) {
      setError('Please enter valid email');
      return false;
    }

    if (formData.password.length < 6) {
      setError('Password must be at least 6 characters');
      return false;
    }

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match');
      return false;
    }

    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    setLoading(true);
    setError('');
    setSuccess('');

    try {
      await hotelAPI.auth.register({
        email: formData.email,
        password: formData.password
      });

      setSuccess('Registration successful! Please check your email for verification code.');
      setActiveStep(1);
    } catch (err) {
      if (err.response?.status === 409) {
        setError('User with this email already exists');
      } else if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.code === 'ECONNREFUSED') {
        setError('Cannot connect to server.');
      } else {
        setError('Registration error occurred. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleVerification = async (e) => {
    e.preventDefault();
    
    if (!verificationCode) {
      setError('Please enter verification code');
      return;
    }

    setLoading(true);
    setError('');
    setSuccess('');

    try {
      await hotelAPI.auth.verify({
        email: formData.email,
        verificationCode: verificationCode
      });

      setSuccess('Email verified successfully! You can now login.');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      if (err.response?.status === 400) {
        setError(err.response.data || 'Invalid verification code');
      } else if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError('Verification error occurred. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleResendCode = async () => {
    setLoading(true);
    setError('');
    
    try {
      await hotelAPI.auth.resend(formData.email);
      setSuccess('Verification code resent successfully!');
    } catch (err) {
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError('Failed to resend verification code');
      }
    } finally {
      setLoading(false);
    }
  };

  const renderStepContent = (step) => {
    switch (step) {
      case 0:
        return (
          <Box component="form" onSubmit={handleSubmit} sx={{ width: '100%' }}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="email"
              label="Email"
              name="email"
              type="email"
              autoComplete="email"
              autoFocus
              value={formData.email}
              onChange={handleChange}
              disabled={loading}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start"><Email /></InputAdornment>
                ),
              }}
              sx={{ mb: 2 }}
            />

            <TextField
              margin="normal"
              required
              fullWidth
              name="password"
              label="Password"
              type={showPassword ? 'text' : 'password'}
              id="password"
              autoComplete="new-password"
              value={formData.password}
              onChange={handleChange}
              disabled={loading}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start"><Lock /></InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      aria-label="toggle password visibility"
                      onClick={() => setShowPassword(!showPassword)}
                      edge="end"
                    >
                      {showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
              sx={{ mb: 2 }}
            />

            <TextField
              margin="normal"
              required
              fullWidth
              name="confirmPassword"
              label="Confirm Password"
              type={showConfirmPassword ? 'text' : 'password'}
              id="confirmPassword"
              autoComplete="new-password"
              value={formData.confirmPassword}
              onChange={handleChange}
              disabled={loading}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start"><Lock /></InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      aria-label="toggle confirm password visibility"
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      edge="end"
                    >
                      {showConfirmPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
              sx={{ mb: 3 }}
            />

            <Button
              type="submit"
              fullWidth
              variant="contained"
              disabled={loading}
              sx={{ mt: 2, mb: 2, py: 1.5, fontSize: '1.1rem' }}
            >
              {loading ? (
                <>
                  <CircularProgress size={20} sx={{ mr: 1 }} />
                  Creating Account...
                </>
              ) : 'Create Account'}
            </Button>
          </Box>
        );

      case 1:
        return (
          <Box component="form" onSubmit={handleVerification} sx={{ width: '100%' }}>
            <Typography variant="body1" sx={{ mb: 3, textAlign: 'center' }}>
              We've sent a verification code to <strong>{formData.email}</strong>
            </Typography>

            <TextField
              margin="normal"
              required
              fullWidth
              id="verificationCode"
              label="Verification Code"
              name="verificationCode"
              type="text"
              autoComplete="off"
              autoFocus
              value={verificationCode}
              onChange={(e) => setVerificationCode(e.target.value)}
              disabled={loading}
              sx={{ mb: 3 }}
            />

            <Button
              type="submit"
              fullWidth
              variant="contained"
              disabled={loading}
              sx={{ mt: 2, mb: 2, py: 1.5, fontSize: '1.1rem' }}
            >
              {loading ? (
                <>
                  <CircularProgress size={20} sx={{ mr: 1 }} />
                  Verifying...
                </>
              ) : 'Verify Email'}
            </Button>

            <Button
              fullWidth
              variant="outlined"
              onClick={handleResendCode}
              disabled={loading}
              sx={{ mb: 2 }}
            >
              Resend Code
            </Button>
          </Box>
        );

      default:
        return null;
    }
  };

  return (
    <Box
      sx={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        width: '100vw',
        height: '100vh',
        backgroundColor: '#f5f5f5'
      }}
    >
      <Paper
        elevation={3}
        sx={{
          padding: 4,
          width: 450,
          borderRadius: 2
        }}
      >
        {/* Logo/Header */}
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', mb: 3 }}>
          <Hotel sx={{ fontSize: 40, color: 'primary.main', mr: 1 }} />
          <Typography variant="h4" component="h1" fontWeight="bold">
            Hotel Manager
          </Typography>
        </Box>

        <Typography variant="h5" component="h2" align="center" sx={{ mb: 3 }}>
          Create Account
        </Typography>

        {/* Stepper */}
        <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
          {steps.map((label) => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

        {renderStepContent(activeStep)}

        <Box sx={{ textAlign: 'center', mt: 2 }}>
                      <Typography variant="body2">
              Already have an account?{' '}
              <Button
                variant="text"
                onClick={() => navigate('/login')}
                disabled={loading}
                sx={{ textTransform: 'none' }}
              >
                Login
              </Button>
            </Typography>
        </Box>
      </Paper>
    </Box>
  );
};

export default Register;
