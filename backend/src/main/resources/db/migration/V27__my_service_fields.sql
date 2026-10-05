CREATE TABLE IF NOT EXISTS my_service_fields (
    id          BIGSERIAL PRIMARY KEY,
    service_id  BIGINT NOT NULL REFERENCES my_services(id) ON DELETE CASCADE,
    direction   VARCHAR(10) NOT NULL,
    sort_order  INT NOT NULL DEFAULT 0,
    group_name  VARCHAR(500),
    field_number VARCHAR(20),
    name_ru     VARCHAR(1000),
    tag_name    VARCHAR(500),
    format_info VARCHAR(500),
    size_info   VARCHAR(500),
    is_required VARCHAR(50),
    notes       TEXT
);

CREATE INDEX IF NOT EXISTS idx_my_service_fields_service_id ON my_service_fields(service_id);
