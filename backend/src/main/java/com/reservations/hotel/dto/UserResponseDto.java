package com.reservations.hotel.dto;

import com.reservations.hotel.models.Role;
import com.reservations.hotel.models.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserResponseDto {
    private Long id;
    private String email;
    private Role role;
    private boolean enabled;


    public UserResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.enabled = user.isEnabled();
    }
}
