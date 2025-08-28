package com.reservations.hotel.dto;

import com.reservations.hotel.models.RoomType;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class RoomUpdateDto {
    @Positive
    private Integer roomNumber;

    private RoomType roomType;

    @Positive
    private Integer capacity;

    @Positive
    private Double pricePerNight;

    private String description;

    public boolean hasRoomNumber(){
        return getRoomNumber() != null;
    }

    public boolean hasRoomType(){
        return getRoomType() != null;
    }

    public boolean hasCapacity(){
        return getCapacity() != null;
    }

    public boolean hasPricePerNight(){
        return getPricePerNight() != null;
    }

    public boolean hasDescription(){
        return getDescription() != null;
    }
}
