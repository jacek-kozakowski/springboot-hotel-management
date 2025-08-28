package com.reservations.hotel.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter

public class ReservationDateDto {
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    public ReservationDateDto(LocalDate checkInDate, LocalDate checkOutDate) {
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }
}
