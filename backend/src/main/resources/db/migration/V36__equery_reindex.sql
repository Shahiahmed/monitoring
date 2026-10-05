-- shep_service_id теперь ключ сервиса, code — отправитель
-- Пересоздаём уникальный индекс под новую логику

DROP INDEX IF EXISTS idx_e_query_counts_unique;

CREATE UNIQUE INDEX idx_e_query_counts_unique
    ON e_query_counts (year_month, shep_service_id, COALESCE(code, ''));
