package com.reservations.hotel.services;

import com.reservations.hotel.dto.AdminViewUserDto;
import com.reservations.hotel.dto.UserResponseDto;
import com.reservations.hotel.exceptions.UserNotFoundException;
import com.reservations.hotel.models.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.reservations.hotel.repositories.UserRepository;

import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AdminViewUserDto getSpecificUsers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found", userId);
                    return new UserNotFoundException("User not found");
                });
        return new AdminViewUserDto(user);
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public List<AdminViewUserDto> getAllUsersForAdmin() {
        List<User> users = userRepository.findAll();
        users.sort(Comparator.comparing(User::getId));
        return users.stream().map(AdminViewUserDto::new).toList();
    }
    public User getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() ->{
                    log.warn("User with ID {} not found", id);
                    return new UserNotFoundException("User not found");});
    }
    public User getCurrentUser(String email) {
        log.info("Fetching current user with email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() ->{
            log.warn("User with email {} not found", email);
            return new UserNotFoundException("User not found");});
    }
    public UserResponseDto getCurrentUserDto(String email) {
        User user = getCurrentUser(email);
        return convertToDto(user);
    }

    private UserResponseDto convertToDto(User user) {
        return new UserResponseDto(user);
    }

}
