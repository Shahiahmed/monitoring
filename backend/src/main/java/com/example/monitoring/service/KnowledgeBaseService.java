package com.example.monitoring.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * База знаний о самой системе SARAP.
 *
 * Файл resources/ai/knowledge.md разбит на разделы "## Заголовок" со строкой
 * "keywords: ...". На вопрос пользователя подбираются несколько наиболее
 * подходящих разделов, и они уходят в промпт как справка. Это то, что не даёт
 * модели выдумывать устройство сайта: живых данных о нём в системе нет,
 * поэтому знание берётся отсюда.
 *
 * Чтобы дополнить знания ассистента — правится knowledge.md, код трогать не нужно.
 */
@Service
@Slf4j
public class KnowledgeBaseService {

    private static final String RESOURCE = "ai/knowledge.md";

    /** Раздел "О системе" уходит в каждый ответ — без него модель забывает, чем является. */
    private static final String ALWAYS_SECTION = "О системе";

    /** Сколько тематических разделов подставлять сверх обязательного. */
    private static final int MAX_MATCHED_SECTIONS = 3;

    /** Предохранитель по размеру: контекст qwen3:8b небезграничен. */
    private static final int MAX_CHARS = 4000;

    private final List<Section> sections = new ArrayList<>();

    private record Section(String title, List<String> keywords, String body) {
        String render() {
            return "## " + title + "\n" + body;
        }
    }

    @PostConstruct
    void load() {
        try (InputStream in = new ClassPathResource(RESOURCE).getInputStream()) {
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            parse(text);
            log.info("База знаний загружена: {} разделов", sections.size());
        } catch (Exception e) {
            log.warn("Не удалось загрузить базу знаний {}: {}", RESOURCE, e.getMessage());
        }
    }

    private void parse(String text) {
        sections.clear();
        String[] chunks = text.split("(?m)^##\\s+");
        // chunks[0] — преамбула файла до первого раздела, она не нужна
        for (int i = 1; i < chunks.length; i++) {
            String[] lines = chunks[i].split("\\R", -1);
            if (lines.length == 0) continue;
            String title = lines[0].trim();
            List<String> keywords = new ArrayList<>();
            StringBuilder body = new StringBuilder();
            for (int j = 1; j < lines.length; j++) {
                String line = lines[j];
                if (keywords.isEmpty() && line.toLowerCase(Locale.ROOT).startsWith("keywords:")) {
                    Arrays.stream(line.substring("keywords:".length()).split(","))
                            .map(s -> s.trim().toLowerCase(Locale.ROOT))
                            .filter(s -> !s.isEmpty())
                            .forEach(keywords::add);
                    continue;
                }
                body.append(line).append('\n');
            }
            String trimmed = body.toString().trim();
            if (!title.isEmpty() && !trimmed.isEmpty()) {
                sections.add(new Section(title, keywords, trimmed));
            }
        }
    }

    public boolean isEmpty() {
        return sections.isEmpty();
    }

    /**
     * Подбирает справку под вопрос. Возвращает пустую строку, если база не загрузилась.
     */
    public String lookup(String question) {
        if (sections.isEmpty()) return "";
        String q = question == null ? "" : question.toLowerCase(Locale.ROOT);

        List<Section> picked = new ArrayList<>();
        sections.stream()
                .filter(s -> s.title().equalsIgnoreCase(ALWAYS_SECTION))
                .findFirst()
                .ifPresent(picked::add);

        sections.stream()
                .filter(s -> !picked.contains(s))
                .map(s -> new Scored(s, score(s, q)))
                .filter(s -> s.score() > 0)
                .sorted(Comparator.comparingInt(Scored::score).reversed())
                .limit(MAX_MATCHED_SECTIONS)
                .forEach(s -> picked.add(s.section()));

        StringBuilder sb = new StringBuilder();
        for (Section s : picked) {
            String piece = s.render();
            if (sb.length() + piece.length() > MAX_CHARS) break;
            if (!sb.isEmpty()) sb.append("\n\n");
            sb.append(piece);
        }
        return sb.toString();
    }

    private record Scored(Section section, int score) {}

    /** Совпадение по ключевым словам весит больше, чем по заголовку. */
    private int score(Section s, String question) {
        int score = 0;
        for (String kw : s.keywords()) {
            if (question.contains(kw)) score += 3;
        }
        for (String word : s.title().toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{Nd}]+")) {
            if (word.length() >= 5 && question.contains(word)) score += 2;
        }
        return score;
    }
}
