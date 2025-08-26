package com.reservations.hotel.controllers;

import com.reservations.hotel.models.User;
import com.reservations.hotel.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<User> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User currentUser = userService.getCurrentUser(email);
        return ResponseEntity.ok(currentUser);
    }
    @GetMapping("/get-users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getSpecificUser(@PathVariable(required = false) Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("GET /users/user: Admin {} is fetching users by userId: {}", authentication.getName(), userId);
        List<User> users = userService.getSpecificUsers(userId);
        log.debug("GET /users/user Admin {} Fetched {} users", authentication.getName(), users.size());
        return ResponseEntity.ok(users);
    }
}
