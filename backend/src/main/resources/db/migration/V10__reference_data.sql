-- Типы инцидентов
CREATE TABLE dic_failure_type (
    id      BIGSERIAL PRIMARY KEY,
    name_ru VARCHAR(255),
    name_kz VARCHAR(255),
    name_en VARCHAR(255)
);

INSERT INTO dic_failure_type (id, name_ru, name_kz, name_en) VALUES
    (1, 'ППО',                    'ППО',                    'ППО'),
    (2, 'Каналы связи',           'Каналы связи',           'Каналы связи'),
    (3, 'Инфраструктура МТСЗН',   'Инфраструктура МТСЗН',   'Инфраструктура МТСЗН'),
    (4, 'Инфраструктура ЦРТР',    'Инфраструктура ЦРТР',    'Инфраструктура ЦРТР'),
    (5, 'Очередь',                'Очередь',                'Очередь'),
    (6, 'СУБД',                   'СУБД',                   'СУБД'),
    (7, 'ПРТГ',                   'ПРТГ',                   'ПРТГ'),
    (8, 'Ошибка сервиса/клиента', 'Ошибка сервиса/клиента', 'Ошибка сервиса/клиента')
ON CONFLICT (id) DO NOTHING;

-- Типы работ (только Плановые и Внеплановые — PRTG отдельная таблица)
CREATE TABLE dic_job (
    id      BIGINT PRIMARY KEY,
    name_ru VARCHAR(255),
    name_kz VARCHAR(255),
    name_en VARCHAR(255)
);

INSERT INTO dic_job (id, name_ru, name_kz, name_en) VALUES
    (1, 'Плановые работы',   'Жоспарлы жұмыстар',    'Planned works'),
    (2, 'Внеплановые работы','Жоспардан тыс жұмыстар','Unplanned works')
ON CONFLICT (id) DO NOTHING;
