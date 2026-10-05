package com.example.monitoring.service;

import com.example.monitoring.dto.IncidentStatsResponse;
import com.example.monitoring.dto.ServerMetricsResponse;
import com.example.monitoring.entity.DicServer;
import com.example.monitoring.repository.DicServerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OllamaService {

    private static final Logger log = LoggerFactory.getLogger(OllamaService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${monitoring.ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${monitoring.ollama.model:qwen3:8b}")
    private String model;

    private final IncidentService   incidentService;
    private final SshMetricsService sshMetricsService;
    private final DicServerRepository serverRepository;
    private final KnowledgeBaseService knowledgeBaseService;

    private static final String SYSTEM_PROMPT = """
            /no_think
            Ты — ИИ-ассистент системы SARAP (мониторинг инцидентов и серверов МТЗСН РК).
            Твоё имя: ИИ-ассистент SARAP. Говори о себе в первом лице (я, меня, мне).
            Если в сообщении ниже есть раздел "АКТУАЛЬНЫЕ ДАННЫЕ ИЗ СИСТЕМЫ" — используй ТОЛЬКО их, не придумывай данные.
            Отвечай кратко, по делу, только на русском языке. Не используй заголовки (#).
            Если данные уже отфильтрованы в разделе — просто перечисли их, не добавляй лишних серверов.
            Раздел "СПРАВКА О СИСТЕМЕ" — единственный источник знаний об устройстве сайта.
            Ничего не додумывай сверх него: не выдумывай названия разделов, кнопок, полей и ролей.
            Если ответа нет ни в справке, ни в данных — так и скажи и подскажи, в каком разделе
            интерфейса пользователь найдёт это сам.
            """;

    private static final Pattern NUMBER_WITH_UNIT =
            Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(гб|гиб|gb|%|процент|мб|mb)?");

    // ── Public API ─────────────────────────────────────────────────────────────

    public void streamChat(List<Map<String, String>> userMessages, OutputStream out) throws IOException {
        String lastMsg = userMessages.isEmpty() ? "" :
                userMessages.get(userMessages.size() - 1).getOrDefault("content", "").toLowerCase();

        // Обогащаем системный промпт актуальными данными
        log.info("[AI] Вопрос: {}", userMessages.isEmpty() ? "" : userMessages.get(userMessages.size()-1).get("content"));
        String context   = buildDataContext(lastMsg);
        log.info("[AI] Контекст добавлен: {} символов", context.length());
        String sysPrompt = SYSTEM_PROMPT + context;

        // Строим список сообщений
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", sysPrompt));
        for (var m : userMessages) {
            messages.add(Map.of("role", m.get("role"), "content", m.get("content")));
        }

        // Реальный стриминг через SSE
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("stream", true);
        body.put("think", false);
        body.put("keep_alive", "60m");
        body.put("num_predict", 600);
        body.put("options", Map.of(
            "temperature", 0.3,
            "top_p", 0.8,
            "repeat_penalty", 1.1
        ));

        String jsonBody = MAPPER.writeValueAsString(body);

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ollamaUrl + "/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        PrintWriter writer  = new PrintWriter(out, false, StandardCharsets.UTF_8);
        boolean[] inThink   = {false};
        boolean[] sentAny   = {false};
        StringBuilder thinkBuf = new StringBuilder();

        try {
            HttpResponse<java.util.stream.Stream<String>> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofLines());

            response.body().forEach(line -> {
                if (line.isBlank()) return;
                try {
                    Map<?, ?> chunk = MAPPER.readValue(line, Map.class);
                    Map<?, ?> msg   = (Map<?, ?>) chunk.get("message");
                    if (msg == null) return;

                    String raw = (String) msg.get("content");
                    if (raw == null || raw.isEmpty()) return;

                    String clean = processChunk(raw, inThink, thinkBuf);
                    if (!clean.isEmpty()) {
                        sentAny[0] = true;
                        writer.write("data: " + MAPPER.writeValueAsString(clean) + "\n\n");
                        writer.flush();
                    }
                } catch (Exception e) {
                    log.warn("Ошибка парсинга чанка: {}", e.getMessage());
                }
            });

            // Если модель поместила весь ответ в <think> — используем его как fallback
            if (!sentAny[0] && thinkBuf.length() > 0) {
                String fallback = thinkBuf.toString().trim();
                if (!fallback.isEmpty()) {
                    writer.write("data: " + MAPPER.writeValueAsString(fallback) + "\n\n");
                    writer.flush();
                }
            }

            writer.write("data: [DONE]\n\n");
            writer.flush();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Прерван", e);
        }
    }

    // ── Context building ───────────────────────────────────────────────────────

    private String buildDataContext(String msg) {
        StringBuilder ctx = new StringBuilder();
        StringBuilder data = new StringBuilder();

        // Справка об устройстве системы. Живых данных о самом сайте в базе нет,
        // и без этого блока модель начинает выдумывать разделы, кнопки и роли.
        String knowledge = knowledgeBaseService.lookup(msg);
        if (!knowledge.isEmpty()) {
            ctx.append("\n\n=== СПРАВКА О СИСТЕМЕ ===\n").append(knowledge).append('\n');
        }

        if (needsIncidentData(msg)) {
            try {
                DateRange range = extractDateRange(msg);
                IncidentStatsResponse stats = incidentService.stats(range.from, range.to);
                data.append(formatIncidentStats(stats, range.label));
            } catch (Exception e) {
                log.warn("Не удалось получить статистику инцидентов: {}", e.getMessage());
            }
        }

        if (needsServerData(msg)) {
            try {
                List<DicServer> servers = serverRepository.findAll();
                log.info("[AI] Запрос метрик для {} серверов (кеш + SSH параллельно)", servers.size());
                long t0 = System.currentTimeMillis();
                List<ServerMetricsResponse> metrics = sshMetricsService.fetchMetrics(servers, false);
                log.info("[AI] Метрики получены за {} мс", System.currentTimeMillis() - t0);
                data.append(formatServerMetrics(servers, metrics, msg));
            } catch (Exception e) {
                log.warn("[AI] Не удалось получить метрики серверов: {}", e.getMessage());
            }
        }

        if (data.length() > 0) {
            ctx.append("\n\n=== АКТУАЛЬНЫЕ ДАННЫЕ ИЗ СИСТЕМЫ ===\n").append(data);
        }
        return ctx.toString();
    }

    // ── Keyword detection ──────────────────────────────────────────────────────

    private boolean needsIncidentData(String msg) {
        return containsAny(msg,
                "инцидент", "сбой", "отказ", "авари", "простой",
                "доступност", "квартал", "статистик", "аис", "ис ");
    }

    private boolean needsServerData(String msg) {
        return containsAny(msg,
                "сервер", "озу", "памят", "ram", "cpu", "процессор",
                "диск", "нагрузк", "свободн", "метрик");
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    // ── Date range parsing ─────────────────────────────────────────────────────

    private record DateRange(OffsetDateTime from, OffsetDateTime to, String label) {}

    private DateRange extractDateRange(String msg) {
        int year = java.time.Year.now().getValue();

        // Ищем год типа "2025"
        Matcher ym = Pattern.compile("\\b(202[0-9])\\b").matcher(msg);
        if (ym.find()) year = Integer.parseInt(ym.group(1));

        // Ищем квартал: "2 квартал", "квартал 2", "Q2", "2 кв"
        Matcher qm = Pattern.compile(
                "([1-4])\\s*квартал|квартал\\s*([1-4])|q([1-4])|([1-4])\\s*кв"
        ).matcher(msg);
        if (qm.find()) {
            String qs = Arrays.asList(qm.group(1), qm.group(2), qm.group(3), qm.group(4))
                    .stream().filter(Objects::nonNull).findFirst().orElse("1");
            int q = Integer.parseInt(qs);
            int startMonth = (q - 1) * 3 + 1;
            OffsetDateTime from = OffsetDateTime.of(year, startMonth, 1, 0, 0, 0, 0, ZoneOffset.UTC);
            OffsetDateTime to   = from.plusMonths(3).minusSeconds(1);
            return new DateRange(from, to, q + " квартал " + year + " года");
        }

        // По умолчанию — весь указанный (или текущий) год
        OffsetDateTime from = OffsetDateTime.of(year, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime to   = OffsetDateTime.of(year, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC);
        return new DateRange(from, to, year + " год");
    }

    // ── Formatters ─────────────────────────────────────────────────────────────

    private String formatIncidentStats(IncidentStatsResponse stats, String label) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nИНЦИДЕНТЫ (").append(label).append("):\n");

        int total = stats.getByType() == null ? 0 :
                stats.getByType().stream().mapToInt(IncidentStatsResponse.TypeCount::getCount).sum();
        sb.append("Всего: ").append(total).append(" инцидентов\n");

        if (stats.getByType() != null && !stats.getByType().isEmpty()) {
            sb.append("По типам:\n");
            for (var t : stats.getByType()) {
                sb.append("  ").append(t.getName()).append(": ").append(t.getCount())
                  .append(" шт, простой ").append(t.getTotalMinutes() / 60).append(" ч ")
                  .append(t.getTotalMinutes() % 60).append(" мин\n");
            }
        }

        if (stats.getByIsAvailability() != null && !stats.getByIsAvailability().isEmpty()) {
            sb.append("По информационным системам:\n");
            for (var is : stats.getByIsAvailability()) {
                sb.append("  ").append(is.getNameRu()).append(": ")
                  .append(is.getCount()).append(" инцидентов, ")
                  .append("простой ").append(is.getTotalDowntimeMinutes() / 60).append(" ч, ")
                  .append("доступность ").append(String.format("%.3f", is.getAvailabilityPercent())).append("%\n");
            }
        }

        return sb.toString();
    }

    private String formatServerMetrics(List<DicServer> servers, List<ServerMetricsResponse> metrics, String query) {
        Map<Long, DicServer> srvMap = servers.stream()
                .collect(Collectors.toMap(DicServer::getId, s -> s));

        // Детектируем фильтр из запроса: "меньше/менее/ниже X гб" или "больше/более/выше X гб/%"
        ServerFilter filter = extractServerFilter(query);

        // Применяем фильтр в Java (не доверяем AI делать математику)
        List<ServerMetricsResponse> filtered = metrics.stream()
                .filter(m -> filter == null || filter.matches(m))
                .toList();

        StringBuilder sb = new StringBuilder();
        if (filter != null) {
            sb.append("\nСЕРВЕРЫ (фильтр: ").append(filter.description()).append("):\n");
            sb.append("Найдено: ").append(filtered.size()).append(" серверов\n");
        } else {
            sb.append("\nМЕТРИКИ СЕРВЕРОВ (текущие):\n");
        }

        for (var m : filtered) {
            DicServer srv = srvMap.get(m.serverId());
            String name = srv != null ? srv.getDescription() + " (" + srv.getIp() + ")" : "id=" + m.serverId();

            if (m.error() != null) {
                if (filter == null) // при фильтре не показываем недоступные
                    sb.append("  ").append(name).append(": недоступен\n");
            } else {
                long freeMb  = m.memoryAvailableMb() != null ? m.memoryAvailableMb() : 0;
                long totalMb = m.memoryTotalMb()     != null ? m.memoryTotalMb()     : 0;
                sb.append("  ").append(name).append(": ")
                  .append("CPU ").append(m.cpuPercent() != null ? String.format("%.0f", m.cpuPercent()) : "?").append("%, ")
                  .append("RAM свободно ").append(String.format("%.1f", freeMb / 1024.0)).append(" GB")
                  .append(" из ").append(String.format("%.0f", totalMb / 1024.0)).append(" GB")
                  .append(" (").append(m.memoryPercent() != null ? String.format("%.0f", m.memoryPercent()) : "?").append("% использовано)")
                  .append(", Диск ").append(m.diskPercent() != null ? String.format("%.0f", m.diskPercent()) : "?").append("%\n");
            }
        }

        if (filter != null && filtered.isEmpty()) {
            sb.append("  Серверов по данному критерию не найдено.\n");
        }

        return sb.toString();
    }

    // ── Server filter ──────────────────────────────────────────────────────────

    private record ServerFilter(String metric, String op, double thresholdGb, String description) {
        boolean matches(ServerMetricsResponse m) {
            if (m.error() != null) return false;
            double value = switch (metric) {
                case "ram_free"  -> m.memoryAvailableMb() != null ? m.memoryAvailableMb() / 1024.0 : -1;
                case "ram_used"  -> m.memoryTotalMb() != null && m.memoryAvailableMb() != null
                                    ? (m.memoryTotalMb() - m.memoryAvailableMb()) / 1024.0 : -1;
                case "ram_pct"   -> m.memoryPercent() != null ? m.memoryPercent() : -1;
                case "cpu_pct"   -> m.cpuPercent()    != null ? m.cpuPercent()    : -1;
                case "disk_pct"  -> m.diskPercent()   != null ? m.diskPercent()   : -1;
                default -> -1;
            };
            if (value < 0) return false;
            return op.equals("<") ? value < thresholdGb : value > thresholdGb;
        }
    }

    private ServerFilter extractServerFilter(String msg) {
        // Определяем оператор
        boolean less = containsAny(msg, "меньше", "менее", "ниже", "не более", "до ");
        boolean more = containsAny(msg, "больше", "более", "выше", "не менее", "от ");
        if (!less && !more) return null;
        String op = less ? "<" : ">";

        // Ищем порог. Берём первое число с единицей измерения, а если единиц нигде нет —
        // первое число, не похожее на год: в вопросе «в 2025 году серверы, где свободно
        // меньше 8 ГБ» порогом должно стать 8, а не 2025.
        Matcher nm = NUMBER_WITH_UNIT.matcher(msg);
        double num = -1;
        String unit = "";
        while (nm.find()) {
            double candidate = Double.parseDouble(nm.group(1).replace(",", "."));
            String candidateUnit = nm.group(2) != null ? nm.group(2).toLowerCase() : "";
            if (!candidateUnit.isEmpty()) {
                num = candidate;
                unit = candidateUnit;
                break;
            }
            if (num < 0 && !looksLikeYear(candidate)) {
                num = candidate;
            }
        }
        if (num < 0) return null;

        // Определяем метрику
        if (containsAny(msg, "cpu", "процессор", "нагрузк")) {
            return new ServerFilter("cpu_pct", op, num,
                    "CPU " + op + " " + (int)num + "%");
        }
        if (containsAny(msg, "диск", "disk")) {
            return new ServerFilter("disk_pct", op, num,
                    "Диск " + op + " " + (int)num + "%");
        }
        // RAM — определяем свободно или занято
        boolean freeQuery = containsAny(msg, "свободн", "free", "доступн");
        String metric = freeQuery ? "ram_free" : "ram_used";
        boolean isPct  = unit.equals("%") || containsAny(msg, "процент", "%");
        if (isPct) metric = "ram_pct";

        String desc = (isPct ? "RAM% " : (freeQuery ? "ОЗУ свободно " : "ОЗУ использовано "))
                    + op + " " + (isPct ? (int)num + "%" : num + " GB");
        return new ServerFilter(metric, op, num, desc);
    }

    /** Целое из диапазона годов — почти наверняка год, а не порог по ресурсам. */
    private boolean looksLikeYear(double value) {
        return value >= 1990 && value <= 2100 && value == Math.rint(value);
    }

    // ── Think-block stripper ───────────────────────────────────────────────────

    private String processChunk(String chunk, boolean[] inThink, StringBuilder thinkBuf) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < chunk.length()) {
            if (!inThink[0]) {
                int start = chunk.indexOf("<think>", i);
                if (start < 0) { out.append(chunk, i, chunk.length()); break; }
                out.append(chunk, i, start);
                inThink[0] = true;
                i = start + 7;
            } else {
                int end = chunk.indexOf("</think>", i);
                if (end < 0) {
                    thinkBuf.append(chunk, i, chunk.length()); // накапливаем think-контент
                    break;
                }
                thinkBuf.append(chunk, i, end);
                inThink[0] = false;
                i = end + 8;
            }
        }
        return out.toString();
    }

}
