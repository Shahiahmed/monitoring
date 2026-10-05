-- Инциденты
CREATE TABLE incidents (
    id                   BIGSERIAL PRIMARY KEY,
    failure_type_id      BIGINT REFERENCES dic_failure_type(id),
    location_id          BIGINT REFERENCES dic_location(id),
    fixed                BOOLEAN,
    empty_time           BOOLEAN,
    include_availability BOOLEAN NOT NULL DEFAULT true,
    in_message           TEXT,
    out_message          TEXT,
    act                  VARCHAR(255),
    problem              TEXT,
    solution             TEXT,
    source_prtg_id       BIGINT REFERENCES prtg_alerts(id) ON DELETE SET NULL,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_incidents_failure_type ON incidents(failure_type_id);
CREATE INDEX idx_incidents_source_prtg  ON incidents(source_prtg_id);

-- Интервалы простоя инцидента
CREATE TABLE incident_intervals (
    id           BIGSERIAL PRIMARY KEY,
    incident_id  BIGINT NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    date_from    TIMESTAMPTZ,
    date_to      TIMESTAMPTZ,
    diff_minutes INTEGER
);

CREATE INDEX idx_incident_intervals_incident_id ON incident_intervals(incident_id);

-- Связь инцидент ↔ ИС МТЗСН
CREATE TABLE incident_is (
    incident_id BIGINT NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    is_id       BIGINT NOT NULL REFERENCES dic_is(id),
    PRIMARY KEY (incident_id, is_id)
);

-- Файлы инцидента
CREATE TABLE incident_files (
    id           BIGSERIAL PRIMARY KEY,
    incident_id  BIGINT NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    file_name    VARCHAR(512) NOT NULL,
    content_type VARCHAR(255),
    file_size    BIGINT,
    data         BYTEA NOT NULL,
    uploaded_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_incident_files_incident_id ON incident_files(incident_id);
