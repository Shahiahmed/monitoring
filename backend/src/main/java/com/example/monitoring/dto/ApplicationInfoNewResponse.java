package com.example.monitoring.dto;

import com.example.monitoring.entity.ApplicationInfoNew;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApplicationInfoNewResponse {
    private Long id;
    private Long applicationId;
    private String applicationName;
    private String artifactId;
    private Long envId;
    private String envNameRu;
    private Long serverId;
    private String serverIp;
    private String serverDescription;
    private Long statusId;
    private String statusNameRu;
    private String info;
    private String innerUrl;
    private String url;
    private String precedent;
    private Boolean canSsh;

    public static ApplicationInfoNewResponse from(ApplicationInfoNew e) {
        boolean canSsh = e.getServer() != null && e.getApplication() != null
                && e.getApplication().getArtifactId() != null
                && !e.getApplication().getArtifactId().isBlank();
        return ApplicationInfoNewResponse.builder()
                .id(e.getId())
                .applicationId(e.getApplication() != null ? e.getApplication().getId() : null)
                .applicationName(e.getApplication() != null ? e.getApplication().getName() : null)
                .artifactId(e.getApplication() != null ? e.getApplication().getArtifactId() : null)
                .envId(e.getEnv() != null ? e.getEnv().getId() : null)
                .envNameRu(e.getEnv() != null ? e.getEnv().getNameRu() : null)
                .serverId(e.getServer() != null ? e.getServer().getId() : null)
                .serverIp(e.getServer() != null ? e.getServer().getIp() : null)
                .serverDescription(e.getServer() != null ? e.getServer().getDescription() : null)
                .statusId(e.getStatus() != null ? e.getStatus().getId() : null)
                .statusNameRu(e.getStatus() != null ? e.getStatus().getNameRu() : null)
                .info(e.getInfo())
                .innerUrl(e.getInnerUrl())
                .url(e.getUrl())
                .precedent(e.getPrecedent())
                .canSsh(canSsh)
                .build();
    }
}
