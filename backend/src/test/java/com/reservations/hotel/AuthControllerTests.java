package com.reservations.hotel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservations.hotel.config.JwtAuthFilter;
import com.reservations.hotel.config.TestSecurityConfig;
import com.reservations.hotel.controllers.AuthController;
import com.reservations.hotel.dto.RegisterDto;
import com.reservations.hotel.dto.UserResponseDto;
import com.reservations.hotel.services.AuthService;
import com.reservations.hotel.services.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        value= AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthFilter.class
        )
)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
public class AuthControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerUser_ShouldReturnCreatedStatus_WhenDataIsValid() throws Exception {
        // given
        RegisterDto registerDto = new RegisterDto();
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password123");

        // mockowane DTO, tak jak zwraca endpoint
        UserResponseDto mockResponse = new UserResponseDto();
        mockResponse.setId(1L);
        mockResponse.setEmail("test@example.com");

        when(authService.registerUser(any(RegisterDto.class))).thenReturn(mockResponse);

        // when / then
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("test@example.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1));

        // verify
        verify(authService).registerUser(any(RegisterDto.class));
    }


}
