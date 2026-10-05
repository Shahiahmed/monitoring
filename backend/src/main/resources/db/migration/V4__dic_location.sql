CREATE TABLE IF NOT EXISTS dic_location
(
    id      bigint NOT NULL PRIMARY KEY,
    name_en varchar(255),
    name_kz varchar(255),
    name_ru varchar(255)
);

CREATE INDEX IF NOT EXISTS idx_name_en_dic_location ON dic_location (name_en);
CREATE INDEX IF NOT EXISTS idx_name_kz_dic_location ON dic_location (name_kz);
CREATE INDEX IF NOT EXISTS idx_name_ru_dic_location ON dic_location (name_ru);

INSERT INTO dic_location (id, name_en, name_kz, name_ru)
VALUES
    (1, 'Astana',    'Астана',    'Астана'),
    (2, 'Karaganda', 'Караганда', 'Караганда')
ON CONFLICT (id) DO NOTHING;
