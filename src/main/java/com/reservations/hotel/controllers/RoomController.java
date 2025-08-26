package com.reservations.hotel.controllers;

import com.reservations.hotel.dto.RoomCreateDto;
import com.reservations.hotel.dto.RoomResponseDto;
import com.reservations.hotel.exceptions.InvalidSearchParametersException;
import com.reservations.hotel.models.Room;
import com.reservations.hotel.models.RoomType;
import com.reservations.hotel.services.RoomService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/rooms")
@Validated
@Slf4j
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<RoomResponseDto>> getAllRooms() {
        List<RoomResponseDto> rooms = roomService.getAllRoomsDto();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/room")
    public ResponseEntity<List<RoomResponseDto>> getSpecific(@RequestParam(required = false) Integer roomNumber,
                                                             @RequestParam(required = false) RoomType type,
                                                             @RequestParam(required = false) Integer minCapacity,
                                                             @RequestParam(required = false) Double maxPricePerNight,
                                                             @RequestParam(required = false) LocalDate checkInDate,
                                                             @RequestParam(required = false) LocalDate checkOutDate) {
        List<RoomResponseDto> rooms = roomService.getSpecificRoomsDto(roomNumber, type, minCapacity, maxPricePerNight, checkInDate, checkOutDate);
        return ResponseEntity.status(HttpStatus.OK).body(rooms);
    }


    @PostMapping("/create-room")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Room> createRoom(@RequestBody @Valid RoomCreateDto room) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("POST /create-room: Admin {} creating room: {} ({}), capacity: {}, price per night: {}", authentication.getName(), room.getRoomNumber(), room.getRoomType(), room.getCapacity(), room.getPricePerNight());
        Room createdRoom = roomService.addRoom(room);
        log.debug("POST /create-room Admin {} Created room successfully", authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
    }
}
