package com.reservations.hotel.services;

import com.reservations.hotel.dto.RoomCreateDto;
import com.reservations.hotel.models.Room;
import com.reservations.hotel.models.RoomType;
import com.reservations.hotel.repositories.ReservationRepository;
import com.reservations.hotel.repositories.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    public boolean isRoomAvailable(Long roomId, LocalDate startDate, LocalDate endDate) {
        return !reservationRepository.existsConflictingReservation(roomId, startDate, endDate);
    }

    public List<Room> getAllAvailableRooms(LocalDate startDate, LocalDate endDate) {
        List<Room> allRooms = roomRepository.findAll();
        return allRooms.stream()
                .filter(room -> isRoomAvailable(room.getId(), startDate, endDate))
                .collect(Collectors.toList());
    }
    public List<Room> getAvailableRoomsByType(RoomType type, LocalDate startDate, LocalDate endDate) {
        List<Room> availableRooms = getAllAvailableRooms(startDate, endDate);
        return availableRooms.stream()
                .filter(room -> room.getType() == type)
                .collect(Collectors.toList());
    }
    public List<Room> getAvailableRoomsByCapacity(Integer minCapacity, LocalDate startDate, LocalDate endDate) {
        List<Room> availableRooms = getAllAvailableRooms(startDate, endDate);
        return availableRooms.stream()
                .filter(room -> room.getCapacity() >= minCapacity)
                .collect(Collectors.toList());
    }
    public List<Room> getAvailableRoomsByCapacityAndPrice(Integer minCapacity, Double maxPricePerNight, LocalDate startDate, LocalDate endDate) {
        List<Room> availableRooms = getAllAvailableRooms(startDate, endDate);
        return availableRooms.stream()
                .filter(room -> room.getCapacity() >= minCapacity && room.getPricePerNight() <= maxPricePerNight)
                .collect(Collectors.toList());
    }
    public List<Room> getAvailableRoomsByMaxPrice(Double maxPricePerNight, LocalDate startDate, LocalDate endDate) {
        List<Room> availableRooms = getAllAvailableRooms(startDate, endDate);
        return availableRooms.stream()
                .filter(room -> room.getPricePerNight() <= maxPricePerNight)
                .collect(Collectors.toList());
    }

    public Room addRoom(RoomCreateDto input) {
        Room room = new Room(input.getRoomNumber(), input.getRoomType(), input.getPricePerNight(), input.getCapacity(), input.getDescription());
        return roomRepository.save(room);
    }

    public List<Room> getSpecificRooms(RoomType type, Integer minCapacity, Double maxPricePerNight, LocalDate checkInDate, LocalDate checkOutDate){
        if(checkInDate != null && checkOutDate != null) {
            return getAvailableRoomsWithFilters(type, minCapacity, maxPricePerNight, checkInDate, checkOutDate);
        }else{
            return getRoomsWithFilters(type, minCapacity, maxPricePerNight);
        }

    }

    private List<Room> getAvailableRoomsWithFilters(RoomType type, Integer minCapacity, Double maxPricePerNight, LocalDate checkInDate, LocalDate checkOutDate) {
        List<Room> availableRooms = getAllAvailableRooms(checkInDate, checkOutDate);
        return availableRooms.stream()
                .filter(room -> (type == null || room.getType() == type) &&
                        (minCapacity == null || room.getCapacity() >= minCapacity) &&
                        (maxPricePerNight == null || room.getPricePerNight() <= maxPricePerNight))
                .collect(Collectors.toList());
    }
    private List<Room> getRoomsWithFilters(RoomType type, Integer minCapacity, Double maxPricePerNight) {
        return roomRepository.findAll().stream()
                .filter(room -> (type == null || room.getType() == type) &&
                        (minCapacity == null || room.getCapacity() >= minCapacity) &&
                        (maxPricePerNight == null || room.getPricePerNight() <= maxPricePerNight))
                .collect(Collectors.toList());
    }
}