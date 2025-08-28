package com.reservations.hotel.dto;

import com.reservations.hotel.models.RoomType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomCreateDto {
    @NotNull
    @Positive
    private Integer roomNumber;

    @NotNull
    private RoomType roomType;

    @NotNull
    @Min(1)
    private Integer capacity;

    @NotNull
    @Positive
    private Double pricePerNight;

    @NotBlank
    private String description;

}
