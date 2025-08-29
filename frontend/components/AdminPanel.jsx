import React, { useState, useEffect } from 'react';
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
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Tabs,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Switch,
  FormControlLabel
} from '@mui/material';
import {
  AdminPanelSettings,
  People,
  Hotel,
  BookOnline,
  Add,
  Edit,
  Delete,
  Refresh,
  CheckCircle,
  Cancel,
  Warning,
  AttachMoney,
  CalendarToday
} from '@mui/icons-material';

const AdminPanel = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  // Users state
  const [users, setUsers] = useState([]);
  const [usersLoading, setUsersLoading] = useState(false);
  const [userIdFilter, setUserIdFilter] = useState('');
  
  // Rooms state
  const [rooms, setRooms] = useState([]);
  const [roomsLoading, setRoomsLoading] = useState(false);
  const [roomDialog, setRoomDialog] = useState({
    open: false,
    mode: 'create',
    room: null
  });
  const [roomForm, setRoomForm] = useState({
    roomNumber: '',
    type: 'SINGLE',
    capacity: 1,
    pricePerNight: '',
    description: ''
  });
  
  // Reservations state
  const [reservations, setReservations] = useState([]);
  const [reservationsLoading, setReservationsLoading] = useState(false);
  const [reservationUserIdFilter, setReservationUserIdFilter] = useState('');

  useEffect(() => {
    fetchAllData();
  }, []);

  const fetchAllData = async () => {
    try {
      setLoading(true);
      await Promise.all([
        fetchUsers(),
        fetchRooms(),
        fetchReservations()
      ]);
    } catch (err) {
      console.error('Error fetching admin data:', err);
      setError('Failed to load admin data');
    } finally {
      setLoading(false);
    }
  };

  const fetchUsers = async () => {
    try {
      setUsersLoading(true);
      const response = await hotelAPI.users.getAllUsers();
      setUsers(response.data || []);
    } catch (err) {
      console.error('Error fetching users:', err);
      setError('Failed to load users');
    } finally {
      setUsersLoading(false);
    }
  };

  const fetchRooms = async () => {
    try {
      setRoomsLoading(true);
      const response = await hotelAPI.rooms.getAll();
      setRooms(response.data || []);
    } catch (err) {
      console.error('Error fetching rooms:', err);
      setError('Failed to load rooms');
    } finally {
      setRoomsLoading(false);
    }
  };

  const fetchReservations = async () => {
    try {
      setReservationsLoading(true);
      // Admin endpoint: get all reservations
      const [resAll, usersRes] = await Promise.all([
        hotelAPI.reservations.getAll(),
        hotelAPI.users.getAllUsers()
      ]);
      const usersMap = new Map((usersRes.data || []).map(u => [u.email, { id: u.id, email: u.email }]));
      const list = (resAll.data || []).map(r => ({
        ...r,
        userId: usersMap.get(r.email)?.id || null,
        userEmail: r.email
      }));
      setReservations(list);
    } catch (err) {
      setError('Failed to load reservations');
    } finally {
      setReservationsLoading(false);
    }
  };

  // Room management
  const openRoomDialog = (mode, room = null) => {
    if (mode === 'edit' && room) {
      setRoomForm({
        roomNumber: room.roomNumber,
        type: room.type || room.roomType,
        capacity: room.capacity,
        pricePerNight: room.pricePerNight,
        description: room.description || ''
      });
    } else {
      setRoomForm({
        roomNumber: '',
        type: 'SINGLE',
        capacity: 1,
        pricePerNight: '',
        description: ''
      });
    }
    setRoomDialog({ open: true, mode, room });
  };

  const closeRoomDialog = () => {
    setRoomDialog({ open: false, mode: 'create', room: null });
    setRoomForm({
      roomNumber: '',
      type: 'SINGLE',
      capacity: 1,
      pricePerNight: '',
      description: ''
    });
  };

  const handleRoomSubmit = async () => {
    try {
      setLoading(true);
      
      // Map frontend fields to backend fields
      const roomData = {
        roomNumber: parseInt(roomForm.roomNumber),
        roomType: roomForm.type,
        capacity: parseInt(roomForm.capacity),
        pricePerNight: parseFloat(roomForm.pricePerNight),
        description: roomForm.description
      };
      
      if (roomDialog.mode === 'create') {
        await hotelAPI.rooms.create(roomData);
      } else {
        await hotelAPI.rooms.update(roomDialog.room.id, roomData);
      }
      
      await fetchRooms();
      closeRoomDialog();
    } catch (err) {
      console.error('Error saving room:', err);
      setError('Failed to save room');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteRoom = async (roomId) => {
    if (!window.confirm('Are you sure you want to delete this room?')) return;
    
    try {
      setLoading(true);
      await hotelAPI.rooms.delete(roomId);
      await fetchRooms();
    } catch (err) {
      console.error('Error deleting room:', err);
      setError('Failed to delete room');
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

  const getRoomTypeColor = (type) => {
    switch (type) {
      case 'SINGLE': return 'primary';
      case 'DOUBLE': return 'secondary';
      case 'SUITE': return 'success';
      case 'DELUXE': return 'warning';
      default: return 'default';
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const TabPanel = ({ children, value, index }) => (
    <div role="tabpanel" hidden={value !== index}>
      {value === index && <Box sx={{ pt: 3 }}>{children}</Box>}
    </div>
  );

  if (loading && !users.length && !rooms.length) {
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
        <Typography variant="h4" component="h1" sx={{ fontWeight: 'bold', mb: 2 }}>
          Admin Panel
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Manage hotel operations, users, rooms, and reservations
        </Typography>
      </Box>

      {/* Error Alert */}
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Tabs */}
      <Card sx={{ mb: 4 }}>
        <Tabs value={activeTab} onChange={(e, newValue) => setActiveTab(newValue)}>
          <Tab label="Users" icon={<People />} />
          <Tab label="Rooms" icon={<Hotel />} />
          <Tab label="Reservations" icon={<BookOnline />} />
        </Tabs>
      </Card>

      {/* Users Tab */}
      <TabPanel value={activeTab} index={0}>
        <Card>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, gap: 2 }}>
              <Typography variant="h6" component="h2">
                User Management
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <TextField
                  size="small"
                  label="Filter by User ID"
                  type="number"
                  value={userIdFilter}
                  onChange={(e) => setUserIdFilter(e.target.value)}
                />
                <Tooltip title="Refresh users">
                  <IconButton onClick={fetchUsers} color="primary">
                    <Refresh />
                  </IconButton>
                </Tooltip>
              </Box>
            </Box>

            {usersLoading ? (
              <LinearProgress />
            ) : (
              <TableContainer>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>User ID</TableCell>
                      <TableCell>Email</TableCell>
                      <TableCell>Role</TableCell>
                      <TableCell>Status</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {users
                      .filter(u => {
                        if (!userIdFilter) return true;
                        const idNum = Number(userIdFilter);
                        if (Number.isNaN(idNum)) return true;
                        return u.id === idNum;
                      })
                      .map((user) => (
                      <TableRow key={user.id}>
                        <TableCell>{user.id}</TableCell>
                        <TableCell>{user.email}</TableCell>
                        <TableCell>
                          <Chip 
                            label={user.role} 
                            color={user.role === 'ADMIN' ? 'secondary' : 'default'}
                            size="small"
                          />
                        </TableCell>
                        <TableCell>
                          <Chip 
                            label={user.enabled ? 'Verified' : 'Pending'} 
                            color={user.enabled ? 'success' : 'warning'}
                            size="small"
                          />
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </CardContent>
        </Card>
      </TabPanel>

      {/* Rooms Tab */}
      <TabPanel value={activeTab} index={1}>
        <Card>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
              <Typography variant="h6" component="h2">
                Room Management
              </Typography>
              <Box>
                <Button
                  variant="contained"
                  startIcon={<Add />}
                  onClick={() => openRoomDialog('create')}
                  sx={{ mr: 1 }}
                >
                  Add Room
                </Button>
                <Tooltip title="Refresh rooms">
                  <IconButton onClick={fetchRooms} color="primary">
                    <Refresh />
                  </IconButton>
                </Tooltip>
              </Box>
            </Box>

            {roomsLoading ? (
              <LinearProgress />
            ) : (
              <Grid container spacing={3}>
                {rooms.map((room) => (
                  <Grid item xs={12} sm={6} md={4} key={room.id}>
                    <Card>
                      <CardContent>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
                          <Typography variant="h6" component="h3">
                            Room {room.roomNumber}
                          </Typography>
                        </Box>
                        
                        <Chip 
                          label={room.type} 
                          color={getRoomTypeColor(room.type)}
                          size="small"
                          sx={{ mb: 1 }}
                        />
                        
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                          Capacity: {room.capacity} | Price: ${room.pricePerNight}/night
                        </Typography>

                        {room.description && (
                          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                            {room.description}
                          </Typography>
                        )}

                        <Box sx={{ display: 'flex', gap: 1 }}>
                          <Button
                            size="small"
                            variant="outlined"
                            startIcon={<Edit />}
                            onClick={() => openRoomDialog('edit', room)}
                          >
                            Edit
                          </Button>
                          <Button
                            size="small"
                            variant="outlined"
                            color="error"
                            startIcon={<Delete />}
                            onClick={() => handleDeleteRoom(room.id)}
                          >
                            Delete
                          </Button>
                        </Box>
                      </CardContent>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            )}
          </CardContent>
        </Card>
      </TabPanel>

      {/* Reservations Tab */}
      <TabPanel value={activeTab} index={2}>
        <Card>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
              <Typography variant="h6" component="h2">
                Reservation Overview
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <TextField
                  size="small"
                  label="Filter by User ID"
                  type="number"
                  value={reservationUserIdFilter}
                  onChange={(e) => setReservationUserIdFilter(e.target.value)}
                />
                <Tooltip title="Refresh reservations">
                  <IconButton onClick={fetchReservations} color="primary">
                    <Refresh />
                  </IconButton>
                </Tooltip>
              </Box>
            </Box>

            {reservationsLoading ? (
              <LinearProgress />
            ) : (
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Reservation ID</TableCell>
                      <TableCell>User ID</TableCell>
                      <TableCell>User Email</TableCell>
                      <TableCell>Room</TableCell>
                      <TableCell>Dates</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell>Total</TableCell>
                      <TableCell>Created</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {reservations
                      .filter(res => {
                        if (!reservationUserIdFilter) return true;
                        const idNum = Number(reservationUserIdFilter);
                        if (Number.isNaN(idNum)) return true;
                        return res.userId === idNum;
                      })
                      .map((res) => (
                      <TableRow key={`${res.userId}-${res.id}`}>
                        <TableCell>{res.id}</TableCell>
                        <TableCell>{res.userId}</TableCell>
                        <TableCell>{res.userEmail}</TableCell>
                        <TableCell>
                          <Typography variant="body2">Room {res.roomNumber}</Typography>
                          <Chip label={res.roomType} size="small" sx={{ mt: 0.5 }} />
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2">{formatDate(res.checkInDate)} - {formatDate(res.checkOutDate)}</Typography>
                        </TableCell>
                        <TableCell>
                          <Chip label={res.status} color={getStatusColor(res.status)} size="small" />
                        </TableCell>
                        <TableCell>${res.totalPrice}</TableCell>
                        <TableCell>{res.createdAt ? formatDate(res.createdAt) : '-'}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </CardContent>
        </Card>
      </TabPanel>

      {/* Room Dialog */}
      <Dialog open={roomDialog.open} onClose={closeRoomDialog} maxWidth="sm" fullWidth>
        <DialogTitle>
          {roomDialog.mode === 'create' ? 'Add New Room' : 'Edit Room'}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 2 }}>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Room Number"
                  value={roomForm.roomNumber}
                  onChange={(e) => setRoomForm(prev => ({ ...prev, roomNumber: e.target.value }))}
                  required
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <FormControl fullWidth>
                  <InputLabel>Room Type</InputLabel>
                  <Select
                    value={roomForm.type}
                    label="Room Type"
                    onChange={(e) => setRoomForm(prev => ({ ...prev, type: e.target.value }))}
                  >
                    <MenuItem value="SINGLE">Single</MenuItem>
                    <MenuItem value="DOUBLE">Double</MenuItem>
                    <MenuItem value="SUITE">Suite</MenuItem>
                    <MenuItem value="DELUXE">Deluxe</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Capacity"
                  type="number"
                  value={roomForm.capacity}
                  onChange={(e) => setRoomForm(prev => ({ ...prev, capacity: parseInt(e.target.value) }))}
                  required
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Price per Night"
                  type="number"
                  value={roomForm.pricePerNight}
                  onChange={(e) => setRoomForm(prev => ({ ...prev, pricePerNight: e.target.value }))}
                  required
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Description"
                  multiline
                  rows={3}
                  value={roomForm.description}
                  onChange={(e) => setRoomForm(prev => ({ ...prev, description: e.target.value }))}
                />
              </Grid>
            </Grid>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={closeRoomDialog}>Cancel</Button>
          <Button 
            onClick={handleRoomSubmit} 
            variant="contained"
            disabled={!roomForm.roomNumber || !roomForm.type || !roomForm.capacity || !roomForm.pricePerNight || !roomForm.description}
          >
            {roomDialog.mode === 'create' ? 'Create' : 'Update'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default AdminPanel;
