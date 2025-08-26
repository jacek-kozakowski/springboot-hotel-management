package com.reservations.hotel.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.reservations.hotel.models.Room;
import com.reservations.hotel.models.RoomType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonPropertyOrder({ "roomNumber", "type", "pricePerNight", "capacity", "description", "bookedDates" })
public class RoomResponseDto {
    private Integer roomNumber;
    private RoomType type;
    private Double pricePerNight;
    private Integer capacity;
    private String description;
    private List<ReservationDateDto> bookedDates;

    public RoomResponseDto(Room room, List<ReservationDateDto> bookedDates) {
        this.roomNumber = room.getRoomNumber();
        this.type = room.getType();
        this.pricePerNight = room.getPricePerNight();
        this.capacity = room.getCapacity();
        this.description = room.getDescription();
        this.bookedDates = bookedDates;
    }

}
