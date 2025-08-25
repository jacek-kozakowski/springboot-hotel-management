package com.reservations.hotel.controllers;

import com.reservations.hotel.dto.RoomCreateDto;
import com.reservations.hotel.models.Room;
import com.reservations.hotel.models.RoomType;
import com.reservations.hotel.services.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
                                                  @RequestParam(required = false) LocalDate checkInDate,
                                                  @RequestParam(required = false) LocalDate checkOutDate) {
        System.out.println("DEBUG: roomNumber=" + roomNumber + ", type=" + type + ", minCapacity=" + minCapacity +
                ", maxPricePerNight=" + maxPricePerNight + ", checkInDate=" + checkInDate + ", checkOutDate=" + checkOutDate);

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
        List<Room> rooms = roomService.getSpecificRooms(type, minCapacity, maxPricePerNight, checkInDate, checkOutDate);
        return ResponseEntity.status(HttpStatus.OK).body(rooms);
    }


    @PostMapping("/create_room")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Room> createRoom(@RequestBody RoomCreateDto room) {
        Room createdRoom = roomService.addRoom(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
    }
}
