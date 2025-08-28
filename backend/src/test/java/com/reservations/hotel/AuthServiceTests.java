package com.reservations.hotel;

import com.reservations.hotel.dto.LoginDto;
import com.reservations.hotel.dto.RegisterDto;
import com.reservations.hotel.dto.UserResponseDto;
import com.reservations.hotel.dto.VerifyDto;
import com.reservations.hotel.exceptions.UserAlreadyExistsException;
import com.reservations.hotel.models.User;
import com.reservations.hotel.repositories.UserRepository;
import com.reservations.hotel.services.EmailService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import com.reservations.hotel.services.AuthService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {
    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerUser_ShouldSuccessfullyRegisterUser_WhenDataIsValid() throws MessagingException {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password123");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDto response = authService.registerUser(registerDto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertFalse(savedUser.isEnabled());
        assertNotNull(savedUser.getVerificationCode());
        assertNotNull(savedUser.getVerificationExpiration());

        // sprawdzamy, że save i emailService zostały wywołane
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(emailService).sendVerificationEmail(
                eq(savedUser.getEmail()),
                anyString(),
                anyString()
        );

        // dodatkowo możemy sprawdzić, że response DTO ma poprawny ID/email
        assertEquals(savedUser.getId(), response.getId());
        assertEquals(savedUser.getEmail(), response.getEmail());
    }


    @Test
    void registerUser_ShouldThrowException_WhenUserAlreadyExists() throws MessagingException {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setEmail("existing@example.com");
        registerDto.setPassword("password123");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            authService.registerUser(registerDto);
        });

        assertEquals("User with this email already exists", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void authenticate_ShouldReturnUser_WhenCredentialsAreValidAndUserIsVerified() {
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password123");

        User mockUser = new User();
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("hashedPassword");
        mockUser.setEnabled(true);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));

        User result = authService.authenticate(loginDto);
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(authenticationManager).authenticate(any());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void verifyUser_ShouldEnableUser_WhenCodeIsValidAndNotExpired() {
        VerifyDto verifyDto = new VerifyDto();
        verifyDto.setEmail("test@example.com");
        verifyDto.setVerificationCode("123456");

        User mockUser = new User();
        mockUser.setEmail("test@example.com");
        mockUser.setVerificationCode("123456");
        mockUser.setVerificationExpiration(java.time.LocalDateTime.now().plusMinutes(15));
        mockUser.setEnabled(false);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        authService.verifyUser(verifyDto);

        assertTrue(mockUser.isEnabled());
        assertNull(mockUser.getVerificationCode());
        assertNull(mockUser.getVerificationExpiration());

        verify(userRepository).save(mockUser);
    }
}
