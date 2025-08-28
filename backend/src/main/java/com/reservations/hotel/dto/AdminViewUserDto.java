package com.reservations.hotel.dto;

import com.reservations.hotel.models.Role;
import com.reservations.hotel.models.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdminViewUserDto {
    Long id;
    String email;
    private String password;

    private boolean enabled;
    private String verificationCode;
    private LocalDateTime verificationExpiration;
    private Role role;
    private boolean credentialsNonExpired;
    private boolean accountNonLocked;
    private boolean accountNonExpired;

    public AdminViewUserDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.enabled = user.isEnabled();
        this.verificationCode = user.getVerificationCode();
        this.verificationExpiration = user.getVerificationExpiration();
        this.role = user.getRole();
        this.credentialsNonExpired = user.isCredentialsNonExpired();
        this.accountNonLocked = user.isAccountNonLocked();
        this.accountNonExpired = user.isAccountNonExpired();
    }

}
