create table if not exists dic_interaction_type
(
    id      bigint not null primary key,
    name_en varchar(255),
    name_kz varchar(255),
    name_ru varchar(255)
);

create index if not exists idx_id_dic_interaction_type on dic_interaction_type (id);
create index if not exists idx_name_ru_dic_interaction_type on dic_interaction_type (name_ru);
create index if not exists idx_name_kz_dic_interaction_type on dic_interaction_type (name_kz);
create index if not exists idx_name_en_dic_interaction_type on dic_interaction_type (name_en);

insert into dic_interaction_type (id, name_en, name_kz, name_ru) values (1, 'Синхронный', 'Синхронный', 'Синхронный') on conflict do nothing;
insert into dic_interaction_type (id, name_en, name_kz, name_ru) values (2, 'Асинхронный', 'Асинхронный', 'Асинхронный') on conflict do nothing;

create table if not exists dic_app_type
(
    id      bigint not null primary key,
    name_en varchar(255),
    name_kz varchar(255),
    name_ru varchar(255)
);

create index if not exists idx_id_dic_app_type on dic_app_type (id);
create index if not exists idx_name_ru_dic_app_type on dic_app_type (name_ru);
create index if not exists idx_name_kz_dic_app_type on dic_app_type (name_kz);
create index if not exists idx_name_en_dic_app_type on dic_app_type (name_en);

insert into dic_app_type (id, name_en, name_kz, name_ru) values (1, 'Сервис', 'Сервис', 'Сервис') on conflict do nothing;
insert into dic_app_type (id, name_en, name_kz, name_ru) values (2, 'Клиент', 'Клиент', 'Клиент') on conflict do nothing;
