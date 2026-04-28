package com.ai.hackathon.telecom.operations.platform.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class ProfileResponse {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private boolean accountLocked;
    private boolean enabled;
    private List<String> roles;
    private List<String> permissions;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
