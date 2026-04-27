package com.ai.hackathon.telecom.operations.platform.auth;

import com.ai.hackathon.telecom.operations.platform.audit.AuditAction;
import com.ai.hackathon.telecom.operations.platform.audit.AuditResult;
import com.ai.hackathon.telecom.operations.platform.audit.AuditService;
import com.ai.hackathon.telecom.operations.platform.dtos.AuthenticationRequest;
import com.ai.hackathon.telecom.operations.platform.dtos.AuthenticationResponse;
import com.ai.hackathon.telecom.operations.platform.dtos.RegistrationRequest;
import com.ai.hackathon.telecom.operations.platform.email.EmailService;
import com.ai.hackathon.telecom.operations.platform.email.EmailTemplateName;
import com.ai.hackathon.telecom.operations.platform.repository.TokenRepository;
import com.ai.hackathon.telecom.operations.platform.repository.UserRepository;
import com.ai.hackathon.telecom.operations.platform.repository.RoleRepository;
import com.ai.hackathon.telecom.operations.platform.security.JwtService;
import com.ai.hackathon.telecom.operations.platform.user.Token;
import com.ai.hackathon.telecom.operations.platform.user.User;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;
    private final AuditService auditService;
    private final HttpServletRequest httpServletRequest;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    public void register(RegistrationRequest request) throws MessagingException {

        var userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not initialized in DB"));

        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .build();

        userRepository.save(user);

        auditService.logAuthEvent(
                AuditAction.USER_REGISTRATION,
                AuditResult.SUCCESS,
                user,
                httpServletRequest,
                "User registered: " + user.getEmail()
        );

        sendValidationEmail(user);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));

            String email = auth.getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            var claims = new HashMap<String, Object>();
            claims.put("fullName", user.getFullName());
            claims.put("roles", user.getAuthorities().stream()
                    .map(role -> role.getAuthority())
                    .toList());
            var jwtToken = jwtService.generateToken(claims, user);

            auditService.logAuthEvent(
                    AuditAction.USER_LOGIN,
                    AuditResult.SUCCESS,
                    user,
                    httpServletRequest,
                    "User logged in: " + user.getEmail()
            );

            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .build();
        } catch (Exception e) {
            auditService.logAuthEventByEmail(
                    AuditAction.USER_LOGIN_FAILED,
                    AuditResult.FAILURE,
                    request.getEmail(),
                    httpServletRequest,
                    "Login failed for: " + request.getEmail()
            );
            throw e;
        }
    }

    @Transactional
    public void activateAccount(String token) throws MessagingException {

        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    auditService.logAuthEventByEmail(
                            AuditAction.ACCOUNT_ACTIVATION_FAILED,
                            AuditResult.FAILURE,
                            null,
                            httpServletRequest,
                            "Invalid activation token"
                    );
                    return new RuntimeException("Invalid activation token");
                });

        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            auditService.logAuthEvent(
                    AuditAction.ACCOUNT_ACTIVATION_FAILED,
                    AuditResult.FAILURE,
                    savedToken.getUser(),
                    httpServletRequest,
                    "Activation token expired for: " + savedToken.getUser().getEmail()
            );

            sendValidationEmail(savedToken.getUser());

            throw new RuntimeException(
                    "Activation token expired. A new token has been sent to your email"
            );
        }

        var user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setEnabled(true);
        userRepository.save(user);

        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);

        auditService.logAuthEvent(
                AuditAction.ACCOUNT_ACTIVATION,
                AuditResult.SUCCESS,
                user,
                httpServletRequest,
                "Account activated: " + user.getEmail()
        );
    }
    private String generateAndSaveActivationToken(User user) {

        String generatedToken = generateActivationCode(6);

        var token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();

        tokenRepository.save(token);

        return generatedToken;
    }
    private void sendValidationEmail(User user) throws MessagingException {

        var newToken = generateAndSaveActivationToken(user);

        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newToken,
                "Account activation"
        );
    }
    private String generateActivationCode(int length) {

        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();

        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }

        return codeBuilder.toString();
    }
}