package com.example.monitoring.dto;

import lombok.Data;

@Data
public class ApplicationNewRequest {
    private String artifactId;
    private String name;
    private String description;
    private String developer;
    private Boolean featured;
    private String projectName;
    private String shepServiceId;
    private String smartBridgePage;
    private String procedures;
    private String schemaDatabase;
    private String urlProduction;
    private String urlTest;
    private String subsystemInout;
    private String isMtszn;
    private Long appTypeId;
    private Long interactionTypeId;
}
