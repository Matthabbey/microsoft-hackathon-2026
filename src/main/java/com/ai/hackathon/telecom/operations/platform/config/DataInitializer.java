package com.ai.hackathon.telecom.operations.platform.config;

import com.ai.hackathon.telecom.operations.platform.role.Role;
import com.ai.hackathon.telecom.operations.platform.repository.RoleRepository;
import com.ai.hackathon.telecom.operations.platform.repository.UserRepository;
import com.ai.hackathon.telecom.operations.platform.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Role viewerRole = createRole("ROLE_VIEWER");
        Role agentRole = createRole("ROLE_AGENT");
        Role managerRole = createRole("ROLE_MANAGER");
        Role adminRole = createRole("ROLE_ADMIN");

        // Seed admin user for dev/testing if not present
        if (userRepository.findByEmail("admin@telecom.com").isEmpty()) {
            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("User")
                    .email("admin@telecom.com")
                    .password(passwordEncoder.encode("admin123"))
                    .enabled(true)
                    .accountLocked(false)
                    .roles(List.of(adminRole))
                    .build();
            userRepository.save(admin);
        }
    }

    private Role createRole(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(name).build()
                ));
    }
}
