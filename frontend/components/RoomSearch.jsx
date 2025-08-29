import React, { useState, useEffect } from 'react';
import { hotelAPI } from '../services/api';
import {
  Box,
  Container,
  Grid,
  Card,
  CardContent,
  CardActions,
  Typography,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
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
  InputAdornment
} from '@mui/material';
import {
  Search,
  Hotel,
  People,
  AttachMoney,
  CalendarToday,
  FilterList,
  Clear,
  BookOnline,
  Star
} from '@mui/icons-material';


const RoomSearch = () => {
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [searchParams, setSearchParams] = useState({
    roomNumber: '',
    type: '',
    minCapacity: '',
    maxPricePerNight: '',
    checkInDate: null,
    checkOutDate: null
  });
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [bookingDialog, setBookingDialog] = useState(false);
  const [bookingData, setBookingData] = useState({
    checkInDate: null,
    checkOutDate: null
  });

  // Basic room availability check (general availability)
  const isRoomAvailable = (room) => {
    if (!room.bookedDates || room.bookedDates.length === 0) {
      return true; // No reservations means available
    }
    // Consider room available if there are any free dates (no specific date check here)
    return true;
  };

  // Availability check for specific dates
  const isRoomAvailableForDates = (room, checkIn, checkOut) => {
    if (!room.bookedDates || room.bookedDates.length === 0) {
      return true; // No reservations means available
    }

    const checkInDate = new Date(checkIn);
    const checkOutDate = new Date(checkOut);
    checkInDate.setHours(0, 0, 0, 0);
    checkOutDate.setHours(0, 0, 0, 0);

    // Check conflicts with existing reservations
    return !room.bookedDates.some(booking => {
      const existingCheckIn = new Date(booking.checkInDate);
      const existingCheckOut = new Date(booking.checkOutDate);
      existingCheckIn.setHours(0, 0, 0, 0);
      existingCheckOut.setHours(0, 0, 0, 0);
      // Overlap check
      return (checkInDate < existingCheckOut && checkOutDate > existingCheckIn);
    });
  };

  useEffect(() => {
    searchRooms();
  }, []);

  const searchRooms = async () => {
    try {
      setLoading(true);
      setError('');

      const cleanParams = Object.fromEntries(
        Object.entries(searchParams).filter(([_, value]) => 
          value !== '' && value != null && value !== ''
        )
      );

      const response = await hotelAPI.rooms.search(cleanParams);
      
      if (response.data && response.data.length > 0) {
        // Check availability for specific dates if provided
        let availableCount = 0;
        let occupiedCount = 0;
        
        if (searchParams.checkInDate && searchParams.checkOutDate) {
          availableCount = response.data.filter(r => 
            isRoomAvailableForDates(r, searchParams.checkInDate, searchParams.checkOutDate)
          ).length;
          occupiedCount = response.data.length - availableCount;
        } else {
          availableCount = response.data.filter(r => isRoomAvailable(r)).length;
          occupiedCount = response.data.length - availableCount;
        }
      }
      
      // Filter rooms by availability for selected dates
      let filteredRooms = response.data || [];
      
      if (searchParams.checkInDate && searchParams.checkOutDate) {
        filteredRooms = response.data.filter(room => 
          isRoomAvailableForDates(room, searchParams.checkInDate, searchParams.checkOutDate)
        );
        console.log('🔍 Filtered rooms for dates:', filteredRooms.length);
      }
      
      setRooms(filteredRooms);
    } catch (err) {
      setError('Failed to search rooms');
    } finally {
      setLoading(false);
    }
  };

  const handleSearchParamChange = (name, value) => {
    setSearchParams(prev => ({ ...prev, [name]: value }));
  };

  const clearFilters = () => {
    setSearchParams({
      roomNumber: '',
      type: '',
      minCapacity: '',
      maxPricePerNight: '',
      checkInDate: null,
      checkOutDate: null
    });
  };

  const handleBookRoom = (room) => {
    if (!isRoomAvailable(room)) {
      setError('This room is not available for booking');
      return;
    }
    
    setSelectedRoom(room);
    setBookingData({
      checkInDate: null,
      checkOutDate: null
    });
    setBookingDialog(true);
  };

  const handleBookingSubmit = async () => {
    if (!bookingData.checkInDate || !bookingData.checkOutDate) {
      setError('Please select check-in and check-out dates');
      return;
    }

    // Check availability for selected dates
    if (!isRoomAvailableForDates(selectedRoom, bookingData.checkInDate, bookingData.checkOutDate)) {
      setError('This room is not available for the selected dates');
      return;
    }

    try {
      setLoading(true);
      await hotelAPI.reservations.create({
        roomId: selectedRoom.id,
        checkInDate: bookingData.checkInDate,
        checkOutDate: bookingData.checkOutDate
      });

      setBookingDialog(false);
      searchRooms(); // Refresh rooms
      setError('');
    } catch (err) {
      console.error('Error booking room:', err);
      setError(err.response?.data || 'Failed to book room');
    } finally {
      setLoading(false);
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

  const getAvailabilityColor = (available) => {
    return available ? 'success' : 'error';
  };

  const RoomCard = ({ room }) => (
    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <CardContent sx={{ flexGrow: 1 }}>
        {/* Room Header */}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
          <Box>
            <Typography variant="h6" component="h3" sx={{ fontWeight: 'bold' }}>
              Room {room.roomNumber}
            </Typography>
            <Chip 
              label={room.type} 
              color={getRoomTypeColor(room.type)}
              size="small"
              sx={{ fontWeight: 'bold' }}
            />
          </Box>
          <Chip 
            label={
              searchParams.checkInDate && searchParams.checkOutDate
                ? (isRoomAvailableForDates(room, searchParams.checkInDate, searchParams.checkOutDate) ? 'Available' : 'Occupied')
                : (isRoomAvailable(room) ? 'Available' : 'Occupied')
            } 
            color={
              searchParams.checkInDate && searchParams.checkOutDate
                ? getAvailabilityColor(isRoomAvailableForDates(room, searchParams.checkInDate, searchParams.checkOutDate))
                : getAvailabilityColor(isRoomAvailable(room))
            }
            size="small"
          />
        </Box>

        {/* Room Details */}
        <Box sx={{ mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
            <People color="primary" sx={{ fontSize: 20 }} />
            <Typography variant="body2" color="text.secondary">
              Capacity: {room.capacity} person{room.capacity > 1 ? 's' : ''}
            </Typography>
          </Box>
          
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
            <AttachMoney color="success" sx={{ fontSize: 20 }} />
            <Typography variant="body2" color="text.secondary">
              Price per night: ${room.pricePerNight}
            </Typography>
          </Box>

          {room.description && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              {room.description}
            </Typography>
          )}

          {/* Booked Dates Info */}
          {room.bookedDates && room.bookedDates.length > 0 && (
            <Box sx={{ mt: 2 }}>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                Booked dates:
              </Typography>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                {room.bookedDates.map((booking, index) => (
                  <Typography key={index} variant="caption" color="error">
                    {new Date(booking.checkInDate).toLocaleDateString()} - {new Date(booking.checkOutDate).toLocaleDateString()}
                  </Typography>
                ))}
              </Box>
            </Box>
          )}
        </Box>

        {/* Amenities */}
        {room.amenities && room.amenities.length > 0 && (
          <Box sx={{ mb: 2 }}>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
              Amenities:
            </Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
              {room.amenities.map((amenity, index) => (
                <Chip 
                  key={index} 
                  label={amenity} 
                  size="small" 
                  variant="outlined"
                />
              ))}
            </Box>
          </Box>
        )}
      </CardContent>

      <CardActions sx={{ p: 2, pt: 0 }}>
        <Button
          fullWidth
          variant="contained"
          startIcon={<BookOnline />}
          onClick={() => handleBookRoom(room)}
          disabled={
            searchParams.checkInDate && searchParams.checkOutDate
              ? !isRoomAvailableForDates(room, searchParams.checkInDate, searchParams.checkOutDate)
              : !isRoomAvailable(room)
          }
          sx={{ fontWeight: 'bold' }}
        >
          {
            searchParams.checkInDate && searchParams.checkOutDate
              ? (isRoomAvailableForDates(room, searchParams.checkInDate, searchParams.checkOutDate) ? 'Book Now' : 'Not Available')
              : (isRoomAvailable(room) ? 'Book Now' : 'Not Available')
          }
        </Button>
      </CardActions>
    </Card>
  );

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" sx={{ fontWeight: 'bold', mb: 2 }}>
          Browse Available Rooms
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Search and book rooms based on your preferences
        </Typography>
      </Box>

        {/* Search Filters */}
        <Card sx={{ mb: 4, p: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3 }}>
            <FilterList color="primary" />
            <Typography variant="h6" component="h2">
              Search Filters
            </Typography>
          </Box>

          <Grid container spacing={3}>
            <Grid item xs={12} sm={6} md={3}>
              <TextField
                fullWidth
                label="Room Number"
                value={searchParams.roomNumber}
                onChange={(e) => handleSearchParamChange('roomNumber', e.target.value)}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <Hotel />
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <FormControl fullWidth>
                <InputLabel>Room Type</InputLabel>
                <Select
                  value={searchParams.type}
                  label="Room Type"
                  onChange={(e) => handleSearchParamChange('type', e.target.value)}
                >
                  <MenuItem value="">All Types</MenuItem>
                  <MenuItem value="SINGLE">Single</MenuItem>
                  <MenuItem value="DOUBLE">Double</MenuItem>
                  <MenuItem value="SUITE">Suite</MenuItem>
                  <MenuItem value="DELUXE">Deluxe</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <TextField
                fullWidth
                label="Min Capacity"
                type="number"
                value={searchParams.minCapacity}
                onChange={(e) => handleSearchParamChange('minCapacity', e.target.value)}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <People />
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <TextField
                fullWidth
                label="Max Price per Night"
                type="number"
                value={searchParams.maxPricePerNight}
                onChange={(e) => handleSearchParamChange('maxPricePerNight', e.target.value)}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <AttachMoney />
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <TextField
                fullWidth
                label="Check-in Date"
                type="date"
                value={searchParams.checkInDate || ''}
                onChange={(e) => handleSearchParamChange('checkInDate', e.target.value)}
                InputLabelProps={{
                  shrink: true,
                }}
              />
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <TextField
                fullWidth
                label="Check-out Date"
                type="date"
                value={searchParams.checkOutDate || ''}
                onChange={(e) => handleSearchParamChange('checkOutDate', e.target.value)}
                InputLabelProps={{
                  shrink: true,
                }}
              />
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <Button
                fullWidth
                variant="contained"
                startIcon={<Search />}
                onClick={searchRooms}
                disabled={loading}
                sx={{ height: 56 }}
              >
                Search
              </Button>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <Button
                fullWidth
                variant="outlined"
                startIcon={<Clear />}
                onClick={clearFilters}
                sx={{ height: 56 }}
              >
                Clear Filters
              </Button>
            </Grid>
          </Grid>
        </Card>

        {/* Results */}
        {error && (
          <Alert severity="error" sx={{ mb: 3 }}>
            {error}
          </Alert>
        )}

        {loading ? (
          <Box sx={{ width: '100%' }}>
            <LinearProgress />
          </Box>
        ) : (
          <>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
              <Typography variant="h6" component="h2">
                Results ({rooms.length} rooms found)
              </Typography>
            </Box>

            {rooms.length === 0 ? (
              <Card sx={{ p: 4, textAlign: 'center' }}>
                <Typography variant="h6" color="text.secondary">
                  No rooms found matching your criteria
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                  Try adjusting your search filters
                </Typography>
              </Card>
            ) : (
              <Grid container spacing={3}>
                {rooms.map((room) => (
                  <Grid item xs={12} sm={6} md={4} key={room.id}>
                    <RoomCard room={room} />
                  </Grid>
                ))}
              </Grid>
            )}
          </>
        )}

        {/* Booking Dialog */}
        <Dialog open={bookingDialog} onClose={() => setBookingDialog(false)} maxWidth="sm" fullWidth>
          <DialogTitle>
            Book Room {selectedRoom?.roomNumber}
          </DialogTitle>
          <DialogContent>
            <Box sx={{ mt: 2 }}>
              <Grid container spacing={3}>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Check-in Date"
                    type="date"
                    value={bookingData.checkInDate || ''}
                    onChange={(e) => setBookingData(prev => ({ ...prev, checkInDate: e.target.value }))}
                    InputLabelProps={{
                      shrink: true,
                    }}
                    inputProps={{
                      min: new Date().toISOString().split('T')[0]
                    }}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Check-out Date"
                    type="date"
                    value={bookingData.checkOutDate || ''}
                    onChange={(e) => setBookingData(prev => ({ ...prev, checkOutDate: e.target.value }))}
                    InputLabelProps={{
                      shrink: true,
                    }}
                    inputProps={{
                      min: bookingData.checkInDate || new Date().toISOString().split('T')[0]
                    }}
                  />
                </Grid>
              </Grid>
              
              {selectedRoom && (
                <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
                  <Typography variant="body2" color="text.secondary">
                    <strong>Room Details:</strong> {selectedRoom.type} room with capacity for {selectedRoom.capacity} person{selectedRoom.capacity > 1 ? 's' : ''}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                    <strong>Price:</strong> ${selectedRoom.pricePerNight} per night
                  </Typography>
                </Box>
              )}
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setBookingDialog(false)}>Cancel</Button>
            <Button 
              onClick={handleBookingSubmit} 
              variant="contained"
              disabled={!bookingData.checkInDate || !bookingData.checkOutDate}
            >
              Confirm Booking
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
    );
  };

export default RoomSearch;
