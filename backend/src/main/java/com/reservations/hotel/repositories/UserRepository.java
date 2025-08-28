package com.reservations.hotel.repositories;

import com.reservations.hotel.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // Method to find a user by their email address
    Optional<User> findByVerificationCode(String verificationCode); // Method to find a user by their verification code
    boolean existsByEmail(String email); // Method to check if a user exists by their email

}
