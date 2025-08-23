package com.reservations.hotel.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyDto {
    private String verificationCode;
    private String email;
}
