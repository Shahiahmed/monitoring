package com.example.monitoring.dto;

import lombok.Data;

@Data
public class ApplicationInfoNewRequest {
    private Long applicationId;
    private Long envId;
    private Long serverId;
    private String info;
    private String innerUrl;
    private String url;
    private String precedent;
}
