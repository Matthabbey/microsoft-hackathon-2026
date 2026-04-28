package com.ai.hackathon.telecom.operations.platform.user;

import com.ai.hackathon.telecom.operations.platform.dtos.ApiResponse;
import com.ai.hackathon.telecom.operations.platform.dtos.ProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Logged-in user profile")
public class ProfileController {

    private static final Map<String, List<String>> ROLE_PERMISSIONS = new LinkedHashMap<>();

    static {
        ROLE_PERMISSIONS.put("ROLE_VIEWER", List.of("reports:read"));
        ROLE_PERMISSIONS.put("ROLE_AGENT", List.of("calls:read", "calls:write"));
        ROLE_PERMISSIONS.put("ROLE_MANAGER", List.of("users:read", "users:write", "users:assign-roles"));
        ROLE_PERMISSIONS.put("ROLE_ADMIN", List.of("users:delete", "admin:access"));
    }

    // Role hierarchy: ADMIN > MANAGER > AGENT > VIEWER
    private static final List<String> HIERARCHY = List.of(
            "ROLE_VIEWER", "ROLE_AGENT", "ROLE_MANAGER", "ROLE_ADMIN"
    );

    @GetMapping("/me")
    @Operation(summary = "Get the profile, roles and permissions of the currently logged-in user")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile(@AuthenticationPrincipal User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .toList();

        List<String> permissions = resolvePermissions(roles);

        ProfileResponse profile = ProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .accountLocked(user.isAccountLocked())
                .enabled(user.isEnabled())
                .roles(roles)
                .permissions(permissions)
                .createdDate(user.getCreatedDate())
                .lastModifiedDate(user.getLastModifiedDate())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    private List<String> resolvePermissions(List<String> userRoles) {
        int highestIndex = -1;
        for (String role : userRoles) {
            int idx = HIERARCHY.indexOf(role);
            if (idx > highestIndex) {
                highestIndex = idx;
            }
        }

        List<String> permissions = new ArrayList<>();
        for (int i = 0; i <= highestIndex; i++) {
            String role = HIERARCHY.get(i);
            List<String> rolePerms = ROLE_PERMISSIONS.get(role);
            if (rolePerms != null) {
                permissions.addAll(rolePerms);
            }
        }
        return permissions;
    }
}
