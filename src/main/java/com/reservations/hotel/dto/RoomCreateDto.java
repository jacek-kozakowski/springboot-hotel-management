package com.reservations.hotel.dto;

import com.reservations.hotel.models.RoomType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomCreateDto {
    private Integer roomNumber;
    private RoomType roomType;
    private Integer capacity;
    private Double pricePerNight;
    private String description;

}
