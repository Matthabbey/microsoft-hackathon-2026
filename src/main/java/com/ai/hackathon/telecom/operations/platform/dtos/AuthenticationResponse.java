package com.ai.hackathon.telecom.operations.platform.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class AuthenticationResponse {
    private String token;
}
