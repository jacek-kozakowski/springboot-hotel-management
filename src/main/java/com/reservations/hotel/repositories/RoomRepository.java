package com.reservations.hotel.repositories;

import com.reservations.hotel.models.Room;
import com.reservations.hotel.models.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomNumber(Integer roomNumber);
    List<Room> findRoomsByType(RoomType type);
    List<Room> findRoomsByCapacityIsGreaterThanEqual(Integer minCapacity);
    List<Room> findRoomsByCapacityIsGreaterThanAndPricePerNightIsLessThanEqual(Integer minCapacity, Double maxPricePerNight);
    List<Room> findRoomsByPricePerNightIsLessThanEqual(Double maxPricePerNight);
    List<Room> findRoomsByPricePerNightBetween(Double minPricePerNight, Double maxPricePerNight);

}
