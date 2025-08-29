import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  IconButton,
  Box,
  Menu,
  MenuItem,
  Avatar,
  Chip,
  useTheme,
  useMediaQuery
} from '@mui/material';
import {
  Hotel,
  Dashboard,
  Search,
  BookOnline,
  AdminPanelSettings,
  AccountCircle,
  Logout,
  Menu as MenuIcon
} from '@mui/icons-material';

const Navigation = () => {
  const { user, logout, loading } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  
  const [anchorEl, setAnchorEl] = useState(null);
  const [mobileMenuAnchor, setMobileMenuAnchor] = useState(null);

  const handleProfileMenuOpen = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMobileMenuOpen = (event) => {
    setMobileMenuAnchor(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setMobileMenuAnchor(null);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
    handleMenuClose();
  };

  const handleNavigation = (path) => {
    navigate(path);
    handleMenuClose();
  };

  const isActive = (path) => location.pathname === path;

  const navItems = [
    { path: '/dashboard', label: 'Dashboard', icon: <Dashboard /> },
    { path: '/rooms', label: 'Browse Rooms', icon: <Search /> },
    { path: '/reservations', label: 'My Reservations', icon: <BookOnline /> },
  ];

  if (user?.role === 'ADMIN') {
    navItems.push({ path: '/admin', label: 'Admin Panel', icon: <AdminPanelSettings /> });
  }

  const renderDesktopNavigation = () => (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
      {navItems.map((item) => (
        <Button
          key={item.path}
          startIcon={item.icon}
          onClick={() => handleNavigation(item.path)}
          sx={{
            color: 'white',
            backgroundColor: isActive(item.path) ? 'rgba(255, 255, 255, 0.1)' : 'transparent',
            '&:hover': {
              backgroundColor: 'rgba(255, 255, 255, 0.2)',
            },
            borderRadius: 2,
            px: 2,
            py: 1,
          }}
        >
          {item.label}
        </Button>
      ))}
    </Box>
  );

  const renderMobileNavigation = () => (
    <>
      <IconButton
        color="inherit"
        onClick={handleMobileMenuOpen}
        sx={{ ml: 'auto' }}
      >
        <MenuIcon />
      </IconButton>
      
      <Menu
        anchorEl={mobileMenuAnchor}
        open={Boolean(mobileMenuAnchor)}
        onClose={handleMenuClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'right',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'right',
        }}
      >
        {navItems.map((item) => (
          <MenuItem
            key={item.path}
            onClick={() => handleNavigation(item.path)}
            selected={isActive(item.path)}
            sx={{
              minWidth: 200,
              '&.Mui-selected': {
                backgroundColor: 'primary.light',
                color: 'white',
              },
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              {item.icon}
              {item.label}
            </Box>
          </MenuItem>
        ))}
      </Menu>
    </>
  );

  return (
    <AppBar position="fixed" sx={{ zIndex: theme.zIndex.drawer + 1 }}>
      <Toolbar>
        {/* Logo */}
        <Box sx={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }} onClick={() => navigate('/dashboard')}>
          <Hotel sx={{ fontSize: 32, mr: 1 }} />
          <Typography variant="h6" component="div" sx={{ fontWeight: 'bold' }}>
            Hotel Manager
          </Typography>
        </Box>

        {/* Desktop Navigation */}
        {!isMobile && (
          <Box sx={{ ml: 4, flexGrow: 1 }}>
            {renderDesktopNavigation()}
          </Box>
        )}

        {/* User Info & Mobile Menu */}
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          {/* User Role Badge */}
          <Chip
            label={user?.role || 'USER'}
            color={user?.role === 'ADMIN' ? 'secondary' : 'default'}
            size="small"
            sx={{ 
              color: 'white',
              backgroundColor: user?.role === 'ADMIN' ? 'secondary.main' : 'rgba(255, 255, 255, 0.2)',
              fontWeight: 'bold'
            }}
          />

          {/* User Email */}
          <Typography variant="body2" sx={{ color: 'white', display: { xs: 'none', sm: 'block' } }}>
            {loading ? 'Loading...' : user?.email || 'Not logged in'}
          </Typography>

          {/* Profile Menu */}
          <IconButton
            onClick={handleProfileMenuOpen}
            sx={{ color: 'white' }}
          >
            <Avatar sx={{ width: 32, height: 32, bgcolor: 'primary.light' }}>
              <AccountCircle />
            </Avatar>
          </IconButton>

          {/* Mobile Menu */}
          {isMobile && renderMobileNavigation()}
        </Box>

        {/* Profile Dropdown Menu */}
        <Menu
          anchorEl={anchorEl}
          open={Boolean(anchorEl)}
          onClose={handleMenuClose}
          anchorOrigin={{
            vertical: 'bottom',
            horizontal: 'right',
          }}
          transformOrigin={{
            vertical: 'top',
            horizontal: 'right',
          }}
        >
          <MenuItem onClick={() => handleNavigation('/dashboard')}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Dashboard />
              Dashboard
            </Box>
          </MenuItem>
          <MenuItem onClick={handleLogout}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Logout />
              Logout
            </Box>
          </MenuItem>
        </Menu>
      </Toolbar>
    </AppBar>
  );
};

export default Navigation;
