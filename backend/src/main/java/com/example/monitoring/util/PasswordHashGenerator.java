package com.example.monitoring.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {

    public static void main(String[] args) {
        String rawPassword = "Blacklotus01";

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(rawPassword);

        System.out.println("Raw password : " + rawPassword);
        System.out.println("BCrypt hash  : " + hash);
    }
}

