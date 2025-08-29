package com.reservations.hotel.dto;

import com.reservations.hotel.models.Role;
import com.reservations.hotel.models.User;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AdminViewUserDto {
    Long id;
    String email;
    private boolean enabled;
    private Role role;
    private boolean credentialsNonExpired;
    private boolean accountNonLocked;
    private boolean accountNonExpired;

    public AdminViewUserDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.enabled = user.isEnabled();
        this.role = user.getRole();
        this.credentialsNonExpired = user.isCredentialsNonExpired();
        this.accountNonLocked = user.isAccountNonLocked();
        this.accountNonExpired = user.isAccountNonExpired();
    }

}
