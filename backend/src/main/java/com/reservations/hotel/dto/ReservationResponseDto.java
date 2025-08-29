package com.reservations.hotel.dto;

import com.reservations.hotel.models.Reservation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationResponseDto {
    private Long id;
    private String email;
    private Integer roomNumber;
    private String roomType;
    private Integer roomCapacity;
    private Double roomPricePerNight;
    private Double totalPrice;
    private String status;
    private String checkInDate;
    private String checkOutDate;
    private String createdAt;
    
    public ReservationResponseDto(Reservation reservation){
        this.id = reservation.getId();
        this.email = reservation.getUser().getEmail();
        this.roomNumber = reservation.getRoom().getRoomNumber();
        this.roomType = reservation.getRoom().getType().name();
        this.roomCapacity = reservation.getRoom().getCapacity();
        this.roomPricePerNight = reservation.getRoom().getPricePerNight();
        long days = reservation.getCheckOutDate().toEpochDay() - reservation.getCheckInDate().toEpochDay();
        double rawTotal = days * reservation.getRoom().getPricePerNight();
        this.totalPrice = Math.round(rawTotal * 100.0) / 100.0;
        this.status = reservation.getStatus().name();
        this.checkInDate = reservation.getCheckInDate().toString();
        this.checkOutDate = reservation.getCheckOutDate().toString();
        this.createdAt = reservation.getCreatedAt() != null ? reservation.getCreatedAt().toString() : null;
    }
}
