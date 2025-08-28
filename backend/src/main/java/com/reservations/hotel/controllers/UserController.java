package com.reservations.hotel.controllers;

import com.reservations.hotel.dto.AdminViewUserDto;
import com.reservations.hotel.dto.ReservationResponseDto;
import com.reservations.hotel.dto.UserResponseDto;
import com.reservations.hotel.models.User;
import com.reservations.hotel.services.ReservationService;
import com.reservations.hotel.services.UserService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@Validated
public class UserController {
    private final UserService userService;
    private final ReservationService reservationService;

    public UserController(UserService userService, ReservationService reservationService) {
        this.userService = userService;
        this.reservationService = reservationService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserResponseDto currentUser = userService.getCurrentUserDto(email);
        return ResponseEntity.ok(currentUser);
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminViewUserDto>> getAllUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("GET /users: Admin {} is fetching all users", authentication.getName());
        List<AdminViewUserDto> users = userService.getAllUsersForAdmin();
        log.debug("GET /users  Fetched {} users", users.size());
        return ResponseEntity.ok(users);
    }
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminViewUserDto> getSpecificUser(@PathVariable(required = false) Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("GET /users/{}: Admin {} is fetching user by userId",userId, authentication.getName());
        AdminViewUserDto user = userService.getSpecificUsers(userId);
        log.debug("GET /users/{}  Fetched user", userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me/reservations")
    public ResponseEntity<List<ReservationResponseDto>> getMyReservations() {
        User user = getCurrentUser();
        List<ReservationResponseDto> reservations = reservationService.getUserReservationsDto(user.getId());
        return ResponseEntity.ok(reservations);
    }
    @GetMapping("/{userId}/reservations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationResponseDto>> getUserReservations(@PathVariable @NonNull Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Admin {} is fetching reservations for user with ID {}", authentication.getName(), userId);
        User user = userService.getUserById(userId);
        List<ReservationResponseDto> reservations = reservationService.getUserReservationsDto(user.getId());
        return ResponseEntity.ok(reservations);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userService.getCurrentUser(email);
    }
}
