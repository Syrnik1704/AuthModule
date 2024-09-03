package com.example.auth.services;

import com.example.auth.entity.*;
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

    public void validateToken(HttpServletRequest request) throws ExpiredJwtException, IllegalArgumentException {
        String token = null;
        String refresh = null;
        for (Cookie cookie : Arrays.stream(request.getCookies()).toList()) {
            if (cookie.getName().equals("token")) {
                token = cookie.getValue();
            } else if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
            }
        }
        try {
            jwtService.validateToken(token);
        } catch (IllegalArgumentException | ExpiredJwtException e) {
            jwtService.validateToken(refresh);
        }
    }

    public void register(UserRegisterDTO userRegisterDTO) {
        User user = new User();
        user.setLogin(userRegisterDTO.getLogin());
        user.setPassword(userRegisterDTO.getPassword());
        user.setEmail(userRegisterDTO.getEmail());
        if (userRegisterDTO.getRole() != null) {
            user.setRole(userRegisterDTO.getRole());
        } else {
            user.setRole(Role.USER);
        }
        saveUser(user);
    }

    public ResponseEntity<?> login(HttpServletResponse response, User requestedUser) {
        User user = userRepository.findUserByLogin(requestedUser.getUsername()).orElse(null);
        if (user != null) {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestedUser.getUsername(), requestedUser.getPassword()));
            if (authentication.isAuthenticated()) {
                Cookie refresh = cookieService.generateCookie("refresh", generateToken(requestedUser.getUsername(), refreshExp), refreshExp);
                Cookie token = cookieService.generateCookie("token", generateToken(requestedUser.getUsername(), exp), exp);
                response.addCookie(token);
                response.addCookie(refresh);
                return ResponseEntity.ok(
                        UserRegisterDTO.builder()
                                .login(user.getUsername())
                                .email(user.getEmail())
                                .role(user.getRole())
                                .build());
            } else {
                return ResponseEntity.ok(new AuthResponse(Code.A1));
            }
        }
        return ResponseEntity.ok(new AuthResponse(Code.A2));
    }

}
