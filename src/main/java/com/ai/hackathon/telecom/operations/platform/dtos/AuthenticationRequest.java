package com.ai.hackathon.telecom.operations.platform.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationRequest {

    @Email
    private String email;
    @NotNull
    private String password;
}