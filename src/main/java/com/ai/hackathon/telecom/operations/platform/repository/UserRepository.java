package com.ai.hackathon.telecom.operations.platform.repository;


import com.ai.hackathon.telecom.operations.platform.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String username);

}
