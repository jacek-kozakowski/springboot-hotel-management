package com.reservations.hotel.services;

import com.reservations.hotel.exceptions.UserNotFoundException;
import com.reservations.hotel.models.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<User> getSpecificUsers(Long userId) {
        if (userId != null) {
            return List.of(getUserById(userId));
        } else {
            return getAllUsers().stream().sorted(Comparator.comparing(User::getId)).toList();
        }
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public User getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() ->{
                    log.warn("User with ID {} not found", id);
                    return new UserNotFoundException("User not found");});
    }
    public User getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() ->{
                    log.warn("User with email {} not found", email);
                    return new UserNotFoundException("User not found");});
    }
    public User getCurrentUser(String email) {
        log.info("Fetching current user with email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() ->{
            log.warn("User with email {} not found", email);
            return new UserNotFoundException("User not found");});
    }

}
