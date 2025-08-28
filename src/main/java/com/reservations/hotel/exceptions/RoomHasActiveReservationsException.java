package com.reservations.hotel.exceptions;

public class RoomHasActiveReservationsException extends RuntimeException {
    public RoomHasActiveReservationsException(String message) {
        super(message);
    }
}
