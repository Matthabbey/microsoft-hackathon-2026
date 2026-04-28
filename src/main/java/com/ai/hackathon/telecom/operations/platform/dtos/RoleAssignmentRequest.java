package com.ai.hackathon.telecom.operations.platform.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class RoleAssignmentRequest {
    @NotEmpty(message = "At least one role is required")
    private List<String> roles;
}
