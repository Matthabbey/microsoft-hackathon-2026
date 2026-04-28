package com.ai.hackathon.telecom.operations.platform.auth;

import com.ai.hackathon.telecom.operations.platform.dtos.ApiResponse;
import com.ai.hackathon.telecom.operations.platform.dtos.AuthenticationRequest;
import com.ai.hackathon.telecom.operations.platform.dtos.AuthenticationResponse;
import com.ai.hackathon.telecom.operations.platform.dtos.RegistrationRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @RequestBody @Valid RegistrationRequest request) throws MessagingException {
        service.register(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully. Check email for activation link."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(@RequestBody @Valid AuthenticationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", service.authenticate(request)));
    }

    @GetMapping("/activate-account")
    public ResponseEntity<ApiResponse<Void>> activateAccount(@RequestParam String token) throws MessagingException {
        service.activateAccount(token);
        return ResponseEntity.ok(ApiResponse.success("Account activated successfully."));
    }
}
