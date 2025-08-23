package com.reservations.hotel.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rooms")
public class Room {
    @Id
    private String roomNumber;
    private String type;
    private double pricePerNight;
    private boolean isAvailable;

    // Additional fields and methods can be added as needed
}
