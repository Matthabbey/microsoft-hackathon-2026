package com.ai.hackathon.telecom.operations.platform.call;

public class CallNotFoundException extends RuntimeException {

    public CallNotFoundException(String message) {
        super(message);
    }
}
