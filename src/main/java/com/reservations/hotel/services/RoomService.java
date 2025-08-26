package com.reservations.hotel.services;

import com.reservations.hotel.dto.ReservationDateDto;
import com.reservations.hotel.dto.RoomCreateDto;
import com.reservations.hotel.dto.RoomResponseDto;
import com.reservations.hotel.models.ReservationStatus;
import com.reservations.hotel.models.Room;
import com.reservations.hotel.models.RoomType;
import com.reservations.hotel.repositories.ReservationRepository;
import com.reservations.hotel.repositories.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
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

    public List<RoomResponseDto> getAllRoomsDto() {
        return roomRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public RoomResponseDto getRoomByRoomNumber(Integer roomNumber) {
        return roomRepository.findByRoomNumber(roomNumber).map(this::convertToDto)
                .orElseThrow(() -> new RuntimeException("Room not found with number: " + roomNumber));
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

    public Room addRoom(RoomCreateDto input) {
        Room room = new Room(input.getRoomNumber(), input.getRoomType(), input.getPricePerNight(), input.getCapacity(), input.getDescription());
        return roomRepository.save(room);
    }

    public List<RoomResponseDto> getSpecificRoomsDto(RoomType type, Integer minCapacity, Double maxPricePerNight, LocalDate checkInDate, LocalDate checkOutDate){
        if(checkInDate != null && checkOutDate != null) {
            return getAvailableRoomsWithFilters(type, minCapacity, maxPricePerNight, checkInDate, checkOutDate).stream().map(this::convertToDto).collect(Collectors.toList());
        }else{
            return getRoomsWithFilters(type, minCapacity, maxPricePerNight).stream().map(this::convertToDto).collect(Collectors.toList());
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

    public RoomResponseDto getRoomDtoByRoomId(Long roomId) {
        return roomRepository.findById(roomId).map(this::convertToDto)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));
    }
    public Room getRoomByRoomId(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));
    }

    private RoomResponseDto convertToDto(Room room) {
        List<ReservationDateDto> bookedDates = reservationRepository.findByRoomId(room.getId()).stream()
                .filter(reservation -> !Arrays.asList(ReservationStatus.CANCELLED, ReservationStatus.COMPLETED)
                        .contains(reservation.getStatus()))
                .map(reservation -> new ReservationDateDto(reservation.getCheckInDate(), reservation.getCheckOutDate()))
                .toList();
        return new RoomResponseDto(room, bookedDates);
    }
}