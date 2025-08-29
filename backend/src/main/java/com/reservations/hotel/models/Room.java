package com.reservations.hotel.models;

import com.reservations.hotel.dto.RoomCreateDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Positive;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer roomNumber;

    @Enumerated(EnumType.STRING)
    private RoomType type;

    @NotNull(message = "Price per night cannot be null")
    @Positive(message = "Price per night must be positive")
    private Double pricePerNight;

    @NotNull(message = "Capacity cannot be null")
    @Positive(message = "Capacity must be positive")
    private Integer capacity;

    private String description;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reservation> reservations = new ArrayList<>();

    public Room(Integer roomNumber, RoomType type, Double pricePerNight, Integer capacity, String description) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
        this.description = description;
    }
    public Room(RoomCreateDto input){
        this.roomNumber = input.getRoomNumber();
        this.type = input.getRoomType();
        this.pricePerNight = input.getPricePerNight();
        this.capacity = input.getCapacity();
        this.description = input.getDescription();
    }

    public Room() {
    }
}
