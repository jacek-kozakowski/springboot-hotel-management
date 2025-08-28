package com.reservations.hotel.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ReservationCreateDto {
    @NotNull
    private Long roomId;

    @NotNull
    @Future
    private LocalDate checkInDate;

    @NotNull
    @Future
    private LocalDate checkOutDate;

    @AssertTrue(message = "Check out date must be after check in date")
    public boolean isCheckOutAfterCheckin(){
        return checkOutDate.isAfter(checkInDate);
    }


}
