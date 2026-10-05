-- Плановые и внеплановые работы
CREATE TABLE works (
    id                   BIGSERIAL PRIMARY KEY,
    dic_job_id           BIGINT REFERENCES dic_job(id),
    location_id          BIGINT REFERENCES dic_location(id),
    empty_time           BOOLEAN,
    include_availability BOOLEAN NOT NULL DEFAULT true,
    in_message           VARCHAR(512),
    out_message          VARCHAR(512),
    solution             TEXT,
    source_prtg_id       BIGINT REFERENCES prtg_alerts(id) ON DELETE SET NULL,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_works_dic_job       ON works(dic_job_id);
CREATE INDEX idx_works_source_prtg   ON works(source_prtg_id);

-- Интервалы работ
CREATE TABLE work_intervals (
    id           BIGSERIAL PRIMARY KEY,
    work_id      BIGINT NOT NULL REFERENCES works(id) ON DELETE CASCADE,
    date_from    TIMESTAMPTZ,
    date_to      TIMESTAMPTZ,
    diff_minutes INTEGER
);

CREATE INDEX idx_work_intervals_work_id ON work_intervals(work_id);

-- Связь работа ↔ ИС МТЗСН
CREATE TABLE work_is (
    work_id BIGINT NOT NULL REFERENCES works(id) ON DELETE CASCADE,
    is_id   BIGINT NOT NULL REFERENCES dic_is(id),
    PRIMARY KEY (work_id, is_id)
);

-- Файлы работы
CREATE TABLE work_files (
    id           BIGSERIAL PRIMARY KEY,
    work_id      BIGINT NOT NULL REFERENCES works(id) ON DELETE CASCADE,
    file_name    VARCHAR(512) NOT NULL,
    content_type VARCHAR(255),
    file_size    BIGINT,
    data         BYTEA NOT NULL,
    uploaded_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_work_files_work_id ON work_files(work_id);
