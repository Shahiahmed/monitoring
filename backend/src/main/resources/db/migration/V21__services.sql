-- Статусы сервисов (совпадает с боевой БД dic_status)
CREATE TABLE IF NOT EXISTS dic_status (
    id      bigint NOT NULL PRIMARY KEY,
    name_en varchar(255),
    name_kz varchar(255),
    name_ru varchar(255)
);

INSERT INTO dic_status (id, name_en, name_kz, name_ru) VALUES
    (1, 'failed',     'failed',     'failed'),
    (2, 'Выключен',   'Выключен',   'Выключен'),
    (3, 'Включён',    'Включён',    'Включён'),
    (4, 'Неизвестно', 'Неизвестно', 'Неизвестно')
ON CONFLICT (id) DO NOTHING;

-- Реестр сервисов — колонки в точном порядке как в боевой БД (для совместимости с pg_dump)
CREATE TABLE IF NOT EXISTS application_new (
    id                   bigint NOT NULL PRIMARY KEY,
    artifact_id          varchar(255),
    subsystem_inout      varchar(255),
    description          varchar(1000),
    developer            varchar(255),
    featured             boolean DEFAULT false,
    is_mtszn             varchar(255),
    name                 varchar(1000),
    precedent_production integer,
    precedent_test       integer,
    procedures           varchar(1000),
    project_name         varchar(255),
    publication_date     timestamp,
    schema_database      varchar(255),
    shep_service_id      varchar(255),
    smart_bridge_page    varchar(255),
    url_production       varchar(255),
    url_test             varchar(255),
    app_type_id          bigint REFERENCES dic_app_type(id),
    interaction_type_id  bigint REFERENCES dic_interaction_type(id)
);

CREATE SEQUENCE IF NOT EXISTS application_new_seq START WITH 500 INCREMENT BY 1;

-- Развёртывания — колонки в точном порядке как в боевой БД
CREATE TABLE IF NOT EXISTS application_info_new (
    id             bigint NOT NULL PRIMARY KEY,
    auto_created   boolean DEFAULT false,
    info           varchar(255),
    inner_url      varchar(255),
    precedent      varchar(255),
    url            varchar(255),
    application_id bigint REFERENCES application_new(id) ON DELETE CASCADE,
    database_id    bigint,
    env_id         bigint REFERENCES dic_env(id),
    server_id      bigint REFERENCES dic_server(id),
    status_id      bigint REFERENCES dic_status(id)
);

CREATE SEQUENCE IF NOT EXISTS application_info_new_seq START WITH 1000 INCREMENT BY 1;
