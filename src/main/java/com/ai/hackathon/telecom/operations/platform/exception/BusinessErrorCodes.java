package com.ai.hackathon.telecom.operations.platform.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

public enum BusinessErrorCodes {
    NO_CODE(0, NOT_IMPLEMENTED, "No code"),
    INCORRECT_CURRENT_PASSWORD(300, BAD_REQUEST, "Current password is incorrect"),
    NEW_PASSWORD_DOES_NOT_MATCH(301, BAD_REQUEST, "The new password does not match"),
    ACCOUNT_LOCKED(302, FORBIDDEN, "User account is locked"),
    ACCOUNT_DISABLED(303, FORBIDDEN, "User account is disabled"),
    BAD_CREDENTIALS(304, FORBIDDEN, "username or Password is incorrect"),
    USER_NOT_FOUND(305, NOT_FOUND, "User not found"),
    INVALID_ACTIVATION_TOKEN(306, BAD_REQUEST, "Invalid activation token"),
    ACTIVATION_TOKEN_EXPIRED(307, BAD_REQUEST, "Activation token has expired"),
    ROLE_NOT_INITIALIZED(308, INTERNAL_SERVER_ERROR, "Required role not initialized"),
    ACCESS_DENIED(309, FORBIDDEN, "You do not have permission to access this resource"),
    ;

    @Getter
    private final int code;
    @Getter
    private final String description;
    @Getter
    private final HttpStatus httpStatus;

    BusinessErrorCodes(int code, HttpStatus status, String description) {
        this.code = code;
        this.description = description;
        this.httpStatus = status;
    }
}
