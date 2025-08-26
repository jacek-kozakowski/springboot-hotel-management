package com.reservations.hotel.dto;

import com.reservations.hotel.models.RoomType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomCreateDto {
    @NotNull
    private Integer roomNumber;
    @NotNull
    private RoomType roomType;
    @NotNull
    @Min(1)
    private Integer capacity;
    @NotNull
    @Min(0)
    private Double pricePerNight;
    @NotBlank
    private String description;

}
