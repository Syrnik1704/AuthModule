package com.example.auth.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Builder
public class UserRegisterDTO {
    @Length(min = 5, max = 50, message = "Login should contains from 5 to max 50 characters")
    private String login;
    @Email(message = "Provided email is not in appropriate format")
    private String email;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Length(min = 8, max = 75, message = "Password should contains from 8 to max 75 characters")
    private String password;
    private Role role;
}
