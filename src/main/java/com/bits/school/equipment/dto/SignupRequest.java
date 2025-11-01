package com.bits.school.equipment.dto;

import lombok.Data;

@Data
public class SignupRequest {
    private String name;
    private String email;
    private String password;
    private String role; // STUDENT, STAFF, ADMIN
}

