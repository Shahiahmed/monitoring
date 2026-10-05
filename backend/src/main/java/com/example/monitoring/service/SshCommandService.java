package com.example.monitoring.service;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class SshCommandService {

    private static final Logger log = LoggerFactory.getLogger(SshCommandService.class);

    private static final String SERVICE_PREFIX = "gcvp-";
    private static final String SERVICE_SUFFIX = ".service";
    private static final String LOG_PATH       = "/var/log/gcvp/";

    // Статусы совпадают с dic_status: 1=failed, 2=выключен, 3=включён, 4=неизвестно
    public static final long STATUS_FAILED   = 1L;
    public static final long STATUS_INACTIVE = 2L;
    public static final long STATUS_ACTIVE   = 3L;
    public static final long STATUS_UNKNOWN  = 4L;

    @Value("${monitoring.ssh.user:monitoringapp}")
    private String sshUser;

    @Value("${monitoring.ssh.password:Qwerty123}")
    private String sshPassword;

    @Value("${monitoring.ssh.port:22}")
    private int sshPort;

    @Value("${monitoring.ssh.timeout-seconds:15}")
    private int timeoutSeconds;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private String serviceName(String artifactId) {
        return SERVICE_PREFIX + artifactId + SERVICE_SUFFIX;
    }

    private String logFile(String artifactId) {
        return LOG_PATH + artifactId + ".log";
    }

    public String execute(String ip, String command) throws Exception {
        try (SSHClient ssh = new SSHClient()) {
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.setConnectTimeout(timeoutSeconds * 1000);
            ssh.connect(ip, sshPort);
            ssh.authPassword(sshUser, sshPassword);
            try (Session session = ssh.startSession()) {
                Session.Command cmd = session.exec(command);
                cmd.join(timeoutSeconds, TimeUnit.SECONDS);
                return new String(cmd.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            }
        }
    }

    public long fetchStatus(String ip, String artifactId) {
        try {
            String cmd = "sudo systemctl status " + serviceName(artifactId)
                    + " | grep 'Active:' | awk '{print $2}'";
            String result = execute(ip, cmd).toLowerCase();
            if (result.contains("active")) return STATUS_ACTIVE;
            if (result.contains("inactive") || result.contains("dead")) return STATUS_INACTIVE;
            if (result.contains("failed")) return STATUS_FAILED;
            return STATUS_UNKNOWN;
        } catch (Exception e) {
            log.warn("SSH status {}@{}: {}", artifactId, ip, e.getMessage());
            return STATUS_UNKNOWN;
        }
    }

    public void start(String ip, String artifactId) throws Exception {
        execute(ip, "sudo systemctl start " + serviceName(artifactId));
    }

    public void stop(String ip, String artifactId) throws Exception {
        execute(ip, "sudo systemctl stop " + serviceName(artifactId));
    }

    public void restart(String ip, String artifactId) throws Exception {
        execute(ip, "sudo systemctl restart " + serviceName(artifactId));
    }

    public SseEmitter streamLogs(String ip, String artifactId, String grep) {
        SseEmitter emitter = new SseEmitter(60_000L);

        executor.execute(() -> {
            String cmd = "tail -f " + logFile(artifactId)
                    + (grep != null && !grep.isBlank() ? " | grep --line-buffered " + grep : "");
            try (SSHClient ssh = new SSHClient()) {
                ssh.addHostKeyVerifier(new PromiscuousVerifier());
                ssh.setConnectTimeout(timeoutSeconds * 1000);
                ssh.connect(ip, sshPort);
                ssh.authPassword(sshUser, sshPassword);
                try (Session session = ssh.startSession()) {
                    session.allocateDefaultPTY();
                    Session.Command sshCmd = session.exec(cmd);
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(sshCmd.getInputStream(), StandardCharsets.UTF_8))) {
                        String line;
                        long deadline = System.currentTimeMillis() + 55_000L;
                        while ((line = reader.readLine()) != null && System.currentTimeMillis() < deadline) {
                            emitter.send(SseEmitter.event().data(line));
                        }
                    }
                    sshCmd.close();
                }
            } catch (Exception e) {
                try { emitter.send(SseEmitter.event().name("error").data(e.getMessage())); } catch (Exception ignored) {}
            } finally {
                emitter.complete();
            }
        });

        return emitter;
    }
}
