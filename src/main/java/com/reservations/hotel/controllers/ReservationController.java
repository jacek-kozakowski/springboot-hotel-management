package com.reservations.hotel.controllers;

import com.reservations.hotel.dto.ReservationCreateDto;
import com.reservations.hotel.dto.ReservationResponseDto;
import com.reservations.hotel.models.Reservation;
import com.reservations.hotel.models.User;
import com.reservations.hotel.services.ReservationService;
import com.reservations.hotel.services.UserService;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService reservationService;
    private final UserService userService;
    public ReservationController(ReservationService reservationService, UserService userService) {
        this.reservationService = reservationService;
        this.userService = userService;
    }
    @GetMapping("/my_reservations")
    public ResponseEntity<List<ReservationResponseDto>> getMyReservations() {
        User user = getCurrentUser();
        List<ReservationResponseDto> reservations = reservationService.getUserReservationsDto(user.getId());
        return ResponseEntity.ok(reservations);
    }
    @GetMapping("/get_reservations/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationResponseDto>> getUserReservations(@PathVariable @NonNull Long userId) {
        User user = userService.getUserById(userId);
        List<ReservationResponseDto> reservations = reservationService.getUserReservationsDto(user.getId());
        return ResponseEntity.ok(reservations);
    }
    @PostMapping("/reserve_room")
    public ResponseEntity<Reservation> reserveRoom(@RequestBody ReservationCreateDto input) {
        User user = getCurrentUser();
        Reservation createdReservation = reservationService.createReservation(user.getId(), input);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReservation);
    }
    @PostMapping("/confirm_reservation/{reservationId}")
    public ResponseEntity<Reservation> confirmReservation(@PathVariable Long reservationId) {

        if (isNotReservationOwner(reservationId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Reservation confirmedReservation = reservationService.confirmReservation(reservationId);
        return ResponseEntity.status(HttpStatus.OK).body(confirmedReservation);
    }

    @PostMapping("/cancel_reservation/{reservationId}")
    public ResponseEntity<Reservation> cancelReservation(@PathVariable Long reservationId) {

        if (isNotReservationOwner(reservationId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Reservation cancelledReservation = reservationService.cancelReservation(reservationId);
        return ResponseEntity.status(HttpStatus.OK).body(cancelledReservation);
    }

    private boolean isNotReservationOwner(Long reservationId) {
        User user = getCurrentUser();
        return user.getReservations().stream()
                .noneMatch(r -> r.getId().equals(reservationId));
    }
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.getCurrentUser(email);
    }
}
