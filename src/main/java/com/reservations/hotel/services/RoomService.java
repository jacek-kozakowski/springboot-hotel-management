package com.reservations.hotel.services;

import com.reservations.hotel.dto.ReservationDateDto;
import com.reservations.hotel.dto.RoomCreateDto;
import com.reservations.hotel.dto.RoomResponseDto;
import com.reservations.hotel.dto.RoomUpdateDto;
import com.reservations.hotel.exceptions.InvalidSearchParametersException;
import com.reservations.hotel.exceptions.RoomAlreadyExistsException;
import com.reservations.hotel.exceptions.RoomHasActiveReservationsException;
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
import java.util.Optional;

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
    public RoomResponseDto addRoom(RoomCreateDto input) {
        log.info("Adding new room with number: {}", input.getRoomNumber());
        if(roomRepository.existsByRoomNumber(input.getRoomNumber())) {
            log.warn("Room with number {} already exists", input.getRoomNumber());
            throw new RoomAlreadyExistsException("Room with this number already exists");
        }
        Room savedRoom = roomRepository.save(new Room(input));
        log.debug("Room created - Details: {}", savedRoom);
        return convertToDto(savedRoom);
    }

    @Transactional
    public RoomResponseDto updateRoom(Long roomId, RoomUpdateDto input){
        log.info("Updating room: id {}", roomId);
        Room roomToUpdate = roomRepository.findById(roomId).orElseThrow(()->{
                log.warn("Room not found for id {}", roomId);
                    return new RoomNotFoundException("Room not found for id " + roomId);
                }
        );
        if (input.hasRoomNumber() &&
                !roomToUpdate.getRoomNumber().equals(input.getRoomNumber())) {
            if (roomRepository.existsByRoomNumber(input.getRoomNumber())) {
                throw new RoomAlreadyExistsException("Room with this room number already exists: " + input.getRoomNumber());
            }
            roomToUpdate.setRoomNumber(input.getRoomNumber());
        }

        if (input.hasRoomType()) {
            roomToUpdate.setType(input.getRoomType());
        }

        if (input.hasCapacity()) {
            roomToUpdate.setCapacity(input.getCapacity());
        }

        if (input.hasPricePerNight()) {
            roomToUpdate.setPricePerNight(input.getPricePerNight());
        }
        if (input.hasDescription()){
            roomToUpdate.setDescription(input.getDescription());
        }
        Room room = roomRepository.save(roomToUpdate);
        log.debug("Successfully updated room: id {}", roomId);
        return convertToDto(room);
    }

    @Transactional
    public void deleteRoom(Long roomId){
        log.info("Deleting room: id {}", roomId);
        Room roomToDelete = roomRepository.findById(roomId).orElseThrow(()->{
                    log.warn("Room not found for id {}", roomId);
                    return new RoomNotFoundException("Room not found for id " + roomId);
                }
        );
        if (reservationRepository.existsByRoomIdAndStatus(roomId, ReservationStatus.CONFIRMED)) {
            log.warn("Cannot delete room id {} with active reservations", roomId);
            throw new RoomHasActiveReservationsException("Cannot delete room with active reservations");
        }
        roomRepository.delete(roomToDelete);
        log.info("Successfully deleted room: id {}", roomId);
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