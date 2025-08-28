package com.reservations.hotel.config;

import com.reservations.hotel.exceptions.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("message", "One or more fields have errors");
        body.put("errors", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleEntityNotFoundException(EntityNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, "Resource not found");
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleGenericException(Exception ex) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred");
    }
    @ExceptionHandler(UserNotVerifiedException.class)
    public ResponseEntity<Map<String,Object>> handleUserNotVerifiedException(UserNotVerifiedException ex) {
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN, ex.getMessage());
    }
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String,Object>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT, ex.getMessage());
    }
    @ExceptionHandler(VerificationExpiredException.class)
    public ResponseEntity<Map<String,Object>> handleVerificationExpiredException(VerificationExpiredException ex) {
        return buildErrorResponse(ex, HttpStatus.GONE, ex.getMessage());
    }
    @ExceptionHandler(UserAlreadyVerifiedException.class)
    public ResponseEntity<Map<String,Object>> handleUserAlreadyVerifiedException(UserAlreadyVerifiedException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<Map<String,Object>> handleInvalidVerificationCodeException(InvalidVerificationCodeException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleUserNotFoundException(UserNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleReservationNotFoundException(ReservationNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, ex.getMessage());
    }
    @ExceptionHandler(InvalidReservationRequestException.class)
    public ResponseEntity<Map<String,Object>> handleInvalidReservationRequestException(InvalidReservationRequestException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, ex.getError().getMessage());
    }
    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<Map<String,Object>> handleRoomNotFoundException(RoomNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, ex.getMessage());
    }
    @ExceptionHandler(InvalidSearchParametersException.class)
    public ResponseEntity<Map<String,Object>> handleInvalidSearchParametersException(InvalidSearchParametersException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(RoomAlreadyExistsException.class)
    public ResponseEntity<Map<String,Object>> handleRoomAlreadyExistsException(RoomAlreadyExistsException ex) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT, ex.getMessage());
    }
    @ExceptionHandler(RoomHasActiveReservationsException.class)
    public ResponseEntity<Map<String,Object>> handleRoomHasActiveReservationsException(RoomHasActiveReservationsException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    private ResponseEntity<Map<String, Object>> buildErrorResponse(Exception ex, HttpStatus httpStatus, String anInternalServerErrorOccurred) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", httpStatus.value());
        body.put("error", httpStatus.getReasonPhrase());
        body.put("message", anInternalServerErrorOccurred);
        body.put("details", ex.getMessage());
        return new ResponseEntity<>(body, httpStatus);
    }
}
