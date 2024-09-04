package com.example.auth.entity;

public enum Code {
    SUCCESS("Operation succeeded"),
    PERMIT("Access granted"),
    LOGIN_FAILED("Failed to log in"),
    BAD_LOGIN_1("User with this login doesn't exist"),
    BAD_TOKEN("This token is empty or expired"),
    BAD_LOGIN_2("User with this login already exists"),
    BAD_EMAIL("User with this email already exists");

    public final String label;

    private Code(String label) {
        this.label = label;
    }
}
