package com.example.monitoring.controller;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Раздача файлов Smart Bridge (XML/XSD/WSDL), хранящихся на диске.
 * Структура: {sb-files.path}/{serviceKey}/{filename}
 * Скопировать папку sb_files/ на сервер в /opt/monitoring/sb_files/
 */
@RestController
@RequestMapping("/api/my-service-sb-files")
public class MyServiceSbFileController {

    private static final Logger log = LoggerFactory.getLogger(MyServiceSbFileController.class);

    @Value("${monitoring.sb-files.path:/opt/monitoring/sb_files}")
    private String sbFilesPath;

    @PostConstruct
    void init() {
        boolean exists = Paths.get(sbFilesPath).toFile().isDirectory();
        log.info("SB files path: {} [{}]", sbFilesPath, exists ? "OK" : "NOT FOUND");
    }

    // fileType → точное имя файла (для xml/xsd/wsdl всегда фиксированное)
    private static final Map<String, String> FIXED_NAMES = Map.of(
            "xml_request",  "request.xml",
            "xml_response", "response.xml",
            "xsd",          "schema.xsd",
            "wsdl",         "service.wsdl"
    );

    // data_format хранится с оригинальным расширением (.xlsx, .zip, ...) — ищем по префиксу
    private static final String DATA_FORMAT_PREFIX = "data_format";

    /** Найти файл data_format.* с любым расширением */
    private Optional<Path> findDataFormat(Path dir) {
        if (!Files.isDirectory(dir)) return Optional.empty();
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(p -> p.getFileName().toString().startsWith(DATA_FORMAT_PREFIX + "."))
                    .findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /** Список доступных файлов для сервиса */
    @GetMapping("/service/{serviceKey}")
    public List<Map<String, String>> list(@PathVariable String serviceKey) {
        List<Map<String, String>> result = new ArrayList<>();
        Path dir = Paths.get(sbFilesPath, serviceKey);
        if (!Files.isDirectory(dir)) return result;

        for (Map.Entry<String, String> entry : FIXED_NAMES.entrySet()) {
            Path f = dir.resolve(entry.getValue());
            if (Files.exists(f)) {
                result.add(Map.of("fileType", entry.getKey(), "filename", entry.getValue()));
            }
        }

        findDataFormat(dir).ifPresent(f ->
                result.add(Map.of("fileType", "data_format", "filename", f.getFileName().toString()))
        );

        return result;
    }

    /** Скачать файл */
    @GetMapping("/download/{serviceKey}/{fileType}")
    public ResponseEntity<byte[]> download(
            @PathVariable String serviceKey,
            @PathVariable String fileType
    ) throws IOException {
        Path dir = Paths.get(sbFilesPath, serviceKey);

        Path file;
        if ("data_format".equals(fileType)) {
            Optional<Path> found = findDataFormat(dir);
            if (found.isEmpty()) return ResponseEntity.notFound().build();
            file = found.get();
        } else {
            String filename = FIXED_NAMES.get(fileType);
            if (filename == null) return ResponseEntity.badRequest().build();
            file = dir.resolve(filename);
            if (!Files.exists(file)) return ResponseEntity.notFound().build();
        }

        String filename = file.getFileName().toString();
        String mime = guessMime(filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(mime))
                .body(Files.readAllBytes(file));
    }

    /** Форматы ШЭП — общий файл для всех сервисов */
    @GetMapping("/shep-formats")
    public ResponseEntity<byte[]> shepFormats() throws IOException {
        // Ищем файл по возможным именам
        String[] candidates = {"shep_formats.zip", "Все по ШЭП.zip", "shep-formats.zip"};
        Path file = null;
        for (String name : candidates) {
            Path p = Paths.get(sbFilesPath, name);
            if (Files.exists(p)) { file = p; break; }
        }
        if (file == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"shep_formats.zip\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(Files.readAllBytes(file));
    }

    private String guessMime(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".xml") || lower.endsWith(".xsd") || lower.endsWith(".wsdl"))
            return "application/xml";
        if (lower.endsWith(".xlsx"))
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".xls"))
            return "application/vnd.ms-excel";
        if (lower.endsWith(".zip"))
            return "application/zip";
        return "application/octet-stream";
    }
}
