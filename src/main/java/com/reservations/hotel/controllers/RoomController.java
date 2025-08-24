package com.reservations.hotel.controllers;

import com.reservations.hotel.dto.RoomCreateDto;
import com.reservations.hotel.models.Room;
import com.reservations.hotel.models.RoomType;
import com.reservations.hotel.services.RoomService;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Room>> getAllRooms() {
        List<Room> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/room")
    public ResponseEntity<List<Room>> getSpecific(@RequestParam(required = false) Integer roomNumber,
                                                  @RequestParam(required = false) RoomType type,
                                                  @RequestParam(required = false) Integer minCapacity,
                                                  @RequestParam(required = false) Double maxPricePerNight,
                                                  @RequestParam(required = false) LocalDateTime checkInDate,
                                                  @RequestParam(required = false) LocalDateTime checkOutDate) {
        if (roomNumber == null && type == null && minCapacity == null && maxPricePerNight == null && checkInDate == null && checkOutDate == null) {
            return ResponseEntity.ok(roomService.getAllRooms());
        }
        if ((checkInDate != null && checkOutDate == null) || (checkInDate == null && checkOutDate != null)) {
            return ResponseEntity.badRequest().body(List.of());
        }
        if (roomNumber != null && (type == null && minCapacity == null && maxPricePerNight == null)) {
            Room room = roomService.getRoomByRoomNumber(roomNumber);
            return ResponseEntity.ok(List.of(room));
        }
        boolean checkForAvailability = checkInDate != null;
        if (checkForAvailability) {
            if (type != null && minCapacity == null && maxPricePerNight == null) {
                List<Room> rooms = roomService.getAvailableRoomsByType(type, checkInDate, checkOutDate);
                return ResponseEntity.ok(rooms);
            }
            if (type == null && minCapacity != null && maxPricePerNight == null) {
                List<Room> rooms = roomService.getAvailableRoomsByCapacity(minCapacity, checkInDate, checkOutDate);
                return ResponseEntity.ok(rooms);
            }
            if (type == null && minCapacity == null && maxPricePerNight != null) {
                List<Room> rooms = roomService.getAvailableRoomsByMaxPrice(maxPricePerNight, checkInDate, checkOutDate);
                return ResponseEntity.ok(rooms);
            }
        }else{
            if (type != null && minCapacity == null && maxPricePerNight == null) {
                List<Room> rooms = roomService.getRoomsByType(type);
                return ResponseEntity.ok(rooms);
            }
            if (type == null && minCapacity != null && maxPricePerNight == null) {
                List<Room> rooms = roomService.getRoomsByCapacity(minCapacity);
                return ResponseEntity.ok(rooms);
            }
            if (type == null && minCapacity == null && maxPricePerNight != null) {
                List<Room> rooms = roomService.getRoomsByMaxPrice(maxPricePerNight);
                return ResponseEntity.ok(rooms);
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of());
    }


    @PostMapping("/create_room")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Room> createRoom(@RequestBody RoomCreateDto room) {
        Room createdRoom = roomService.addRoom(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
    }
}
