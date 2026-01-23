package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public String login(String username, String password) {

        User user = userRepository
                .findByUsernameAndPassword(username, password)
                .orElseThrow(() -> new RuntimeException("Sai tài khoản hoặc mật khẩu"));

        // TODO: sinh JWT sau
        return "fake-jwt-token-for-" + user.getUsername();
    }
}
