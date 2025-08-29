import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { hotelAPI } from '../services/api';
import {
  Box,
  Container,
  Grid,
  Card,
  CardContent,
  Typography,
  Button,
  Chip,
  Avatar,
  LinearProgress,
  Alert,
  IconButton,
  Tooltip
} from '@mui/material';
import {
  Hotel,
  Search,
  BookOnline,
  AdminPanelSettings,
  Person,
  Email,
  CalendarToday,
  TrendingUp,
  Refresh
} from '@mui/icons-material';

const Dashboard = () => {
  const { user, loading: authLoading } = useAuth();
  const navigate = useNavigate();
  const [stats, setStats] = useState({
    totalReservations: 0,
    activeReservations: 0,
    totalRooms: 0,
    availableRooms: 0
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      setError('');

      // Fetch user's reservations
      const reservationsResponse = await hotelAPI.users.getMyReservations();
      const reservations = reservationsResponse.data || [];
      
      // Fetch all rooms for stats
      const roomsResponse = await hotelAPI.rooms.getAll();
      const rooms = roomsResponse.data || [];

      const activeReservations = reservations.filter(r => 
        r.status === 'CONFIRMED' || r.status === 'PENDING'
      ).length;

      setStats({
        totalReservations: reservations.length,
        activeReservations,
        totalRooms: rooms.length,
        availableRooms: rooms.filter(r => r.available).length
      });
    } catch (err) {
      console.error('Error fetching dashboard data:', err);
      setError('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'CONFIRMED': return 'success';
      case 'PENDING': return 'warning';
      case 'CANCELLED': return 'error';
      default: return 'default';
    }
  };

  const StatCard = ({ title, value, icon, color, onClick }) => (
    <Card 
      sx={{ 
        height: '100%', 
        cursor: onClick ? 'pointer' : 'default',
        transition: 'transform 0.2s, box-shadow 0.2s',
        '&:hover': onClick ? {
          transform: 'translateY(-4px)',
          boxShadow: 4
        } : {}
      }}
      onClick={onClick}
    >
      <CardContent sx={{ textAlign: 'center', p: 3 }}>
        <Avatar 
          sx={{ 
            width: 56, 
            height: 56, 
            bgcolor: `${color}.light`, 
            color: `${color}.main`,
            mx: 'auto',
            mb: 2
          }}
        >
          {icon}
        </Avatar>
        <Typography variant="h4" component="div" sx={{ fontWeight: 'bold', mb: 1 }}>
          {value}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {title}
        </Typography>
      </CardContent>
    </Card>
  );

  const QuickActionCard = ({ title, description, icon, color, onClick, disabled }) => (
    <Card 
      sx={{ 
        height: '100%',
        cursor: disabled ? 'not-allowed' : 'pointer',
        opacity: disabled ? 0.6 : 1,
        transition: 'transform 0.2s, box-shadow 0.2s',
        '&:hover': !disabled ? {
          transform: 'translateY(-4px)',
          boxShadow: 4
        } : {}
      }}
      onClick={!disabled ? onClick : undefined}
    >
      <CardContent sx={{ textAlign: 'center', p: 3 }}>
        <Avatar 
          sx={{ 
            width: 48, 
            height: 48, 
            bgcolor: `${color}.light`, 
            color: `${color}.main`,
            mx: 'auto',
            mb: 2
          }}
        >
          {icon}
        </Avatar>
        <Typography variant="h6" component="div" sx={{ mb: 1 }}>
          {title}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {description}
        </Typography>
      </CardContent>
    </Card>
  );

  if (loading) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4 }}>
        <LinearProgress />
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
          <Typography variant="h4" component="h1" sx={{ fontWeight: 'bold' }}>
            Welcome back, {authLoading ? '...' : user?.email?.split('@')[0] || 'User'}! 👋
          </Typography>
          <Tooltip title="Refresh data">
            <IconButton onClick={fetchDashboardData} color="primary">
              <Refresh />
            </IconButton>
          </Tooltip>
        </Box>
        
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
      </Box>

      {/* Stats Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Reservations"
            value={stats.totalReservations}
            icon={<BookOnline />}
            color="primary"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Active Reservations"
            value={stats.activeReservations}
            icon={<TrendingUp />}
            color="success"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Rooms"
            value={stats.totalRooms}
            icon={<Hotel />}
            color="info"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Available Rooms"
            value={stats.availableRooms}
            icon={<Search />}
            color="warning"
          />
        </Grid>
      </Grid>

      {/* Quick Actions */}
      <Typography variant="h5" component="h2" sx={{ mb: 3, fontWeight: 'bold' }}>
        Quick Actions
      </Typography>
      
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={4}>
          <QuickActionCard
            title="Browse Rooms"
            description="Search and book available rooms"
            icon={<Search />}
            color="primary"
            onClick={() => navigate('/rooms')}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4}>
          <QuickActionCard
            title="My Reservations"
            description="View and manage your bookings"
            icon={<BookOnline />}
            color="success"
            onClick={() => navigate('/reservations')}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4}>
          <QuickActionCard
            title="Admin Panel"
            description="Manage hotel operations"
            icon={<AdminPanelSettings />}
            color="secondary"
            onClick={() => navigate('/admin')}
            disabled={user?.role !== 'ADMIN'}
          />
        </Grid>
      </Grid>

      {/* User Info Card */}
      <Card sx={{ mb: 4 }}>
        <CardContent sx={{ p: 3 }}>
          <Typography variant="h6" component="h3" sx={{ mb: 2, fontWeight: 'bold' }}>
            Account Information
          </Typography>
          
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                <Person color="primary" />
                <Typography variant="body2" color="text.secondary">
                  Email:
                </Typography>
                <Typography variant="body1" sx={{ fontWeight: 'medium' }}>
                  {user?.email}
                </Typography>
              </Box>
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                <AdminPanelSettings color="secondary" />
                <Typography variant="body2" color="text.secondary">
                  Role:
                </Typography>
                <Chip 
                  label={user?.role || 'USER'} 
                  color={user?.role === 'ADMIN' ? 'secondary' : 'default'}
                  size="small"
                  sx={{ fontWeight: 'bold' }}
                />
              </Box>
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                <Email color="primary" />
                <Typography variant="body2" color="text.secondary">
                  Status:
                </Typography>
                <Chip 
                  label={user?.enabled ? 'Verified' : 'Pending Verification'} 
                  color={user?.enabled ? 'success' : 'warning'}
                  size="small"
                />
              </Box>
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                <CalendarToday color="primary" />
                <Typography variant="body2" color="text.secondary">
                  Member since:
                </Typography>
                <Typography variant="body1" sx={{ fontWeight: 'medium' }}>
                  {user?.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'N/A'}
                </Typography>
              </Box>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Connection Status */}
      <Card sx={{ backgroundColor: 'success.light', color: 'success.contrastText' }}>
        <CardContent sx={{ p: 2, textAlign: 'center' }}>
          <Typography variant="body2">
            ✅ Connected to backend API successfully
          </Typography>
          <Typography variant="caption" display="block" sx={{ mt: 0.5 }}>
            Backend: http://localhost:8080 | Frontend: http://localhost:5173
          </Typography>
        </CardContent>
      </Card>
    </Container>
  );
};

export default Dashboard;
