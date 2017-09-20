-- Create table
create table AE_MAIN
(
  id             NUMBER not null,
  ae_name_event  NUMBER,
  name_object    VARCHAR2(500 CHAR),
  code_object    VARCHAR2(500 CHAR),
  ae_kind_event  NUMBER,
  date_event     DATE,
  ref_respondent NUMBER,
  date_in        DATE,
  rec_id         NUMBER,
  user_id        NUMBER,
  user_location  VARCHAR2(255 CHAR),
  datlast        DATE default sysdate not null,
  is_archive     NUMBER default 0
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table 
comment on table AE_MAIN
  is 'Таблица аудита событий ФРСП';
-- Add comments to the columns 
comment on column AE_MAIN.id
  is 'id таблицы';
comment on column AE_MAIN.ae_name_event
  is 'Id таблицы наименований аудируемых событий';
comment on column AE_MAIN.name_object
  is 'Наименование объекта';
comment on column AE_MAIN.code_object
  is 'Код объекта';
comment on column AE_MAIN.ae_kind_event
  is 'Id таблицы видов аудирумеых событий';
comment on column AE_MAIN.date_event
  is 'Дата события';
comment on column AE_MAIN.ref_respondent
  is 'Id таблицы справочника респондентов';
comment on column AE_MAIN.date_in
  is 'Для отчетов - дата отчета(report_date). Для справочников - дата начала(begin_date)';
comment on column AE_MAIN.rec_id
  is 'Rec_id записи в справочниках';
comment on column AE_MAIN.user_id
  is 'Id таблицы пользователей';
comment on column AE_MAIN.user_location
  is 'Местоположение';
comment on column AE_MAIN.datlast
  is 'Дата последнего редактирования';
comment on column AE_MAIN.is_archive
  is '1-в архиве, 0-не в архиве';
-- Create/Recreate primary, unique and foreign key constraints 
alter table AE_MAIN
  add constraint PK_AE_MAIN primary key (ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
