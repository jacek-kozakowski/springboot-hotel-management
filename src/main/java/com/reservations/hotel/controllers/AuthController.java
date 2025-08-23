package com.reservations.hotel.controllers;

import com.reservations.hotel.dto.LoginDto;
import com.reservations.hotel.dto.RegisterDto;
import com.reservations.hotel.dto.VerifyDto;
import com.reservations.hotel.models.User;
import com.reservations.hotel.responses.LoginResponse;
import com.reservations.hotel.services.AuthService;
import com.reservations.hotel.services.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
public class AuthController {
    private final JwtService jwtService;
    private final AuthService authenticationService;

    public AuthController(JwtService jwtService, AuthService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterDto user) {
        User registeredUser = authenticationService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginDto user) {
        User authenticatedUser = authenticationService.authenticate(user);
        String token = jwtService.generateToken(authenticatedUser);
        long expiresIn = jwtService.getExpirationTime();
        LoginResponse response = new LoginResponse(token, expiresIn);
        return ResponseEntity.ok(response);

    }
    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyDto verifyDto) {
        try{
            authenticationService.verifyUser(verifyDto);
            return ResponseEntity.ok("User verified successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerification(@RequestBody String email) {
        try {
            authenticationService.resendVerificationCode(email);
            return ResponseEntity.ok("Verification email resent successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
