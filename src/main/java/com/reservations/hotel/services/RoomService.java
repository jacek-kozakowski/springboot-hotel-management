package com.reservations.hotel.services;

import com.reservations.hotel.dto.RoomCreateDto;
import com.reservations.hotel.models.Room;
import com.reservations.hotel.models.RoomType;
import com.reservations.hotel.repositories.ReservationRepository;
import com.reservations.hotel.repositories.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public RoomService(RoomRepository roomRepository, ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomByRoomNumber(Integer roomNumber) {
        return roomRepository.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new RuntimeException("Room not found with number: " + roomNumber));
    }
    public List<Room> getRoomsByType(RoomType type) {
        return roomRepository.findRoomsByType(type);
    }
    public List<Room> getRoomsByCapacity(Integer minCapacity) {
        return roomRepository.findRoomsByCapacityIsGreaterThanEqual(minCapacity);
    }
    public List<Room> getRoomsByCapacityAndPrice(Integer minCapacity, Double maxPricePerNight) {
        return roomRepository.findRoomsByCapacityIsGreaterThanEqualAndPricePerNightIsLessThanEqual(minCapacity, maxPricePerNight);
    }
    public List<Room> getRoomsByMaxPrice(Double maxPricePerNight) {
        return roomRepository.findRoomsByPricePerNightIsLessThanEqual(maxPricePerNight);
    }

    // Available rooms within a price range
    public boolean isRoomAvailable(Long roomId, LocalDateTime startDate, LocalDateTime endDate) {
        return !reservationRepository.existsConflictingReservation(roomId, startDate, endDate);
    }

    public List<Room> getAllAvailableRooms(LocalDateTime startDate, LocalDateTime endDate) {
        List<Room> allRooms = roomRepository.findAll();
        return allRooms.stream()
                .filter(room -> isRoomAvailable(room.getId(), startDate, endDate))
                .collect(Collectors.toList());
    }
    public List<Room> getAvailableRoomsByType(RoomType type, LocalDateTime startDate, LocalDateTime endDate) {
        List<Room> availableRooms = getAllAvailableRooms(startDate, endDate);
        return availableRooms.stream()
                .filter(room -> room.getType() == type)
                .collect(Collectors.toList());
    }
    public List<Room> getAvailableRoomsByCapacity(Integer minCapacity, LocalDateTime startDate, LocalDateTime endDate) {
        List<Room> availableRooms = getAllAvailableRooms(startDate, endDate);
        return availableRooms.stream()
                .filter(room -> room.getCapacity() >= minCapacity)
                .collect(Collectors.toList());
    }
    public List<Room> getAvailableRoomsByCapacityAndPrice(Integer minCapacity, Double maxPricePerNight, LocalDateTime startDate, LocalDateTime endDate) {
        List<Room> availableRooms = getAllAvailableRooms(startDate, endDate);
        return availableRooms.stream()
                .filter(room -> room.getCapacity() >= minCapacity && room.getPricePerNight() <= maxPricePerNight)
                .collect(Collectors.toList());
    }
    public List<Room> getAvailableRoomsByMaxPrice(Double maxPricePerNight, LocalDateTime startDate, LocalDateTime endDate) {
        List<Room> availableRooms = getAllAvailableRooms(startDate, endDate);
        return availableRooms.stream()
                .filter(room -> room.getPricePerNight() <= maxPricePerNight)
                .collect(Collectors.toList());
    }

    public Room addRoom(RoomCreateDto input) {
        Room room = new Room(input.getRoomNumber(), input.getRoomType(), input.getPricePerNight(), input.getCapacity(), input.getDescription());
        return roomRepository.save(room);
    }
}