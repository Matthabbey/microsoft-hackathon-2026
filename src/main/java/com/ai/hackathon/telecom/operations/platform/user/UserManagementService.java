package com.ai.hackathon.telecom.operations.platform.user;

import com.ai.hackathon.telecom.operations.platform.audit.AuditAction;
import com.ai.hackathon.telecom.operations.platform.audit.AuditLog;
import com.ai.hackathon.telecom.operations.platform.audit.AuditResult;
import com.ai.hackathon.telecom.operations.platform.audit.AuditService;
import com.ai.hackathon.telecom.operations.platform.auth.EmailAlreadyExistsException;
import com.ai.hackathon.telecom.operations.platform.dtos.CreateUserRequest;
import com.ai.hackathon.telecom.operations.platform.dtos.UpdateUserRequest;
import com.ai.hackathon.telecom.operations.platform.dtos.UserResponse;
import com.ai.hackathon.telecom.operations.platform.exception.OperationNotPermittedException;
import com.ai.hackathon.telecom.operations.platform.repository.RoleRepository;
import com.ai.hackathon.telecom.operations.platform.repository.UserRepository;
import com.ai.hackathon.telecom.operations.platform.role.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private static final Map<String, Integer> ROLE_RANK = Map.of(
            "ROLE_VIEWER", 1,
            "ROLE_AGENT", 2,
            "ROLE_MANAGER", 3,
            "ROLE_ADMIN", 4
    );

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public Page<UserResponse> listAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<UserResponse> listManageableUsers(Pageable pageable) {
        return userRepository.findByRolesNameIn(List.of("ROLE_AGENT", "ROLE_VIEWER"), pageable)
                .map(this::toResponse);
    }

    public Page<UserResponse> searchUsers(String search, Pageable pageable) {
        return userRepository.searchUsers(search, pageable).map(this::toResponse);
    }

    public UserResponse getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return toResponse(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request, User actor) {
        assertCanAssignRoles(actor, request.getRoles());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("A user with email " + request.getEmail() + " already exists");
        }

        List<Role> roles = request.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName)))
                .collect(Collectors.toList());

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .roles(roles)
                .enabled(true)
                .accountLocked(false)
                .build();

        user = userRepository.save(user);

        auditService.logEventAsync(AuditLog.builder()
                .action(AuditAction.USER_CREATE)
                .result(AuditResult.SUCCESS)
                .userId(actor.getId() != null ? actor.getId().longValue() : null)
                .userEmail(actor.getEmail())
                .entityType("User")
                .entityId(user.getId().toString())
                .timestamp(LocalDateTime.now())
                .description("User created: " + user.getEmail() + " with roles " + request.getRoles()));

        return toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Integer id, UpdateUserRequest request, User actor) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        assertCanManage(actor, target);

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            assertCanAssignRoles(actor, request.getRoles());
        }

        if (request.getFirstName() != null) {
            target.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            target.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            target.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAccountLocked() != null) {
            target.setAccountLocked(request.getAccountLocked());
        }
        if (request.getEnabled() != null) {
            target.setEnabled(request.getEnabled());
        }
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            List<Role> roles = request.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName)))
                    .collect(Collectors.toList());
            target.setRoles(roles);
        }

        target = userRepository.save(target);

        auditService.logEventAsync(AuditLog.builder()
                .action(AuditAction.USER_UPDATE)
                .result(AuditResult.SUCCESS)
                .userId(actor.getId() != null ? actor.getId().longValue() : null)
                .userEmail(actor.getEmail())
                .entityType("User")
                .entityId(target.getId().toString())
                .timestamp(LocalDateTime.now())
                .description("User updated: " + target.getEmail()));

        return toResponse(target);
    }

    @Transactional
    public UserResponse assignRoles(Integer id, List<String> roles, User actor) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        assertCanManage(actor, target);
        assertCanAssignRoles(actor, roles);

        List<Role> roleEntities = roles.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName)))
                .collect(Collectors.toList());

        target.setRoles(roleEntities);
        target = userRepository.save(target);

        auditService.logEventAsync(AuditLog.builder()
                .action(AuditAction.ROLE_CHANGE)
                .result(AuditResult.SUCCESS)
                .userId(actor.getId() != null ? actor.getId().longValue() : null)
                .userEmail(actor.getEmail())
                .entityType("User")
                .entityId(target.getId().toString())
                .timestamp(LocalDateTime.now())
                .description("Roles changed for " + target.getEmail() + " to " + roles));

        return toResponse(target);
    }

    @Transactional
    public void deleteUser(Integer id, User actor) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        assertCanManage(actor, target);

        userRepository.delete(target);

        auditService.logEventAsync(AuditLog.builder()
                .action(AuditAction.USER_DELETE)
                .result(AuditResult.SUCCESS)
                .userId(actor.getId() != null ? actor.getId().longValue() : null)
                .userEmail(actor.getEmail())
                .entityType("User")
                .entityId(target.getId().toString())
                .timestamp(LocalDateTime.now())
                .description("User deleted: " + target.getEmail()));
    }

    private void assertCanManage(User actor, User target) {
        if (actor.getId().equals(target.getId())) {
            throw new OperationNotPermittedException("Cannot modify your own account through this endpoint");
        }
        int actorRank = getHighestRank(actor);
        int targetRank = getHighestRank(target);
        if (actorRank <= targetRank) {
            throw new OperationNotPermittedException("You do not have sufficient privileges to manage this user");
        }
    }

    private void assertCanAssignRoles(User actor, List<String> roles) {
        int actorRank = getHighestRank(actor);
        for (String role : roles) {
            Integer roleRank = ROLE_RANK.get(role);
            if (roleRank == null) {
                throw new IllegalStateException("Unknown role: " + role);
            }
            if (roleRank >= actorRank) {
                throw new OperationNotPermittedException("Cannot assign role " + role + " — it is at or above your own rank");
            }
        }
    }

    private int getHighestRank(User user) {
        return user.getRoles().stream()
                .mapToInt(role -> ROLE_RANK.getOrDefault(role.getName(), 0))
                .max()
                .orElse(0);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .accountLocked(user.isAccountLocked())
                .enabled(user.isEnabled())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .createdDate(user.getCreatedDate())
                .lastModifiedDate(user.getLastModifiedDate())
                .build();
    }
}
