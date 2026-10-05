CREATE TABLE IF NOT EXISTS dic_is
(
    id      bigint NOT NULL PRIMARY KEY,
    name_en varchar(255),
    name_kz varchar(255),
    name_ru varchar(255),
    go_id   bigint NOT NULL
        CONSTRAINT fk_dic_is_go REFERENCES dic_go (id)
);

CREATE INDEX IF NOT EXISTS idx_name_en_dic_is ON dic_is (name_en);
CREATE INDEX IF NOT EXISTS idx_name_kz_dic_is ON dic_is (name_kz);
CREATE INDEX IF NOT EXISTS idx_name_ru_dic_is ON dic_is (name_ru);
CREATE INDEX IF NOT EXISTS idx_goid_dic_is    ON dic_is (go_id);

-- Каталог как в legacy `ppo` (ИС МТЗСН / PPO для инцидентов). go_id = 3 — Минтруд РК (dic_go).
INSERT INTO dic_is (id, name_en, name_kz, name_ru, go_id) VALUES
    (1,  'АИС Емакет', 'АИС Емакет', 'АИС Емакет', 3),
    (2,  'АИС Есобес', 'АИС Есобес', 'АИС Есобес', 3),
    (3,  'АИС ООП', 'АИС ООП', 'АИС ООП', 3),
    (4,  'АИС ЦБД', 'АИС ЦБД', 'АИС ЦБД', 3),
    (5,  'АИС ЦБДИ', 'АИС ЦБДИ', 'АИС ЦБДИ', 3),
    (6,  'АИС Социальная помощь', 'АИС Социальная помощь', 'АИС Социальная помощь', 3),
    (7,  'АИС Рынок труда', 'АИС Рынок труда', 'АИС Рынок труда', 3),
    (8,  'АИС Охрана труда', 'АИС Охрана труда', 'АИС Охрана труда', 3),
    (9,  'АИС E-HR', 'АИС E-HR', 'АИС E-HR', 3),
    (10, 'АИС Кандас', 'АИС Кандас', 'АИС Кандас', 3),
    (11, 'АИС ИРС', 'АИС ИРС', 'АИС ИРС', 3),
    (12, 'АИС СИК', 'АИС СИК', 'АИС СИК', 3),
    (13, 'портал ПСУ', 'портал ПСУ', 'портал ПСУ', 3),
    (14, 'портал Работа', 'портал Работа', 'портал Работа', 3),
    (15, 'портал СЗИ', 'портал СЗИ', 'портал СЗИ', 3),
    (16, 'портал ТСР', 'портал ТСР', 'портал ТСР', 3)
ON CONFLICT (id) DO NOTHING;
