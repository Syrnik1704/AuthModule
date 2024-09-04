package com.example.auth.services;

import com.example.auth.entity.*;
import com.example.auth.exceptions.UserExistingWithEmail;
import com.example.auth.exceptions.UserExistingWithName;
import com.example.auth.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CookieService cookieService;
    @Value("${jwt.exp}")
    private int exp;
    @Value("${jwt.refresh.exp}")
    private int refreshExp;

    private User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.saveAndFlush(user);
    }

    private String generateToken(String username, int exp) {
        return jwtService.generateToken(username, exp);
    }

    public void validateToken(HttpServletRequest request, HttpServletResponse response) throws ExpiredJwtException, IllegalArgumentException {
        String token = null;
        String refresh = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : Arrays.stream(request.getCookies()).toList()) {
                if (cookie.getName().equals("Authorization")) {
                    token = cookie.getValue();
                } else if (cookie.getName().equals("refresh")) {
                    refresh = cookie.getValue();
                }
            }
        } else {
            throw new IllegalArgumentException("Token can't be null");
        }
        try {
            jwtService.validateToken(token);
        } catch (IllegalArgumentException | ExpiredJwtException e) {
            jwtService.validateToken(refresh);
            Cookie refreshCookie = cookieService.generateCookie("refresh", jwtService.refreshToken(refresh, refreshExp), refreshExp);
            Cookie authorizationCookie = cookieService.generateCookie("Authorization", jwtService.refreshToken(refresh, exp), exp);
            response.addCookie(authorizationCookie);
            response.addCookie(refreshCookie);
        }
    }

    public void register(UserRegisterDTO userRegisterDTO) throws UserExistingWithEmail, UserExistingWithName {
        userRepository.findUserByLogin(userRegisterDTO.getLogin()).ifPresent(value -> {
            throw new UserExistingWithName("User with this login already exists");
        });
        userRepository.findUserByEmail(userRegisterDTO.getEmail()).ifPresent(value -> {
            throw new UserExistingWithEmail("User with this email already exists");
        });
        User user = new User();
        user.setLogin(userRegisterDTO.getLogin());
        user.setPassword(userRegisterDTO.getPassword());
        user.setEmail(userRegisterDTO.getEmail());
        user.setRole(Role.USER);
        saveUser(user);
    }

    public ResponseEntity<?> login(HttpServletResponse response, User requestedUser) {
        User user = userRepository.findUserByLogin(requestedUser.getUsername()).orElse(null);
        if (user != null) {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestedUser.getUsername(), requestedUser.getPassword()));
            if (authentication.isAuthenticated()) {
                Cookie refresh = cookieService.generateCookie("refresh", generateToken(requestedUser.getUsername(), refreshExp), refreshExp);
                Cookie token = cookieService.generateCookie("Authorization", generateToken(requestedUser.getUsername(), exp), exp);
                response.addCookie(token);
                response.addCookie(refresh);
                return ResponseEntity.ok(
                        UserRegisterDTO.builder()
                                .login(user.getUsername())
                                .email(user.getEmail())
                                .role(user.getRole())
                                .build());
            } else {
                return ResponseEntity.ok(new AuthResponse(Code.LOGIN_FAILED));
            }
        }
        return ResponseEntity.ok(new AuthResponse(Code.INCORRECT_DATA));
    }

    public void setAsAdmin(UserRegisterDTO userRegisterDTO) {
        userRepository.findUserByLogin(userRegisterDTO.getLogin()).ifPresent(value -> {
            value.setRole(Role.ADMIN);
            userRepository.save(value);
        });
    }

}
