Актуализировать !!!
-- Create table
create table REF_MAIN
(
  id         NUMBER(14) not null,
  name       VARCHAR2(250) not null,
  code       VARCHAR2(100) not null,
  table_name VARCHAR2(50),
  nsi_code   VARCHAR2(50),
  ref_knd    NUMBER(1) default 0 not null,
  date_load  DATE
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_MAIN
is 'Таблица справочников';
-- Add comments to the columns 
comment on column REF_MAIN.id
is 'Идентификатор';
comment on column REF_MAIN.name
is 'Наименование справочника';
comment on column REF_MAIN.code
is 'Код справочника';
comment on column REF_MAIN.table_name
is 'Наименование таблицы в сис-ме ЕССП';
comment on column REF_MAIN.nsi_code
is 'Код справочника в сис-ме НСИ';
comment on column REF_MAIN.ref_knd
is 'Тип справочника (0- не загружаемый, 1- загружаемый)';
comment on column REF_MAIN.date_load
is 'Дата последней загрузки';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_MAIN
add constraint PK_REF_MAIN primary key (ID)
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

-- Create table
create table TYPE_CHANGE
(
  TYPE_CHANGE NUMBER(14) not null,
  NAME        VARCHAR2(50) not null
)
tablespace USERS
pctfree 10
pctused 40
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table TYPE_CHANGE
is 'Виды изменений';
-- Add comments to the columns 
comment on column TYPE_CHANGE.TYPE_CHANGE
is 'Идентификатор';
comment on column TYPE_CHANGE.NAME
is 'Наименование';
-- Create/Recreate primary, unique and foreign key constraints 
alter table TYPE_CHANGE
add constraint PK_TYPE_CHANGE primary key (TYPE_CHANGE)
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


-- Create table
create table CROSSCHECK_TYPE
(
  id   NUMBER(14) not null,
  name VARCHAR2(100) not null
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
comment on table CROSSCHECK_TYPE
is 'Тип контроля';
-- Add comments to the columns 
comment on column CROSSCHECK_TYPE.id
is 'Id таблицы';
comment on column CROSSCHECK_TYPE.name
is 'Наименование';
-- Create/Recreate primary, unique and foreign key constraints 
alter table CROSSCHECK_TYPE
add constraint PK_CROSSCHEK_TYPE primary key (ID)
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



-- Create table
create table REP_PER_DUR_MONTHS
(
  rep_per_dur_months NUMBER(14) not null,
  name               VARCHAR2(50) not null
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REP_PER_DUR_MONTHS
is 'Периодичность сдачи отчетов';
-- Add comments to the columns 
comment on column REP_PER_DUR_MONTHS.rep_per_dur_months
is 'Период сдачи (1-раз в месяц, 3-раз в квартал)';
comment on column REP_PER_DUR_MONTHS.name
is 'Наименование';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REP_PER_DUR_MONTHS
add constraint PK_REP_PER_DUR_MONTHS primary key (REP_PER_DUR_MONTHS)
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



-- Create table
create table SENT_KND
(
  SENT_KND NUMBER(14) not null,
  name     VARCHAR2(50) not null
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table SENT_KND
is 'Статус отправки';
-- Add comments to the columns 
comment on column SENT_KND.SENT_KND
is 'Id';
comment on column SENT_KND.name
is 'Наименование';
-- Create/Recreate primary, unique and foreign key constraints 
alter table SENT_KND
add constraint PK_SENT_KND primary key (SENT_KND)
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



-- Create table
create table TYPE_CHANGE
(
  TYPE_CHANGE NUMBER(14) not null,
  NAME        VARCHAR2(50) not null
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table TYPE_CHANGE
is 'Виды изменений';
-- Add comments to the columns 
comment on column TYPE_CHANGE.TYPE_CHANGE
is 'Идентификатор';
comment on column TYPE_CHANGE.NAME
is 'Наименование';
-- Create/Recreate primary, unique and foreign key constraints 
alter table TYPE_CHANGE
add constraint PK_TYPE_CHANGE primary key (TYPE_CHANGE)
  using index
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255;




-- Create table
create table REF_DOC_TYPE
(
  id                  NUMBER(14) not null,
  rec_id              NUMBER(14) not null,
  code                VARCHAR2(50 CHAR) not null,
  name_kz             VARCHAR2(250 CHAR),
  name_ru             VARCHAR2(250 CHAR) not null,
  name_en             VARCHAR2(250 CHAR),
  is_identification   NUMBER(1) default 0 not null,
  is_organization_doc NUMBER(1) default 0 not null,
  is_person_doc       NUMBER(1) default 0 not null,
  sign_count          NUMBER,
  weight              NUMBER,
  begin_date          DATE not null,
  delfl               NUMBER(1) default 0 not null,
  datlast             DATE default SYSDATE not null,
  id_usr              NUMBER(18) not null,
  user_location       VARCHAR2(50 CHAR),
  sent_knd            NUMBER(14) default 0 not null,
  entity_id           NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_DOC_TYPE
is 'Справочник типов документов';
-- Add comments to the columns 
comment on column REF_DOC_TYPE.id
is 'Идентификатор';
comment on column REF_DOC_TYPE.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_DOC_TYPE.code
is 'Код';
comment on column REF_DOC_TYPE.name_kz
is 'Наименование на казахском языке';
comment on column REF_DOC_TYPE.name_ru
is 'Наименование на русском';
comment on column REF_DOC_TYPE.name_en
is 'Наименование на английском языке';
comment on column REF_DOC_TYPE.is_identification
is 'Признак идентификационный';
comment on column REF_DOC_TYPE.is_organization_doc
is 'Признак организационный';
comment on column REF_DOC_TYPE.is_person_doc
is 'Признак персональный';
comment on column REF_DOC_TYPE.sign_count
is 'sign_count';
comment on column REF_DOC_TYPE.weight
is 'weight';
comment on column REF_DOC_TYPE.begin_date
is 'Дата начала действия';
comment on column REF_DOC_TYPE.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_DOC_TYPE.datlast
is 'Дата последнего редактирования';
comment on column REF_DOC_TYPE.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_DOC_TYPE.user_location
is 'Местоположение';
comment on column REF_DOC_TYPE.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_DOC_TYPE.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_DOC_TYPE
add constraint PK_REF_DOC_TYPE primary key (ID)
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
alter table REF_DOC_TYPE
add constraint FK_REF_DOC_TYPE_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_DOC_TYPE
add constraint FK_REF_DOC_TYPE_USER foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_DOC_TYPE
add constraint CHECK_REF_DOC_TYPE_DLFL
check (DELFL in (0,1));


-- Create table
create table REF_DOC_TYPE_HST
(
  id_hst              NUMBER(14) not null,
  id                  NUMBER(14) not null,
  rec_id              NUMBER(14) not null,
  code                VARCHAR2(50 CHAR) not null,
  name_kz             VARCHAR2(250 CHAR),
  name_ru             VARCHAR2(250 CHAR) not null,
  name_en             VARCHAR2(250 CHAR),
  is_identification   NUMBER(1) default 0 not null,
  is_organization_doc NUMBER(1) default 0 not null,
  is_person_doc       NUMBER(1) default 0 not null,
  sign_count          NUMBER,
  weight              NUMBER,
  begin_date          DATE not null,
  delfl               NUMBER(1) default 0 not null,
  datlast             DATE default SYSDATE not null,
  id_usr              NUMBER(18) not null,
  user_location       VARCHAR2(50 CHAR),
  type_change         NUMBER(14) not null,
  sent_knd            NUMBER(14) default 0 not null,
  entity_id           NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_DOC_TYPE_HST
is 'История справочника типов документов';
-- Add comments to the columns 
comment on column REF_DOC_TYPE_HST.id_hst
is 'Идентификатор';
comment on column REF_DOC_TYPE_HST.id
is 'Идентификатор основной таблицы';
comment on column REF_DOC_TYPE_HST.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_DOC_TYPE_HST.code
is 'Код';
comment on column REF_DOC_TYPE_HST.name_kz
is 'Наименование на казахском языке';
comment on column REF_DOC_TYPE_HST.name_ru
is 'Наименование на русском языке';
comment on column REF_DOC_TYPE_HST.name_en
is 'Наименование на английском языке';
comment on column REF_DOC_TYPE_HST.is_identification
is 'Признак идентификационный';
comment on column REF_DOC_TYPE_HST.is_organization_doc
is 'Признак организационный';
comment on column REF_DOC_TYPE_HST.is_person_doc
is 'Признак персональный';
comment on column REF_DOC_TYPE_HST.sign_count
is 'sign_count';
comment on column REF_DOC_TYPE_HST.weight
is 'weight';
comment on column REF_DOC_TYPE_HST.begin_date
is 'Дата начала действия';
comment on column REF_DOC_TYPE_HST.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_DOC_TYPE_HST.datlast
is 'Дата последнего редактирования';
comment on column REF_DOC_TYPE_HST.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_DOC_TYPE_HST.user_location
is 'Местоположение';
comment on column REF_DOC_TYPE_HST.type_change
is 'Тип изменения(Добавление, Редактирование, Удаление)';
comment on column REF_DOC_TYPE_HST.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_DOC_TYPE_HST.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_DOC_TYPE_HST
add constraint PK_REF_DOC_TYPE_HST primary key (ID_HST)
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
alter table REF_DOC_TYPE_HST
add constraint FK_REF_DOC_TYPE_HST_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_DOC_TYPE_HST
add constraint FK_REF_DOC_TYPE_HST_TC foreign key (TYPE_CHANGE)
references TYPE_CHANGE (TYPE_CHANGE);
alter table REF_DOC_TYPE_HST
add constraint FK_REF_DOC_TYPE_HST_U foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_DOC_TYPE_HST
add constraint CHECK_REF_DOC_TYPE_HST_DLFL
check (DELFL in (0,1));


-- Create table
create table REF_SUBJECT_TYPE
(
  id                 NUMBER(14) not null,
  rec_id             NUMBER(14) not null,
  code               VARCHAR2(50 CHAR) not null,
  name_kz            VARCHAR2(250 CHAR),
  name_ru            VARCHAR2(250 CHAR) not null,
  name_en            VARCHAR2(250 CHAR),
  kind_id            NUMBER,
  rep_per_dur_months NUMBER default 1 not null,
  is_advance         NUMBER(1) default 0 not null,
  begin_date         DATE not null,
  delfl              NUMBER(1) default 0 not null,
  datlast            DATE default SYSDATE not null,
  id_usr             NUMBER(18) not null,
  user_location      VARCHAR2(50 CHAR),
  sent_knd           NUMBER(14) default 0 not null,
  entity_id          NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_SUBJECT_TYPE
is 'Справочник типов организации';
-- Add comments to the columns 
comment on column REF_SUBJECT_TYPE.id
is 'Идентификатор';
comment on column REF_SUBJECT_TYPE.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_SUBJECT_TYPE.code
is 'Код';
comment on column REF_SUBJECT_TYPE.name_kz
is 'Наименование на казахском языке';
comment on column REF_SUBJECT_TYPE.name_ru
is 'Наименование на русском';
comment on column REF_SUBJECT_TYPE.name_en
is 'Наименование на английском языке';
comment on column REF_SUBJECT_TYPE.kind_id
is 'KIND_ID';
comment on column REF_SUBJECT_TYPE.rep_per_dur_months
is 'Id таблицы период сдачи';
comment on column REF_SUBJECT_TYPE.begin_date
is 'Дата начала действия';
comment on column REF_SUBJECT_TYPE.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_SUBJECT_TYPE.datlast
is 'Дата последнего редактирования';
comment on column REF_SUBJECT_TYPE.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_SUBJECT_TYPE.user_location
is 'Местоположение';
comment on column REF_SUBJECT_TYPE.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_SUBJECT_TYPE.entity_id
is 'Id записи в метаклассе ЕССП';
comment on column REF_SUBJECT_TYPE.is_advance
is 'Признак подотчетного лица';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_SUBJECT_TYPE
add constraint PK_REF_SUBJECT_TYPE primary key (ID)
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
alter table REF_SUBJECT_TYPE
add constraint FK_REF_SUBJECT_TYPE_KND_DM foreign key (REP_PER_DUR_MONTHS)
references REP_PER_DUR_MONTHS (REP_PER_DUR_MONTHS);
alter table REF_SUBJECT_TYPE
add constraint FK_REF_SUBJECT_TYPE_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_SUBJECT_TYPE
add constraint FK_REF_SUBJECT_TYPE_USER foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_SUBJECT_TYPE
add constraint CHECK_REF_SUBJECT_TYPE_DLFL
check (DELFL in (0,1));



-- Create table
create table REF_SUBJECT_TYPE_HST
(
  id_hst             NUMBER(14) not null,
  id                 NUMBER(14) not null,
  rec_id             NUMBER(14) not null,
  code               VARCHAR2(50 CHAR) not null,
  name_kz            VARCHAR2(250 CHAR),
  name_ru            VARCHAR2(250 CHAR) not null,
  name_en            VARCHAR2(250 CHAR),
  kind_id            NUMBER,
  rep_per_dur_months NUMBER,
  is_advance         NUMBER(1) default 0 not null,
  begin_date         DATE not null,
  delfl              NUMBER(1) default 0 not null,
  datlast            DATE default SYSDATE not null,
  id_usr             NUMBER(18) not null,
  user_location      VARCHAR2(50 CHAR),
  type_change        NUMBER(14),
  sent_knd           NUMBER(14) default 0 not null,
  entity_id          NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_SUBJECT_TYPE_HST
is 'История справочника типов организации';
-- Add comments to the columns 
comment on column REF_SUBJECT_TYPE_HST.id_hst
is 'Идентификатор';
comment on column REF_SUBJECT_TYPE_HST.id
is 'Идентификатор основной таблицы';
comment on column REF_SUBJECT_TYPE_HST.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_SUBJECT_TYPE_HST.code
is 'Код';
comment on column REF_SUBJECT_TYPE_HST.name_kz
is 'Наименование на казахском языке';
comment on column REF_SUBJECT_TYPE_HST.name_ru
is 'Наименование на русском языке';
comment on column REF_SUBJECT_TYPE_HST.name_en
is 'Наименование на английском языке';
comment on column REF_SUBJECT_TYPE_HST.kind_id
is 'KIND_ID';
comment on column REF_SUBJECT_TYPE_HST.rep_per_dur_months
is 'Id таблицы REP_PER_DUR_MONTHS период сдачи';
comment on column REF_SUBJECT_TYPE_HST.begin_date
is 'Дата начала действия';
comment on column REF_SUBJECT_TYPE_HST.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_SUBJECT_TYPE_HST.datlast
is 'Дата последнего редактирования';
comment on column REF_SUBJECT_TYPE_HST.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_SUBJECT_TYPE_HST.user_location
is 'Местоположение';
comment on column REF_SUBJECT_TYPE_HST.type_change
is 'Тип изменения(Добавление, Редактирование, Удаление)';
comment on column REF_SUBJECT_TYPE_HST.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_SUBJECT_TYPE_HST.entity_id
is 'Id записи в метаклассе ЕССП';
comment on column REF_SUBJECT_TYPE_HST.is_advance
is 'Признак подотченого лица';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_SUBJECT_TYPE_HST
add constraint PK_REF_SUBJECT_TYPE_HST primary key (ID_HST)
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
alter table REF_SUBJECT_TYPE_HST
add constraint FK_REF_SUBJECT_TYPE_HST_DM foreign key (REP_PER_DUR_MONTHS)
references REP_PER_DUR_MONTHS (REP_PER_DUR_MONTHS);
alter table REF_SUBJECT_TYPE_HST
add constraint FK_REF_SUBJECT_TYPE_HST_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_SUBJECT_TYPE_HST
add constraint FK_REF_SUBJECT_TYPE_HST_TC foreign key (TYPE_CHANGE)
references TYPE_CHANGE (TYPE_CHANGE);
alter table REF_SUBJECT_TYPE_HST
add constraint FK_REF_SUBJECT_TYPE_HST_U foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_SUBJECT_TYPE_HST
add constraint CHECK_REF_SUB_TYPE_HST_DLFL
check (DELFL in (0,1));



-- Create table
create table REF_TYPE_BUS_ENTITY
(
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  name_kz       VARCHAR2(250 CHAR),
  name_ru       VARCHAR2(250 CHAR) not null,
  name_en       VARCHAR2(250 CHAR),
  begin_date    DATE not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_TYPE_BUS_ENTITY
is 'Справочник организационно-правовая форма';
-- Add comments to the columns 
comment on column REF_TYPE_BUS_ENTITY.id
is 'Идентификатор';
comment on column REF_TYPE_BUS_ENTITY.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_TYPE_BUS_ENTITY.code
is 'Код';
comment on column REF_TYPE_BUS_ENTITY.name_kz
is 'Наименование на казахском языке';
comment on column REF_TYPE_BUS_ENTITY.name_ru
is 'Наименование на русском';
comment on column REF_TYPE_BUS_ENTITY.name_en
is 'Наименование на английском языке';
comment on column REF_TYPE_BUS_ENTITY.begin_date
is 'Дата начала действия';
comment on column REF_TYPE_BUS_ENTITY.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_TYPE_BUS_ENTITY.datlast
is 'Дата последнего редактирования';
comment on column REF_TYPE_BUS_ENTITY.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_TYPE_BUS_ENTITY.user_location
is 'Местоположение';
comment on column REF_TYPE_BUS_ENTITY.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_TYPE_BUS_ENTITY.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_TYPE_BUS_ENTITY
add constraint PK_REF_TYPE_BUS_ENTITY primary key (ID)
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
alter table REF_TYPE_BUS_ENTITY
add constraint FK_REF_TYPE_BUS_ENTITY_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_TYPE_BUS_ENTITY
add constraint FK_REF_TYPE_BUS_ENTITY_USER foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_TYPE_BUS_ENTITY
add constraint CHECK_REF_T_B_E_DLFL
check (DELFL in (0,1));

-- Create table
create table REF_TYPE_BUS_ENTITY_HST
(
  id_hst        NUMBER(14) not null,
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  name_kz       VARCHAR2(250 CHAR),
  name_ru       VARCHAR2(250 CHAR) not null,
  name_en       VARCHAR2(250 CHAR),
  begin_date    DATE not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  type_change   NUMBER(14),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_TYPE_BUS_ENTITY_HST
is 'История справочника организационно-правовая форма';
-- Add comments to the columns 
comment on column REF_TYPE_BUS_ENTITY_HST.id_hst
is 'Идентификатор';
comment on column REF_TYPE_BUS_ENTITY_HST.id
is 'Идентификатор основной таблицы';
comment on column REF_TYPE_BUS_ENTITY_HST.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_TYPE_BUS_ENTITY_HST.code
is 'Код';
comment on column REF_TYPE_BUS_ENTITY_HST.name_kz
is 'Наименование на казахском языке';
comment on column REF_TYPE_BUS_ENTITY_HST.name_ru
is 'Наименование на русском языке';
comment on column REF_TYPE_BUS_ENTITY_HST.name_en
is 'Наименование на английском языке';
comment on column REF_TYPE_BUS_ENTITY_HST.begin_date
is 'Дата начала действия';
comment on column REF_TYPE_BUS_ENTITY_HST.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_TYPE_BUS_ENTITY_HST.datlast
is 'Дата последнего редактирования';
comment on column REF_TYPE_BUS_ENTITY_HST.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_TYPE_BUS_ENTITY_HST.user_location
is 'Местоположение';
comment on column REF_TYPE_BUS_ENTITY_HST.type_change
is 'Тип изменения(Добавление, Редактирование, Удаление)';
comment on column REF_TYPE_BUS_ENTITY_HST.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_TYPE_BUS_ENTITY_HST.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_TYPE_BUS_ENTITY_HST
add constraint PK_REF_TYPE_BUS_ENTITY_HST primary key (ID_HST)
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
alter table REF_TYPE_BUS_ENTITY_HST
add constraint FK_REF_TYPE_BUS_ENTITY_HST_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_TYPE_BUS_ENTITY_HST
add constraint FK_REF_TYPE_BUS_ENTITY_HST_TC foreign key (TYPE_CHANGE)
references TYPE_CHANGE (TYPE_CHANGE);
alter table REF_TYPE_BUS_ENTITY_HST
add constraint FK_REF_TYPE_BUS_ENTITY_HST_U foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_TYPE_BUS_ENTITY_HST
add constraint CHECK_REF_T_B_E_HST_DLFL
check (DELFL in (0,1));


-- Create table
create table REF_COUNTRY
(
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  name_kz       VARCHAR2(250 CHAR),
  name_ru       VARCHAR2(250 CHAR) not null,
  name_en       VARCHAR2(250 CHAR),
  begin_date    DATE default sysdate not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_COUNTRY
is 'Справочник стран';
-- Add comments to the columns 
comment on column REF_COUNTRY.id
is 'Идентификатор';
comment on column REF_COUNTRY.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_COUNTRY.code
is 'Код';
comment on column REF_COUNTRY.name_kz
is 'Наименование на казахском';
comment on column REF_COUNTRY.name_ru
is 'Наименование на русском';
comment on column REF_COUNTRY.name_en
is 'Наименование на английском';
comment on column REF_COUNTRY.begin_date
is 'Дата начала действия';
comment on column REF_COUNTRY.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_COUNTRY.datlast
is 'Дата последнего редактирования';
comment on column REF_COUNTRY.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_COUNTRY.user_location
is 'Местоположение';
comment on column REF_COUNTRY.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_COUNTRY.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_COUNTRY
add constraint PK_REF_COUNTRY primary key (ID)
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
alter table REF_COUNTRY
add constraint FK_REF_COUNTRY_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_COUNTRY
add constraint FK_REF_COUNTRY_USR foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_COUNTRY
add constraint CHECK_REF_COUNTRY_DELFL
check (DELFL in (0,1));


-- Create table
create table REF_REGION
(
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  name_kz       VARCHAR2(250 CHAR),
  name_ru       VARCHAR2(250 CHAR) not null,
  name_en       VARCHAR2(250 CHAR),
  oblast_name   VARCHAR2(250 CHAR),
  begin_date    DATE not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_REGION
is 'Справочник регионов';
-- Add comments to the columns 
comment on column REF_REGION.id
is 'Идентификатор';
comment on column REF_REGION.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_REGION.code
is 'Код';
comment on column REF_REGION.name_kz
is 'Наименование на казахском языке';
comment on column REF_REGION.name_ru
is 'Наименование на русском';
comment on column REF_REGION.name_en
is 'Наименование на английском языке';
comment on column REF_REGION.oblast_name
is 'Область';
comment on column REF_REGION.begin_date
is 'Дата начала действия';
comment on column REF_REGION.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_REGION.datlast
is 'Дата последнего редактирования';
comment on column REF_REGION.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_REGION.user_location
is 'Местоположение';
comment on column REF_REGION.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_REGION.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_REGION
add constraint PK_REF_REGION primary key (ID)
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
alter table REF_REGION
add constraint FK_REF_REGION_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_REGION
add constraint FK_REF_REGION_USER foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_REGION
add constraint CHECK_REF_REGION_DLFL
check (DELFL in (0,1));


-- Create table
create table REF_MANAGERS
(
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  fm            VARCHAR2(50 CHAR) not null,
  nm            VARCHAR2(50 CHAR) not null,
  ft            VARCHAR2(50 CHAR),
  fio_kz        VARCHAR2(250 CHAR),
  fio_en        VARCHAR2(250 CHAR),
  begin_date    DATE default sysdate not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_MANAGERS
is 'Справочник руководящих работников';
-- Add comments to the columns 
comment on column REF_MANAGERS.id
is 'Идентификатор';
comment on column REF_MANAGERS.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_MANAGERS.code
is 'Код';
comment on column REF_MANAGERS.fm
is 'Фамилия';
comment on column REF_MANAGERS.nm
is 'Имя';
comment on column REF_MANAGERS.ft
is 'Отчество';
comment on column REF_MANAGERS.fio_kz
is 'ФИО на казахском';
comment on column REF_MANAGERS.fio_en
is 'ФИО на английском';
comment on column REF_MANAGERS.begin_date
is 'Дата начала действия';
comment on column REF_MANAGERS.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_MANAGERS.datlast
is 'Дата последнего редактирования';
comment on column REF_MANAGERS.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_MANAGERS.user_location
is 'Местоположение';
comment on column REF_MANAGERS.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_MANAGERS.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_MANAGERS
add constraint PK_REF_MANAGERS primary key (ID)
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
alter table REF_MANAGERS
add constraint FK_REF_MANAGERS_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_MANAGERS
add constraint FK_REF_MANAGERS_USER foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_MANAGERS
add constraint CHECK_REF_MANAGERS_DELFL
check (DELFL in (0,1));


-- Create table
create table REF_MANAGERS_HST
(
  id_hst        NUMBER(14) not null,
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  fm            VARCHAR2(50 CHAR) not null,
  nm            VARCHAR2(50 CHAR) not null,
  ft            VARCHAR2(50 CHAR),
  fio_kz        VARCHAR2(250 CHAR),
  fio_en        VARCHAR2(250 CHAR),
  begin_date    DATE default sysdate not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  type_change   NUMBER(14),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_MANAGERS_HST
is 'История справочника руководящих работников';
-- Add comments to the columns 
comment on column REF_MANAGERS_HST.id_hst
is 'Идентификатор';
comment on column REF_MANAGERS_HST.id
is 'Идентификатор основной таблицы REF_MANAGERS';
comment on column REF_MANAGERS_HST.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_MANAGERS_HST.code
is 'Код';
comment on column REF_MANAGERS_HST.fm
is 'Фамилия';
comment on column REF_MANAGERS_HST.nm
is 'Имя';
comment on column REF_MANAGERS_HST.ft
is 'Отчество';
comment on column REF_MANAGERS_HST.fio_kz
is 'ФИО на казахском';
comment on column REF_MANAGERS_HST.fio_en
is 'ФИО на английском';
comment on column REF_MANAGERS_HST.begin_date
is 'Дата начала действия';
comment on column REF_MANAGERS_HST.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_MANAGERS_HST.datlast
is 'Дата последнего редактирования';
comment on column REF_MANAGERS_HST.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_MANAGERS_HST.user_location
is 'Местоположение';
comment on column REF_MANAGERS_HST.type_change
is 'Тип изменения(Добавление, Редактирование, Удаление)';
comment on column REF_MANAGERS_HST.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_MANAGERS_HST.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_MANAGERS_HST
add constraint PK_REF_MANAGERS_HST primary key (ID_HST)
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
alter table REF_MANAGERS_HST
add constraint FK_REF_MANAGERS_HST_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_MANAGERS_HST
add constraint FK_REF_MANAGERS_HST_TC foreign key (TYPE_CHANGE)
references TYPE_CHANGE (TYPE_CHANGE);
alter table REF_MANAGERS_HST
add constraint FK_REF_MANAGERS_HST_USER foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_MANAGERS_HST
add constraint CHECK_REF_MANAGERS_HST_DELFL
check (DELFL in (0,1));


-- Create table
create table REF_LEGAL_PERSON
(
  id                  NUMBER(14) not null,
  rec_id              NUMBER(14) not null,
  code                VARCHAR2(50 CHAR) not null,
  idn                 VARCHAR2(12 CHAR),
  name_kz             VARCHAR2(250 CHAR),
  name_ru             VARCHAR2(250 CHAR) not null,
  name_en             VARCHAR2(250 CHAR),
  ref_subject_type    NUMBER(14) not null,
  ref_type_bus_entity NUMBER(14) not null,
  ref_country         NUMBER(14) not null,
  ref_region          NUMBER(14),
  postal_index        VARCHAR2(50 CHAR),
  address_street      VARCHAR2(100 CHAR),
  address_num_house   VARCHAR2(50 CHAR),
  ref_managers        NUMBER(14),
  legal_address       VARCHAR2(200 CHAR),
  fact_address        VARCHAR2(200 CHAR),
  note                VARCHAR2(200 CHAR),
  begin_date          DATE default sysdate not null,
  delfl               NUMBER(1) default 0 not null,
  datlast             DATE default sysdate not null,
  id_usr              NUMBER(18) not null,
  user_location       VARCHAR2(50 CHAR),
  sent_knd            NUMBER(14) default 0 not null,
  short_name_ru       VARCHAR2(250 CHAR),
  short_name_kz       VARCHAR2(250 CHAR),
  short_name_en       VARCHAR2(250 CHAR),
  entity_id           NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_LEGAL_PERSON
is 'Справочник юридических лиц';
-- Add comments to the columns 
comment on column REF_LEGAL_PERSON.id
is 'Идентификатор';
comment on column REF_LEGAL_PERSON.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_LEGAL_PERSON.code
is 'Код';
comment on column REF_LEGAL_PERSON.idn
is 'ИН';
comment on column REF_LEGAL_PERSON.name_kz
is 'Наименование на казахском';
comment on column REF_LEGAL_PERSON.name_ru
is 'Наименование на русском';
comment on column REF_LEGAL_PERSON.name_en
is 'Наименование на английском';
comment on column REF_LEGAL_PERSON.ref_subject_type
is 'Id таблицы Тип организации';
comment on column REF_LEGAL_PERSON.ref_type_bus_entity
is 'Id таблицы организ правовая форма';
comment on column REF_LEGAL_PERSON.ref_country
is 'Id таблицы страна резиденства';
comment on column REF_LEGAL_PERSON.ref_region
is 'Id таблицы город/регионы';
comment on column REF_LEGAL_PERSON.postal_index
is 'Почтовый индекс';
comment on column REF_LEGAL_PERSON.address_street
is 'Улица/Микрорайон/Проспект';
comment on column REF_LEGAL_PERSON.address_num_house
is 'Номер дома';
comment on column REF_LEGAL_PERSON.ref_managers
is 'Id таблицы руководитель';
comment on column REF_LEGAL_PERSON.legal_address
is 'Юридический адрес';
comment on column REF_LEGAL_PERSON.fact_address
is 'Фактический адрес';
comment on column REF_LEGAL_PERSON.note
is 'Примечание';
comment on column REF_LEGAL_PERSON.begin_date
is 'Дата начала действия';
comment on column REF_LEGAL_PERSON.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_LEGAL_PERSON.datlast
is 'Дата последнего редактирования';
comment on column REF_LEGAL_PERSON.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_LEGAL_PERSON.user_location
is 'Местоположение';
comment on column REF_LEGAL_PERSON.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_LEGAL_PERSON.short_name_ru
is 'Краткое наименование на русском';
comment on column REF_LEGAL_PERSON.short_name_kz
is 'Краткое наименование на казахском';
comment on column REF_LEGAL_PERSON.short_name_en
is 'Краткое наименование на русском';
comment on column REF_LEGAL_PERSON.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_LEGAL_PERSON
add constraint PK_REF_LEGAL_PERSON primary key (ID)
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
alter table REF_LEGAL_PERSON
add constraint FK_REF_LEGAL_PER_CRY foreign key (REF_COUNTRY)
references REF_COUNTRY (ID);
alter table REF_LEGAL_PERSON
add constraint FK_REF_LEGAL_PER_MAN foreign key (REF_MANAGERS)
references REF_MANAGERS (ID);
alter table REF_LEGAL_PERSON
add constraint FK_REF_LEGAL_PER_REG foreign key (REF_REGION)
references REF_REGION (ID);
alter table REF_LEGAL_PERSON
add constraint FK_REF_LEGAL_PER_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_LEGAL_PERSON
add constraint FK_REF_LEGAL_PER_SUB foreign key (REF_SUBJECT_TYPE)
references REF_SUBJECT_TYPE (ID);
alter table REF_LEGAL_PERSON
add constraint FK_REF_LEGAL_PER_TBE foreign key (REF_TYPE_BUS_ENTITY)
references REF_TYPE_BUS_ENTITY (ID);
alter table REF_LEGAL_PERSON
add constraint FK_REF_LEGAL_PER_USR foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_LEGAL_PERSON
add constraint CHECK_REF_LEGAL_PERSON_DELFL
check (DELFL in (0,1));



-- Create table
create table REF_LEGAL_PERSON_HST
(
  id_hst              NUMBER(14) not null,
  id                  NUMBER(14) not null,
  rec_id              NUMBER(14) not null,
  code                VARCHAR2(50 CHAR) not null,
  idn                 VARCHAR2(12 CHAR),
  name_kz             VARCHAR2(250 CHAR),
  name_ru             VARCHAR2(250 CHAR) not null,
  name_en             VARCHAR2(250 CHAR),
  ref_subject_type    NUMBER(14) not null,
  ref_type_bus_entity NUMBER(14) not null,
  ref_country         NUMBER(14) not null,
  ref_region          NUMBER(14),
  postal_index        VARCHAR2(50 CHAR),
  address_street      VARCHAR2(100 CHAR),
  address_num_house   VARCHAR2(50 CHAR),
  ref_managers        NUMBER(14),
  legal_address       VARCHAR2(200 CHAR),
  fact_address        VARCHAR2(200 CHAR),
  note                VARCHAR2(200 CHAR),
  begin_date          DATE default sysdate not null,
  delfl               NUMBER(1) default 0 not null,
  datlast             DATE default sysdate not null,
  id_usr              NUMBER(18) not null,
  user_location       VARCHAR2(50 CHAR),
  type_change         NUMBER(14) not null,
  sent_knd            NUMBER(14) default 0 not null,
  short_name_kz       VARCHAR2(250 CHAR),
  short_name_ru       VARCHAR2(250 CHAR),
  short_name_en       VARCHAR2(250 CHAR),
  entity_id           NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_LEGAL_PERSON_HST
is 'История справочника юридических лиц';
-- Add comments to the columns 
comment on column REF_LEGAL_PERSON_HST.id_hst
is 'Идентификатор';
comment on column REF_LEGAL_PERSON_HST.id
is 'Идентификатор основной таблицы';
comment on column REF_LEGAL_PERSON_HST.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_LEGAL_PERSON_HST.code
is 'Код';
comment on column REF_LEGAL_PERSON_HST.idn
is 'ИН';
comment on column REF_LEGAL_PERSON_HST.name_kz
is 'Наименование на казахском';
comment on column REF_LEGAL_PERSON_HST.name_ru
is 'Наименование на русском';
comment on column REF_LEGAL_PERSON_HST.name_en
is 'Наименование на английском';
comment on column REF_LEGAL_PERSON_HST.ref_subject_type
is 'Id таблицы тип организации';
comment on column REF_LEGAL_PERSON_HST.ref_type_bus_entity
is 'Id таблицы организ правовая форма';
comment on column REF_LEGAL_PERSON_HST.ref_country
is 'Id таблицы страна резиденства';
comment on column REF_LEGAL_PERSON_HST.ref_region
is 'Id таблицы город';
comment on column REF_LEGAL_PERSON_HST.postal_index
is 'Почтовый индекс';
comment on column REF_LEGAL_PERSON_HST.address_street
is 'Улица/Микрорайон/Проспект';
comment on column REF_LEGAL_PERSON_HST.address_num_house
is 'Номер дома';
comment on column REF_LEGAL_PERSON_HST.ref_managers
is 'Id таблицы руководитель';
comment on column REF_LEGAL_PERSON_HST.legal_address
is 'Юридический адрес';
comment on column REF_LEGAL_PERSON_HST.fact_address
is 'Фактический адрес';
comment on column REF_LEGAL_PERSON_HST.note
is 'Примечание';
comment on column REF_LEGAL_PERSON_HST.begin_date
is 'Дата начала действия';
comment on column REF_LEGAL_PERSON_HST.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_LEGAL_PERSON_HST.datlast
is 'Дата последнего редактирования';
comment on column REF_LEGAL_PERSON_HST.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_LEGAL_PERSON_HST.user_location
is 'Местоположение';
comment on column REF_LEGAL_PERSON_HST.type_change
is 'Тип изменения(Добавление, Редактирование, Удаление)';
comment on column REF_LEGAL_PERSON_HST.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_LEGAL_PERSON_HST.short_name_kz
is 'Краткое наименование на казахском';
comment on column REF_LEGAL_PERSON_HST.short_name_ru
is 'Краткое наименование на русском';
comment on column REF_LEGAL_PERSON_HST.short_name_en
is 'Краткое наименование на английском';
comment on column REF_LEGAL_PERSON_HST.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_LEGAL_PERSON_HST
add constraint PK_REF_LEGAL_PERSON_HST primary key (ID_HST)
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
alter table REF_LEGAL_PERSON_HST
add constraint FK_REF_LEGAL_PER_H_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_LEGAL_PERSON_HST
add constraint FK_REF_LEGAL_PER_H_TC foreign key (TYPE_CHANGE)
references TYPE_CHANGE (TYPE_CHANGE);
alter table REF_LEGAL_PERSON_HST
add constraint FK_REF_LEGAL_PER_H_USR foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_LEGAL_PERSON_HST
add constraint CHECK_REF_LEGAL_PERSON_H_DELFL
check (DELFL in (0,1));




-- Create table
create table REF_RESPONDENT
(
  id               NUMBER(14) not null,
  rec_id           NUMBER(14) not null,
  code             VARCHAR2(50 CHAR) not null,
  ref_legal_person NUMBER(14) not null,
  nokbdb_code      VARCHAR2(50 CHAR),
  main_buh         VARCHAR2(50 CHAR),
  date_begin_lic   DATE,
  date_end_lic     DATE,
  stop_lic         VARCHAR2(100 CHAR),
  vid_activity     VARCHAR2(50 CHAR),
  begin_date       DATE not null,
  delfl            NUMBER(1) default 0 not null,
  datlast          DATE default SYSDATE not null,
  id_usr           NUMBER(18) not null,
  user_location    VARCHAR2(50 CHAR),
  sent_knd         NUMBER(14) default 0 not null,
  entity_id        NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_RESPONDENT
is 'Справочник респондентов';
-- Add comments to the columns 
comment on column REF_RESPONDENT.id
is 'Идентификатор';
comment on column REF_RESPONDENT.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_RESPONDENT.code
is 'Код';
comment on column REF_RESPONDENT.ref_legal_person
is 'Id таблицы юр.лица';
comment on column REF_RESPONDENT.nokbdb_code
is 'КБДБ код';
comment on column REF_RESPONDENT.main_buh
is 'Главный бухгалтер';
comment on column REF_RESPONDENT.date_begin_lic
is 'Дата начала лицензии';
comment on column REF_RESPONDENT.date_end_lic
is 'Дата окончания лицензии';
comment on column REF_RESPONDENT.stop_lic
is 'Причина прекращения лицензии';
comment on column REF_RESPONDENT.vid_activity
is 'Вид деятельности';
comment on column REF_RESPONDENT.begin_date
is 'Дата начала действия';
comment on column REF_RESPONDENT.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_RESPONDENT.datlast
is 'Дата последнего редактирования';
comment on column REF_RESPONDENT.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_RESPONDENT.user_location
is 'Местоположение';
comment on column REF_RESPONDENT.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_RESPONDENT.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_RESPONDENT
add constraint PK_REF_RESPONDENT primary key (ID)
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
alter table REF_RESPONDENT
add constraint FK_REF_RESPONDENT_LP foreign key (REF_LEGAL_PERSON)
references REF_LEGAL_PERSON (ID);
alter table REF_RESPONDENT
add constraint FK_REF_RESPONDENT_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_RESPONDENT
add constraint FK_REF_RESPONDENT_USER foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_RESPONDENT
add constraint CHECK_REF_RESPONDENT_DLFL
check (DELFL in (0,1));


-- Create table
create table REF_RESPONDENT_HST
(
  id_hst           NUMBER(14) not null,
  id               NUMBER(14) not null,
  rec_id           NUMBER(14) not null,
  code             VARCHAR2(50 CHAR) not null,
  ref_legal_person NUMBER(14) not null,
  nokbdb_code      VARCHAR2(50 CHAR),
  main_buh         VARCHAR2(50 CHAR),
  date_begin_lic   DATE,
  date_end_lic     DATE,
  stop_lic         VARCHAR2(100 CHAR),
  vid_activity     VARCHAR2(50 CHAR),
  begin_date       DATE not null,
  delfl            NUMBER(1) default 0 not null,
  datlast          DATE default SYSDATE not null,
  id_usr           NUMBER(18) not null,
  user_location    VARCHAR2(50 CHAR),
  type_change      NUMBER(14),
  sent_knd         NUMBER(14) default 0 not null,
  entity_id        NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_RESPONDENT_HST
is 'История справочника респондентов';
-- Add comments to the columns 
comment on column REF_RESPONDENT_HST.id_hst
is 'Идентификатор';
comment on column REF_RESPONDENT_HST.id
is 'Идентификатор основной таблицы';
comment on column REF_RESPONDENT_HST.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_RESPONDENT_HST.code
is 'Код';
comment on column REF_RESPONDENT_HST.ref_legal_person
is 'Id таблицы юр лиц.';
comment on column REF_RESPONDENT_HST.nokbdb_code
is 'КБДБ код';
comment on column REF_RESPONDENT_HST.main_buh
is 'Главный бухгалтер';
comment on column REF_RESPONDENT_HST.date_begin_lic
is 'Дата начала лицензии';
comment on column REF_RESPONDENT_HST.date_end_lic
is 'Дата окончания лицензии';
comment on column REF_RESPONDENT_HST.stop_lic
is 'Причина прекращения лицензии';
comment on column REF_RESPONDENT_HST.vid_activity
is 'Вид деятельности';
comment on column REF_RESPONDENT_HST.begin_date
is 'Дата начала действия';
comment on column REF_RESPONDENT_HST.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_RESPONDENT_HST.datlast
is 'Дата последнего редактирования';
comment on column REF_RESPONDENT_HST.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_RESPONDENT_HST.user_location
is 'Местоположение';
comment on column REF_RESPONDENT_HST.type_change
is 'Тип изменения(Добавление, Редактирование, Удаление)';
comment on column REF_RESPONDENT_HST.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_RESPONDENT_HST.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_RESPONDENT_HST
add constraint PK_REF_RESPONDENT_HST primary key (ID_HST)
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
alter table REF_RESPONDENT_HST
add constraint FK_REF_RESPONDENT_HST_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_RESPONDENT_HST
add constraint FK_REF_RESPONDENT_HST_TC foreign key (TYPE_CHANGE)
references TYPE_CHANGE (TYPE_CHANGE);
alter table REF_RESPONDENT_HST
add constraint FK_REF_RESPONDENT_HST_U foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_RESPONDENT_HST
add constraint CHECK_REF_RESPONDENT_HST_DLFL
check (DELFL in (0,1));


-- Create table
create table REF_DOCUMENT
(
  id             NUMBER(14) not null,
  rec_id         NUMBER(14) not null,
  code           VARCHAR2(50 CHAR) not null,
  name_kz        VARCHAR2(250 CHAR),
  name_ru        VARCHAR2(250 CHAR) not null,
  name_en        VARCHAR2(250 CHAR),
  ref_doc_type   NUMBER(14) not null,
  ref_respondent NUMBER(14) not null,
  begin_date     DATE not null,
  delfl          NUMBER(1) default 0 not null,
  datlast        DATE default SYSDATE not null,
  id_usr         NUMBER(18) not null,
  user_location  VARCHAR2(50 CHAR),
  sent_knd       NUMBER(14) default 0 not null,
  entity_id      NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_DOCUMENT
is 'Справочник документов';
-- Add comments to the columns 
comment on column REF_DOCUMENT.id
is 'Идентификатор';
comment on column REF_DOCUMENT.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_DOCUMENT.code
is 'Код';
comment on column REF_DOCUMENT.name_kz
is 'Наименование на REF_DOCUMENT языке';
comment on column REF_DOCUMENT.name_en
is 'Наименование на английском языке';
comment on column REF_DOCUMENT.ref_doc_type
is 'Идентификатор типов документов';
comment on column REF_DOCUMENT.ref_respondent
is 'Идентификатор респондентов';
comment on column REF_DOCUMENT.begin_date
is 'Дата начала действия';
comment on column REF_DOCUMENT.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_DOCUMENT.datlast
is 'Дата последнего редактирования';
comment on column REF_DOCUMENT.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_DOCUMENT.user_location
is 'Местоположение';
comment on column REF_DOCUMENT.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_DOCUMENT.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_DOCUMENT
add constraint PK_REF_DOCUMENT primary key (ID)
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
alter table REF_DOCUMENT
add constraint FK_REF_DOCUMENT_DT foreign key (REF_DOC_TYPE)
references REF_DOC_TYPE (ID);
alter table REF_DOCUMENT
add constraint FK_REF_DOCUMENT_RESP foreign key (REF_RESPONDENT)
references REF_RESPONDENT (ID);
alter table REF_DOCUMENT
add constraint FK_REF_DOCUMENT_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_DOCUMENT
add constraint FK_REF_DOCUMENT_USER foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_DOCUMENT
add constraint CHECK_REF_DOCUMENT_DLFL
check (DELFL in (0,1));



-- Create table
create table REF_DOCUMENT_HST
(
  id_hst         NUMBER(14) not null,
  id             NUMBER(14) not null,
  rec_id         NUMBER(14) not null,
  code           VARCHAR2(50 CHAR) not null,
  name_kz        VARCHAR2(250 CHAR),
  name_ru        VARCHAR2(250 CHAR) not null,
  name_en        VARCHAR2(250 CHAR),
  ref_doc_type   NUMBER(14) not null,
  ref_respondent NUMBER(14) not null,
  begin_date     DATE not null,
  delfl          NUMBER(1) default 0 not null,
  datlast        DATE default SYSDATE not null,
  id_usr         NUMBER(18) not null,
  user_location  VARCHAR2(50 CHAR),
  type_change    NUMBER(14),
  sent_knd       NUMBER(14) default 0 not null,
  entity_id      NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_DOCUMENT_HST
is 'История справочника документов';
-- Add comments to the columns 
comment on column REF_DOCUMENT_HST.id_hst
is 'Идентификатор';
comment on column REF_DOCUMENT_HST.id
is 'Идентификатор основной таблицы';
comment on column REF_DOCUMENT_HST.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_DOCUMENT_HST.code
is 'Код';
comment on column REF_DOCUMENT_HST.name_kz
is 'Наименование на казахском языке';
comment on column REF_DOCUMENT_HST.name_ru
is 'Наименование на русском языке';
comment on column REF_DOCUMENT_HST.name_en
is 'Наименование на английском языке';
comment on column REF_DOCUMENT_HST.ref_doc_type
is 'Идентификатор типов документов';
comment on column REF_DOCUMENT_HST.ref_respondent
is 'Идентификатор респондентов';
comment on column REF_DOCUMENT_HST.begin_date
is 'Дата начала действия';
comment on column REF_DOCUMENT_HST.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_DOCUMENT_HST.datlast
is 'Дата последнего редактирования';
comment on column REF_DOCUMENT_HST.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_DOCUMENT_HST.user_location
is 'Местоположение';
comment on column REF_DOCUMENT_HST.type_change
is 'Тип изменения(Добавление, Редактирование, Удаление)';
comment on column REF_DOCUMENT_HST.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_DOCUMENT_HST.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_DOCUMENT_HST
add constraint PK_REF_DOCUMENT_HST primary key (ID_HST)
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
alter table REF_DOCUMENT_HST
add constraint FK_REF_DOCUMENT_HST_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_DOCUMENT_HST
add constraint FK_REF_DOCUMENT_HST_TC foreign key (TYPE_CHANGE)
references TYPE_CHANGE (TYPE_CHANGE);
alter table REF_DOCUMENT_HST
add constraint FK_REF_DOCUMENT_HST_U foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_DOCUMENT_HST
add constraint CHECK_REF_DOCUMENT_HST_DLFL
check (DELFL in (0,1));

-- Create table
create table REF_RATE_AGENCY
(
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  name_kz       VARCHAR2(250 CHAR),
  name_en       VARCHAR2(250 CHAR),
  name_ru       VARCHAR2(250 CHAR) not null,
  begin_date    DATE not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_RATE_AGENCY
is 'Справочник рейтинговых агенств';
-- Add comments to the columns 
comment on column REF_RATE_AGENCY.id
is 'Идентификатор';
comment on column REF_RATE_AGENCY.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_RATE_AGENCY.code
is 'Код';
comment on column REF_RATE_AGENCY.name_kz
is 'Наименование на казахском языке';
comment on column REF_RATE_AGENCY.name_en
is 'Наименование на английском языке';
comment on column REF_RATE_AGENCY.name_ru
is 'Наименование на русском';
comment on column REF_RATE_AGENCY.begin_date
is 'Дата начала действия';
comment on column REF_RATE_AGENCY.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_RATE_AGENCY.datlast
is 'Дата последнего редактирования';
comment on column REF_RATE_AGENCY.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_RATE_AGENCY.user_location
is 'Местоположение';
comment on column REF_RATE_AGENCY.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_RATE_AGENCY.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_RATE_AGENCY
add constraint PK_REF_RATE_AGENCY primary key (ID)
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
alter table REF_RATE_AGENCY
add constraint FK_REF_RATE_AGENCY_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_RATE_AGENCY
add constraint FK_REF_RATE_AGENCY_USER foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_RATE_AGENCY
add constraint CHECK_REF_RATE_AGENCY_DLFL
check (DELFL in (0,1));



-- Create table
create table REF_CURRENCY_RATE
(
  id              NUMBER(14) not null,
  rec_id          NUMBER(14) not null,
  code            VARCHAR2(50 CHAR) not null,
  name_kz         VARCHAR2(250 CHAR),
  name_en         VARCHAR2(250 CHAR),
  name_ru         VARCHAR2(250 CHAR) not null,
  ref_rate_agency NUMBER(14),
  begin_date      DATE not null,
  delfl           NUMBER(1) default 0 not null,
  datlast         DATE default SYSDATE not null,
  id_usr          NUMBER(18) not null,
  user_location   VARCHAR2(50 CHAR),
  sent_knd        NUMBER(14) default 0 not null,
  entity_id       NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_CURRENCY_RATE
is 'Справочник рейтинг валют';
-- Add comments to the columns 
comment on column REF_CURRENCY_RATE.id
is 'Идентификатор';
comment on column REF_CURRENCY_RATE.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_CURRENCY_RATE.code
is 'Код';
comment on column REF_CURRENCY_RATE.name_kz
is 'Наименование на казахском языке';
comment on column REF_CURRENCY_RATE.name_en
is 'Наименование на английском языке';
comment on column REF_CURRENCY_RATE.name_ru
is 'Наименование на русском';
comment on column REF_CURRENCY_RATE.ref_rate_agency
is 'ID Таблицы рейтинговых агенств';
comment on column REF_CURRENCY_RATE.begin_date
is 'Дата начала действия';
comment on column REF_CURRENCY_RATE.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_CURRENCY_RATE.datlast
is 'Дата последнего редактирования';
comment on column REF_CURRENCY_RATE.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_CURRENCY_RATE.user_location
is 'Местоположение';
comment on column REF_CURRENCY_RATE.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_CURRENCY_RATE.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_CURRENCY_RATE
add constraint PK_REF_CURRENCY_RATE primary key (ID)
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
alter table REF_CURRENCY_RATE
add constraint FK_REF_CURRENCY_RATE foreign key (ID_USR)
references F_USERS (USER_ID);
alter table REF_CURRENCY_RATE
add constraint FK_REF_CURRENCY_RATE_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
-- Create/Recreate check constraints 
alter table REF_CURRENCY_RATE
add constraint CHECK_REF_CURRENCY_RATE_DLFL
check (DELFL in (0,1));


-- Create table
create table REF_CURRENCY
(
  id                NUMBER(14) not null,
  rec_id            NUMBER(14) not null,
  code              VARCHAR2(50 CHAR) not null,
  minor_units       NUMBER,
  rate              NUMBER,
  name_kz           VARCHAR2(250 CHAR),
  name_en           VARCHAR2(250 CHAR),
  name_ru           VARCHAR2(250 CHAR) not null,
  ref_currency_rate NUMBER(14),
  begin_date        DATE not null,
  delfl             NUMBER(1) default 0 not null,
  datlast           DATE default SYSDATE not null,
  id_usr            NUMBER(18) not null,
  user_location     VARCHAR2(50 CHAR),
  sent_knd          NUMBER(14) default 0 not null,
  entity_id         NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_CURRENCY
is 'Справочник валют';
-- Add comments to the columns 
comment on column REF_CURRENCY.id
is 'Идентификатор';
comment on column REF_CURRENCY.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_CURRENCY.code
is 'Код';
comment on column REF_CURRENCY.minor_units
is 'Малая денежная единица';
comment on column REF_CURRENCY.rate
is 'Ставка';
comment on column REF_CURRENCY.name_kz
is 'Наименование на казахском языке';
comment on column REF_CURRENCY.name_en
is 'Наименование на английском языке';
comment on column REF_CURRENCY.name_ru
is 'Наименование на русском';
comment on column REF_CURRENCY.ref_currency_rate
is 'Id Таблицы рейтинг валют';
comment on column REF_CURRENCY.begin_date
is 'Дата начала действия';
comment on column REF_CURRENCY.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_CURRENCY.datlast
is 'Дата последнего редактирования';
comment on column REF_CURRENCY.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_CURRENCY.user_location
is 'Местоположение';
comment on column REF_CURRENCY.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_CURRENCY.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_CURRENCY
add constraint PK_REF_CURRENCY primary key (ID)
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
alter table REF_CURRENCY
add constraint FK_REF_CURRENCY foreign key (ID_USR)
references F_USERS (USER_ID);
alter table REF_CURRENCY
add constraint FK_REF_CURRENCY_CR foreign key (REF_CURRENCY_RATE)
references REF_CURRENCY_RATE (ID);
alter table REF_CURRENCY
add constraint FK_REF_CURRENCY_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
-- Create/Recreate check constraints 
alter table REF_CURRENCY
add constraint CHECK_REF_CURRENCY_DLFL
check (DELFL in (0,1));


-- Create table
create table REF_POST
(
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  name_kz       VARCHAR2(250 CHAR),
  name_ru       VARCHAR2(250 CHAR) not null,
  name_en       VARCHAR2(250 CHAR),
  type_post     VARCHAR2(50 CHAR),
  is_activity   NUMBER(1) default 1 not null,
  is_main_ruk   NUMBER(1) default 0 not null,
  begin_date    DATE default sysdate not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_POST
is 'Справочник должностей';
-- Add comments to the columns 
comment on column REF_POST.id
is 'Идентификатор';
comment on column REF_POST.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_POST.code
is 'Код';
comment on column REF_POST.name_kz
is 'Наименование на казахском';
comment on column REF_POST.name_ru
is 'Наименование на русском';
comment on column REF_POST.name_en
is 'Наименование на английском';
comment on column REF_POST.type_post
is 'Тип должности';
comment on column REF_POST.is_activity
is 'Признак активность';
comment on column REF_POST.is_main_ruk
is 'Признак главный руководитель';
comment on column REF_POST.begin_date
is 'Дата начала действия';
comment on column REF_POST.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_POST.datlast
is 'Дата последнего редактирования';
comment on column REF_POST.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_POST.user_location
is 'Местоположение';
comment on column REF_POST.sent_knd
is 'Id таблицы Статус отправки';
comment on column REF_POST.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_POST
add constraint PK_REF_POST primary key (ID)
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
alter table REF_POST
add constraint FK_REF_POST_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_POST
add constraint FK_REF_POST_USR foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_POST
add constraint CHECK_REF_POST_DELFL
check (DELFL in (0,1));
alter table REF_POST
add constraint CHECK_REF_POST_IS_ACTIVITY
check (IS_ACTIVITY in (0,1));
alter table REF_POST
add constraint CHECK_REF_POST_IS_MAIN_RUK
check (IS_MAIN_RUK in (0,1));


-- Create table
create table REF_POST_HST
(
  id_hst        NUMBER(14) not null,
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  name_kz       VARCHAR2(250 CHAR),
  name_ru       VARCHAR2(250 CHAR) not null,
  name_en       VARCHAR2(250 CHAR),
  type_post     VARCHAR2(50 CHAR),
  is_activity   NUMBER(1) not null,
  is_main_ruk   NUMBER(1) not null,
  begin_date    DATE not null,
  delfl         NUMBER(1) not null,
  datlast       DATE not null,
  id_usr        NUMBER(18) not null,
  type_change   NUMBER(14) not null,
  user_location VARCHAR2(50 CHAR),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_POST_HST
is 'История справочника должностей';
-- Add comments to the columns 
comment on column REF_POST_HST.id_hst
is 'Идентификатор';
comment on column REF_POST_HST.id
is 'Id Таблицы REF_post';
comment on column REF_POST_HST.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_POST_HST.code
is 'Код';
comment on column REF_POST_HST.name_kz
is 'Наименование на казахском';
comment on column REF_POST_HST.name_ru
is 'Наименование на русском';
comment on column REF_POST_HST.name_en
is 'Наименование на английском';
comment on column REF_POST_HST.type_post
is 'Тип должности';
comment on column REF_POST_HST.is_activity
is 'Признак активность';
comment on column REF_POST_HST.is_main_ruk
is 'Признак главный руководитель';
comment on column REF_POST_HST.begin_date
is 'Дата начала действия';
comment on column REF_POST_HST.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_POST_HST.datlast
is 'Дата последнего редактирования';
comment on column REF_POST_HST.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_POST_HST.type_change
is 'Тип изменения(Добавление, Редактирование, Удаление)';
comment on column REF_POST_HST.user_location
is 'Местоположение';
comment on column REF_POST_HST.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_POST_HST.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_POST_HST
add constraint PK_REF_POST_HST primary key (ID_HST)
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
alter table REF_POST_HST
add constraint FK_REF_POST_HST_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_POST_HST
add constraint FK_REF_POST_HST_TC foreign key (TYPE_CHANGE)
references TYPE_CHANGE (TYPE_CHANGE);


-- Create table
create table REF_PERSON
(
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  idn           VARCHAR2(12 CHAR) not null,
  fm            VARCHAR2(50 CHAR) not null,
  nm            VARCHAR2(50 CHAR) not null,
  ft            VARCHAR2(50 CHAR),
  fio_kz        VARCHAR2(250 CHAR),
  fio_en        VARCHAR2(250 CHAR),
  ref_country   NUMBER(14) not null,
  phone_work    VARCHAR2(50 CHAR),
  fax           VARCHAR2(50 CHAR),
  address_work  VARCHAR2(200 CHAR) not null,
  note          VARCHAR2(200 CHAR),
  begin_date    DATE default sysdate not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_PERSON
is 'Справочник физических лиц';
-- Add comments to the columns 
comment on column REF_PERSON.id
is 'Id основной таблицы';
comment on column REF_PERSON.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_PERSON.code
is 'Код';
comment on column REF_PERSON.idn
is 'ИН';
comment on column REF_PERSON.fm
is 'Фамилия';
comment on column REF_PERSON.nm
is 'Имя';
comment on column REF_PERSON.ft
is 'Отчество';
comment on column REF_PERSON.fio_kz
is 'ФИО на казахском';
comment on column REF_PERSON.fio_en
is 'ФИО на английском';
comment on column REF_PERSON.ref_country
is 'Гражданство';
comment on column REF_PERSON.phone_work
is 'Рабочий телефон';
comment on column REF_PERSON.fax
is 'Факс';
comment on column REF_PERSON.address_work
is 'Рабочий адрес';
comment on column REF_PERSON.note
is 'Примечание';
comment on column REF_PERSON.begin_date
is 'Дата начала действия';
comment on column REF_PERSON.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_PERSON.datlast
is 'Дата последнего редактирования';
comment on column REF_PERSON.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_PERSON.user_location
is 'Местоположение';
comment on column REF_PERSON.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_PERSON.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_PERSON
add constraint PK_REF_PERSON primary key (ID)
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
alter table REF_PERSON
add constraint FK_REF_PERSON_CRY foreign key (REF_COUNTRY)
references REF_COUNTRY (ID);
alter table REF_PERSON
add constraint FK_REF_PERSON_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_PERSON
add constraint FK_REF_PERSON_USER foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_PERSON
add constraint CHECK_REF_PERSON_DELFL
check (DELFL in (0,1));


-- Create table
create table REF_PERSON_HST
(
  id_hst        NUMBER(14) not null,
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  idn           VARCHAR2(12 CHAR) not null,
  fm            VARCHAR2(50 CHAR) not null,
  nm            VARCHAR2(50 CHAR) not null,
  ft            VARCHAR2(50 CHAR),
  fio_kz        VARCHAR2(250 CHAR),
  fio_en        VARCHAR2(250 CHAR),
  ref_country   NUMBER(14) not null,
  phone_work    VARCHAR2(50 CHAR),
  fax           VARCHAR2(50 CHAR),
  address_work  VARCHAR2(200 CHAR) not null,
  note          VARCHAR2(200 CHAR),
  begin_date    DATE default sysdate not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  type_change   NUMBER(14) not null,
  user_location VARCHAR2(50 CHAR),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_PERSON_HST
is 'История справочника физических лиц';
-- Add comments to the columns 
comment on column REF_PERSON_HST.id_hst
is 'Идентификатор';
comment on column REF_PERSON_HST.id
is 'Id таблицы REF_PERSON';
comment on column REF_PERSON_HST.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_PERSON_HST.code
is 'Код';
comment on column REF_PERSON_HST.idn
is 'ИН';
comment on column REF_PERSON_HST.fm
is 'Фамилия';
comment on column REF_PERSON_HST.nm
is 'Имя';
comment on column REF_PERSON_HST.ft
is 'Отчество';
comment on column REF_PERSON_HST.fio_kz
is 'ФИО на казахском';
comment on column REF_PERSON_HST.fio_en
is 'ФИО на английском';
comment on column REF_PERSON_HST.ref_country
is 'Гражданство';
comment on column REF_PERSON_HST.phone_work
is 'Рабочий телефон';
comment on column REF_PERSON_HST.fax
is 'Факс';
comment on column REF_PERSON_HST.address_work
is 'Рабочий адрес';
comment on column REF_PERSON_HST.note
is 'Примечание';
comment on column REF_PERSON_HST.begin_date
is 'Дата начала действия';
comment on column REF_PERSON_HST.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_PERSON_HST.datlast
is 'Дата последнего редактирования';
comment on column REF_PERSON_HST.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_PERSON_HST.type_change
is 'Тип изменения(Добавление, Редактирование, Удаление)';
comment on column REF_PERSON_HST.user_location
is 'Местоположение';
comment on column REF_PERSON_HST.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_PERSON_HST.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_PERSON_HST
add constraint PK_REF_PERSON_HST primary key (ID_HST)
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
alter table REF_PERSON_HST
add constraint FK_REF_PERSON_HST_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_PERSON_HST
add constraint FK_REF_PERSON_HST_TC foreign key (TYPE_CHANGE)
references TYPE_CHANGE (TYPE_CHANGE);
alter table REF_PERSON_HST
add constraint FK_REF_PERSON_HST_USER foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_PERSON_HST
add constraint CHECK_REF_PERSON_HST_DELFL
check (DELFL in (0,1));



-- Create table
create table REF_REQUIREMENT
(
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  name_kz       VARCHAR2(250 CHAR),
  name_ru       VARCHAR2(250 CHAR) not null,
  name_en       VARCHAR2(250 CHAR),
  begin_date    DATE not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_REQUIREMENT
is 'Справочник требований и обязательств';
-- Add comments to the columns 
comment on column REF_REQUIREMENT.id
is 'Идентификатор';
comment on column REF_REQUIREMENT.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_REQUIREMENT.code
is 'Код';
comment on column REF_REQUIREMENT.name_kz
is 'Наименование на казахском языке';
comment on column REF_REQUIREMENT.name_ru
is 'Наименование на русском';
comment on column REF_REQUIREMENT.name_en
is 'Наименование на английском языке';
comment on column REF_REQUIREMENT.begin_date
is 'Дата начала действия';
comment on column REF_REQUIREMENT.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_REQUIREMENT.datlast
is 'Дата последнего редактирования';
comment on column REF_REQUIREMENT.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_REQUIREMENT.user_location
is 'Местоположение';
comment on column REF_REQUIREMENT.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_REQUIREMENT.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_REQUIREMENT
add constraint PK_REF_REQUIREMENT primary key (ID)
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
alter table REF_REQUIREMENT
add constraint FK_REF_REQUIREMENT_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_REQUIREMENT
add constraint FK_REF_REQUIREMENT_USER foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_REQUIREMENT
add constraint CHECK_REF_REQUIREMENT_DLFL
check (DELFL in (0,1));


-- Create table
create table REF_REQUIREMENT_HST
(
  id_hst        NUMBER(14) not null,
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  name_kz       VARCHAR2(250 CHAR),
  name_ru       VARCHAR2(250 CHAR) not null,
  name_en       VARCHAR2(250 CHAR),
  begin_date    DATE not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  type_change   NUMBER(14),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_REQUIREMENT_HST
is 'История справочника требований и обязательств';
-- Add comments to the columns 
comment on column REF_REQUIREMENT_HST.id_hst
is 'Идентификатор';
comment on column REF_REQUIREMENT_HST.id
is 'Идентификатор основной таблицы';
comment on column REF_REQUIREMENT_HST.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_REQUIREMENT_HST.code
is 'Код';
comment on column REF_REQUIREMENT_HST.name_kz
is 'Наименование на казахском языке';
comment on column REF_REQUIREMENT_HST.name_ru
is 'Наименование на русском языке';
comment on column REF_REQUIREMENT_HST.name_en
is 'Наименование на английском языке';
comment on column REF_REQUIREMENT_HST.begin_date
is 'Дата начала действия';
comment on column REF_REQUIREMENT_HST.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_REQUIREMENT_HST.datlast
is 'Дата последнего редактирования';
comment on column REF_REQUIREMENT_HST.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_REQUIREMENT_HST.user_location
is 'Местоположение';
comment on column REF_REQUIREMENT_HST.type_change
is 'Тип изменения(Добавление, Редактирование, Удаление)';
comment on column REF_REQUIREMENT_HST.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_REQUIREMENT_HST.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_REQUIREMENT_HST
add constraint PK_REF_REQUIREMENT_HST primary key (ID_HST)
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
alter table REF_REQUIREMENT_HST
add constraint FK_REF_REQUIREMENT_HST_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_REQUIREMENT_HST
add constraint FK_REF_REQUIREMENT_HST_TC foreign key (TYPE_CHANGE)
references TYPE_CHANGE (TYPE_CHANGE);
alter table REF_REQUIREMENT_HST
add constraint FK_REF_REQUIREMENT_HST_U foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_REQUIREMENT_HST
add constraint CHECK_REF_REQUIREMENT_HST_DLFL
check (DELFL in (0,1));


-- Create table
create table REF_TYPE_PROVIDE
(
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  name_kz       VARCHAR2(250 CHAR),
  name_ru       VARCHAR2(250 CHAR) not null,
  name_en       VARCHAR2(250 CHAR),
  begin_date    DATE not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_TYPE_PROVIDE
is 'Справочник видов обеспечения';
-- Add comments to the columns 
comment on column REF_TYPE_PROVIDE.id
is 'Идентификатор';
comment on column REF_TYPE_PROVIDE.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_TYPE_PROVIDE.code
is 'Код';
comment on column REF_TYPE_PROVIDE.name_kz
is 'Наименование на казахском языке';
comment on column REF_TYPE_PROVIDE.name_ru
is 'Наименование на русском';
comment on column REF_TYPE_PROVIDE.name_en
is 'Наименование на английском языке';
comment on column REF_TYPE_PROVIDE.begin_date
is 'Дата начала действия';
comment on column REF_TYPE_PROVIDE.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_TYPE_PROVIDE.datlast
is 'Дата последнего редактирования';
comment on column REF_TYPE_PROVIDE.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_TYPE_PROVIDE.user_location
is 'Местоположение';
comment on column REF_TYPE_PROVIDE.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_TYPE_PROVIDE.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_TYPE_PROVIDE
add constraint PK_REF_TYPE_PROVIDE primary key (ID)
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
alter table REF_TYPE_PROVIDE
add constraint FK_REF_TYPE_PROVIDE_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_TYPE_PROVIDE
add constraint FK_REF_TYPE_PROVIDE_USER foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_TYPE_PROVIDE
add constraint CHECK_REF_TYPE_PROVIDE_DLFL
check (DELFL in (0,1));

-- Create table
create table REF_TYPE_PROVIDE_HST
(
  id_hst        NUMBER(14) not null,
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  name_kz       VARCHAR2(250 CHAR),
  name_ru       VARCHAR2(250 CHAR) not null,
  name_en       VARCHAR2(250 CHAR),
  begin_date    DATE not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  type_change   NUMBER(14),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_TYPE_PROVIDE_HST
is 'История справочника видов обеспечения';
-- Add comments to the columns 
comment on column REF_TYPE_PROVIDE_HST.id_hst
is 'Идентификатор';
comment on column REF_TYPE_PROVIDE_HST.id
is 'Идентификатор основной таблицы';
comment on column REF_TYPE_PROVIDE_HST.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_TYPE_PROVIDE_HST.code
is 'Код';
comment on column REF_TYPE_PROVIDE_HST.name_kz
is 'Наименование на казахском языке';
comment on column REF_TYPE_PROVIDE_HST.name_ru
is 'Наименование на русском языке';
comment on column REF_TYPE_PROVIDE_HST.name_en
is 'Наименование на английском языке';
comment on column REF_TYPE_PROVIDE_HST.begin_date
is 'Дата начала действия';
comment on column REF_TYPE_PROVIDE_HST.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_TYPE_PROVIDE_HST.datlast
is 'Дата последнего редактирования';
comment on column REF_TYPE_PROVIDE_HST.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_TYPE_PROVIDE_HST.user_location
is 'Местоположение';
comment on column REF_TYPE_PROVIDE_HST.type_change
is 'Тип изменения(Добавление, Редактирование, Удаление)';
comment on column REF_TYPE_PROVIDE_HST.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_TYPE_PROVIDE_HST.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_TYPE_PROVIDE_HST
add constraint PK_REF_TYPE_PROVIDE_HST primary key (ID_HST)
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
alter table REF_TYPE_PROVIDE_HST
add constraint FK_REF_TYPE_PROVIDE_HST_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_TYPE_PROVIDE_HST
add constraint FK_REF_TYPE_PROVIDE_HST_TC foreign key (TYPE_CHANGE)
references TYPE_CHANGE (TYPE_CHANGE);
alter table REF_TYPE_PROVIDE_HST
add constraint FK_REF_TYPE_PROVIDE_HST_U foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_TYPE_PROVIDE_HST
add constraint CHECK_REF_TYPE_PROV_HST_DLFL
check (DELFL in (0,1));



-- Create table
create table REF_TRANS_TYPES
(
  id               NUMBER(14) not null,
  rec_id           NUMBER(14) not null,
  code             VARCHAR2(50 CHAR) not null,
  name_kz          VARCHAR2(250 CHAR),
  name_ru          VARCHAR2(250 CHAR) not null,
  name_en          VARCHAR2(250 CHAR),
  kind_of_activity VARCHAR2(250 CHAR) not null,
  short_name       VARCHAR2(150 CHAR),
  begin_date       DATE not null,
  delfl            NUMBER(1) default 0 not null,
  datlast          DATE default SYSDATE not null,
  id_usr           NUMBER(18) not null,
  user_location    VARCHAR2(50 CHAR),
  sent_knd         NUMBER(14) default 0 not null,
  entity_id        NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_TRANS_TYPES
is 'Справочник типов сделок';
-- Add comments to the columns 
comment on column REF_TRANS_TYPES.id
is 'Идентификатор';
comment on column REF_TRANS_TYPES.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_TRANS_TYPES.code
is 'Код';
comment on column REF_TRANS_TYPES.name_kz
is 'Наименование на казахском языке';
comment on column REF_TRANS_TYPES.name_ru
is 'Наименование на русском';
comment on column REF_TRANS_TYPES.name_en
is 'Наименование на английском языке';
comment on column REF_TRANS_TYPES.kind_of_activity
is 'Вид деятельности';
comment on column REF_TRANS_TYPES.short_name
is 'Краткое наименования';
comment on column REF_TRANS_TYPES.begin_date
is 'Дата начала действия';
comment on column REF_TRANS_TYPES.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_TRANS_TYPES.datlast
is 'Дата последнего редактирования';
comment on column REF_TRANS_TYPES.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_TRANS_TYPES.user_location
is 'Местоположение';
comment on column REF_TRANS_TYPES.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_TRANS_TYPES.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_TRANS_TYPES
add constraint PK_REF_TRANS_TYPES primary key (ID)
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
alter table REF_TRANS_TYPES
add constraint FK_REF_TRANS_TYPES_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_TRANS_TYPES
add constraint FK_REF_TRANS_TYPES_USER foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_TRANS_TYPES
add constraint CHECK_REF_TRANS_TYPES_DLFL
check (DELFL in (0,1));



-- Create table
create table REF_TRANS_TYPES_HST
(
  id_hst           NUMBER(14) not null,
  id               NUMBER(14) not null,
  rec_id           NUMBER(14) not null,
  code             VARCHAR2(50 CHAR) not null,
  name_kz          VARCHAR2(250 CHAR),
  name_ru          VARCHAR2(250 CHAR) not null,
  name_en          VARCHAR2(250 CHAR),
  kind_of_activity VARCHAR2(50 CHAR) not null,
  short_name       VARCHAR2(50 CHAR),
  begin_date       DATE not null,
  delfl            NUMBER(1) default 0 not null,
  datlast          DATE default SYSDATE not null,
  id_usr           NUMBER(18) not null,
  user_location    VARCHAR2(50 CHAR),
  type_change      NUMBER(14),
  sent_knd         NUMBER(14) default 0 not null,
  entity_id        NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_TRANS_TYPES_HST
is 'История справочника видов обеспечения';
-- Add comments to the columns 
comment on column REF_TRANS_TYPES_HST.id_hst
is 'Идентификатор';
comment on column REF_TRANS_TYPES_HST.id
is 'Идентификатор основной таблицы';
comment on column REF_TRANS_TYPES_HST.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_TRANS_TYPES_HST.code
is 'Код';
comment on column REF_TRANS_TYPES_HST.name_kz
is 'Наименование на казахском языке';
comment on column REF_TRANS_TYPES_HST.name_ru
is 'Наименование на русском языке';
comment on column REF_TRANS_TYPES_HST.name_en
is 'Наименование на английском языке';
comment on column REF_TRANS_TYPES_HST.kind_of_activity
is 'Вид деятельности';
comment on column REF_TRANS_TYPES_HST.short_name
is 'Краткое наименования';
comment on column REF_TRANS_TYPES_HST.begin_date
is 'Дата начала действия';
comment on column REF_TRANS_TYPES_HST.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_TRANS_TYPES_HST.datlast
is 'Дата последнего редактирования';
comment on column REF_TRANS_TYPES_HST.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_TRANS_TYPES_HST.user_location
is 'Местоположение';
comment on column REF_TRANS_TYPES_HST.type_change
is 'Тип изменения(Добавление, Редактирование, Удаление)';
comment on column REF_TRANS_TYPES_HST.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_TRANS_TYPES_HST.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_TRANS_TYPES_HST
add constraint PK_REF_TRANS_TYPES_HST primary key (ID_HST)
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
alter table REF_TRANS_TYPES_HST
add constraint FK_REF_TRANS_TYPES_HST_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_TRANS_TYPES_HST
add constraint FK_REF_TRANS_TYPES_HST_TC foreign key (TYPE_CHANGE)
references TYPE_CHANGE (TYPE_CHANGE);
alter table REF_TRANS_TYPES_HST
add constraint FK_REF_TRANS_TYPES_HST_U foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_TRANS_TYPES_HST
add constraint CHECK_REF_TRANS_TYPES_HST_DLFL
check (DELFL in (0,1));


-- Create table
create table REF_BALANCE_ACCOUNT
(
  id            NUMBER not null,
  rec_id        NUMBER(14) default 0 not null,
  level_code    VARCHAR2(10 CHAR),
  code          VARCHAR2(50 CHAR) not null,
  parent_code   VARCHAR2(50 CHAR),
  name_kz       VARCHAR2(250 CHAR),
  name_ru       VARCHAR2(250 CHAR),
  name_en       VARCHAR2(250 CHAR),
  begin_date    DATE not null,
  end_date      DATE,
  delfl         NUMBER(1) default 0,
  datlast       DATE default SYSDATE,
  id_usr        NUMBER(18),
  user_location VARCHAR2(50 CHAR),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
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
-- Add comments to the columns 
comment on column REF_BALANCE_ACCOUNT.id
is 'Id Таблицы';
comment on column REF_BALANCE_ACCOUNT.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_BALANCE_ACCOUNT.level_code
is 'Код уровня';
comment on column REF_BALANCE_ACCOUNT.code
is 'Код счета';
comment on column REF_BALANCE_ACCOUNT.parent_code
is 'Код родительского счета';
comment on column REF_BALANCE_ACCOUNT.name_kz
is 'Наименование на каз';
comment on column REF_BALANCE_ACCOUNT.name_ru
is 'Наименование на рус';
comment on column REF_BALANCE_ACCOUNT.name_en
is 'Наименование на англ';
comment on column REF_BALANCE_ACCOUNT.begin_date
is 'Дата начала действия записи';
comment on column REF_BALANCE_ACCOUNT.end_date
is 'Дата окончания ';
comment on column REF_BALANCE_ACCOUNT.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_BALANCE_ACCOUNT.datlast
is 'Дата последнего редактирования';
comment on column REF_BALANCE_ACCOUNT.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_BALANCE_ACCOUNT.user_location
is 'Местоположение';
comment on column REF_BALANCE_ACCOUNT.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_BALANCE_ACCOUNT.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate indexes 
create index IDX_REF_BALANCE_ACCOUNT_CODE on REF_BALANCE_ACCOUNT (CODE)
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
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_BALANCE_ACCOUNT
add constraint PK_REF_BALANCE_ACC primary key (ID)
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
alter table REF_BALANCE_ACCOUNT
add constraint FK_REF_BALANCE_ACC_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_BALANCE_ACCOUNT
add constraint FK_REF_BALANCE_ACC_USER foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_BALANCE_ACCOUNT
add constraint CHECK_REF_BALANCE_ACCOUNT_DLFL
check (DELFL in (0,1));


-- Create table
create table REF_BALANCE_ACCOUNT_HST
(
  id_hst        NUMBER(14) not null,
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  parent_code   VARCHAR2(50 CHAR),
  level_code    VARCHAR2(10 CHAR),
  name_kz       VARCHAR2(250 CHAR),
  name_ru       VARCHAR2(250 CHAR) not null,
  name_en       VARCHAR2(250 CHAR),
  begin_date    DATE not null,
  end_date      DATE,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  type_change   NUMBER(14),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_BALANCE_ACCOUNT_HST
is 'История справочника балансовых счетов для отчетов о сделках';
-- Add comments to the columns 
comment on column REF_BALANCE_ACCOUNT_HST.id_hst
is 'Идентификатор';
comment on column REF_BALANCE_ACCOUNT_HST.id
is 'Идентификатор основной таблицы';
comment on column REF_BALANCE_ACCOUNT_HST.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_BALANCE_ACCOUNT_HST.code
is 'Код';
comment on column REF_BALANCE_ACCOUNT_HST.parent_code
is 'Код родительского счета';
comment on column REF_BALANCE_ACCOUNT_HST.level_code
is 'Код уровня';
comment on column REF_BALANCE_ACCOUNT_HST.name_kz
is 'Наименование на казахском языке';
comment on column REF_BALANCE_ACCOUNT_HST.name_ru
is 'Наименование на русском языке';
comment on column REF_BALANCE_ACCOUNT_HST.name_en
is 'Наименование на английском языке';
comment on column REF_BALANCE_ACCOUNT_HST.begin_date
is 'Дата начала действия';
comment on column REF_BALANCE_ACCOUNT_HST.end_date
is 'Дата окончания';
comment on column REF_BALANCE_ACCOUNT_HST.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_BALANCE_ACCOUNT_HST.datlast
is 'Дата последнего редактирования';
comment on column REF_BALANCE_ACCOUNT_HST.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_BALANCE_ACCOUNT_HST.user_location
is 'Местоположение';
comment on column REF_BALANCE_ACCOUNT_HST.type_change
is 'Тип изменения(Добавление, Редактирование, Удаление)';
comment on column REF_BALANCE_ACCOUNT_HST.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_BALANCE_ACCOUNT_HST.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_BALANCE_ACCOUNT_HST
add constraint PK_REF_BALANCE_ACCOUNT_HST primary key (ID_HST)
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
alter table REF_BALANCE_ACCOUNT_HST
add constraint FK_REF_BALANCE_ACCOUNT_HST_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_BALANCE_ACCOUNT_HST
add constraint FK_REF_BALANCE_ACCOUNT_HST_TC foreign key (TYPE_CHANGE)
references TYPE_CHANGE (TYPE_CHANGE);
alter table REF_BALANCE_ACCOUNT_HST
add constraint FK_REF_BALANCE_ACCOUNT_HST_U foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_BALANCE_ACCOUNT_HST
add constraint CHECK_REF_BAL_ACCOUNT_HST_DLFL
check (DELFL in (0,1));


-- Create table
create table REF_CONN_ORG
(
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  name_kz       VARCHAR2(500 CHAR),
  name_ru       VARCHAR2(500 CHAR) not null,
  name_en       VARCHAR2(500 CHAR),
  short_name    VARCHAR2(400 CHAR),
  begin_date    DATE not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_CONN_ORG
is 'Справочник признаков связанности с подотчетной орг-й особыми отношениями';
-- Add comments to the columns 
comment on column REF_CONN_ORG.id
is 'Идентификатор';
comment on column REF_CONN_ORG.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_CONN_ORG.code
is 'Код';
comment on column REF_CONN_ORG.name_kz
is 'Наименование на казахском языке';
comment on column REF_CONN_ORG.name_ru
is 'Наименование на русском';
comment on column REF_CONN_ORG.name_en
is 'Наименование на английском языке';
comment on column REF_CONN_ORG.short_name
is 'Краткое наименования';
comment on column REF_CONN_ORG.begin_date
is 'Дата начала действия';
comment on column REF_CONN_ORG.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_CONN_ORG.datlast
is 'Дата последнего редактирования';
comment on column REF_CONN_ORG.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_CONN_ORG.user_location
is 'Местоположение';
comment on column REF_CONN_ORG.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_CONN_ORG.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_CONN_ORG
add constraint PK_REF_CONN_ORG primary key (ID)
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
alter table REF_CONN_ORG
add constraint FK_REF_CONN_ORG_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_CONN_ORG
add constraint FK_REF_CONN_ORG_USER foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_CONN_ORG
add constraint CHECK_REF_CONN_ORG_DLFL
check (DELFL in (0,1));


-- Create table
create table REF_CONN_ORG_HST
(
  id_hst        NUMBER(14) not null,
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  name_kz       VARCHAR2(500 CHAR),
  name_ru       VARCHAR2(500 CHAR) not null,
  name_en       VARCHAR2(500 CHAR),
  short_name    VARCHAR2(400 CHAR),
  begin_date    DATE not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  type_change   NUMBER(14),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_CONN_ORG_HST
is 'История справочника признаков связанности с подотчетной орг-й особоыми отношениями';
-- Add comments to the columns 
comment on column REF_CONN_ORG_HST.id_hst
is 'Идентификатор';
comment on column REF_CONN_ORG_HST.id
is 'Идентификатор основной таблицы';
comment on column REF_CONN_ORG_HST.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_CONN_ORG_HST.code
is 'Код';
comment on column REF_CONN_ORG_HST.name_kz
is 'Наименование на казахском языке';
comment on column REF_CONN_ORG_HST.name_ru
is 'Наименование на русском языке';
comment on column REF_CONN_ORG_HST.name_en
is 'Наименование на английском языке';
comment on column REF_CONN_ORG_HST.short_name
is 'Краткое наименования';
comment on column REF_CONN_ORG_HST.begin_date
is 'Дата начала действия';
comment on column REF_CONN_ORG_HST.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_CONN_ORG_HST.datlast
is 'Дата последнего редактирования';
comment on column REF_CONN_ORG_HST.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_CONN_ORG_HST.user_location
is 'Местоположение';
comment on column REF_CONN_ORG_HST.type_change
is 'Тип изменения(Добавление, Редактирование, Удаление)';
comment on column REF_CONN_ORG_HST.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_CONN_ORG_HST.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_CONN_ORG_HST
add constraint PK_REF_CONN_ORG_HST primary key (ID_HST)
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
alter table REF_CONN_ORG_HST
add constraint FK_REF_CONN_ORG_HST_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_CONN_ORG_HST
add constraint FK_REF_CONN_ORG_HST_TC foreign key (TYPE_CHANGE)
references TYPE_CHANGE (TYPE_CHANGE);
alter table REF_CONN_ORG_HST
add constraint FK_REF_CONN_ORG_HST_U foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_CONN_ORG_HST
add constraint CHECK_REF_CONN_ORG_HST_DLFL
check (DELFL in (0,1));



-- Create table
create table REF_DEPARTMENT
(
  id            NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  rec_id        NUMBER(14) not null,
  name_kz       VARCHAR2(250 CHAR),
  name_ru       VARCHAR2(250 CHAR) not null,
  name_en       VARCHAR2(250 CHAR),
  begin_date    DATE not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_DEPARTMENT
is 'Справочник подразделений НБ РК';
-- Add comments to the columns 
comment on column REF_DEPARTMENT.id
is 'Идентификатор';
comment on column REF_DEPARTMENT.code
is 'Код';
comment on column REF_DEPARTMENT.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_DEPARTMENT.name_kz
is 'Наименование на казахском языке';
comment on column REF_DEPARTMENT.name_ru
is 'Наименование на русском';
comment on column REF_DEPARTMENT.name_en
is 'Наименование на английском языке';
comment on column REF_DEPARTMENT.begin_date
is 'Дата начала действия';
comment on column REF_DEPARTMENT.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_DEPARTMENT.datlast
is 'Дата последнего редактирования';
comment on column REF_DEPARTMENT.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_DEPARTMENT.user_location
is 'Местоположение';
comment on column REF_DEPARTMENT.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_DEPARTMENT.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_DEPARTMENT
add constraint PK_REF_DEPARTMENT primary key (ID)
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
alter table REF_DEPARTMENT
add constraint FK_REF_DEPARMENT_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_DEPARTMENT
add constraint FK_REF_DEPARTMENT_USER foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_DEPARTMENT
add constraint CHECK_REF_DEPARTMENT_DLFL
check (DELFL in (0,1));


-- Create table
create table REF_BANK
(
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  bic           VARCHAR2(50 CHAR),
  bic_head      VARCHAR2(50 CHAR),
  bic_nbrk      VARCHAR2(50 CHAR),
  name_kz       VARCHAR2(250 CHAR),
  name_ru       VARCHAR2(250 CHAR),
  name_en       VARCHAR2(250 CHAR),
  idn           VARCHAR2(12 CHAR),
  post_address  VARCHAR2(300 CHAR),
  phone_num     VARCHAR2(100 CHAR),
  begin_date    DATE default sysdate not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_BANK
is 'Справочник банков второго уровня';
-- Add comments to the columns 
comment on column REF_BANK.id
is 'Идентификатор';
comment on column REF_BANK.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_BANK.code
is 'Код';
comment on column REF_BANK.bic
is 'БИК';
comment on column REF_BANK.bic_head
is 'Головной БИК';
comment on column REF_BANK.bic_nbrk
is 'БИК НБ РК';
comment on column REF_BANK.name_kz
is 'Наименование на казахском';
comment on column REF_BANK.name_ru
is 'Наименование на русском';
comment on column REF_BANK.name_en
is 'Наименование на английском';
comment on column REF_BANK.idn
is 'ИН';
comment on column REF_BANK.post_address
is 'Адрес';
comment on column REF_BANK.phone_num
is 'Номер телефона';
comment on column REF_BANK.begin_date
is 'Дата начала действия';
comment on column REF_BANK.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_BANK.datlast
is 'Дата последнего редактирования';
comment on column REF_BANK.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_BANK.user_location
is 'Местоположение';
comment on column REF_BANK.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_BANK.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_BANK
add constraint PK_REF_BANK primary key (ID)
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
alter table REF_BANK
add constraint FK_REF_BANK foreign key (ID_USR)
references F_USERS (USER_ID);
alter table REF_BANK
add constraint FK_REF_BANK_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
-- Create/Recreate check constraints 
alter table REF_BANK
add constraint CHECK_REF_BANK_DELFL
check (DELFL in (0,1));


-- Create table
create table REF_ISSUERS
(
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(64 CHAR),
  name_kz       VARCHAR2(1000 CHAR),
  name_en       VARCHAR2(1000 CHAR),
  name_ru       VARCHAR2(1000 CHAR) not null,
  begin_date    DATE not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_ISSUERS
is 'Справочник эмитентов';
-- Add comments to the columns 
comment on column REF_ISSUERS.id
is 'Идентификатор';
comment on column REF_ISSUERS.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_ISSUERS.code
is 'Код';
comment on column REF_ISSUERS.name_kz
is 'Наименование на казахском языке';
comment on column REF_ISSUERS.name_en
is 'Наименование на английском языке';
comment on column REF_ISSUERS.name_ru
is 'Наименование на русском';
comment on column REF_ISSUERS.begin_date
is 'Дата начала действия';
comment on column REF_ISSUERS.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_ISSUERS.datlast
is 'Дата последнего редактирования';
comment on column REF_ISSUERS.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_ISSUERS.user_location
is 'Местоположение';
comment on column REF_ISSUERS.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_ISSUERS.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_ISSUERS
add constraint PK_REF_ISSUERS primary key (ID)
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
alter table REF_ISSUERS
add constraint FK_REF_ISSUERS foreign key (ID_USR)
references F_USERS (USER_ID);
alter table REF_ISSUERS
add constraint FK_REF_ISSUERS_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
-- Create/Recreate check constraints 
alter table REF_ISSUERS
add constraint CHECK_REF_ISSUERS
check (DELFL in (0,1));


-- Create table
create table REF_SECURITIES
(
  id                   NUMBER(14) not null,
  rec_id               NUMBER(14) not null,
  code                 VARCHAR2(64 CHAR) not null,
  s_issuer             NUMBER,
  issuer_name          VARCHAR2(256 CHAR),
  s_g_issuer_sign      NUMBER,
  sign_code            VARCHAR2(256 CHAR),
  sign_name            VARCHAR2(256 CHAR),
  is_resident          NUMBER(1),
  is_state             NUMBER(1),
  nominal_value        NUMBER,
  nin                  VARCHAR2(50 CHAR),
  circul_date          DATE,
  maturity_date        DATE,
  security_cnt         NUMBER,
  s_g_security_variety NUMBER,
  variety_code         VARCHAR2(256 CHAR),
  variety_name         VARCHAR2(256 CHAR),
  s_g_security_type    NUMBER,
  type_code            VARCHAR2(256 CHAR),
  type_name            VARCHAR2(256 CHAR),
  nominal_currency     NUMBER,
  currency_code        VARCHAR2(256 CHAR),
  currency_name        VARCHAR2(256 CHAR),
  name_kz              VARCHAR2(256 CHAR),
  name_en              VARCHAR2(256 CHAR),
  name_ru              VARCHAR2(256 CHAR),
  begin_date           DATE not null,
  delfl                NUMBER(1) default 0 not null,
  datlast              DATE default SYSDATE not null,
  id_usr               NUMBER(18) not null,
  user_location        VARCHAR2(50 CHAR),
  sent_knd             NUMBER(14) default 0 not null,
  entity_id            NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_SECURITIES
is 'Справочник Ценных бумаг';
-- Add comments to the columns 
comment on column REF_SECURITIES.id
is 'Идентификатор';
comment on column REF_SECURITIES.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_SECURITIES.code
is 'Код';
comment on column REF_SECURITIES.s_issuer
is 'Id Эмитента ref_issuers';
comment on column REF_SECURITIES.issuer_name
is 'Наименование Эмитента';
comment on column REF_SECURITIES.s_g_issuer_sign
is 'Id Справочника признак Эмитента';
comment on column REF_SECURITIES.sign_code
is 'Вид Эмитента(Код)';
comment on column REF_SECURITIES.sign_name
is 'Вид Эмитента(Наименование)';
comment on column REF_SECURITIES.is_resident
is 'Признак резидент';
comment on column REF_SECURITIES.is_state
is 'Признак государсвтенный';
comment on column REF_SECURITIES.nominal_value
is 'Номинальная стоимость';
comment on column REF_SECURITIES.nin
is 'НИН';
comment on column REF_SECURITIES.circul_date
is 'Дата начала обращения';
comment on column REF_SECURITIES.maturity_date
is 'Дата погашения';
comment on column REF_SECURITIES.security_cnt
is 'Кол-во ЦБ';
comment on column REF_SECURITIES.s_g_security_variety
is 'Id справочника видов ЦБ';
comment on column REF_SECURITIES.variety_code
is 'Вид ЦБ (Код)';
comment on column REF_SECURITIES.variety_name
is 'Вид ЦБ (Наименование)';
comment on column REF_SECURITIES.s_g_security_type
is 'Id справочника типов ЦБ';
comment on column REF_SECURITIES.type_code
is 'Тип ЦБ (Код)';
comment on column REF_SECURITIES.type_name
is 'Тип ЦБ (Наименование)';
comment on column REF_SECURITIES.nominal_currency
is 'Id справочника валюты';
comment on column REF_SECURITIES.currency_code
is 'Код валюты';
comment on column REF_SECURITIES.currency_name
is 'Валюта';
comment on column REF_SECURITIES.name_kz
is 'Наименование на казахском языке';
comment on column REF_SECURITIES.name_en
is 'Наименование на английском языке';
comment on column REF_SECURITIES.name_ru
is 'Наименование на русском';
comment on column REF_SECURITIES.begin_date
is 'Дата начала действия';
comment on column REF_SECURITIES.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_SECURITIES.datlast
is 'Дата последнего редактирования';
comment on column REF_SECURITIES.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_SECURITIES.user_location
is 'Местоположение';
comment on column REF_SECURITIES.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_SECURITIES.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_SECURITIES
add constraint PK_REF_SECURITIES primary key (ID)
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
alter table REF_SECURITIES
add constraint FK_REF_SECURITIES foreign key (ID_USR)
references F_USERS (USER_ID);
alter table REF_SECURITIES
add constraint FK_REF_SECURITIES_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
-- Create/Recreate check constraints 
alter table REF_SECURITIES
add constraint CHECK_REF_SECURITIES
check (DELFL in (0,1));


-- Create table
create table REF_VID_OPER
(
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  name_kz       VARCHAR2(250 CHAR),
  name_ru       VARCHAR2(250 CHAR) not null,
  name_en       VARCHAR2(250 CHAR),
  begin_date    DATE not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_VID_OPER
is 'Справочник видов операций';
-- Add comments to the columns 
comment on column REF_VID_OPER.id
is 'Идентификатор';
comment on column REF_VID_OPER.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_VID_OPER.code
is 'Код';
comment on column REF_VID_OPER.name_kz
is 'Наименование на казахском языке';
comment on column REF_VID_OPER.name_ru
is 'Наименование на русском';
comment on column REF_VID_OPER.name_en
is 'Наименование на английском языке';
comment on column REF_VID_OPER.begin_date
is 'Дата начала действия';
comment on column REF_VID_OPER.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_VID_OPER.datlast
is 'Дата последнего редактирования';
comment on column REF_VID_OPER.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_VID_OPER.user_location
is 'Местоположение';
comment on column REF_VID_OPER.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_VID_OPER.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_VID_OPER
add constraint PK_REF_VID_OPER primary key (ID)
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
alter table REF_VID_OPER
add constraint FK_REF_VID_OPER_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_VID_OPER
add constraint FK_REF_VID_OPER_USER foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_VID_OPER
add constraint CHECK_REF_VID_OPER_DLFL
check (DELFL in (0,1));


-- Create table
create table REF_VID_OPER_HST
(
  id_hst        NUMBER(14) not null,
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  name_kz       VARCHAR2(250 CHAR),
  name_ru       VARCHAR2(250 CHAR) not null,
  name_en       VARCHAR2(250 CHAR),
  begin_date    DATE not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  type_change   NUMBER(14),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_VID_OPER_HST
is 'История справочника видов операций';
-- Add comments to the columns 
comment on column REF_VID_OPER_HST.id_hst
is 'Идентификатор';
comment on column REF_VID_OPER_HST.id
is 'Идентификатор основной таблицы';
comment on column REF_VID_OPER_HST.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_VID_OPER_HST.code
is 'Код';
comment on column REF_VID_OPER_HST.name_kz
is 'Наименование на казахском языке';
comment on column REF_VID_OPER_HST.name_ru
is 'Наименование на русском языке';
comment on column REF_VID_OPER_HST.name_en
is 'Наименование на английском языке';
comment on column REF_VID_OPER_HST.begin_date
is 'Дата начала действия';
comment on column REF_VID_OPER_HST.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_VID_OPER_HST.datlast
is 'Дата последнего редактирования';
comment on column REF_VID_OPER_HST.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_VID_OPER_HST.user_location
is 'Местоположение';
comment on column REF_VID_OPER_HST.type_change
is 'Тип изменения(Добавление, Редактирование, Удаление)';
comment on column REF_VID_OPER_HST.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_VID_OPER_HST.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_VID_OPER_HST
add constraint PK_REF_VID_OPER_HST primary key (ID_HST)
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
alter table REF_VID_OPER_HST
add constraint FK_REF_VID_OPER_HST_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_VID_OPER_HST
add constraint FK_REF_VID_OPER_HST_TC foreign key (TYPE_CHANGE)
references TYPE_CHANGE (TYPE_CHANGE);
alter table REF_VID_OPER_HST
add constraint FK_REF_VID_OPER_HST_U foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_VID_OPER_HST
add constraint CHECK_REF_VID_OPER_HST_DLFL
check (DELFL in (0,1));


-- Create table
create table REF_BRANCH
(
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  name_kz       VARCHAR2(250 CHAR),
  name_ru       VARCHAR2(250 CHAR) not null,
  name_en       VARCHAR2(250 CHAR),
  begin_date    DATE not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_BRANCH
is 'Справочник отраслей';
-- Add comments to the columns 
comment on column REF_BRANCH.id
is 'Идентификатор';
comment on column REF_BRANCH.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_BRANCH.code
is 'Код';
comment on column REF_BRANCH.name_kz
is 'Наименование на казахском языке';
comment on column REF_BRANCH.name_ru
is 'Наименование на русском';
comment on column REF_BRANCH.name_en
is 'Наименование на английском языке';
comment on column REF_BRANCH.begin_date
is 'Дата начала действия';
comment on column REF_BRANCH.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_BRANCH.datlast
is 'Дата последнего редактирования';
comment on column REF_BRANCH.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_BRANCH.user_location
is 'Местоположение';
comment on column REF_BRANCH.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_BRANCH.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_BRANCH
add constraint PK_REF_BRANCH primary key (ID)
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
alter table REF_BRANCH
add constraint FK_REF_BRANCH_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_BRANCH
add constraint FK_REF_BRANCH_USER foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_BRANCH
add constraint CHECK_REF_BRANCH_DLFL
check (DELFL in (0,1));


-- Create table
create table REF_BRANCH_HST
(
  id_hst        NUMBER(14) not null,
  id            NUMBER(14) not null,
  rec_id        NUMBER(14) not null,
  code          VARCHAR2(50 CHAR) not null,
  name_kz       VARCHAR2(250 CHAR),
  name_ru       VARCHAR2(250 CHAR) not null,
  name_en       VARCHAR2(250 CHAR),
  begin_date    DATE not null,
  delfl         NUMBER(1) default 0 not null,
  datlast       DATE default SYSDATE not null,
  id_usr        NUMBER(18) not null,
  user_location VARCHAR2(50 CHAR),
  type_change   NUMBER(14),
  sent_knd      NUMBER(14) default 0 not null,
  entity_id     NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_BRANCH_HST
is 'История справочника отраслей';
-- Add comments to the columns 
comment on column REF_BRANCH_HST.id_hst
is 'Идентификатор';
comment on column REF_BRANCH_HST.id
is 'Идентификатор основной таблицы';
comment on column REF_BRANCH_HST.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_BRANCH_HST.code
is 'Код';
comment on column REF_BRANCH_HST.name_kz
is 'Наименование на казахском языке';
comment on column REF_BRANCH_HST.name_ru
is 'Наименование на русском языке';
comment on column REF_BRANCH_HST.name_en
is 'Наименование на английском языке';
comment on column REF_BRANCH_HST.begin_date
is 'Дата начала действия';
comment on column REF_BRANCH_HST.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_BRANCH_HST.datlast
is 'Дата последнего редактирования';
comment on column REF_BRANCH_HST.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_BRANCH_HST.user_location
is 'Местоположение';
comment on column REF_BRANCH_HST.type_change
is 'Тип изменения(Добавление, Редактирование, Удаление)';
comment on column REF_BRANCH_HST.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_BRANCH_HST.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_BRANCH_HST
add constraint PK_REF_BRANCH_HST primary key (ID_HST)
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
alter table REF_BRANCH_HST
add constraint FK_REF_BRANCH_HST_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_BRANCH_HST
add constraint FK_REF_BRANCH_HST_TC foreign key (TYPE_CHANGE)
references TYPE_CHANGE (TYPE_CHANGE);
alter table REF_BRANCH_HST
add constraint FK_REF_BRANCH_HST_U foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_BRANCH_HST
add constraint CHECK_REF_BRANCH_HST_DLFL
check (DELFL in (0,1));


-- Create table
create table REF_CROSSCHECK
(
  id              NUMBER(14) not null,
  rec_id          NUMBER(14) not null,
  formula         VARCHAR2(1500 CHAR) not null,
  form_code       VARCHAR2(50 CHAR) not null,
  descr_rus       VARCHAR2(300 CHAR) not null,
  crosscheck_type NUMBER(1) default 1 not null,
  begin_date      DATE not null,
  end_date        DATE,
  delfl           NUMBER(1) default 0 not null,
  datlast         DATE default SYSDATE not null,
  id_usr          NUMBER(18) not null,
  user_location   VARCHAR2(50 CHAR),
  sent_knd        NUMBER(1)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_CROSSCHECK
is 'Справочник меж/внутр форменных контролей';
-- Add comments to the columns 
comment on column REF_CROSSCHECK.id
is 'Идентификатор';
comment on column REF_CROSSCHECK.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_CROSSCHECK.formula
is 'Формула';
comment on column REF_CROSSCHECK.form_code
is 'Наименование формы';
comment on column REF_CROSSCHECK.descr_rus
is 'Описание на русском';
comment on column REF_CROSSCHECK.crosscheck_type
is 'Id таблицы Тип контроля';
comment on column REF_CROSSCHECK.begin_date
is 'Дата начала действия';
comment on column REF_CROSSCHECK.end_date
is 'Дата окончания';
comment on column REF_CROSSCHECK.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_CROSSCHECK.datlast
is 'Дата последнего редактирования';
comment on column REF_CROSSCHECK.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_CROSSCHECK.user_location
is 'Местоположение';
comment on column REF_CROSSCHECK.sent_knd
is 'Статус отправки';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_CROSSCHECK
add constraint PK_REF_CROSSCHECK primary key (ID)
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
alter table REF_CROSSCHECK
add constraint FK_REF_CROSSCHECK_TYPE foreign key (CROSSCHECK_TYPE)
references CROSSCHECK_TYPE (ID);
alter table REF_CROSSCHECK
add constraint FK_REF_CROSSCHECK_USER foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_CROSSCHECK
add constraint CHECK_REF_CROSSCHECK_DLFL
check (DELFL in (0,1));







-- Create table
create table REF_CROSSCHECK_HST
(
  ID_HST          NUMBER(14) not null,
  ID              NUMBER(14) not null,
  REC_ID          NUMBER(14) not null,
  FORMULA         VARCHAR2(1000 CHAR) not null,
  FORM_CODE       VARCHAR2(50 CHAR) not null,
  DESCR_RUS       VARCHAR2(300 CHAR) not null,
  CROSSCHECK_TYPE NUMBER(1) default 1 not null,
  BEGIN_DATE      DATE not null,
  END_DATE        DATE,
  DELFL           NUMBER(1) default 0 not null,
  DATLAST         DATE default SYSDATE not null,
  ID_USR          NUMBER(18) not null,
  USER_LOCATION   VARCHAR2(50 CHAR),
  TYPE_CHANGE     NUMBER(14)
)
tablespace USERS
pctfree 10
pctused 40
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_CROSSCHECK_HST
is 'История справочника межформенного контроля';
-- Add comments to the columns 
comment on column REF_CROSSCHECK_HST.ID_HST
is 'Идентификатор';
comment on column REF_CROSSCHECK_HST.ID
is 'Идентификатор основной таблицы';
comment on column REF_CROSSCHECK_HST.REC_ID
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_CROSSCHECK_HST.FORMULA
is 'Формула ';
comment on column REF_CROSSCHECK_HST.FORM_CODE
is 'Наименование формы';
comment on column REF_CROSSCHECK_HST.DESCR_RUS
is 'Описание на русском';
comment on column REF_CROSSCHECK_HST.CROSSCHECK_TYPE
is 'Id таблицы Тип контроля';
comment on column REF_CROSSCHECK_HST.BEGIN_DATE
is 'Дата начала действия';
comment on column REF_CROSSCHECK_HST.END_DATE
is 'Дата окончания';
comment on column REF_CROSSCHECK_HST.DELFL
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_CROSSCHECK_HST.DATLAST
is 'Дата последнего редактирования';
comment on column REF_CROSSCHECK_HST.ID_USR
is 'Исполнитель, редактировавший данные';
comment on column REF_CROSSCHECK_HST.USER_LOCATION
is 'Местоположение';
comment on column REF_CROSSCHECK_HST.TYPE_CHANGE
is 'Тип изменения(Добавление, Редактирование, Удаление)';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_CROSSCHECK_HST
add constraint PK_REF_CROSSCHECK_HST primary key (ID_HST)
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
alter table REF_CROSSCHECK_HST
add constraint FK_REF_CROSSCHECK_HST_ID foreign key (ID)
references REF_CROSSCHECK (ID);
alter table REF_CROSSCHECK_HST
add constraint FK_REF_CROSSCHECK_HST_TC foreign key (TYPE_CHANGE)
references TYPE_CHANGE (TYPE_CHANGE);
alter table REF_CROSSCHECK_HST
add constraint FK_REF_CROSSCHECK_HST_TYPE foreign key (CROSSCHECK_TYPE)
references CROSSCHECK_TYPE (ID);
alter table REF_CROSSCHECK_HST
add constraint FK_REF_CROSSCHECK_HST_U foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_CROSSCHECK_HST
add constraint CHECK_REF_CROSSCHECK_HST_DLFL
check (DELFL in (0,1));




-- Create table
create table REF_REPORTS_RULES
(
  id                  NUMBER(14) not null,
  rec_id              NUMBER(14) not null,
  code                VARCHAR2(50 CHAR) not null,
  name_kz             VARCHAR2(250 CHAR),
  name_ru             VARCHAR2(250 CHAR) not null,
  name_en             VARCHAR2(250 CHAR),
  formname            VARCHAR2(50 CHAR),
  fieldname           VARCHAR2(50 CHAR),
  formula             VARCHAR2(1000 CHAR),
  is_calc_other_field NUMBER(1) default 0 not null,
  coeff               NUMBER,
  condition           VARCHAR2(50 CHAR),
  begin_date          DATE not null,
  delfl               NUMBER(1) default 0 not null,
  datlast             DATE default SYSDATE not null,
  id_usr              NUMBER(18) not null,
  user_location       VARCHAR2(50 CHAR),
  sent_knd            NUMBER(14) default 0 not null,
  entity_id           NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_REPORTS_RULES
is 'Справочник правил выходных форм';
-- Add comments to the columns 
comment on column REF_REPORTS_RULES.id
is 'Идентификатор';
comment on column REF_REPORTS_RULES.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_REPORTS_RULES.code
is 'Код';
comment on column REF_REPORTS_RULES.name_kz
is 'Наименование на казахском языке';
comment on column REF_REPORTS_RULES.name_ru
is 'Наименование на русском';
comment on column REF_REPORTS_RULES.name_en
is 'Наименование на английском языке';
comment on column REF_REPORTS_RULES.formname
is 'Наименование формы';
comment on column REF_REPORTS_RULES.fieldname
is 'Название поля';
comment on column REF_REPORTS_RULES.formula
is 'Формула';
comment on column REF_REPORTS_RULES.is_calc_other_field
is 'Признак вычисления по другим полям';
comment on column REF_REPORTS_RULES.coeff
is 'Коэффициент';
comment on column REF_REPORTS_RULES.condition
is 'Условие';
comment on column REF_REPORTS_RULES.begin_date
is 'Дата начала действия';
comment on column REF_REPORTS_RULES.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_REPORTS_RULES.datlast
is 'Дата последнего редактирования';
comment on column REF_REPORTS_RULES.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_REPORTS_RULES.user_location
is 'Местоположение';
comment on column REF_REPORTS_RULES.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_REPORTS_RULES.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_REPORTS_RULES
add constraint PK_REF_REPORTS_RULES primary key (ID)
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
alter table REF_REPORTS_RULES
add constraint FK_REF_REPORTS_RULES_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_REPORTS_RULES
add constraint FK_REF_REPORTS_RULES_USER foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_REPORTS_RULES
add constraint CHECK_REF_REPORTS_RULES_DLFL
check (DELFL in (0,1));


-- Create table
create table REF_REPORTS_RULES_HST
(
  id_hst              NUMBER(14) not null,
  id                  NUMBER(14) not null,
  rec_id              NUMBER(14) not null,
  code                VARCHAR2(50 CHAR) not null,
  name_kz             VARCHAR2(250 CHAR),
  name_ru             VARCHAR2(250 CHAR) not null,
  name_en             VARCHAR2(250 CHAR),
  formname            VARCHAR2(50 CHAR),
  fieldname           VARCHAR2(50 CHAR),
  formula             VARCHAR2(1000 CHAR),
  is_calc_other_field NUMBER(1) default 0 not null,
  coeff               NUMBER,
  condition           VARCHAR2(50 CHAR),
  begin_date          DATE not null,
  delfl               NUMBER(1) default 0 not null,
  datlast             DATE default SYSDATE not null,
  id_usr              NUMBER(18) not null,
  user_location       VARCHAR2(50 CHAR),
  type_change         NUMBER(14),
  sent_knd            NUMBER(14) default 0 not null,
  entity_id           NUMBER(14)
)
tablespace USERS
pctfree 10
initrans 1
maxtrans 255
storage
(
initial 256K
next 1M
minextents 1
maxextents unlimited
);
-- Add comments to the table 
comment on table REF_REPORTS_RULES_HST
is 'История справочника правил выходных форм';
-- Add comments to the columns 
comment on column REF_REPORTS_RULES_HST.id_hst
is 'Идентификатор';
comment on column REF_REPORTS_RULES_HST.id
is 'Идентификатор основной таблицы';
comment on column REF_REPORTS_RULES_HST.rec_id
is 'Идентификатор (изначальный для одной сущности)';
comment on column REF_REPORTS_RULES_HST.code
is 'Код';
comment on column REF_REPORTS_RULES_HST.name_kz
is 'Наименование на казахском языке';
comment on column REF_REPORTS_RULES_HST.name_ru
is 'Наименование на русском языке';
comment on column REF_REPORTS_RULES_HST.name_en
is 'Наименование на английском языке';
comment on column REF_REPORTS_RULES_HST.formname
is 'Наименование формы';
comment on column REF_REPORTS_RULES_HST.fieldname
is 'Название поля';
comment on column REF_REPORTS_RULES_HST.formula
is 'Формула';
comment on column REF_REPORTS_RULES_HST.is_calc_other_field
is 'Признак вычисления по другим полям';
comment on column REF_REPORTS_RULES_HST.coeff
is 'Коэффициент';
comment on column REF_REPORTS_RULES_HST.condition
is 'Условие';
comment on column REF_REPORTS_RULES_HST.begin_date
is 'Дата начала действия';
comment on column REF_REPORTS_RULES_HST.delfl
is 'Признак удаления (0 - запись неудалена, 1 - удалена)';
comment on column REF_REPORTS_RULES_HST.datlast
is 'Дата последнего редактирования';
comment on column REF_REPORTS_RULES_HST.id_usr
is 'Исполнитель, редактировавший данные';
comment on column REF_REPORTS_RULES_HST.user_location
is 'Местоположение';
comment on column REF_REPORTS_RULES_HST.type_change
is 'Тип изменения(Добавление, Редактирование, Удаление)';
comment on column REF_REPORTS_RULES_HST.sent_knd
is 'Id таблицы статус отправки';
comment on column REF_REPORTS_RULES_HST.entity_id
is 'Id записи в метаклассе ЕССП';
-- Create/Recreate primary, unique and foreign key constraints 
alter table REF_REPORTS_RULES_HST
add constraint PK_REF_REPORTS_RULES_HST primary key (ID_HST)
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
alter table REF_REPORTS_RULES_HST
add constraint FK_REF_REPORTS_RULES_HST_SK foreign key (SENT_KND)
references SENT_KND (SENT_KND);
alter table REF_REPORTS_RULES_HST
add constraint FK_REF_REPORTS_RULES_HST_TC foreign key (TYPE_CHANGE)
references TYPE_CHANGE (TYPE_CHANGE);
alter table REF_REPORTS_RULES_HST
add constraint FK_REF_REPORTS_RULES_HST_U foreign key (ID_USR)
references F_USERS (USER_ID);
-- Create/Recreate check constraints 
alter table REF_REPORTS_RULES_HST
add constraint CHECK_REF_REPS_RUL_HST_DLFL
check (DELFL in (0,1));




