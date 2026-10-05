package com.example.monitoring.dto;

public record ProcessInfo(
        String pid,
        String name,
        double cpuPercent,
        double memPercent,
        long rssKb
) {}
