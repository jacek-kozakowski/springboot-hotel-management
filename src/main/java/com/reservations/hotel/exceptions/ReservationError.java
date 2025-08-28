package com.reservations.hotel.exceptions;

import lombok.Getter;

@Getter
public enum ReservationError {
    CHECK_IN_DATE_IN_PAST("Check-in date cannot be in the past."),
    CHECK_OUT_DATE_BEFORE_CHECK_IN("Check-out date must be after check-in date."),
    MINIMUM_STAY_ONE_NIGHT("Minimum stay is 1 night."),
    ROOM_NOT_AVAILABLE("The selected room is not available for the given dates."),
    CANCELLATION_TOO_LATE("Cancellations must be made at least 24 hours before check-in."),
    INVALID_RESERVATION_STATUS("Reservation cannot be modified in its current status.")
    ;

    private final String message;
    ReservationError(String s) {
        this.message = s;
    }

}
