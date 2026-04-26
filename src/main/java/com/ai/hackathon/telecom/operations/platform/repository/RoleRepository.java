package com.ai.hackathon.telecom.operations.platform.repository;

import com.ai.hackathon.telecom.operations.platform.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String roleStudent);
}
