package com.reservations.hotel.services;

import com.reservations.hotel.dto.LoginDto;
import com.reservations.hotel.dto.RegisterDto;
import com.reservations.hotel.dto.VerifyDto;
import com.reservations.hotel.exceptions.*;
import com.reservations.hotel.models.User;
import com.reservations.hotel.repositories.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
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

    public User registerUser(@Valid RegisterDto input) {
        // Check if the user already exists
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }
        User newUser = new User(input.getEmail(), passwordEncoder.encode(input.getPassword()));
        newUser.setVerificationCode(generateVerificationCode());
        newUser.setVerificationExpiration(LocalDateTime.now().plusMinutes(EXPIRATION_TIME_MINUTES));
        newUser.setEnabled(false);
        sendVerificationEmail(newUser);
        return userRepository.save(newUser);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email != null && email.matches(emailRegex);
    }

    public User authenticate(LoginDto input) {
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + input.getEmail()));
        if (!user.isEnabled()) {
            throw new UserNotVerifiedException("User account is not verified. Please check your email for verification link.");
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword())
        );

        return user;
    }

    public void verifyUser(VerifyDto input) {
        Optional<User> userOptional = userRepository.findByEmail(input.getEmail());
        if (userOptional.isPresent()){
            User user = userOptional.get();
            if(user.isEnabled()){
                throw new UserAlreadyVerifiedException("User account is already verified.");
            }
            if (user.getVerificationExpiration().isBefore(LocalDateTime.now())) {
                throw new VerificationExpiredException("Verification code has expired. Please request a new verification code.");
            }
            if (user.getVerificationCode().equals(input.getVerificationCode())) {
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationExpiration(null);
                userRepository.save(user);
            } else {
                throw new InvalidVerificationCodeException("Invalid verification code.");
            }
        }else{
            throw new UserNotFoundException("User not found with email: " + input.getEmail());
        }
    }
    public void resendVerificationCode(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.isEnabled()) {
                throw new UserAlreadyVerifiedException("User account is already verified.");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationExpiration(LocalDateTime.now().plusMinutes(15));
            sendVerificationEmail(user);
            userRepository.save(user);
        } else {
            throw new UserNotFoundException("User not found with email: " + email);
        }
    }

    public void sendVerificationEmail(User user) {
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
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000)+ 100000;
        return String.valueOf(code);
    }
}


