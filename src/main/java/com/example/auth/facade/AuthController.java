package com.example.auth.facade;

import com.example.auth.entity.AuthResponse;
import com.example.auth.entity.Code;
import com.example.auth.entity.UserRegisterDTO;
import com.example.auth.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @RequestMapping(path = "register", method = RequestMethod.POST)
    public ResponseEntity<AuthResponse> addNewUser(@RequestBody UserRegisterDTO user) {
        userService.register(user);
        return ResponseEntity.ok(new AuthResponse(Code.SUCCESS));
    }
}
