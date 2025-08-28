package com.reservations.hotel.controllers;

import com.reservations.hotel.dto.ReservationCreateDto;
import com.reservations.hotel.dto.ReservationResponseDto;
import com.reservations.hotel.models.Reservation;
import com.reservations.hotel.models.User;
import com.reservations.hotel.services.ReservationService;
import com.reservations.hotel.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/reservations")
@Slf4j
@Validated
public class ReservationController {
    private final ReservationService reservationService;
    private final UserService userService;
    public ReservationController(ReservationService reservationService, UserService userService) {
        this.reservationService = reservationService;
        this.userService = userService;
    }
    @PostMapping
    public ResponseEntity<ReservationResponseDto> reserveRoom(@RequestBody @Valid ReservationCreateDto input) {
        User user = getCurrentUser();
        ReservationResponseDto createdReservation = reservationService.createReservation(user.getId(), input);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReservation);
    }
    @PatchMapping("/{reservationId}/confirm")
    public ResponseEntity<ReservationResponseDto> confirmReservation(@PathVariable Long reservationId) {

        if (isNotReservationOwner(reservationId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ReservationResponseDto confirmedReservation = reservationService.confirmReservation(reservationId);
        return ResponseEntity.status(HttpStatus.OK).body(confirmedReservation);
    }

    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<ReservationResponseDto> cancelReservation(@PathVariable Long reservationId) {

        if (isNotReservationOwner(reservationId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ReservationResponseDto cancelledReservation = reservationService.cancelReservation(reservationId);
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
