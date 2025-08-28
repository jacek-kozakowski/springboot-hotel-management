package com.reservations.hotel.exceptions;

import lombok.Getter;

@Getter
public class InvalidReservationRequestException extends RuntimeException {
    private final ReservationError error;
    public InvalidReservationRequestException(ReservationError error) {
        super(error.getMessage());
        this.error = error;
    }
}
