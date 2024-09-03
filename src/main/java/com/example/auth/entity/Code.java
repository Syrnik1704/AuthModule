package com.example.auth.entity;

public enum Code {
    SUCCESS("Operation succeeded"),
    PERMIT("Access granted"),
    A1("Failed to log in"),
    A2("User with this username doesn't exist"),
    A3("This token is empty or expired");

    public final String label;

    private Code(String label) {
        this.label = label;
    }
}
