package com.example.monitoring.service;

import com.example.monitoring.dto.ApplicationInfoNewRequest;
import com.example.monitoring.dto.ApplicationInfoNewResponse;
import com.example.monitoring.dto.ApplicationNewRequest;
import com.example.monitoring.dto.ApplicationNewResponse;
import com.example.monitoring.entity.*;
import com.example.monitoring.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationNewService {

    private final ApplicationNewRepository appRepo;
    private final ApplicationInfoNewRepository infoRepo;
    private final DicStatusRepository statusRepo;
    private final DicServerRepository serverRepo;
    private final DicEnvRepository envRepo;
    private final DicAppTypeRepository appTypeRepo;
    private final DicInteractionTypeRepository interactionTypeRepo;
    private final SshCommandService sshService;

    @Transactional(readOnly = true)
    public List<ApplicationNewResponse> listAll() {
        return appRepo.findAllOrderByName().stream()
                .map(ApplicationNewResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationNewResponse getById(Long id) {
        return ApplicationNewResponse.from(appRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found: " + id)));
    }

    @Transactional
    public ApplicationNewResponse create(ApplicationNewRequest req) {
        ApplicationNew e = new ApplicationNew();
        fillFields(e, req);
        return ApplicationNewResponse.from(appRepo.save(e));
    }

    @Transactional
    public ApplicationNewResponse update(Long id, ApplicationNewRequest req) {
        ApplicationNew e = appRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found: " + id));
        fillFields(e, req);
        return ApplicationNewResponse.from(appRepo.save(e));
    }

    @Transactional
    public void delete(Long id) {
        appRepo.deleteById(id);
    }

    // ── ApplicationInfoNew (развёртывания) ──

    @Transactional(readOnly = true)
    public List<ApplicationInfoNewResponse> listDeployments(Long appId) {
        return infoRepo.findByApplicationId(appId).stream()
                .map(ApplicationInfoNewResponse::from)
                .toList();
    }

    @Transactional
    public ApplicationInfoNewResponse createDeployment(ApplicationInfoNewRequest req) {
        ApplicationInfoNew e = new ApplicationInfoNew();
        fillInfoFields(e, req);
        return ApplicationInfoNewResponse.from(infoRepo.save(e));
    }

    @Transactional
    public ApplicationInfoNewResponse updateDeployment(Long id, ApplicationInfoNewRequest req) {
        ApplicationInfoNew e = infoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Deployment not found: " + id));
        fillInfoFields(e, req);
        return ApplicationInfoNewResponse.from(infoRepo.save(e));
    }

    @Transactional
    public void deleteDeployment(Long id) {
        infoRepo.deleteById(id);
    }

    // ── SSH actions ──

    @Transactional
    public ApplicationInfoNewResponse sshAction(Long infoId, String action) {
        ApplicationInfoNew e = infoRepo.findById(infoId)
                .orElseThrow(() -> new RuntimeException("Deployment not found: " + infoId));
        String ip = e.getServer() != null ? e.getServer().getIp() : null;
        String artifactId = e.getApplication() != null ? e.getApplication().getArtifactId() : null;
        if (ip == null || artifactId == null) throw new RuntimeException("Сервер или artifactId не заданы");

        try {
            switch (action) {
                case "start"   -> sshService.start(ip, artifactId);
                case "stop"    -> sshService.stop(ip, artifactId);
                case "restart" -> sshService.restart(ip, artifactId);
            }
        } catch (Exception ex) {
            throw new RuntimeException("SSH ошибка: " + ex.getMessage());
        }

        // Обновляем статус после действия
        long statusId = sshService.fetchStatus(ip, artifactId);
        DicStatus status = statusRepo.findById(statusId).orElse(null);
        e.setStatus(status);
        return ApplicationInfoNewResponse.from(infoRepo.save(e));
    }

    @Transactional
    public ApplicationInfoNewResponse refreshStatus(Long infoId) {
        ApplicationInfoNew e = infoRepo.findById(infoId)
                .orElseThrow(() -> new RuntimeException("Deployment not found: " + infoId));
        String ip = e.getServer() != null ? e.getServer().getIp() : null;
        String artifactId = e.getApplication() != null ? e.getApplication().getArtifactId() : null;
        if (ip == null || artifactId == null) throw new RuntimeException("Сервер или artifactId не заданы");

        long statusId = sshService.fetchStatus(ip, artifactId);
        DicStatus status = statusRepo.findById(statusId).orElse(null);
        e.setStatus(status);
        return ApplicationInfoNewResponse.from(infoRepo.save(e));
    }

    public SseEmitter streamLogs(Long infoId, String grep) {
        ApplicationInfoNew e = infoRepo.findById(infoId)
                .orElseThrow(() -> new RuntimeException("Deployment not found: " + infoId));
        String ip = e.getServer() != null ? e.getServer().getIp() : null;
        String artifactId = e.getApplication() != null ? e.getApplication().getArtifactId() : null;
        if (ip == null || artifactId == null) throw new RuntimeException("Сервер или artifactId не заданы");
        return sshService.streamLogs(ip, artifactId, grep);
    }

    // ── helpers ──

    private void fillFields(ApplicationNew e, ApplicationNewRequest req) {
        e.setArtifactId(req.getArtifactId());
        e.setName(req.getName());
        e.setDescription(req.getDescription());
        e.setDeveloper(req.getDeveloper());
        e.setFeatured(req.getFeatured() != null && req.getFeatured());
        e.setProjectName(req.getProjectName());
        e.setShepServiceId(req.getShepServiceId());
        e.setSmartBridgePage(req.getSmartBridgePage());
        e.setProcedures(req.getProcedures());
        e.setSchemaDatabase(req.getSchemaDatabase());
        e.setUrlProduction(req.getUrlProduction());
        e.setUrlTest(req.getUrlTest());
        e.setSubsystemInout(req.getSubsystemInout());
        e.setIsMtszn(req.getIsMtszn());
        if (req.getAppTypeId() != null)
            e.setAppType(appTypeRepo.findById(req.getAppTypeId()).orElse(null));
        if (req.getInteractionTypeId() != null)
            e.setInteractionType(interactionTypeRepo.findById(req.getInteractionTypeId()).orElse(null));
    }

    private void fillInfoFields(ApplicationInfoNew e, ApplicationInfoNewRequest req) {
        if (req.getApplicationId() != null)
            e.setApplication(appRepo.findById(req.getApplicationId()).orElse(null));
        if (req.getEnvId() != null)
            e.setEnv(envRepo.findById(req.getEnvId()).orElse(null));
        if (req.getServerId() != null)
            e.setServer(serverRepo.findById(req.getServerId()).orElse(null));
        e.setInfo(req.getInfo());
        e.setInnerUrl(req.getInnerUrl());
        e.setUrl(req.getUrl());
        e.setPrecedent(req.getPrecedent());
        e.setAutoCreated(false);
    }
}
