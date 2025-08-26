package com.reservations.hotel.services;

import com.reservations.hotel.dto.ReservationDateDto;
import com.reservations.hotel.dto.RoomCreateDto;
import com.reservations.hotel.dto.RoomResponseDto;
import com.reservations.hotel.exceptions.InvalidSearchParametersException;
import com.reservations.hotel.exceptions.RoomAlreadyExistsException;
import com.reservations.hotel.exceptions.RoomNotFoundException;
import com.reservations.hotel.models.ReservationStatus;
import com.reservations.hotel.models.Room;
import com.reservations.hotel.models.RoomType;
import com.reservations.hotel.repositories.ReservationRepository;
import com.reservations.hotel.repositories.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class RoomService {
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public RoomService(RoomRepository roomRepository, ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<RoomResponseDto> getAllRoomsDto() {
        return roomRepository.findAll().stream().map(this::convertToDto).sorted(Comparator.comparing(RoomResponseDto::getRoomNumber)).toList();
    }
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public RoomResponseDto getRoomByRoomNumber(Integer roomNumber) {
        return roomRepository.findByRoomNumber(roomNumber).map(this::convertToDto)
                .orElseThrow(() -> new RoomNotFoundException("Room not found with number: " + roomNumber));
    }


    // Available rooms within a price range
    public boolean isRoomAvailable(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        return !reservationRepository.existsConflictingReservation(roomId, checkInDate, checkOutDate);
    }

    public List<Room> getAllAvailableRooms(LocalDate startDate, LocalDate endDate) {
        List<Room> allRooms = roomRepository.findAll();
        return allRooms.stream()
                .filter(room -> isRoomAvailable(room.getId(), startDate, endDate))
                .sorted(Comparator.comparing(Room::getRoomNumber)).toList();
    }

    @Transactional
    public Room addRoom(RoomCreateDto input) {
        log.info("Adding new room with number: {}", input.getRoomNumber());
        if(roomRepository.existsByRoomNumber(input.getRoomNumber())) {
            log.warn("Room with number {} already exists", input.getRoomNumber());
            throw new RoomAlreadyExistsException("Room with this number already exists");
        }
        Room room = new Room(input.getRoomNumber(), input.getRoomType(), input.getPricePerNight(), input.getCapacity(), input.getDescription());
        log.debug("Room created - Details: {}", room);
        return roomRepository.save(room);
    }

    public List<RoomResponseDto> getSpecificRoomsDto(Integer roomNumber, RoomType type, Integer minCapacity, Double maxPricePerNight, LocalDate checkInDate, LocalDate checkOutDate){
        if (roomNumber == null && type == null && minCapacity == null && maxPricePerNight == null && checkInDate == null && checkOutDate == null) {
            return getAllRoomsDto();
        }
        if (roomNumber != null) {
            RoomResponseDto room = getRoomByRoomNumber(roomNumber);
            return List.of(room);
        }
        if ((checkInDate != null && checkOutDate == null) || (checkInDate == null && checkOutDate != null)) {
            throw new InvalidSearchParametersException("Both checkIn and checkOut dates must be provided");
        }
        if(checkInDate != null) {
            return getAvailableRoomsWithFilters(type, minCapacity, maxPricePerNight, checkInDate, checkOutDate).stream().map(this::convertToDto).toList();
        }else{
            return getRoomsWithFilters(type, minCapacity, maxPricePerNight).stream().map(this::convertToDto).toList();
        }

    }

    private List<Room> getAvailableRoomsWithFilters(RoomType type, Integer minCapacity, Double maxPricePerNight, LocalDate checkInDate, LocalDate checkOutDate) {
        List<Room> availableRooms = getAllAvailableRooms(checkInDate, checkOutDate);
        return availableRooms.stream()
                .filter(room -> (type == null || room.getType() == type) &&
                        (minCapacity == null || room.getCapacity() >= minCapacity) &&
                        (maxPricePerNight == null || room.getPricePerNight() <= maxPricePerNight))
                .sorted(Comparator.comparing(Room::getRoomNumber))
                .toList();
    }
    private List<Room> getRoomsWithFilters(RoomType type, Integer minCapacity, Double maxPricePerNight) {
        return roomRepository.findAll().stream()
                .filter(room -> (type == null || room.getType() == type) &&
                        (minCapacity == null || room.getCapacity() >= minCapacity) &&
                        (maxPricePerNight == null || room.getPricePerNight() <= maxPricePerNight))
                .sorted(Comparator.comparing(Room::getRoomNumber))
                .toList();
    }

    public RoomResponseDto getRoomDtoByRoomId(Long roomId) {
        return roomRepository.findById(roomId).map(this::convertToDto)
                .orElseThrow(() -> new RoomNotFoundException("Room not found with id: " + roomId));
    }
    public Room getRoomByRoomId(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found with id: " + roomId));
    }

    private RoomResponseDto convertToDto(Room room) {
        List<ReservationDateDto> bookedDates = reservationRepository.findByRoomId(room.getId()).stream()
                .filter(reservation -> !List.of(ReservationStatus.CANCELLED, ReservationStatus.COMPLETED)
                        .contains(reservation.getStatus()))
                .map(reservation -> new ReservationDateDto(reservation.getCheckInDate(), reservation.getCheckOutDate()))
                .toList();
        return new RoomResponseDto(room, bookedDates);
    }
}