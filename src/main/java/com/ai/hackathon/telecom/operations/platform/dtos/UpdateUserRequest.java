package com.ai.hackathon.telecom.operations.platform.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Boolean accountLocked;
    private Boolean enabled;
    private List<String> roles;
}
