package com.reservations.hotel;

import com.reservations.hotel.config.JwtAuthFilter;
import com.reservations.hotel.config.TestSecurityConfig;
import com.reservations.hotel.controllers.ReservationController;
import com.reservations.hotel.dto.ReservationResponseDto;
import com.reservations.hotel.models.Reservation;
import com.reservations.hotel.models.Room;
import com.reservations.hotel.models.RoomType;
import com.reservations.hotel.models.User;
import com.reservations.hotel.services.ReservationService;
import com.reservations.hotel.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ReservationController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthFilter.class
        )
)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
public class ReservationControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ReservationService reservationService;
    @MockitoBean private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllReservations_ShouldReturnAllReservations() throws Exception {
        ReservationResponseDto dto = new ReservationResponseDto(mockReservation());
        when(reservationService.getAllReservations()).thenReturn(List.of(dto));

        mockMvc.perform(MockMvcRequestBuilders.get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dto.getId()));
    }

    @Test
    @WithMockUser
    void reserveRoom_createsReservation() throws Exception{
        User user = mockUser(1L, "u@e.com");
        when(userService.getCurrentUser(anyString())).thenReturn(user);
        ReservationResponseDto dto = new ReservationResponseDto(mockReservation());
        when(reservationService.createReservation(eq(1L), any())).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomId\":5,\"checkInDate\":\"2030-01-10\",\"checkOutDate\":\"2030-01-12\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(dto.getId()));
    }

    // Helpers
    private Reservation mockReservation() {
        User user = mockUser(1L, "u@e.com");
        Room room = new Room(101, RoomType.SINGLE, 100.0, 1, "desc");
        room.setId(5L);
        Reservation reservation = new Reservation(user, room, LocalDate.of(2030,1,10), LocalDate.of(2030,1,12));
        reservation.setId(10L);
        return reservation;
    }

    private User mockUser(Long id, String email) {
        User u = new User(email, "pass");
        u.setId(id);
        return u;
    }
}
