package com.reservations.hotel.exceptions;

public class InvalidSearchParametersException extends RuntimeException {
    public InvalidSearchParametersException(String message) {
        super(message);
    }
}
