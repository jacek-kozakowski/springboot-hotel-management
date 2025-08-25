package com.reservations.hotel.services;

import com.reservations.hotel.dto.ReservationCreateDto;
import com.reservations.hotel.models.Reservation;
import com.reservations.hotel.models.ReservationStatus;
import com.reservations.hotel.models.Room;
import com.reservations.hotel.models.User;
import com.reservations.hotel.repositories.ReservationRepository;
import com.reservations.hotel.repositories.RoomRepository;
import com.reservations.hotel.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final RoomService roomService;
    private final UserService userService;
    public ReservationService(ReservationRepository reservationRepository, RoomService roomService, UserService userService) {
        this.reservationRepository = reservationRepository;
        this.roomService = roomService;
        this.userService = userService;
    }
    public List<Reservation> getUserReservations(Long userId) {
        return reservationRepository.findByUserId(userId);
    }
    public Reservation createReservation(Long userId, ReservationCreateDto reservationDto) {
        User user = userService.getUserById(userId);
        Room room = roomService.getRoomByRoomId(reservationDto.getRoomId());

        validateReservationDates(reservationDto.getCheckInDate(), reservationDto.getCheckOutDate());

        if (!roomService.isRoomAvailable(room.getId(), reservationDto.getCheckInDate(), reservationDto.getCheckOutDate())) {
            throw new RuntimeException("Room is not available for the selected dates");
        }
        Reservation reservation = new Reservation(user, room, reservationDto.getCheckInDate(), reservationDto.getCheckOutDate());
        return reservationRepository.save(reservation);
    }
    public Reservation confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        reservation.setStatus(ReservationStatus.CONFIRMED);

        return reservationRepository.save(reservation);
    }

    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        if (reservation.getCheckInDate().isBefore(LocalDate.now().plusDays(1))) {
            throw new RuntimeException("Cancellation must be made at least 24 hours before check-in");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(reservation);
    }

    private void validateReservationDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn.isBefore(LocalDate.now())) {
            throw new RuntimeException("Check-in date cannot be in the past");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new RuntimeException("Check-out date must be after check-in date");
        }
        if (checkIn.isEqual(checkOut)) {
            throw new RuntimeException("Minimum stay is 1 night");
        }
    }
}
