package com.reservations.hotel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyDto {
    @NotBlank
    private String verificationCode;
    @NotBlank
    @Email
    private String email;
}
