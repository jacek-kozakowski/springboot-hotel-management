package com.reservations.hotel.exceptions;

public class VerificationExpiredException extends RuntimeException {
    public VerificationExpiredException(String message) {
        super(message);
    }
}
