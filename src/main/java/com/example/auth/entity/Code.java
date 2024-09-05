package com.example.auth.entity;

public enum Code {
    SUCCESS("Operation succeeded"),
    PERMIT("Access granted"),
    LOGIN_FAILED("User with this login doesn't exist or Your account is not active"),
    INCORRECT_DATA("Provided data is incorrect"),
    BAD_TOKEN("This token is empty or expired"),
    BAD_LOGIN("User with this login already exists"),
    BAD_EMAIL("User with this email already exists"),
    USER_DONT_EXIST("User don't exist");

    public final String label;

    private Code(String label) {
        this.label = label;
    }
}
