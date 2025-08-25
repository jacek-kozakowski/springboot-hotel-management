package com.reservations.hotel.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDate checkInDate;
    @Column(nullable = false)
    private LocalDate checkOutDate;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    public Reservation(@NonNull User user, @NonNull Room room, @NonNull LocalDate checkInDate, @NonNull LocalDate checkOutDate) {
        this.user = user;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.createdAt = LocalDateTime.now();
        this.status = ReservationStatus.PENDING; // Default status
    }
    public Reservation() {
        this.createdAt = LocalDateTime.now();
        this.status = ReservationStatus.PENDING; // Default status
    }
}
