package com.ai.hackathon.telecom.operations.platform.config;

import com.ai.hackathon.telecom.operations.platform.role.Role;
import com.ai.hackathon.telecom.operations.platform.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        createRole("ROLE_USER");
        createRole("ROLE_ADMIN");
        createRole("ROLE_SUPER_ADMIN");
    }

    private void createRole(String name) {
        roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(name).build()
                ));
    }
}