CREATE TABLE IF NOT EXISTS dic_env
(
    id      bigint NOT NULL PRIMARY KEY,
    name_en varchar(255),
    name_kz varchar(255),
    name_ru varchar(255)
);

CREATE INDEX IF NOT EXISTS idx_id_dic_env ON dic_env (id);
CREATE INDEX IF NOT EXISTS idx_name_ru_dic_env ON dic_env (name_ru);
CREATE INDEX IF NOT EXISTS idx_name_kz_dic_env ON dic_env (name_kz);
CREATE INDEX IF NOT EXISTS idx_name_en_dic_env ON dic_env (name_en);

INSERT INTO dic_env (id, name_en, name_kz, name_ru) VALUES (1, 'Test', 'Тест', 'Тест');
INSERT INTO dic_env (id, name_en, name_kz, name_ru) VALUES (2, 'Production', 'Бой', 'Бой')
ON CONFLICT (id) DO NOTHING;
