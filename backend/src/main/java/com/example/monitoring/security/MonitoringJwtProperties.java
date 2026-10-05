package com.example.monitoring.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "monitoring.jwt")
public record MonitoringJwtProperties(
        String secret,
        long expirationMs
) {}
