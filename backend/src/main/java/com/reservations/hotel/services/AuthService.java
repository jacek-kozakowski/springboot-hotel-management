package com.reservations.hotel.services;

import com.reservations.hotel.dto.LoginDto;
import com.reservations.hotel.dto.RegisterDto;
import com.reservations.hotel.dto.UserResponseDto;
import com.reservations.hotel.dto.VerifyDto;
import com.reservations.hotel.exceptions.*;
import com.reservations.hotel.models.User;
import com.reservations.hotel.repositories.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class AuthService {
    private static final int EXPIRATION_TIME_MINUTES = 15;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, EmailService emailService, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public UserResponseDto registerUser(@Valid RegisterDto input) {
        log.info("Registering user with email: {}", input.getEmail());
        // Check if the user already exists
        if (userRepository.existsByEmail(input.getEmail())) {
            log.warn("User with email {} already exists", input.getEmail());
            throw new UserAlreadyExistsException("User with this email already exists");
        }
        User newUser = new User(input.getEmail(), passwordEncoder.encode(input.getPassword()));
        newUser.setVerificationCode(generateVerificationCode());
        newUser.setVerificationExpiration(LocalDateTime.now().plusMinutes(EXPIRATION_TIME_MINUTES));
        newUser.setEnabled(false);
        sendVerificationEmail(newUser);
        User registeredUser = userRepository.save(newUser);
        log.debug("User registered successfully: {}", newUser);
        return new UserResponseDto(registeredUser);
    }

    public User authenticate(LoginDto input) {
        log.info("Authenticating user with email: {}", input.getEmail());
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed - User with email {} not found", input.getEmail());
                    return new UserNotFoundException("User not found with email: " + input.getEmail());
                });
        if (!user.isEnabled()) {
            log.warn("Login Failed - User with email {} is not verified", input.getEmail());
            throw new UserNotVerifiedException("User account is not verified. Please check your email for verification link.");
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword())
        );
        log.debug("User authenticated successfully: {}", user);
        return user;
    }

    public void verifyUser(VerifyDto input) {
        log.info("Verifying user with email: {}", input.getEmail());
        Optional<User> userOptional = userRepository.findByEmail(input.getEmail());
        if (userOptional.isPresent()){
            User user = userOptional.get();
            if(user.isEnabled()){
                log.warn("Verification Failed - User with email {} is already verified", input.getEmail());
                throw new UserAlreadyVerifiedException("User account is already verified.");
            }
            if (user.getVerificationExpiration().isBefore(LocalDateTime.now())) {
                log.warn("Verification Failed - Verification code for user with email {} has expired", input.getEmail());
                throw new VerificationExpiredException("Verification code has expired. Please request a new verification code.");
            }
            if (user.getVerificationCode().equals(input.getVerificationCode())) {
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationExpiration(null);
                userRepository.save(user);
                log.info("User with email {} verified successfully", input.getEmail());
            } else {
                log.warn("Verification Failed - Invalid verification code for user with email {}", input.getEmail());
                throw new InvalidVerificationCodeException("Invalid verification code.");
            }
        }else{
            log.warn("Verification Failed - User with email {} not found", input.getEmail());
            throw new UserNotFoundException("User not found with email: " + input.getEmail());
        }
    }
    public void resendVerificationCode(String email) {
        log.info("Resending verification code to email: {}", email);
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.isEnabled()) {
                log.warn("Resend Verification Failed - User with email {} is already verified", email);
                throw new UserAlreadyVerifiedException("User account is already verified.");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationExpiration(LocalDateTime.now().plusMinutes(15));
            sendVerificationEmail(user);
            userRepository.save(user);
            log.info("Verification code resent successfully to email: {}", email);
        } else {
            log.warn("Resend Verification Failed - User with email {} not found", email);
            throw new UserNotFoundException("User not found with email: " + email);
        }
    }

    public void sendVerificationEmail(User user) {
        log.info("Sending verification email to: {}", user.getEmail());
        String subject = "Account Verification";
        String verificationCode = user.getVerificationCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to Hotel!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try{
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
            log.info("Verification email sent to: {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", user.getEmail(), e);
        }
    }


    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(900000)+ 100000;
        return String.valueOf(code);
    }
}


