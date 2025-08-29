package com.reservations.hotel.services;

import com.reservations.hotel.dto.ReservationCreateDto;
import com.reservations.hotel.dto.ReservationResponseDto;
import com.reservations.hotel.exceptions.InvalidReservationRequestException;
import com.reservations.hotel.exceptions.ReservationError;
import com.reservations.hotel.exceptions.ReservationNotFoundException;
import com.reservations.hotel.models.Reservation;
import com.reservations.hotel.models.ReservationStatus;
import com.reservations.hotel.models.Room;
import com.reservations.hotel.models.User;
import com.reservations.hotel.repositories.ReservationRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
@Slf4j
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

    public List<ReservationResponseDto> getUserReservationsDto(Long userId) {
        List<Reservation> reservations = reservationRepository.findByUserId(userId);
        return reservations.stream()
                .map(this::convertToDto).toList();
    }

    public List<ReservationResponseDto> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(this::convertToDto).toList();
    }

    @Transactional
    public ReservationResponseDto createReservation(Long userId, ReservationCreateDto reservationDto) {
        log.info("Creating reservation for user ID: {} with details: {}", userId, reservationDto);
        User user = userService.getUserById(userId);
        Room room = roomService.getRoomByRoomId(reservationDto.getRoomId());

        validateReservationDates(reservationDto.getCheckInDate(), reservationDto.getCheckOutDate());

        if (!roomService.isRoomAvailable(room.getId(), reservationDto.getCheckInDate(), reservationDto.getCheckOutDate())) {
            log.warn("Room ID: {} is not available from {} to {}", room.getRoomNumber(), reservationDto.getCheckInDate(), reservationDto.getCheckOutDate());
            throw new InvalidReservationRequestException(ReservationError.ROOM_NOT_AVAILABLE);
        }
        Reservation reservation = new Reservation(user, room, reservationDto.getCheckInDate(), reservationDto.getCheckOutDate());
        log.debug("Reservation Created - Reservation details: {}", reservation);
        return convertToDto(reservationRepository.save(reservation));
    }
    @Transactional
    public ReservationResponseDto confirmReservation(Long reservationId) {
        log.info("Confirming reservation ID: {}", reservationId);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() ->{
                    log.warn("Reservation Confirmation Failed - Reservation ID: {} not found for confirmation", reservationId);
                    return new ReservationNotFoundException("Reservation not found");});
        if (!reservation.getStatus().equals(ReservationStatus.PENDING)) {
            log.warn("Reservation Confirmation Failed - Invalid Status - Reservation ID: {} is not in PENDING status", reservationId);
            throw new InvalidReservationRequestException(ReservationError.INVALID_RESERVATION_STATUS);
        }
        reservation.setStatus(ReservationStatus.CONFIRMED);
        log.debug("Reservation Confirmed - Reservation details: {}", reservation);
        return convertToDto(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponseDto cancelReservation(Long reservationId) {
        log.info("Cancelling reservation ID: {}", reservationId);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() ->{
                    log.warn("Reservation Cancellation Failed - Reservation ID: {} not found for cancellation", reservationId);
                    return new ReservationNotFoundException("Reservation not found");});
        if (reservation.getCheckInDate().isBefore(LocalDate.now().plusDays(1))) {
            log.warn("Reservation Cancellation Failed - Cancellation Too Late - Reservation ID: {} cannot be cancelled less than 24 hours before check-in", reservationId);
            throw new InvalidReservationRequestException(ReservationError.CANCELLATION_TOO_LATE);
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        log.debug("Reservation Cancelled - Reservation details: {}", reservation);

        return convertToDto(reservationRepository.save(reservation));
    }


    @Transactional
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateCompletedReservations() {
        log.info("Starting automatic update of completed reservations");

        LocalDate today = LocalDate.now();
        List<Reservation> completedReservations = reservationRepository
                .findByStatusAndCheckOutDateBefore(ReservationStatus.CONFIRMED, today);

        for (Reservation reservation : completedReservations) {
            reservation.setStatus(ReservationStatus.COMPLETED);
            log.debug("Updated reservation ID: {} to COMPLETED", reservation.getId());
        }

        reservationRepository.saveAll(completedReservations);
        log.info("Updated {} reservations to COMPLETED status", completedReservations.size());
    }

    private void validateReservationDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn.isBefore(LocalDate.now())) {
            throw new InvalidReservationRequestException(ReservationError.CHECK_IN_DATE_IN_PAST);
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidReservationRequestException(ReservationError.CHECK_OUT_DATE_BEFORE_CHECK_IN);
        }
        if (checkIn.isEqual(checkOut)) {
            throw new InvalidReservationRequestException(ReservationError.MINIMUM_STAY_ONE_NIGHT);
        }
    }
    private ReservationResponseDto convertToDto(Reservation reservation) {
        return new ReservationResponseDto(reservation);
    }
}
