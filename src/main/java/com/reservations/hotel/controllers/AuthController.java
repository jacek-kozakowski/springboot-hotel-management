package com.reservations.hotel.controllers;

import com.reservations.hotel.dto.LoginDto;
import com.reservations.hotel.dto.RegisterDto;
import com.reservations.hotel.dto.VerifyDto;
import com.reservations.hotel.exceptions.InvalidVerificationCodeException;
import com.reservations.hotel.exceptions.UserAlreadyVerifiedException;
import com.reservations.hotel.exceptions.UserNotFoundException;
import com.reservations.hotel.exceptions.VerificationExpiredException;
import com.reservations.hotel.models.User;
import com.reservations.hotel.responses.LoginResponse;
import com.reservations.hotel.services.AuthService;
import com.reservations.hotel.services.JwtService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    private final JwtService jwtService;
    private final AuthService authenticationService;

    public AuthController(JwtService jwtService, AuthService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody @Valid RegisterDto user) {
        log.info("POST /auth/register: Registering user with email: {}", user.getEmail());
        User registeredUser = authenticationService.registerUser(user);
        log.debug("POST /auth/register User registered with ID: {}", registeredUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody @Valid LoginDto user) {
        log.info("POST /auth/login: Authenticating user with email: {}", user.getEmail());
        User authenticatedUser = authenticationService.authenticate(user);
        String token = jwtService.generateToken(authenticatedUser);
        long expiresIn = jwtService.getExpirationTime();
        LoginResponse response = new LoginResponse(token, expiresIn);
        log.debug("POST /auth/login User authenticated with ID: {}", authenticatedUser.getId());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody @Valid VerifyDto verifyDto) {
        log.info("POST /auth/verify: Verifying user with email: {}", verifyDto.getEmail());
        try{
            authenticationService.verifyUser(verifyDto);
            log.debug("POST /auth/verify User successfully verified with email: {}", verifyDto.getEmail());
            return ResponseEntity.ok("User verified successfully");
        } catch (UserAlreadyVerifiedException | VerificationExpiredException | InvalidVerificationCodeException |
                 UserNotFoundException e) {
            log.warn("POST /auth/verify Verification failed for email: {}: {}", verifyDto.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerification(@RequestBody String email) {
        log.info("POST /auth/resend: Resending verification email to: {}", email);
        try {
            authenticationService.resendVerificationCode(email);
            log.debug("POST /auth/resend Verification email resent successfully to: {}", email);
            return ResponseEntity.ok("Verification email resent successfully");
        } catch (UserAlreadyVerifiedException | UserNotFoundException e) {
            log.warn("POST /auth/resend Resend failed for email: {}: {}", email, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
