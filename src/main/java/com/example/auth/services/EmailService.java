package com.example.auth.services;

import java.nio.charset.StandardCharsets;
import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import com.example.auth.configuration.EmailConfiguration;
import com.example.auth.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final EmailConfiguration emailConfiguration;
    @Value("classpath:static/email_activate.html")
    Resource activateTemplate;
    @Value("${frontend.url}")
    private String frontendUrl;

    public void sendActivationEmail(User user) {
        try {
            File file = activateTemplate.getFile();
            String html = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            html = html.replace("https://google.com", frontendUrl + "/activate/" + user.getUuid());
            emailConfiguration.sendEmail(user.getEmail(), "Activate Your Account", html, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
