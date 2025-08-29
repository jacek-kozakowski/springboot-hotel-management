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
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper
} from '@mui/material';
import {
  BookOnline,
  CheckCircle,
  Cancel,
  Refresh,
  CalendarToday,
  Hotel,
  AttachMoney,
  People,
  Warning,
  Info
} from '@mui/icons-material';

const MyReservations = () => {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [confirmDialog, setConfirmDialog] = useState({
    open: false,
    type: '',
    reservation: null
  });

  useEffect(() => {
    fetchReservations();
  }, []);

  const fetchReservations = async () => {
    try {
      setLoading(true);
      setError('');

      const response = await hotelAPI.users.getMyReservations();
      setReservations(response.data || []);
    } catch (err) {
      console.error('Error fetching reservations:', err);
      setError('Failed to load reservations');
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmReservation = async (reservationId) => {
    try {
      setLoading(true);
      await hotelAPI.reservations.confirm(reservationId);
      await fetchReservations(); // Refresh list
      setConfirmDialog({ open: false, type: '', reservation: null });
    } catch (err) {
      console.error('Error confirming reservation:', err);
      setError('Failed to confirm reservation');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelReservation = async (reservationId) => {
    try {
      setLoading(true);
      await hotelAPI.reservations.cancel(reservationId);
      await fetchReservations(); // Refresh list
      setConfirmDialog({ open: false, type: '', reservation: null });
    } catch (err) {
      console.error('Error cancelling reservation:', err);
      setError('Failed to cancel reservation');
    } finally {
      setLoading(false);
    }
  };

  const openConfirmDialog = (type, reservation) => {
    setConfirmDialog({ open: true, type, reservation });
  };

  const closeConfirmDialog = () => {
    setConfirmDialog({ open: false, type: '', reservation: null });
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'CONFIRMED': return 'success';
      case 'PENDING': return 'warning';
      case 'CANCELLED': return 'error';
      default: return 'default';
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'CONFIRMED': return <CheckCircle />;
      case 'PENDING': return <Warning />;
      case 'CANCELLED': return <Cancel />;
      default: return <Info />;
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const calculateTotalPrice = (reservation) => {
    // Prefer totalPrice from backend if provided
    if (reservation.totalPrice) {
      return reservation.totalPrice;
    }
    
    // Fallback: calculate from dates and price per night
    const checkIn = new Date(reservation.checkInDate);
    const checkOut = new Date(reservation.checkOutDate);
    const nights = Math.ceil((checkOut - checkIn) / (1000 * 60 * 60 * 24));
    return nights * reservation.roomPricePerNight;
  };

  const ReservationCard = ({ reservation }) => (
    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <CardContent sx={{ flexGrow: 1 }}>
        {/* Header */}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
          <Box>
            <Typography variant="h6" component="h3" sx={{ fontWeight: 'bold' }}>
              Room {reservation.roomNumber}
            </Typography>
            <Chip 
              label={reservation.roomType} 
              color="primary"
              size="small"
              sx={{ fontWeight: 'bold' }}
            />
          </Box>
          <Chip 
            label={reservation.status} 
            color={getStatusColor(reservation.status)}
            icon={getStatusIcon(reservation.status)}
            size="small"
            sx={{ fontWeight: 'bold' }}
          />
        </Box>

        {/* Dates */}
        <Box sx={{ mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
            <CalendarToday color="primary" sx={{ fontSize: 20 }} />
            <Typography variant="body2" color="text.secondary">
              Check-in: {formatDate(reservation.checkInDate)}
            </Typography>
          </Box>
          
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
            <CalendarToday color="primary" sx={{ fontSize: 20 }} />
            <Typography variant="body2" color="text.secondary">
              Check-out: {formatDate(reservation.checkOutDate)}
            </Typography>
          </Box>
        </Box>

        {/* Room Details */}
        <Box sx={{ mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
            <People color="primary" sx={{ fontSize: 20 }} />
            <Typography variant="body2" color="text.secondary">
              Capacity: {reservation.roomCapacity} person{reservation.roomCapacity > 1 ? 's' : ''}
            </Typography>
          </Box>
          
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
            <AttachMoney color="success" sx={{ fontSize: 20 }} />
            <Typography variant="body2" color="text.secondary">
              Price per night: ${reservation.roomPricePerNight}
            </Typography>
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
            <AttachMoney color="success" sx={{ fontSize: 20 }} />
            <Typography variant="body2" color="text.secondary">
              Total: ${calculateTotalPrice(reservation)}
            </Typography>
          </Box>
        </Box>

        {/* Created Date */}
        <Box sx={{ mb: 2 }}>
          <Typography variant="caption" color="text.secondary">
            Booked on: {formatDate(reservation.createdAt)}
          </Typography>
        </Box>
      </CardContent>

      {/* Actions */}
      <Box sx={{ p: 2, pt: 0 }}>
        {reservation.status === 'PENDING' && (
          <Grid container spacing={1}>
            <Grid item xs={6}>
              <Button
                fullWidth
                variant="contained"
                color="success"
                startIcon={<CheckCircle />}
                onClick={() => openConfirmDialog('confirm', reservation)}
                disabled={loading}
                size="small"
              >
                Confirm
              </Button>
            </Grid>
            <Grid item xs={6}>
              <Button
                fullWidth
                variant="outlined"
                color="error"
                startIcon={<Cancel />}
                onClick={() => openConfirmDialog('cancel', reservation)}
                disabled={loading}
                size="small"
              >
                Cancel
              </Button>
            </Grid>
          </Grid>
        )}
        
        {reservation.status === 'CONFIRMED' && (
          <Button
            fullWidth
            variant="outlined"
            color="error"
            startIcon={<Cancel />}
            onClick={() => openConfirmDialog('cancel', reservation)}
            disabled={loading}
            size="small"
          >
            Cancel
          </Button>
        )}

        {reservation.status === 'CANCELLED' && (
          <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', fontStyle: 'italic' }}>
            Reservation cancelled
          </Typography>
        )}
      </Box>
    </Card>
  );

  if (loading && reservations.length === 0) {
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
            My Reservations
          </Typography>
          <Tooltip title="Refresh reservations">
            <IconButton onClick={fetchReservations} color="primary">
              <Refresh />
            </IconButton>
          </Tooltip>
        </Box>
        
        <Typography variant="body1" color="text.secondary">
          Manage your hotel reservations and bookings
        </Typography>
      </Box>

      {/* Error Alert */}
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Loading */}
      {loading && (
        <Box sx={{ width: '100%', mb: 3 }}>
          <LinearProgress />
        </Box>
      )}

      {/* Reservations */}
      {reservations.length === 0 ? (
        <Card sx={{ p: 4, textAlign: 'center' }}>
          <BookOnline sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
          <Typography variant="h6" color="text.secondary" sx={{ mb: 1 }}>
            No reservations found
          </Typography>
          <Typography variant="body2" color="text.secondary">
            You haven't made any reservations yet. Start by browsing available rooms!
          </Typography>
        </Card>
      ) : (
        <Grid container spacing={3}>
          {reservations.map((reservation) => (
            <Grid item xs={12} sm={6} md={4} key={reservation.id}>
              <ReservationCard reservation={reservation} />
            </Grid>
          ))}
        </Grid>
      )}

      {/* Confirmation Dialog */}
      <Dialog open={confirmDialog.open} onClose={closeConfirmDialog} maxWidth="sm" fullWidth>
        <DialogTitle>
          {confirmDialog.type === 'confirm' ? 'Confirm Reservation' : 'Cancel Reservation'}
        </DialogTitle>
        <DialogContent>
          <Typography variant="body1" sx={{ mb: 2 }}>
            {confirmDialog.type === 'confirm' 
              ? `Are you sure you want to confirm your reservation for Room ${confirmDialog.reservation?.roomNumber}?`
              : `Are you sure you want to cancel your reservation for Room ${confirmDialog.reservation?.roomNumber}?`
            }
          </Typography>
          
          {confirmDialog.reservation && (
            <Box sx={{ p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
              <Typography variant="body2" color="text.secondary">
                <strong>Dates:</strong> {formatDate(confirmDialog.reservation.checkInDate)} - {formatDate(confirmDialog.reservation.checkOutDate)}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                <strong>Total:</strong> ${calculateTotalPrice(confirmDialog.reservation)}
              </Typography>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={closeConfirmDialog}>Cancel</Button>
          <Button 
            onClick={() => {
              if (confirmDialog.type === 'confirm') {
                handleConfirmReservation(confirmDialog.reservation.id);
              } else {
                handleCancelReservation(confirmDialog.reservation.id);
              }
            }}
            variant="contained"
            color={confirmDialog.type === 'confirm' ? 'success' : 'error'}
            disabled={loading}
          >
            {confirmDialog.type === 'confirm' ? 'Confirm' : 'Cancel Reservation'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default MyReservations;
