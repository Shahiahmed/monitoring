package com.example.monitoring.dto;

import com.example.monitoring.entity.ApplicationNew;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ApplicationNewResponse {
    private Long id;
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
    private String appTypeNameRu;
    private Long interactionTypeId;
    private String interactionTypeNameRu;
    private List<ApplicationInfoNewResponse> deployments;

    public static ApplicationNewResponse from(ApplicationNew e) {
        List<ApplicationInfoNewResponse> deps = e.getDeployments() == null ? List.of()
                : e.getDeployments().stream().map(ApplicationInfoNewResponse::from).toList();
        return ApplicationNewResponse.builder()
                .id(e.getId())
                .artifactId(e.getArtifactId())
                .name(e.getName())
                .description(e.getDescription())
                .developer(e.getDeveloper())
                .featured(e.getFeatured())
                .projectName(e.getProjectName())
                .shepServiceId(e.getShepServiceId())
                .smartBridgePage(e.getSmartBridgePage())
                .procedures(e.getProcedures())
                .schemaDatabase(e.getSchemaDatabase())
                .urlProduction(e.getUrlProduction())
                .urlTest(e.getUrlTest())
                .subsystemInout(e.getSubsystemInout())
                .isMtszn(e.getIsMtszn())
                .appTypeId(e.getAppType() != null ? e.getAppType().getId() : null)
                .appTypeNameRu(e.getAppType() != null ? e.getAppType().getNameRu() : null)
                .interactionTypeId(e.getInteractionType() != null ? e.getInteractionType().getId() : null)
                .interactionTypeNameRu(e.getInteractionType() != null ? e.getInteractionType().getNameRu() : null)
                .deployments(deps)
                .build();
    }
}
