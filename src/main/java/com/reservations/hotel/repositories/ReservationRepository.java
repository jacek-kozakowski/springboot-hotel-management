package com.reservations.hotel.repositories;

import com.reservations.hotel.models.Reservation;
import com.reservations.hotel.models.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId); // Method to find reservations by user ID
    List<Reservation> findByRoomId(Long roomId); // Method to find reservations by room ID
    List<Reservation> findByUserIdAndRoomId(Long userId, Long roomId); // Method to find reservations by user ID and room ID

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Reservation r " +
            "WHERE r.room.id = :roomId " +
            "AND r.status NOT IN (com.reservations.hotel.models.ReservationStatus.CANCELLED, " +
            "com.reservations.hotel.models.ReservationStatus.COMPLETED) " +
            "AND (:checkIn < r.checkOutDate AND :checkOut > r.checkInDate)")
    boolean existsConflictingReservation(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    List<Reservation> findByStatusAndCheckOutDateBefore(ReservationStatus reservationStatus, LocalDate today);
}
