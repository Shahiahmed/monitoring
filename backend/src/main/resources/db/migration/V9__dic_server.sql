CREATE TABLE IF NOT EXISTS dic_server
(
    id          bigint NOT NULL PRIMARY KEY,
    active      boolean,
    description varchar(255),
    ip          varchar(255),
    env_id      bigint REFERENCES dic_env (id)
);

CREATE INDEX IF NOT EXISTS idx_id_dic_server ON dic_server (id);
CREATE INDEX IF NOT EXISTS idx_active_dic_server ON dic_server (active);
CREATE INDEX IF NOT EXISTS idx_description_dic_server ON dic_server (description);
CREATE INDEX IF NOT EXISTS idx_ip_dic_server ON dic_server (ip);
CREATE INDEX IF NOT EXISTS idx_envid_dic_server ON dic_server (env_id);

INSERT INTO dic_server (id, active, description, ip, env_id) VALUES (2, true, 'Боевой сервер', '192.168.98.205', 2);
INSERT INTO dic_server (id, active, description, ip, env_id) VALUES (3, true, 'Тестовый сервер', '172.31.33.86', 1);
INSERT INTO dic_server (id, active, description, ip, env_id) VALUES (5, true, 'Боевой сервер', '192.168.98.200', 2);
INSERT INTO dic_server (id, active, description, ip, env_id) VALUES (6, true, 'Боевой сервер', '192.168.98.201', 2);
INSERT INTO dic_server (id, active, description, ip, env_id) VALUES (7, true, 'Боевой сервер', '192.168.98.202', 2);
INSERT INTO dic_server (id, active, description, ip, env_id) VALUES (8, true, 'Боевой сервер', '192.168.98.203', 2);
INSERT INTO dic_server (id, active, description, ip, env_id) VALUES (9, true, 'Боевой сервер', '192.168.98.204', 2);
INSERT INTO dic_server (id, active, description, ip, env_id) VALUES (11, true, 'Боевой сервер', '192.168.98.206', 2);
INSERT INTO dic_server (id, active, description, ip, env_id) VALUES (12, true, 'Тестовый сервер', '172.16.16.41', 1);
INSERT INTO dic_server (id, active, description, ip, env_id) VALUES (13, true, 'Тестовый сервер', '172.16.17.101', 1);
INSERT INTO dic_server (id, active, description, ip, env_id) VALUES (14, true, 'Тестовый сервер', '172.16.17.208', 1);
INSERT INTO dic_server (id, active, description, ip, env_id) VALUES (15, true, 'Тестовый сервер', '172.31.33.81', 1);
INSERT INTO dic_server (id, active, description, ip, env_id) VALUES (16, true, 'Тестовый сервер', '172.31.33.82', 1);
INSERT INTO dic_server (id, active, description, ip, env_id) VALUES (17, true, 'Тестовый сервер', '172.31.33.83', 1);
INSERT INTO dic_server (id, active, description, ip, env_id) VALUES (18, true, 'Боевой сервер', '192.168.98.207', 2);
INSERT INTO dic_server (id, active, description, ip, env_id) VALUES (20, true, 'Боевой сервер', '192.168.98.208', 2)
ON CONFLICT (id) DO NOTHING;
