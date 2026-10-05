package com.example.monitoring.controller;

import com.example.monitoring.service.OllamaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);
    private final OllamaService ollamaService;

    public record MessageDto(String role, String content) {}
    public record ChatRequest(List<MessageDto> messages) {}

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<StreamingResponseBody> chatStream(@RequestBody ChatRequest request) {
        List<Map<String, String>> messages = request.messages().stream()
                .map(m -> Map.of("role", m.role(), "content", m.content()))
                .toList();

        StreamingResponseBody body = out -> {
            try {
                ollamaService.streamChat(messages, out);
            } catch (Exception e) {
                log.error("Ошибка стриминга ИИ: {}", e.getMessage());
                out.write("data: \"ИИ временно недоступен. Убедитесь что Ollama запущен (ollama serve).\"\n\ndata: [DONE]\n\n".getBytes());
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .header("Cache-Control", "no-cache")
                .header("X-Accel-Buffering", "no")
                .body(body);
    }
}
