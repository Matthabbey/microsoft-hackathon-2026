package com.ai.hackathon.telecom.operations.platform.user;

import com.ai.hackathon.telecom.operations.platform.dtos.ApiResponse;
import com.ai.hackathon.telecom.operations.platform.dtos.CreateUserRequest;
import com.ai.hackathon.telecom.operations.platform.dtos.RoleAssignmentRequest;
import com.ai.hackathon.telecom.operations.platform.dtos.UpdateUserRequest;
import com.ai.hackathon.telecom.operations.platform.dtos.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/management/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
@Tag(name = "User Management", description = "CRUD operations for user management")
public class UserController {

    private final UserManagementService userManagementService;

    @GetMapping
    @Operation(summary = "List users (Admins see all, Managers see agents+viewers)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> listUsers(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String search
//            Pageable pageable
    ) {
        Page<UserResponse> result;
        if (search != null && !search.isBlank()) {
            result = userManagementService.searchUsers(search, pageable);
        } else {
            boolean isAdmin = currentUser.getRoles().stream()
                    .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
            result = isAdmin
                    ? userManagementService.listAllUsers(pageable)
                    : userManagementService.listManageableUsers(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", userManagementService.getUserById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new user with roles")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success("User created successfully", userManagementService.createUser(request, currentUser)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user details and/or roles")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", userManagementService.updateUser(id, request, currentUser)));
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Assign roles to a user")
    public ResponseEntity<ApiResponse<UserResponse>> assignRoles(
            @PathVariable Integer id,
            @Valid @RequestBody RoleAssignmentRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success("Roles assigned successfully", userManagementService.assignRoles(id, request.getRoles(), currentUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a user (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Integer id,
            @AuthenticationPrincipal User currentUser
    ) {
        userManagementService.deleteUser(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }
}
