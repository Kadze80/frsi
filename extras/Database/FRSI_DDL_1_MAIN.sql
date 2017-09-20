-- Drop sequences
DROP SEQUENCE seq_ref_delivery_way_id;
DROP SEQUENCE seq_ref_form_status_id;
DROP SEQUENCE seq_forms_id;
DROP SEQUENCE seq_approval_id;
DROP SEQUENCE seq_out_data;
DROP SEQUENCE seq_out_data_resps;
DROP SEQUENCE seq_guide_crosscheck_id;
DROP SEQUENCE seq_subjecttype_forms;
DROP SEQUENCE seq_reports_id;
DROP SEQUENCE SEQ_OUTREP_IDN;
DROP SEQUENCE SEQ_CONTROL_RESULT_ID;
DROP SEQUENCE SEQ_REF_CROSSCHECK_FORMS_ID;
DROP SEQUENCE SEQ_REPORT_HISTORY_STATUSES_ID;
DROP SEQUENCE SEQ_OUTREPORT_RULES_ID;
DROP SEQUENCE SEQ_REF_EXTIND_ID;
DROP SEQUENCE SEQ_REF_EXTIND_HST_ID;
DROP SEQUENCE SEQ_REF_EXTIND_PARAMS_HST_ID;
DROP SEQUENCE SEQ_REF_PERIOD_ALG_ID;
DROP SEQUENCE SEQ_REF_PERIOD_ALG_HST_ID;
DROP SEQUENCE SEQ_REF_PERIOD_ID;
DROP SEQUENCE SEQ_REF_PERIOD_HST_ID;
DROP SEQUENCE SEQ_REF_PERIOD_ARGS_HST_ID;
DROP SEQUENCE SEQ_AE_MAIN_PARAMS_ID;
DROP SEQUENCE SEQ_GROUP_SUBJECT_TYPES_ID;
DROP SEQUENCE SEQ_USER_SUBJECT_TYPES_ID;
DROP SEQUENCE SEQ_GROUP_RESP_FORMS_ID;
DROP SEQUENCE SEQ_USER_RESP_FORMS_ID;
DROP SEQUENCE SEQ_USER_WARRANT_ID;

-- Create sequences
CREATE SEQUENCE seq_ref_delivery_way_id MINVALUE 0 START WITH 0;
CREATE SEQUENCE seq_ref_form_status_id MINVALUE 0 START WITH 0;
CREATE SEQUENCE seq_forms_id MINVALUE 0 START WITH 0;
create sequence SEQ_FORM_HISTORY_ID minvalue 1 maxvalue 9999999999999999999999999999 start with 100 increment by 1 nocache order;
CREATE SEQUENCE seq_approval_id MINVALUE 0 START WITH 0;
CREATE SEQUENCE seq_out_data MINVALUE 0 START WITH 0;
CREATE SEQUENCE seq_out_data_resps MINVALUE 0 START WITH 0;
CREATE SEQUENCE seq_guide_crosscheck_id MINVALUE 0 START WITH 0;
CREATE SEQUENCE seq_subjecttype_forms MINVALUE 0 START WITH 0;
CREATE SEQUENCE seq_reports_id MINVALUE 0 START WITH 0;
CREATE SEQUENCE SEQ_OUTREP_IDN MINVALUE 1 START WITH 1;
CREATE SEQUENCE SEQ_CONTROL_RESULT_ID MINVALUE 1 START WITH 1;
CREATE SEQUENCE SEQ_REF_CROSSCHECK_FORMS_ID MINVALUE 1 START WITH 1;
CREATE SEQUENCE SEQ_REPORT_HISTORY_STATUSES_ID  MINVALUE 1 INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE SEQ_OUTREPORT_RULES_ID MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE SEQ_REF_EXTSYS_ID  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 ORDER;
CREATE SEQUENCE SEQ_REF_EXTSYS_HST_ID  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 ORDER;
CREATE SEQUENCE SEQ_REF_EXTIND_ID  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 ORDER;
CREATE SEQUENCE SEQ_REF_EXTIND_HST_ID  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 ORDER;
CREATE SEQUENCE SEQ_REF_EXTIND_PARAMS_HST_ID  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 ORDER;
CREATE SEQUENCE SEQ_REF_PERIOD_ALG_ID  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 ORDER;
CREATE SEQUENCE SEQ_REF_PERIOD_ALG_HST_ID  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 ORDER;
CREATE SEQUENCE SEQ_REF_PERIOD_ID MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 ORDER;
CREATE SEQUENCE SEQ_REF_PERIOD_HST_ID MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 ORDER;
CREATE SEQUENCE SEQ_REF_PERIOD_ARGS_HST_ID MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 ORDER;
CREATE SEQUENCE SEQ_AE_MAIN_PARAMS_ID MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 ORDER;
CREATE SEQUENCE SEQ_GROUP_SUBJECT_TYPES_ID MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 ORDER;
CREATE SEQUENCE SEQ_USER_SUBJECT_TYPES_ID MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 ORDER;
CREATE SEQUENCE SEQ_GROUP_RESP_FORMS_ID MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 ORDER;
CREATE SEQUENCE SEQ_USER_RESP_FORMS_ID MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 ORDER;
CREATE SEQUENCE SEQ_USER_WARRANT_ID MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 ORDER;

-- Drop tables
DROP TABLE forms;
DROP TABLE form_history;
DROP TABLE subjecttype_forms;
DROP TABLE approval;
DROP TABLE guide_crosscheck;
DROP TABLE out_data;
DROP TABLE out_data_resps;
DROP TABLE ref_delivery_way;
DROP TABLE ref_form_status;
DROP TABLE ref_balance_account;
DROP TABLE guide_reports_forumula;
DROP TABLE frsi_load_guide;
DROP TABLE reports;
DROP TABLE input_reports;
DROP TABLE control_result;
DROP TABLE REF_CROSSCHECK_FORMS;
DROP TABLE REPORT_SIGN;
DROP TABLE REPORT_HISTORY;
DROP TABLE REPORT_HISTORY_STATUSES;
DROP TABLE REPORT_FILE;
DROP TABLE OUTREPORT_RULES;
DROP TABLE REF_EXTIND_PARAMS_HST;
DROP TABLE REF_EXTIND_PARAMS;
DROP TABLE REF_EXTIND_HST;
DROP TABLE REF_EXTIND;
DROP TABLE EXTSYS;
DROP TABLE REF_PERIOD_ALG_HST;
DROP TABLE REF_PERIOD_ALG;
DROP TABLE REF_PERIOD_HST;
DROP TABLE REF_PERIOD;
DROP TABLE REF_PERIOD_HST;
DROP TABLE REF_PERIOD_ARGS;
DROP TABLE SETTINGS_ITEMS;
DROP TABLE AE_MAIN_PARAMS;
DROP TABLE F_SESSION_RESP_FORMS;
DROP TABLE GROUP_SUBJECT_TYPES;
DROP TABLE USER_SUBJECT_TYPES;
DROP TABLE F_SESSION_SUBJECT_TYPES;
DROP TABLE GROUP_RESP_FORMS;
DROP TABLE USER_RESP_FORMS;
DROP TABLE USER_WARRANT;

-- Create tables

-- Create table
create table REPORTS
(
  id           NUMBER not null,
  idn          VARCHAR2(12) not null,
  report_date  DATE not null,
  form_code    VARCHAR2(250) not null,
  control_result_code VARCHAR2(50 CHAR)
);
comment on column REPORTS.control_date
is 'Дата последнего контроля (межформенного/внутриформенного)';
comment on column REPORTS.control_result_code
is 'Результат последнего контроля';
alter table REPORTS
add primary key (ID);
alter table REPORTS
add constraint REPORTS_UQ unique (IDN, REPORT_DATE, FORM_CODE);

-- Create table
create table INPUT_REPORTS
(
  OUT_REPORT_ID NUMBER not null,
  IN_REPORT_ID  NUMBER not null
);

-- Add comments to the columns
comment on column INPUT_REPORTS.OUT_REPORT_ID
is 'ID выходного отчета';
comment on column INPUT_REPORTS.IN_REPORT_ID
is 'ID входного отчета';
-- Create/Recreate primary, unique and foreign key constraints
alter table INPUT_REPORTS
add constraint PK_INPUT_REPORTS primary key (OUT_REPORT_ID, IN_REPORT_ID);
alter table INPUT_REPORTS
add constraint FK_INPUT_REPORTS_IN_REPORT_ID foreign key (IN_REPORT_ID)
references REPORTS (ID);
alter table INPUT_REPORTS
add constraint FK_INPUT_REPORTS_OUT_REPORT_ID foreign key (OUT_REPORT_ID)
references REPORTS (ID) on delete cascade;

-- Create table
create table REPORT_SIGN
(
  id                NUMBER(13) not null,
  report_history_id NUMBER(13) not null,
  user_id           NUMBER(13) not null,
  ref_post          NUMBER(13) not null,
  signature         CLOB not null,
  sign_date         DATE
);
-- Add comments to the columns
comment on column REPORT_SIGN.report_history_id
  is 'Ссылка на историю отчета';
comment on column REPORT_SIGN.user_id
  is 'Пользователь';
comment on column REPORT_SIGN.ref_post
  is 'Должность';
comment on column REPORT_SIGN.signature
  is 'Сигнатура';
comment on column REPORT_SIGN.sign_date
  is 'Дата подписи';
-- Create/Recreate primary, unique and foreign key constraints
alter table REPORT_SIGN
  add constraint REPORT_SIGN_PK primary key (ID);

-- Create table
create table DEL_REPORT_HISTORY
(
  id_hst               NUMBER not null,
  id                   NUMBER not null,
  report_id            NUMBER not null,
  save_date            DATE not null,
  data                 CLOB,
  data_size            NUMBER,
  comments             VARCHAR2(2000),
  attachment           BLOB,
  attachment_size      NUMBER,
  attachment_file_name VARCHAR2(250),
  hash                 VARCHAR2(128),
  delivery_way_code    VARCHAR2(50),
  user_id              NUMBER not null,
  user_info            VARCHAR2(250),
  su_user_id           NUMBER,
  su_user_info         VARCHAR2(250),
  su_comments          VARCHAR2(2000),
  datlast              DATE default sysdate
);
-- Add comments to the table
comment on table DEL_REPORT_HISTORY
  is 'Таблица истории report_history';
-- Add comments to the columns
comment on column DEL_REPORT_HISTORY.id_hst
  is 'Id таблицы истории';
comment on column DEL_REPORT_HISTORY.id
  is 'Id Таблицы report_histoy';
comment on column DEL_REPORT_HISTORY.report_id
  is 'Id Таблицы reports';
comment on column DEL_REPORT_HISTORY.datlast
  is 'Дата удаления';
-- Create/Recreate primary, unique and foreign key constraints
alter table DEL_REPORT_HISTORY
  add constraint PK_DEL_REPORT_HISTORY_ID_HST primary key (ID_HST);

-- Create table
create table DEL_REPORT_STATUS_HISTORY
(
  id_hst        NUMBER not null,
  id            NUMBER not null,
  report_id     NUMBER not null,
  status_code   VARCHAR2(50 CHAR) not null,
  status_date   DATE not null,
  message       VARCHAR2(250 CHAR),
  user_id       NUMBER,
  user_info     VARCHAR2(250 CHAR),
  user_location VARCHAR2(250 CHAR),
  datlast       DATE default sysdate
);
-- Add comments to the table
comment on table DEL_REPORT_STATUS_HISTORY
  is 'Таблица истории REPORT_STATUS_HISTORY';
-- Add comments to the columns
comment on column DEL_REPORT_STATUS_HISTORY.id_hst
  is 'Id Таблицы истории';
comment on column DEL_REPORT_STATUS_HISTORY.id
  is 'Id таблицы REPORT_STATUS_HISTORY';
comment on column DEL_REPORT_STATUS_HISTORY.report_id
  is 'Id Таблицы REPORTS';
comment on column DEL_REPORT_STATUS_HISTORY.datlast
  is 'Дата удаления';
-- Create/Recreate primary, unique and foreign key constraints
alter table DEL_REPORT_STATUS_HISTORY
  add constraint PK_REPORT_STS_HST_ID_HST primary key (ID_HST);

-- Create table
create table DEL_REPORTS
(
  id_hst       NUMBER not null,
  id           NUMBER not null,
  idn          VARCHAR2(12) not null,
  report_date  DATE not null,
  form_code    VARCHAR2(250) not null,
  control_date DATE,
  datlast      DATE default SYSDATE not null
);
-- Add comments to the table
comment on table DEL_REPORTS
  is 'Таблица истории отчетов';
-- Add comments to the columns
comment on column DEL_REPORTS.id_hst
  is 'Id таблицы';
comment on column DEL_REPORTS.id
  is 'Id таблицы reports';
comment on column DEL_REPORTS.idn
  is 'ИИН';
comment on column DEL_REPORTS.report_date
  is 'Дата отчета';
comment on column DEL_REPORTS.form_code
  is 'Код формы';
comment on column DEL_REPORTS.control_date
  is 'Дата контроля';
comment on column DEL_REPORTS.datlast
  is 'Дата удаления';
-- Create/Recreate primary, unique and foreign key constraints
alter table DEL_REPORTS
  add constraint PK_DEL_REPORTS_ID_HST primary key (ID_HST);

create or replace trigger TRG_BD_REPORTS
  before delete on reports
REFERENCING
  OLD AS OLD
  for each row
begin
  if (substr(:old.idn,0,5) != 'DRAFT') then
    insert into del_reports
      (id_hst,
       id,
       idn,
       report_date,
       form_code,
       control_date)
    values
      (seq_del_reports_id.nextval,
       :old.id,
       :old.idn,
       :old.report_date,
       :old.form_code,
       :old.control_date);
  end if;
end TRG_BD_REPORTS;


create or replace trigger TRG_BD_REPORT_HISTORY
  before delete on REPORT_HISTORY
REFERENCING
  OLD AS OLD
  for each row
begin

  if (substr(nvl(trim(:old.comments),0),0,5) != 'DRAFT') then
    insert into del_report_history
      (id_hst,
       id,
       report_id,
       save_date,
       data,
       data_size,
       comments,
       attachment,
       attachment_size,
       attachment_file_name,
       hash,
       delivery_way_code,
       user_id,
       user_info,
       su_user_id,
       su_user_info,
       su_comments)
    values
      (seq_del_report_history_id.nextval,
       :old.id,
       :old.report_id,
       :old.save_date,
       :old.data,
       :old.data_size,
       :old.comments,
       :old.attachment,
       :old.attachment_size,
       :old.attachment_file_name,
       :old.hash,
       :old.delivery_way_code,
       :old.user_id,
       :old.user_info,
       :old.su_user_id,
       :old.su_user_info,
       :old.su_comments);
  end if;

end TRG_BD_REPORT_HISTORY;


create or replace trigger TRG_BD_REPORT_STATUS_HISTORY
  before delete on report_status_history
REFERENCING
  OLD AS OLD
  for each row
begin
  if (substr(:old.status_code,0,5) != 'DRAFT') then
    insert into del_report_status_history
      (id_hst,
       id,
       report_id,
       status_code,
       status_date,
       message,
       user_id,
       user_info,
       user_location)
    values
      (seq_report_status_history_id.nextval,
       :old.id,
       :old.report_id,
       :old.status_code,
       :old.status_date,
       :old.message,
       :old.user_id,
       :old.user_info,
       :old.user_location);
   end if;
end TRG_BD_REPORT_STATUS_HISTORY;

create or replace procedure clear_reports_draft
is
  begin
    delete reports
    where substr(idn,0,5) = 'DRAFT';

    Commit;
    exception
    when others then
    Rollback;
  end clear_reports_draft;
/

begin
  dbms_scheduler.create_job(job_name            => 'FRSI.JOB_CLEAR_REPORTS_DRAFT',
                            job_type            => 'PLSQL_BLOCK',
                            job_action          => 'begin clear_reports_draft; end;',
                            start_date          => to_date(null),
                            repeat_interval     => 'Freq=Daily;ByHour=02;ByMinute=00',
                            end_date            => to_date(null),
                            job_class           => 'DBMS_JOB$',
                            enabled             => true,
                            auto_drop           => false,
                            comments            => '');
end;
/



-- Create sequence
create sequence SEQ_DEL_REPORT_HISTORY_ID
minvalue 1
maxvalue 9999999999999999999999999999
start with 1
increment by 1
nocache
order;

-- Create sequence
create sequence SEQ_DEL_REPORT_STS_HST_ID
minvalue 1
maxvalue 9999999999999999999999999999
start with 1
increment by 1
nocache
order;

-- Create sequence
create sequence SEQ_DEL_REPORTS_ID
minvalue 1
maxvalue 9999999999999999999999999999
start with 1
increment by 1
nocache
order;


CREATE TABLE REF_DELIVERY_WAY
(
  ID      NUMBER PRIMARY KEY,
  CODE    VARCHAR2(250 CHAR) NOT NULL,
  NAME_EN VARCHAR2(250 CHAR),
  NAME_KZ VARCHAR2(250 CHAR),
  NAME_RU VARCHAR2(250 CHAR),
  TAG     VARCHAR2(250 CHAR)
);

CREATE TABLE REF_FORM_STATUS
(
  ID      NUMBER PRIMARY KEY,
  CODE    VARCHAR2(250 CHAR) NOT NULL,
  NAME_EN VARCHAR2(250 CHAR),
  NAME_KZ VARCHAR2(250 CHAR),
  NAME_RU VARCHAR2(250 CHAR),
  TAG     VARCHAR2(250 CHAR)
);

CREATE TABLE REF_BALANCE_ACCOUNT
(
  id NUMBER PRIMARY KEY,
  rec_id NUMBER NOT NULL,
  level_code VARCHAR2(10 CHAR),
  code VARCHAR2(50 CHAR) NOT NULL,
  parent_code VARCHAR2(50 CHAR),
  name_en VARCHAR2(250 CHAR),
  name_kz VARCHAR2(250 CHAR),
  name_ru VARCHAR2(250 CHAR),
  begin_date DATE,
  end_date DATE,
  delfl NUMBER(1) DEFAULT 0,
  datlast DATE DEFAULT SYSDATE,
  id_usr NUMBER,
  user_location VARCHAR2(50 CHAR),
  sent_knd NUMBER DEFAULT 0 NOT NULL
);

create table FORMS
(
  ID                  NUMBER not null,
  CODE                VARCHAR2(250 CHAR) not null,
  TYPE_CODE           VARCHAR2(50 CHAR) not null,
  NAME                VARCHAR2(500 CHAR) not null,
  SHORT_NAME          VARCHAR2(70 CHAR)
);
-- Add comments to the columns
comment on column FORMS.xls_out
is 'Шаблоны Excle для выгрузки данных';
-- Create/Recreate primary, unique and foreign key constraints
alter table FORMS
add constraint FORMS_PK primary key (ID);
alter table FORMS
add constraint FK_FORMS_CODE unique (CODE);


create table FORM_HISTORY
(
  id                  NUMBER not null,
  form_id             NUMBER not null,
  begin_date          DATE not null,
  end_date            DATE,
  xml                 CLOB not null,
  html                CLOB not null,
  xls                 BLOB,
  xls_out             BLOB,
  initial_values      CLOB,
  input_value_checks  CLOB,
  last_update_xml     DATE not null,
  last_update_xls     DATE,
  last_update_xls_out DATE,
  tag                 VARCHAR2(250 CHAR),
  language_code CHAR(2) NOT NULL,
  js_code       CLOB
);
-- Add comments to the table
comment on table FORM_HISTORY
is 'Таблица форм(с данными по каждой форме)';
-- Add comments to the columns
comment on column FORM_HISTORY.id
is 'Id таблицы';
comment on column FORM_HISTORY.form_id
is 'Id основной таблицы FORMS';
comment on column FORM_HISTORY.begin_date
is 'Дата начла';
comment on column FORM_HISTORY.end_date
is 'Дата окончания';
comment on column FORM_HISTORY.xml
is 'Шаблон XML';
comment on column FORM_HISTORY.html
is 'Шаблон HTML';
comment on column FORM_HISTORY.xls
is 'Шаблоны Excle для загрузки данных';
comment on column FORM_HISTORY.xls_out
is 'Шаблоны Excle для выгрузки данных';
comment on column FORM_HISTORY.initial_values
is 'Значения по умолчанию';
comment on column FORM_HISTORY.input_value_checks
is 'Формат вводимых значений';
comment on column FORM_HISTORY.last_update_xml
is 'Дата посл. ред. XML';
comment on column FORM_HISTORY.last_update_xls
is 'Дата посл. ред. входного Excel ';
comment on column FORM_HISTORY.last_update_xls_out
is 'Дата посл. ред. выходного Excel ';
comment on column FORM_HISTORY.language_code
is 'Код языка';
comment on column FORM_HISTORY.js_code
is 'JavaScript код';
-- Create/Recreate primary, unique and foreign key constraints
alter table FORM_HISTORY
add constraint PK_FORM_HISTORY_ID primary key (ID)
  using index;
alter table FORM_HISTORY
add constraint FK_FORM_HISTORY_FORM_ID foreign key (FORM_ID)
references FORMS (ID) on delete cascade;


CREATE TABLE SUBJECTTYPE_FORMS
(
  ID NUMBER PRIMARY KEY,
  CODE VARCHAR2(64 CHAR) NOT NULL,
  FORM_NAME VARCHAR2(64 CHAR) NOT NULL
);

create table SUBJECTTYPE_FORMS
(
  id                      NUMBER not null,
  ref_subject_type_rec_id NUMBER not null,
  form_code               VARCHAR2(64 CHAR) not null,
  period_id               NUMBER default 1 not null,
  REF_PERIOD_REC_ID       NUMBER
);
alter table SUBJECTTYPE_FORMS
  add primary key (ID);
alter table SUBJECTTYPE_FORMS
  add constraint SUBJECTTYPE_FORMS_UK unique (REF_SUBJECT_TYPE_REC_ID, FORM_CODE);
alter table SUBJECTTYPE_FORMS
  add constraint SUBJECTTYPE_FORMS_FK foreign key (PERIOD_ID)
  references REP_PER_DUR_MONTHS (ID);

CREATE TABLE APPROVAL
(
  ID            NUMBER PRIMARY KEY,
  BATCH_ID      NUMBER NOT NULL,
  USER_ID       NUMBER NOT NULL,
  REPORT_DATE   DATE,
  RECEIVED_DATE DATE,
  ENTITY_ID     NUMBER NOT NULL,
  IS_APPROVED   NUMBER(1),
  FORM_NAME     VARCHAR2(250 CHAR),
  RESPONDENT_ID NUMBER,
  APPROVAL_DATE DATE
);

CREATE TABLE GUIDE_CROSSCHECK
(
  ID               NUMBER PRIMARY KEY,
  REPORT_DATE      DATE NOT NULL,
  INTERNAL_FORMULA VARCHAR2(255 CHAR) NOT NULL,
  EXTERNAL_FORMULA VARCHAR2(255 CHAR) NOT NULL,
  FORMNAME         VARCHAR2(255 CHAR) NOT NULL,
  CONDITION        VARCHAR2(3 CHAR),
  EXPRESSION       VARCHAR2(64 CHAR)
);

CREATE TABLE OUT_DATA
(
  ID            NUMBER PRIMARY KEY,
  USER_ID       NUMBER NOT NULL,
  REPORT_DATE   DATE,
  IS_APPROVED   NUMBER(1),
  FORM_NAME     VARCHAR2(250 CHAR),
  RECEIVED_DATE DATE,
  COUCHBASE_ID  NUMBER,
  NOTE          VARCHAR2(2048 CHAR),
  APPROVAL_DATE DATE
);

CREATE OR REPLACE TYPE RESPS_ARRAY AS TABLE OF VARCHAR(128);
CREATE OR REPLACE TYPE REPORT_ID_ARRAY AS TABLE OF NUMBER(14);
CREATE OR REPLACE TYPE FORM_CODE_ARRAY AS TABLE OF VARCHAR2(250);
CREATE OR REPLACE TYPE NUMBER_ARRAY AS TABLE OF NUMBER(14);

create table OUT_DATA_RESPS
(
  ID          NUMBER not null,
  RESPNAME    VARCHAR2(128),
  OUT_DATA_ID NUMBER not null
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
alter table OUT_DATA_RESPS
  add constraint OUT_DATA_RESPS_PK primary key (ID)
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

create table GUIDE_REPORTS_FORUMULA
(
  ID                  NUMBER not null,
  REPORT_DATE         DATE not null,
  FORMNAME            VARCHAR2(255 CHAR) not null,
  FIELDNAME           VARCHAR2(255 CHAR) not null,
  FORMULA             VARCHAR2(4000 CHAR) not null,
  IS_CALC_OTHER_FIELD NUMBER,
  COEFF               NUMBER,
  CONDITION           VARCHAR2(10 CHAR)
)

create table FRSI_LOAD_GUIDE
(
  FRSI_LOAD_GUIDE NUMBER(14) not null,
  NAME            VARCHAR2(50) not null,
  CODE            VARCHAR2(50) not null,
  TYPE            VARCHAR2(10) not null,
  TABLE_NAME      VARCHAR2(50),
  DATE_LOAD       DATE default SYSDATE not null,
  STATUS          VARCHAR2(100) not null,
  MAT_NAME        VARCHAR2(50)
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
comment on table FRSI_LOAD_GUIDE
  is 'Таблица загружаемых справочников';
-- Add comments to the columns
comment on column FRSI_LOAD_GUIDE.FRSI_LOAD_GUIDE
  is 'Идентификатор';
comment on column FRSI_LOAD_GUIDE.NAME
  is 'Наименование справочника';
comment on column FRSI_LOAD_GUIDE.CODE
  is 'Код справочника';
comment on column FRSI_LOAD_GUIDE.TYPE
  is 'Тип (MV-мат. вьюшка; LX- load xml, загружается через XML файл из внешней системы в ЕССП; LXMV-обновлять и справ-к в ЕССП и мат.вьюшка в ФРСП)';
comment on column FRSI_LOAD_GUIDE.TABLE_NAME
  is 'Наименование таблицы, вьюшки';
comment on column FRSI_LOAD_GUIDE.DATE_LOAD
  is 'Дата последней загрузки';
comment on column FRSI_LOAD_GUIDE.STATUS
  is 'Статус загрузки';
comment on column FRSI_LOAD_GUIDE.MAT_NAME
  is 'Название связаной мат вьюшки';
-- Create/Recreate primary, unique and foreign key constraints
alter table FRSI_LOAD_GUIDE
  add constraint PK_FRSI_LOAD_GUIDE primary key (FRSI_LOAD_GUIDE)
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

create table CONTROL_RESULT
(
  id                    NUMBER(14) not null,
  ref_crosscheck_rec_id NUMBER(14) not null,
  report_date           DATE not null,
  idn                   VARCHAR2(12) not null,
  description_ru        VARCHAR2(300 CHAR) not null,
  result_type           NUMBER(14) not null
)
;
-- Add comments to the columns
comment on column CONTROL_RESULT.id
is 'Идентификатор';
comment on column CONTROL_RESULT.ref_crosscheck_rec_id
is 'Rec_Id таблицы форменного контроля';
comment on column CONTROL_RESULT.report_date
is 'Дата отчета';
comment on column CONTROL_RESULT.idn
is 'БИН';
comment on column CONTROL_RESULT.description_ru
is 'Описание на русском';
comment on column CONTROL_RESULT.result_type
is 'Результат проверки (1-выполнено, 2-не выполнено, 3-ошибка)';
-- Create/Recreate primary, unique and foreign key constraints
alter table control_result
add constraint control_result_pk primary key (ID);

create table REPORT_PROPS
(
  report_history_id NUMBER not null,
  prop_key          VARCHAR2(255) not null,
  prop_value        VARCHAR2(4000)
);
alter table REPORT_PROPS
add constraint REPORT_PROPS_UQ1 unique (REPORT_HISTORY_ID, PROP_KEY);
alter table REPORT_PROPS
add constraint REPORT_PROPS_FK1 foreign key (REPORT_HISTORY_ID)
references REPORT_HISTORY (ID) on delete cascade;

create table REF_CROSSCHECK_FORMS
(
  id                NUMBER(14) not null,
  ref_crosscheck_id NUMBER(14) not null,
  form_code         VARCHAR2(250 CHAR) not null
);
comment on column REF_CROSSCHECK_FORMS.ref_crosscheck_id
is 'Ссылка на справочник форменных контролей';
comment on column REF_CROSSCHECK_FORMS.form_code
is 'Код формы';
-- Create/Recreate primary, unique and foreign key constraints
alter table REF_CROSSCHECK_FORMS
add constraint REF_CROSSCHECK_FORMS_PK primary key (ID);
alter table REF_CROSSCHECK_FORMS
add constraint REF_CROSSCHECK_FORMS_UK unique (REF_CROSSCHECK_ID, FORM_CODE);
alter table REF_CROSSCHECK_FORMS
add constraint REF_CROSSCHECK_FORMS_FK foreign key (REF_CROSSCHECK_ID)
references REF_CROSSCHECK (ID);

CREATE TABLE REPORT_HISTORY
(
    ID NUMBER(*) PRIMARY KEY NOT NULL,
    REPORT_ID NUMBER(*) NOT NULL,
    SAVE_DATE DATE NOT NULL,
    DATA CLOB,
    DATA_SIZE NUMBER(*),
    COMMENTS VARCHAR2(2000),
    ATTACHMENT BLOB,
    ATTACHMENT_SIZE NUMBER(*),
    ATTACHMENT_FILE_NAME VARCHAR2(250),
    HASH VARCHAR2(128),
    DELIVERY_WAY_CODE VARCHAR2(50),
    USER_ID NUMBER(*) NOT NULL,
    USER_INFO VARCHAR2(250),
    SU_USER_ID NUMBER(*),
    SU_USER_INFO VARCHAR2(250),
    SU_COMMENTS VARCHAR2(2000),
    CONTROL_RESULT_CODE VARCHAR2(50),
    CONTROL_RESULT_CODE2 VARCHAR2(50),
    CONSTRAINT REPORT_HISTORY_FK FOREIGN KEY (REPORT_ID) REFERENCES REPORTS (ID) ON DELETE CASCADE
);
CREATE INDEX REPORT_HISTORY_IDX ON REPORT_HISTORY (REPORT_ID, SAVE_DATE);

CREATE TABLE REPORT_HISTORY_STATUSES
(
  ID                NUMBER(*) PRIMARY KEY NOT NULL,
  REPORT_HISTORY_ID NUMBER(*)             NOT NULL,
  STATUS_CODE       VARCHAR2(50)          NOT NULL,
  STATUS_DATE       DATE                  NOT NULL,
  MESSAGE           VARCHAR2(250),
  USER_ID           NUMBER(*),
  USER_INFO         VARCHAR2(250),
  USER_LOCATION     VARCHAR2(250),
  CONSTRAINT REPORT_HISTORY_STATUSES_FK FOREIGN KEY (REPORT_HISTORY_ID) REFERENCES REPORT_HISTORY (ID) ON DELETE CASCADE
);

CREATE TABLE REPORT_FILE
(
  ID                NUMBER(14) PRIMARY KEY NOT NULL,
  REPORT_ID         NUMBER(14),
  FILE_DATA         BLOB,
  FILE_TYPE         VARCHAR2(1024),
  FILE_NAME         VARCHAR2(1024),
  FILE_DATE         DATE,
  ID_USR            NUMBER(18),
  REPORT_HISTORY_ID NUMBER(14),
  CONSTRAINT FK_REPORT_DOCUMENT_REPORT_ID FOREIGN KEY (REPORT_ID) REFERENCES REPORTS (ID) ON DELETE CASCADE,
  CONSTRAINT FK_REPORT_DOCUMENT_ID_USR FOREIGN KEY (ID_USR) REFERENCES F_USERS (USER_ID) ON DELETE CASCADE,
  CONSTRAINT REPORT_FILE_FK1 FOREIGN KEY (REPORT_HISTORY_ID) REFERENCES REPORT_HISTORY (ID) ON DELETE CASCADE
);
COMMENT ON COLUMN REPORT_FILE.ID IS 'Id Основной таблицы';
COMMENT ON COLUMN REPORT_FILE.REPORT_ID IS 'Id таблицы REPORTS';
COMMENT ON COLUMN REPORT_FILE.FILE_DATA IS 'Файл';
COMMENT ON COLUMN REPORT_FILE.FILE_TYPE IS 'Тип документа(*.pdf,*.txt,*.doc,*.docx,*.xlsx,*.xlsm)';
COMMENT ON COLUMN REPORT_FILE.FILE_NAME IS 'Наименование документа';
COMMENT ON COLUMN REPORT_FILE.FILE_DATE IS 'Дата загрузки документа';
COMMENT ON COLUMN REPORT_FILE.ID_USR IS 'Пользователь загрузивший документ';

CREATE TABLE OUTREPORT_RULES
(
    ID NUMBER(14) PRIMARY KEY NOT NULL,
    FORM_CODE VARCHAR2(50),
    TABLE_NAME VARCHAR2(64),
    FIELD_NAME VARCHAR2(50),
    FORMULA VARCHAR2(4000),
    BEGIN_DATE DATE NOT NULL,
    PRIORITY NUMBER(4) NOT NULL,
    END_DATE DATE,
    KEYVALUE VARCHAR2(64),
    DATA_TYPE VARCHAR2(20),
    GROUPING NUMBER(1),
    FORM_HISTORY_ID NUMBER(*) DEFAULT NULL  NOT NULL,
    CONSTRAINT OUTREPORT_RULES_FK1 FOREIGN KEY (FORM_HISTORY_ID) REFERENCES FORM_HISTORY (ID) ON DELETE CASCADE
);

CREATE TABLE EXTSYS
(
  ID            NUMBER(14) PRIMARY KEY NOT NULL,
  NAME_KZ       VARCHAR2(250),
  NAME_RU       VARCHAR2(250)          NOT NULL,
  NAME_EN       VARCHAR2(250)
);
COMMENT ON COLUMN EXTSYS.ID IS 'Идентификатор';
COMMENT ON COLUMN EXTSYS.NAME_KZ IS 'Наименование на казахском языке';
COMMENT ON COLUMN EXTSYS.NAME_RU IS 'Наименование на русском';
COMMENT ON COLUMN EXTSYS.NAME_EN IS 'Наименование на английском языке';

CREATE TABLE REF_EXTIND
(
  ID            NUMBER(14) PRIMARY KEY NOT NULL,
  REC_ID        NUMBER(14)             NOT NULL,
  EXTSYS_ID     NUMBER(14)             NOT NULL,
  CODE          VARCHAR2(100)          NOT NULL,
  NAME_KZ       VARCHAR2(250),
  NAME_EN       VARCHAR2(250),
  NAME_RU       VARCHAR2(250),
  ALG           CLOB                   NOT NULL,
  VALUE_TYPE    VARCHAR2(25)           NOT NULL,
  BEGIN_DATE    DATE                   NOT NULL,
  DELFL         NUMBER(1) DEFAULT 0    NOT NULL,
  DATLAST       DATE DEFAULT SYSDATE   NOT NULL,
  ID_USR        NUMBER(18)             NOT NULL,
  USER_LOCATION VARCHAR2(50),
  SENT_KND      NUMBER(14) DEFAULT 0   NOT NULL,
  END_DATE      DATE,
  CONSTRAINT FK_REF_EXTIND_EXTSYS FOREIGN KEY (EXTSYS_ID) REFERENCES EXTSYS (ID),
  CONSTRAINT FK_REF_EXTIND_USER FOREIGN KEY (ID_USR) REFERENCES F_USERS (USER_ID)
);

CREATE TABLE REF_EXTIND_HST
(
  ID_HST        NUMBER(14) PRIMARY KEY NOT NULL,
  ID            NUMBER(14)             NOT NULL,
  REC_ID        NUMBER(14)             NOT NULL,
  EXTSYS_ID     NUMBER(14)             NOT NULL,
  CODE          VARCHAR2(100)          NOT NULL,
  NAME_KZ       VARCHAR2(250),
  NAME_EN       VARCHAR2(250),
  NAME_RU       VARCHAR2(250),
  ALG           CLOB                   NOT NULL,
  VALUE_TYPE    VARCHAR2(25)           NOT NULL,
  BEGIN_DATE    DATE                   NOT NULL,
  DELFL         NUMBER(1) DEFAULT 0    NOT NULL,
  DATLAST       DATE DEFAULT SYSDATE   NOT NULL,
  ID_USR        NUMBER(18)             NOT NULL,
  USER_LOCATION VARCHAR2(50),
  SENT_KND      NUMBER(14) DEFAULT 0   NOT NULL,
  END_DATE      DATE,
  TYPE_CHANGE   NUMBER(14),
  CONSTRAINT FK_REF_EXTIND_HST_TC FOREIGN KEY (TYPE_CHANGE) REFERENCES TYPE_CHANGE (TYPE_CHANGE)
);

CREATE TABLE REF_EXTIND_PARAMS
(
  REF_EXTIND_ID NUMBER(14) NOT NULL,
  NAME VARCHAR2(50) NOT NULL,
  VALUE_TYPE VARCHAR2(25) NOT NULL,
  CONSTRAINT FK_REF_EXTIND_PARAMS FOREIGN KEY (REF_EXTIND_ID) REFERENCES REF_EXTIND (ID)
);
COMMENT ON COLUMN REF_EXTIND_PARAMS.NAME IS 'Имя параметра';
COMMENT ON COLUMN REF_EXTIND_PARAMS.VALUE_TYPE IS 'Тип данных:
NUMBER_0-NUMBER_8 число
STRING строка
DATE дата
BOOLEAN лог.тип';

CREATE TABLE REF_EXTIND_PARAMS_HST
(
  ID_HST        NUMBER(14)   NOT NULL,
  REF_EXTIND_ID NUMBER(14)   NOT NULL,
  NAME          VARCHAR2(50) NOT NULL,
  VALUE_TYPE    VARCHAR2(25)  NOT NULL,
  TYPE_CHANGE NUMBER(14),
  CONSTRAINT FK_REF_EXTIND_PARAMS_HST_TC FOREIGN KEY (TYPE_CHANGE) REFERENCES TYPE_CHANGE (TYPE_CHANGE)
);
COMMENT ON COLUMN REF_EXTIND_PARAMS_HST.NAME IS 'Имя параметра';
COMMENT ON COLUMN REF_EXTIND_PARAMS_HST.VALUE_TYPE IS 'Тип данных:
NUMBER_0-NUMBER_8 число
STRING строка
DATE дата
BOOLEAN лог.тип';

CREATE TABLE REF_PERIOD_ALG (
  ID              NUMBER(14)            NOT NULL,
  REC_ID          NUMBER(14)            NOT NULL,
  NAME_KZ         VARCHAR2(250 CHAR),
  NAME_RU         VARCHAR2(250 CHAR),
  NAME_EN         VARCHAR2(250 CHAR),
  ALG             CLOB,
  BEGIN_DATE      DATE DEFAULT sysdate  NOT NULL,
  DELFL           NUMBER(1) DEFAULT 0   NOT NULL,
  DATLAST         DATE DEFAULT SYSDATE  NOT NULL,
  ID_USR          NUMBER(18)            NOT NULL,
  USER_LOCATION   VARCHAR2(50 CHAR),
  SENT_KND        NUMBER(14) DEFAULT 0  NOT NULL,
  END_DATE        DATE,
  CONSTRAINT PK_REF_PERIOD_ALG PRIMARY KEY (ID),
  CONSTRAINT FK_REF_PERIOD_ALG_SK FOREIGN KEY (SENT_KND) REFERENCES SENT_KND (SENT_KND),
  CONSTRAINT FK_REF_PERIOD_ALG FOREIGN KEY (ID_USR) REFERENCES F_USERS (USER_ID),
  CONSTRAINT CHECK_REF_PERIOD_ALG_DELFL CHECK (DELFL IN (0, 1))
);
COMMENT ON TABLE REF_PERIOD_ALG IS 'Справочник алгоритмов расчета сроков предоставления';
COMMENT ON COLUMN REF_PERIOD_ALG.ID IS 'Идентификатор';
COMMENT ON COLUMN REF_PERIOD_ALG.REC_ID IS 'Идентификатор (изначальный для одной сущности)';
COMMENT ON COLUMN REF_PERIOD_ALG.NAME_KZ IS 'Наименование на казахском';
COMMENT ON COLUMN REF_PERIOD_ALG.NAME_RU IS 'Наименование на русском';
COMMENT ON COLUMN REF_PERIOD_ALG.NAME_EN IS 'Наименование на английском';
COMMENT ON COLUMN REF_PERIOD_ALG.ALG IS 'Алгоритм';
COMMENT ON COLUMN REF_PERIOD_ALG.BEGIN_DATE IS 'Дата начала действия';
COMMENT ON COLUMN REF_PERIOD_ALG.DELFL IS 'Признак удаления (0 - запись неудалена, 1 - удалена)';
COMMENT ON COLUMN REF_PERIOD_ALG.DATLAST IS 'Дата последнего редактирования';
COMMENT ON COLUMN REF_PERIOD_ALG.ID_USR IS 'Исполнитель, редактировавший данные';
COMMENT ON COLUMN REF_PERIOD_ALG.USER_LOCATION IS 'Местоположение';
COMMENT ON COLUMN REF_PERIOD_ALG.SENT_KND IS 'Id таблицы статус отправки';
COMMENT ON COLUMN REF_PERIOD_ALG.END_DATE IS 'Дата окончания действия';


CREATE TABLE REF_PERIOD_ALG_HST (
  ID_HST          NUMBER(14)            NOT NULL,
  ID              NUMBER(14)            NOT NULL,
  REC_ID          NUMBER(14)            NOT NULL,
  NAME_KZ         VARCHAR2(250 CHAR),
  NAME_RU         VARCHAR2(250 CHAR)    NOT NULL,
  NAME_EN         VARCHAR2(250 CHAR),
  ALG         CLOB,
  BEGIN_DATE      DATE                  NOT NULL,
  DELFL           NUMBER(1) DEFAULT 0   NOT NULL,
  DATLAST         DATE DEFAULT SYSDATE  NOT NULL,
  ID_USR          NUMBER(18)            NOT NULL,
  USER_LOCATION   VARCHAR2(50 CHAR),
  TYPE_CHANGE     NUMBER(14),
  SENT_KND        NUMBER(14) DEFAULT 0  NOT NULL,
  END_DATE        DATE,
  CONSTRAINT PK_REF_PERIOD_ALG_HST PRIMARY KEY (ID_HST)
);
COMMENT ON TABLE REF_PERIOD_ALG_HST IS 'История справочника алгоритмов расчета сроков предоставления';
COMMENT ON COLUMN REF_PERIOD_ALG_HST.ID_HST IS 'Идентификатор';
COMMENT ON COLUMN REF_PERIOD_ALG_HST.ID IS 'Идентификатор основной таблицы';
COMMENT ON COLUMN REF_PERIOD_ALG_HST.REC_ID IS 'Идентификатор (изначальный для одной сущности)';
COMMENT ON COLUMN REF_PERIOD_ALG_HST.NAME_KZ IS 'Наименование на казахском языке';
COMMENT ON COLUMN REF_PERIOD_ALG_HST.NAME_RU IS 'Наименование на русском языке';
COMMENT ON COLUMN REF_PERIOD_ALG_HST.NAME_EN IS 'Наименование на английском языке';
COMMENT ON COLUMN REF_PERIOD_ALG_HST.ALG IS 'Алгоритм';
COMMENT ON COLUMN REF_PERIOD_ALG_HST.BEGIN_DATE IS 'Дата начала действия';
COMMENT ON COLUMN REF_PERIOD_ALG_HST.DELFL IS 'Признак удаления (0 - запись неудалена, 1 - удалена)';
COMMENT ON COLUMN REF_PERIOD_ALG_HST.DATLAST IS 'Дата последнего редактирования';
COMMENT ON COLUMN REF_PERIOD_ALG_HST.ID_USR IS 'Исполнитель, редактировавший данные';
COMMENT ON COLUMN REF_PERIOD_ALG_HST.USER_LOCATION IS 'Местоположение';
COMMENT ON COLUMN REF_PERIOD_ALG_HST.TYPE_CHANGE IS 'Тип изменения(Добавление, Редактирование, Удаление)';
COMMENT ON COLUMN REF_PERIOD_ALG_HST.SENT_KND IS 'Id таблицы статус отправки';
COMMENT ON COLUMN REF_PERIOD_ALG_HST.END_DATE IS 'Дата окончания действия';

CREATE TABLE REF_PERIOD (
  ID             NUMBER(14)            NOT NULL,
  REC_ID         NUMBER(14)            NOT NULL,
  NAME_KZ        VARCHAR2(250 CHAR),
  NAME_RU        VARCHAR2(250 CHAR),
  NAME_EN        VARCHAR2(250 CHAR),
  SHORT_NAME_KZ  VARCHAR2(250 CHAR),
  SHORT_NAME_RU  VARCHAR2(250 CHAR),
  SHORT_NAME_EN  VARCHAR2(250 CHAR),
  REF_PERIOD_ALG NUMBER(14)            NOT NULL,
  AUTO_APPROVE   NUMBER(1) DEFAULT 0   NOT NULL,
  BEGIN_DATE     DATE DEFAULT sysdate  NOT NULL,
  DELFL          NUMBER(1) DEFAULT 0   NOT NULL,
  DATLAST        DATE DEFAULT SYSDATE  NOT NULL,
  ID_USR         NUMBER(18)            NOT NULL,
  USER_LOCATION  VARCHAR2(50 CHAR),
  SENT_KND       NUMBER(14) DEFAULT 0  NOT NULL,
  END_DATE       DATE,
  CONSTRAINT PK_REF_PERIOD PRIMARY KEY (ID),
  CONSTRAINT FK_REF_PERIOD_PA FOREIGN KEY (REF_PERIOD_ALG) REFERENCES REF_PERIOD_ALG (ID),
  CONSTRAINT FK_REF_PERIOD_SK FOREIGN KEY (SENT_KND) REFERENCES SENT_KND (SENT_KND),
  CONSTRAINT FK_REF_PERIOD FOREIGN KEY (ID_USR) REFERENCES F_USERS (USER_ID),
  CONSTRAINT CHECK_REF_PERIOD_DELFL CHECK (DELFL IN (0, 1)),
  CONSTRAINT CHECK_REF_PERIOD_AA CHECK (AUTO_APPROVE IN (0, 1))
);
COMMENT ON TABLE REF_PERIOD IS 'Справочник алгоритмов расчета сроков предоставления';
COMMENT ON COLUMN REF_PERIOD.ID IS 'Идентификатор';
COMMENT ON COLUMN REF_PERIOD.REC_ID IS 'Идентификатор (изначальный для одной сущности)';
COMMENT ON COLUMN REF_PERIOD.NAME_KZ IS 'Наименование на казахском';
COMMENT ON COLUMN REF_PERIOD.NAME_RU IS 'Наименование на русском';
COMMENT ON COLUMN REF_PERIOD.NAME_EN IS 'Наименование на английском';
COMMENT ON COLUMN REF_PERIOD.SHORT_NAME_KZ IS 'Короткое наименование на казахском';
COMMENT ON COLUMN REF_PERIOD.SHORT_NAME_RU IS 'Короткое наименование на русском';
COMMENT ON COLUMN REF_PERIOD.SHORT_NAME_EN IS 'Короткое наименование на английском';
COMMENT ON COLUMN REF_PERIOD.REF_PERIOD_ALG IS ' Rec_Id таблицы алгоритмов расчета сроков предоставления';
COMMENT ON COLUMN REF_PERIOD.AUTO_APPROVE IS 'Признак автоматического утверждения формы';
COMMENT ON COLUMN REF_PERIOD.BEGIN_DATE IS 'Дата начала действия';
COMMENT ON COLUMN REF_PERIOD.DELFL IS 'Признак удаления (0 - запись неудалена, 1 - удалена)';
COMMENT ON COLUMN REF_PERIOD.DATLAST IS 'Дата последнего редактирования';
COMMENT ON COLUMN REF_PERIOD.ID_USR IS 'Исполнитель, редактировавший данные';
COMMENT ON COLUMN REF_PERIOD.USER_LOCATION IS 'Местоположение';
COMMENT ON COLUMN REF_PERIOD.SENT_KND IS 'Id таблицы статус отправки';
COMMENT ON COLUMN REF_PERIOD.END_DATE IS 'Дата окончания действия';


CREATE TABLE REF_PERIOD_HST (
  ID_HST         NUMBER(14)            NOT NULL,
  ID             NUMBER(14)            NOT NULL,
  REC_ID         NUMBER(14)            NOT NULL,
  NAME_KZ        VARCHAR2(250 CHAR),
  NAME_RU        VARCHAR2(250 CHAR)    NOT NULL,
  NAME_EN        VARCHAR2(250 CHAR),
  SHORT_NAME_KZ  VARCHAR2(250 CHAR),
  SHORT_NAME_RU  VARCHAR2(250 CHAR),
  SHORT_NAME_EN  VARCHAR2(250 CHAR),
  REF_PERIOD_ALG NUMBER(14)            NOT NULL,
  AUTO_APPROVE   NUMBER(1) DEFAULT 0   NOT NULL,
  BEGIN_DATE     DATE                  NOT NULL,
  DELFL          NUMBER(1) DEFAULT 0   NOT NULL,
  DATLAST        DATE DEFAULT SYSDATE  NOT NULL,
  ID_USR         NUMBER(18)            NOT NULL,
  USER_LOCATION  VARCHAR2(50 CHAR),
  TYPE_CHANGE    NUMBER(14),
  SENT_KND       NUMBER(14) DEFAULT 0  NOT NULL,
  END_DATE       DATE,
  CONSTRAINT PK_REF_PERIOD_HST PRIMARY KEY (ID_HST)
);
COMMENT ON TABLE REF_PERIOD_HST IS 'История справочника алгоритмов расчета сроков предоставления';
COMMENT ON COLUMN REF_PERIOD_HST.ID_HST IS 'Идентификатор';
COMMENT ON COLUMN REF_PERIOD_HST.ID IS 'Идентификатор основной таблицы';
COMMENT ON COLUMN REF_PERIOD_HST.REC_ID IS 'Идентификатор (изначальный для одной сущности)';
COMMENT ON COLUMN REF_PERIOD_HST.NAME_KZ IS 'Наименование на казахском языке';
COMMENT ON COLUMN REF_PERIOD_HST.NAME_RU IS 'Наименование на русском языке';
COMMENT ON COLUMN REF_PERIOD_HST.NAME_EN IS 'Наименование на английском языке';
COMMENT ON COLUMN REF_PERIOD_HST.SHORT_NAME_KZ IS 'Короткое наименование на казахском';
COMMENT ON COLUMN REF_PERIOD_HST.SHORT_NAME_RU IS 'Короткое наименование на русском';
COMMENT ON COLUMN REF_PERIOD_HST.SHORT_NAME_EN IS 'Короткое наименование на английском';
COMMENT ON COLUMN REF_PERIOD_HST.REF_PERIOD_ALG IS ' Id таблицы алгоритмов расчета сроков предоставления';
COMMENT ON COLUMN REF_PERIOD_HST.AUTO_APPROVE IS 'Признак автоматического утверждения формы';
COMMENT ON COLUMN REF_PERIOD_HST.BEGIN_DATE IS 'Дата начала действия';
COMMENT ON COLUMN REF_PERIOD_HST.DELFL IS 'Признак удаления (0 - запись неудалена, 1 - удалена)';
COMMENT ON COLUMN REF_PERIOD_HST.DATLAST IS 'Дата последнего редактирования';
COMMENT ON COLUMN REF_PERIOD_HST.ID_USR IS 'Исполнитель, редактировавший данные';
COMMENT ON COLUMN REF_PERIOD_HST.USER_LOCATION IS 'Местоположение';
COMMENT ON COLUMN REF_PERIOD_HST.TYPE_CHANGE IS 'Тип изменения(Добавление, Редактирование, Удаление)';
COMMENT ON COLUMN REF_PERIOD_HST.SENT_KND IS 'Id таблицы статус отправки';
COMMENT ON COLUMN REF_PERIOD_HST.END_DATE IS 'Дата окончания действия';

CREATE TABLE REF_PERIOD_ARGS
(
  REF_PERIOD_ID NUMBER(14) NOT NULL,
  NAME VARCHAR2(50) NOT NULL,
  VALUE_TYPE VARCHAR2(25) NOT NULL,
  INTEGER_VALUE NUMBER(15, 0),
  REAL_VALUE    NUMBER(18, 8),
  BOOLEAN_VALUE NUMBER,
  STRING_VALUE VARCHAR2(2000),
  DATE_VALUE DATE,
  CONSTRAINT FK_REF_PERIOD_ARGS FOREIGN KEY (REF_PERIOD_ID) REFERENCES REF_PERIOD (ID),
  CONSTRAINT CHECK_REF_PERIOD_ARGS_BV CHECK (BOOLEAN_VALUE IN (0, 1))
);
COMMENT ON COLUMN REF_PERIOD_ARGS.NAME IS 'Имя аргумента';
COMMENT ON COLUMN REF_PERIOD_ARGS.VALUE_TYPE IS 'Тип данных:
NUMBER_0-NUMBER_8 число
STRING строка
DATE дата
BOOLEAN лог.тип';
COMMENT ON COLUMN REF_PERIOD_ARGS.INTEGER_VALUE IS 'Значение целого числового аргумента';
COMMENT ON COLUMN REF_PERIOD_ARGS.REAL_VALUE IS 'Значение вещественного числового аргумента';
COMMENT ON COLUMN REF_PERIOD_ARGS.BOOLEAN_VALUE IS 'Значение булевого аргумента';
COMMENT ON COLUMN REF_PERIOD_ARGS.STRING_VALUE IS 'Значение строкового аргумента';
COMMENT ON COLUMN REF_PERIOD_ARGS.DATE_VALUE IS 'Значение аргумента с типом DATE';

CREATE TABLE REF_PERIOD_ARGS_HST
(
  ID_HST        NUMBER(14)   NOT NULL,
  REF_PERIOD_ID NUMBER(14)   NOT NULL,
  NAME          VARCHAR2(50) NOT NULL,
  VALUE_TYPE    VARCHAR2(25)  NOT NULL,
  TYPE_CHANGE NUMBER(14),
  INTEGER_VALUE NUMBER(15, 0),
  REAL_VALUE    NUMBER(18, 8),
  BOOLEAN_VALUE NUMBER,
  STRING_VALUE VARCHAR2(2000),
  DATE_VALUE DATE,
  CONSTRAINT FK_REF_PERIOD_ARGS_HST FOREIGN KEY (TYPE_CHANGE) REFERENCES TYPE_CHANGE (TYPE_CHANGE)
);
COMMENT ON COLUMN REF_PERIOD_ARGS_HST.NAME IS 'Имя аргумента';
COMMENT ON COLUMN REF_PERIOD_ARGS_HST.VALUE_TYPE IS 'Тип данных:
NUMBER_0-NUMBER_8 число
STRING строка
DATE дата
BOOLEAN лог.тип';
COMMENT ON COLUMN REF_PERIOD_ARGS_HST.INTEGER_VALUE IS 'Значение целого числового аргумента';
COMMENT ON COLUMN REF_PERIOD_ARGS_HST.REAL_VALUE IS 'Значение вещественного числового аргумента';
COMMENT ON COLUMN REF_PERIOD_ARGS_HST.BOOLEAN_VALUE IS 'Значение булевого аргумента';
COMMENT ON COLUMN REF_PERIOD_ARGS_HST.STRING_VALUE IS 'Значение строкового аргумента';
COMMENT ON COLUMN REF_PERIOD_ARGS_HST.DATE_VALUE IS 'Значение аргумента с типом DATE';

CREATE TABLE SETTINGS_ITEMS
(
  SETTING_TYPE VARCHAR2(400) NOT NULL,
  USER_ID      NUMBER(13)    NOT NULL,
  RAW_VALUE      CLOB
);
COMMENT ON COLUMN SETTINGS_ITEMS.SETTING_TYPE IS 'Тип настройки. Значения берется из перечисления SettingsItem.Type';
COMMENT ON COLUMN SETTINGS_ITEMS.USER_ID IS 'id пользователя портала';
COMMENT ON COLUMN SETTINGS_ITEMS.RAW_VALUE IS 'Значение настройки (json)';

CREATE TABLE
  AE_MAIN_PARAMS
(
  ID            NUMBER(15) DEFAULT NULL NOT NULL,
  AE_MAIN_ID    NUMBER(15)              NOT NULL,
  CODE          VARCHAR2(256 CHAR)      NOT NULL,
  VALUE_TYPE    VARCHAR2(25)            NOT NULL,
  INTEGER_VALUE NUMBER(15, 0),
  REAL_VALUE    NUMBER(18, 8),
  BOOLEAN_VALUE NUMBER,
  STRING_VALUE  VARCHAR2(2000 CHAR),
  DATE_VALUE    DATE,
  CONSTRAINT AE_MAIN_PARAMS_ID_PK PRIMARY KEY (ID),
  CONSTRAINT AE_MAIN_PARAMS__FK FOREIGN KEY (AE_MAIN_ID) REFERENCES AE_MAIN (ID),
  CONSTRAINT CHECK_AE_MAIN_PARAMS_BV CHECK (BOOLEAN_VALUE IN (0, 1))
);
COMMENT ON TABLE AE_MAIN_PARAMS
IS
'Параметры событии';
COMMENT ON COLUMN AE_MAIN_PARAMS.AE_MAIN_ID
IS
'ID таблицы AE_MAIN';
COMMENT ON COLUMN AE_MAIN_PARAMS.CODE
IS
'Код параметра';
COMMENT ON COLUMN AE_MAIN_PARAMS.VALUE_TYPE
IS
'Тип значения параметра';
COMMENT ON COLUMN AE_MAIN_PARAMS.INTEGER_VALUE
IS
'Целое числовое значение';
COMMENT ON COLUMN AE_MAIN_PARAMS.REAL_VALUE
IS
'Вещественное числовое значение';
COMMENT ON COLUMN AE_MAIN_PARAMS.BOOLEAN_VALUE
IS
'Логическое значение';
COMMENT ON COLUMN AE_MAIN_PARAMS.STRING_VALUE
IS
'Строковое значение';
COMMENT ON COLUMN AE_MAIN_PARAMS.DATE_VALUE
IS
'Тип значение дата';

CREATE UNIQUE INDEX AE_MAIN_PARAMS__UQ
  ON AE_MAIN_PARAMS (AE_MAIN_ID, CODE);


CREATE TABLE F_SESSION_RESP_FORMS
(
  USER_ID               NUMBER(13)    NOT NULL,
  RIGHT_ITEM_NAME       VARCHAR2(250) NOT NULL,
  FORM_CODE             VARCHAR2(250) NOT NULL,
  REF_RESPONDENT_REC_ID NUMBER(13)    NOT NULL
);

CREATE UNIQUE INDEX F_SESSION_RESP_FORMS__UQ
  ON F_SESSION_RESP_FORMS (USER_ID, RIGHT_ITEM_NAME, FORM_CODE, REF_RESPONDENT_REC_ID);

COMMENT ON TABLE F_SESSION_RESP_FORMS IS 'Хранит окончательные доступы пользователи к формам';

CREATE TABLE GROUP_SUBJECT_TYPES
(
  ID                      NUMBER(14) NOT NULL
    CONSTRAINT GROUP_SUBJECT_TYPES_PK
    PRIMARY KEY,
  GROUP_ID                NUMBER(14) NOT NULL,
  REF_SUBJECT_TYPE_REC_ID NUMBER(14) NOT NULL,
  IS_ACTIVE               NUMBER(1)  NOT NULL,
  CONSTRAINT GROUP_SUBJECT_TYPES_UQ
  UNIQUE (GROUP_ID, REF_SUBJECT_TYPE_REC_ID)
);

CREATE TABLE USER_SUBJECT_TYPES
(
  ID                      NUMBER(14) NOT NULL
    CONSTRAINT USER_SUBJECT_TYPES_PK
    PRIMARY KEY,
  USER_ID                 NUMBER(14) NOT NULL,
  REF_SUBJECT_TYPE_REC_ID NUMBER(14) NOT NULL,
  IS_ACTIVE               NUMBER(1)  NOT NULL,
  CONSTRAINT USER_SUBJECT_TYPES_UQ
  UNIQUE (USER_ID, REF_SUBJECT_TYPE_REC_ID)
);

create table F_SESSION_SUBJECT_TYPES
(
	USER_ID NUMBER(13) not null,
	SUBJECT_TYPE_ID NUMBER(13) not null,
	constraint F_SESSION_SUBJECT_TYPES_UK
		unique (USER_ID, SUBJECT_TYPE_ID)
);

CREATE TABLE GROUP_RESP_FORMS
(
  ID                    NUMBER(13)    NOT NULL
    CONSTRAINT GROUP_RESP_FORMS_ID_PK
    PRIMARY KEY,
  GROUP_ID              NUMBER(13)    NOT NULL,
  RIGHT_ITEM_ID         NUMBER(13)    NOT NULL,
  IS_ACTIVE             NUMBER(1)     NOT NULL,
  FORM_CODE             VARCHAR2(250) NOT NULL,
  REF_RESPONDENT_REC_ID NUMBER(13)    NOT NULL
);

CREATE TABLE USER_RESP_FORMS
(
  ID                    NUMBER(13)    NOT NULL
    CONSTRAINT USER_RESP_FORMS_ID_PK
    PRIMARY KEY,
  USER_ID               NUMBER(13)    NOT NULL,
  RIGHT_ITEM_ID         NUMBER(13)    NOT NULL,
  IS_ACTIVE             NUMBER(1)     NOT NULL,
  FORM_CODE             VARCHAR2(250) NOT NULL,
  REF_RESPONDENT_REC_ID NUMBER(13)    NOT NULL
);

CREATE TABLE USER_WARRANT
(
  ID         NUMBER(13) NOT NULL
    PRIMARY KEY,
  CODE       VARCHAR2(250),
  PRINCIPAL  NUMBER(13) NOT NULL,
  ATTORNEY   NUMBER(13) NOT NULL,
  BEGIN_DATE DATE       NOT NULL,
  END_DATE   DATE,
  READONLY   NUMBER,
  CANCELED   NUMBER
);
COMMENT ON TABLE USER_WARRANT IS 'Доверенности пользователей';
COMMENT ON COLUMN USER_WARRANT.PRINCIPAL IS 'доверитель или представляемый. Ссылка на ID таблицы F_USERS';
COMMENT ON COLUMN USER_WARRANT.ATTORNEY IS 'представитель или поверенный. Ссылка на ID таблицы F_USERS';
COMMENT ON COLUMN USER_WARRANT.BEGIN_DATE IS 'Дата начало доверенности';
COMMENT ON COLUMN USER_WARRANT.END_DATE IS 'Дата окончания доверенности. NULL - безсрочный';
COMMENT ON COLUMN USER_WARRANT.READONLY IS '0 - разрешено изменение
1 - запрет на изменение';
COMMENT ON COLUMN USER_WARRANT.CANCELED IS 'Аннулированный';


insert into FRSI_LOAD_GUIDE (FRSI_LOAD_GUIDE, NAME, CODE, TYPE, TABLE_NAME, DATE_LOAD, STATUS, MAT_NAME)
values (1, 'Справочник подразделений НБРК', 'ref_department', 'LX', 'NSI_NBRK_DEPARTMENT', to_date('29-03-2015 13:15:50', 'dd-mm-yyyy hh24:mi:ss'), 'загружен', null);
insert into FRSI_LOAD_GUIDE (FRSI_LOAD_GUIDE, NAME, CODE, TYPE, TABLE_NAME, DATE_LOAD, STATUS, MAT_NAME)
values (2, 'Справочник банков', 'ref_bank', 'LXMV', 'NSI_NBRK_BANK', to_date('29-03-2015 13:24:37', 'dd-mm-yyyy hh24:mi:ss'), 'загружен', 'MV_BANK');
insert into FRSI_LOAD_GUIDE (FRSI_LOAD_GUIDE, NAME, CODE, TYPE, TABLE_NAME, DATE_LOAD, STATUS, MAT_NAME)
values (3, 'Справочник стран', 'ref_country', 'LX', 'NSI_NBRK_COUNTRY', to_date('29-03-2015 13:25:02', 'dd-mm-yyyy hh24:mi:ss'), 'загружен', null);
insert into FRSI_LOAD_GUIDE (FRSI_LOAD_GUIDE, NAME, CODE, TYPE, TABLE_NAME, DATE_LOAD, STATUS, MAT_NAME)
values (4, 'Справочник городов/районов областей РК', 'ref_region', 'LX', 'NSI_NBRK_REGION', to_date('29-03-2015 13:25:32', 'dd-mm-yyyy hh24:mi:ss'), 'загружен', null);
insert into FRSI_LOAD_GUIDE (FRSI_LOAD_GUIDE, NAME, CODE, TYPE, TABLE_NAME, DATE_LOAD, STATUS, MAT_NAME)
values (5, 'Справочник валют', 'ref_currency', 'LXMV', 'NSI_NBRK_CURRENCY', to_date('29-03-2015 13:26:09', 'dd-mm-yyyy hh24:mi:ss'), 'загружен', 'MV_CURRENCY');
insert into FRSI_LOAD_GUIDE (FRSI_LOAD_GUIDE, NAME, CODE, TYPE, TABLE_NAME, DATE_LOAD, STATUS, MAT_NAME)
values (6, 'Справочник рейтинг валют', 'ref_currency_rate', 'LXMV', 'NSI_NBRK_CRCY_RATE', to_date('29-03-2015 13:26:49', 'dd-mm-yyyy hh24:mi:ss'), 'загружен', 'MV_CURRENCY_RATE');
insert into FRSI_LOAD_GUIDE (FRSI_LOAD_GUIDE, NAME, CODE, TYPE, TABLE_NAME, DATE_LOAD, STATUS, MAT_NAME)
values (7, 'Справочник эмитентов', 'ref_issuers', 'MV', null, to_date('29-03-2015 13:28:09', 'dd-mm-yyyy hh24:mi:ss'), 'загружен', 'MV_ISSUER');
insert into FRSI_LOAD_GUIDE (FRSI_LOAD_GUIDE, NAME, CODE, TYPE, TABLE_NAME, DATE_LOAD, STATUS, MAT_NAME)
values (8, 'Справочник ценных бумаг', 'ref_securities', 'MV', null, to_date('29-03-2015 13:28:09', 'dd-mm-yyyy hh24:mi:ss'), 'загружен', 'MV_SECURITY');
insert into FRSI_LOAD_GUIDE (FRSI_LOAD_GUIDE, NAME, CODE, TYPE, TABLE_NAME, DATE_LOAD, STATUS, MAT_NAME)
values (9, 'Справочник рейтинг агенств', 'ref_agent_rate', 'MV', null, to_date('29-03-2015 14:01:28', 'dd-mm-yyyy hh24:mi:ss'), 'загружен', 'MV_RATE_AGENCY');

CREATE OR REPLACE Procedure frsi_read_load_guide(
	 cur           OUT SYS_REFCURSOR,
   Err_Code      OUT NUMBER,
   Err_Msg       OUT VARCHAR2
)
IS
  Procnum CONSTANT VARCHAR2(24) := 'frsi_read_load_guide';
BEGIN
  OPEN cur FOR
      select t.frsi_load_guide,
             t.name,
             t.code,
             t.type,
             t.table_name,
             t.date_load,
             t.status,
             t.mat_name
      from
        frsi_load_guide t;

EXCEPTION
  WHEN NO_DATA_FOUND THEN
      NULL;

WHEN OTHERS THEN
   Err_Code := SQLCODE;
   Err_Msg := ProcNum || ' ' || SQLERRM;
   raise_application_error(-20001,'An error was encountered - '||SQLCODE||' -ERROR- '||SQLERRM);
END frsi_read_load_guide;
/
create or replace procedure frsi_refresh_mv(
  Name_    in  Varchar2,
  Err_Code out Number,
  Err_Msg  out VARCHAR2
)
is
  Procnum CONSTANT VARCHAR2(24) := 'frsi_refresh_mv';
begin
  Err_Code := 0;
  Err_Msg  := ' ';

  dbms_mview.refresh(Name_);

exception
  WHEN OTHERS THEN
    Err_Code := SQLCODE;
    Err_Msg := ProcNum || ' ' || SQLERRM;
    raise_application_error(-20001,'An error was encountered - '||SQLCODE||' -ERROR- '||SQLERRM);
end frsi_refresh_mv;
/

-- Create indices

CREATE INDEX ref_balance_account_code_idx ON ref_balance_account (code);

-- Insert initial data

Insert into REF_DELIVERY_WAY (ID,CODE,NAME_EN,NAME_KZ,NAME_RU,TAG) values (1,'XML','XML file','XML файл','XML файл',null);
Insert into REF_DELIVERY_WAY (ID,CODE,NAME_EN,NAME_KZ,NAME_RU,TAG) values (2,'EXCEL','EXCEL file','EXCEL файл','EXCEL файл',null);
Insert into REF_DELIVERY_WAY (ID,CODE,NAME_EN,NAME_KZ,NAME_RU,TAG) values (3,'WEB_FORM','Web form','Веб форма','Веб форма',null);

Insert into REF_FORM_STATUS (ID,CODE,NAME_EN,NAME_KZ,NAME_RU,TAG) values (1,'DRAFT','Draft','Алғашқы түрі','Черновик',null);
Insert into REF_FORM_STATUS (ID,CODE,NAME_EN,NAME_KZ,NAME_RU,TAG) values (2,'SIGNED','Signed','Қол қойылды','Подписана',null);
Insert into REF_FORM_STATUS (ID,CODE,NAME_EN,NAME_KZ,NAME_RU,TAG) values (3,'WAITING','Waiting','Кезекке қойылды','Ждёт обработки',null);
Insert into REF_FORM_STATUS (ID,CODE,NAME_EN,NAME_KZ,NAME_RU,TAG) values (4,'PROCESSING','Processing','Өңдеу басталды','Начата обработка',null);
Insert into REF_FORM_STATUS (ID,CODE,NAME_EN,NAME_KZ,NAME_RU,TAG) values (5,'COMPLETED','Processed successfully','Өңдеу ойдағыдай аяқталды','Обработана успешно',null);
Insert into REF_FORM_STATUS (ID,CODE,NAME_EN,NAME_KZ,NAME_RU,TAG) values (6,'ERROR','Processed with error','Өңдеу қате аяқталды','Обработана с ошибкой',null);
Insert into REF_FORM_STATUS (ID,CODE,NAME_EN,NAME_KZ,NAME_RU,TAG) values (7,'APPROVED','Approved','Бекітілді','Утверждена',null);
Insert into REF_FORM_STATUS (ID,CODE,NAME_EN,NAME_KZ,NAME_RU,TAG) values (8,'DISAPPROVED','Disapproved','Бекітілмеген','Разутверждена',null);

insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (25, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'uk', 'prud_array*sum:num:1+prud_array*sum:num:2', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (26, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'ik', 'prud_array*sum:num:10', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (27, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'av', 'prud_array*sum:num:12', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (28, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'uvo', 'prud_array*sum:num:13', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (29, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'or', 'prud_array*sum:num:7', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (30, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'k1', 'prud_array*sum:num:11-1', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (31, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'k2', 'prud_array*sum:num:11-2', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (32, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'sk', 'prud_array*sum:num:11', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (33, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'assets', 'prud_array*sum:num:13-1', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (34, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'opr', 'prud_array*sum:num:14-3', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (35, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'kf_sk_k1', 'prud_array*sum:num:14', null, .06, '>=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (36, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'kf_sk_k1-2', 'prud_array*sum:num:14-1', null, .06, '>=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (37, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'kf_sk_k1-3', 'prud_array*sum:num:14-2', null, .12, '>=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (38, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'max_risk_debtor', 'prud_array*sum:num:15', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (39, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'max_risk_k2', 'prud_array*sum:num:16', null, .25, '<=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (40, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'max_sp_org', 'prud_array*sum:num:17', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (41, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'max_sp_k2', 'prud_array*sum:num:18', null, 8, '<=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (42, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'likv_sum_ass', 'prud_array*sum:num:19', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (43, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'likv_sum_liab', 'prud_array*sum:num:20', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (44, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'likv_k3', 'prud_array*sum:num:21', null, .5, '>=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (45, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'oh4', 'prud_array*sum:num:22', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (46, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'k4', 'prud_array*sum:num:23', null, 1, '<=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (47, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'oh5', 'prud_array*sum:num:24', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (48, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'k5', 'prud_array*sum:num:25', null, 2, '<=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (49, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'oh6', 'prud_array*sum:num:26', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (50, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'k6', 'prud_array*sum:num:27', null, 3, '<=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (51, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudIO', 'result', 'prud_array*sum:num:0000', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (1, to_date('01-11-2014', 'dd-mm-yyyy'), 'InfoIO', 'assets', 'kl_1_array*sum:num_acc:ItogoAssets', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (2, to_date('01-11-2014', 'dd-mm-yyyy'), 'InfoIO', 'liability', 'kl_1_array*sum:num_acc:ItogoLiab', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (3, to_date('01-11-2014', 'dd-mm-yyyy'), 'InfoIO', 'cap', 'kl_1_array*sum:num_acc:ItogoCap', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (4, to_date('01-11-2014', 'dd-mm-yyyy'), 'InfoIO', 'surplus_earn', 'kl_1_array*sum:num_acc:3599', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (5, to_date('01-11-2014', 'dd-mm-yyyy'), 'InfoIO', 'zaim', 'kl_1_array*sum:num_acc:1301+kl_1_array*sum:num_acc:1302+kl_1_array*sum:num_acc:1303+kl_1_array*sum:num_acc:1304+kl_1_array*sum:num_acc:1305+kl_1_array*sum:num_acc:1306+kl_1_array*sum:num_acc:1309+kl_1_array*sum:num_acc:1321+kl_1_array*sum:num_acc:1322+kl_1_array*sum:num_acc:1323+kl_1_array*sum:num_acc:1326+kl_1_array*sum:num_acc:1327+kl_1_array*sum:num_acc:1328+kl_1_array*sum:num_acc:1401+kl_1_array*sum:num_acc:1403+kl_1_array*sum:num_acc:1405+kl_1_array*sum:num_acc:1407+kl_1_array*sum:num_acc:1409+kl_1_array*sum:num_acc:1411+kl_1_array*sum:num_acc:1417+kl_1_array*sum:num_acc:1420+kl_1_array*sum:num_acc:1421+kl_1_array*sum:num_acc:1422+kl_1_array*sum:num_acc:1423+kl_1_array*sum:num_acc:1424+kl_1_array*sum:num_acc:1425+kl_1_array*sum:num_acc:1429+kl_1_array*sum:num_acc:1461+kl_1_array*sum:num_acc:1462', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (52, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudIO', 'sk', 'prud_array*sum:num:11', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (53, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudIO', 'k1', 'prud_array*sum:num:14', null, .06, '>=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (54, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudIO', 'k1-2', 'prud_array*sum:num:14-1', null, .06, '>=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (55, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudIO', 'k1-3', 'prud_array*sum:num:14-2', null, .12, '>=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (56, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudIO', 'k2', 'prud_array*sum:num:16', null, .25, '<=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (57, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudIO', 'max_sp_k2', 'prud_array*sum:num:18', null, 8, '<=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (58, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudIO', 'k3', 'prud_array*sum:num:21', null, .5, '>=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (59, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudIO', 'k4', 'prud_array*sum:num:23', null, 1, '<=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (60, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudIO', 'k5', 'prud_array*sum:num:25', null, 2, '<=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (61, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudIO', 'k6', 'prud_array*sum:num:27', null, 3, '<=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (6, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondIO', 'auth_cap', 'kl_1_array*sum:num_acc:3000', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (7, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondIO', 'cap', 'kl_1_array*sum:num_acc:ItogoCap', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (8, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondIO', 'liab_vsego', 'kl_1_array*sum:num_acc:ItogoLiab', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (9, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondIO', 'liab_zaim', 'kl_1_array*sum:num_acc:2030+kl_1_array*sum:num_acc:2040+kl_1_array*sum:num_acc:2050+kl_1_array*sum:num_acc:2110+kl_1_array*sum:num_acc:2255', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (10, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondIO', 'liab_pastduebal', 'kl_1_array*sum:num_acc:2058+kl_1_array*sum:num_acc:2059+kl_1_array*sum:num_acc:2068', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (11, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondIO', 'liab_dep', 'kl_1_array*sum:num_acc:2120+kl_1_array*sum:num_acc:2200', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (12, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondIO', 'laib_other', 'liab_vsego-liab_zaim-liab_dep', 1, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (13, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondIO', 'ass_vsego', 'kl_1_array*sum:num_acc:ItogoAssets', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (14, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondIO', 'ass_money', 'kl_1_array*sum:num_acc:1000', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (15, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondIO', 'ass_operacc', 'kl_1_array*sum:num_acc:1053', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (16, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondIO', 'ass_cb', 'kl_1_array*sum:num_acc:1201+kl_1_array*sum:num_acc:1202+kl_1_array*sum:num_acc:1205+kl_1_array*sum:num_acc:1206+kl_1_array*sum:num_acc:1208+kl_1_array*sum:num_acc:1209+kl_1_array*sum:num_acc:1452+kl_1_array*sum:num_acc:1453+kl_1_array*sum:num_acc:1454+kl_1_array*sum:num_acc:1456+kl_1_array*sum:num_acc:1457+kl_1_array*sum:num_acc:1459+kl_1_array*sum:num_acc:1481+kl_1_array*sum:num_acc:1482+kl_1_array*sum:num_acc:1483+kl_1_array*sum:num_acc:1485', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (17, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondIO', 'ass_dep', 'kl_1_array*sum:num_acc:1250', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (18, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondIO', 'ass_zaimbank', 'kl_1_array*sum:num_acc:1301+kl_1_array*sum:num_acc:1302+kl_1_array*sum:num_acc:1303+kl_1_array*sum:num_acc:1304+kl_1_array*sum:num_acc:1305+kl_1_array*sum:num_acc:1306+kl_1_array*sum:num_acc:1309+kl_1_array*sum:num_acc:1321+kl_1_array*sum:num_acc:1322+kl_1_array*sum:num_acc:1323+kl_1_array*sum:num_acc:1326+kl_1_array*sum:num_acc:1327+kl_1_array*sum:num_acc:1328', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (19, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondIO', 'ass_client', 'kl_1_array*sum:num_acc:1401+kl_1_array*sum:num_acc:1403+kl_1_array*sum:num_acc:1405+kl_1_array*sum:num_acc:1407+kl_1_array*sum:num_acc:1409+kl_1_array*sum:num_acc:1411+kl_1_array*sum:num_acc:1417+kl_1_array*sum:num_acc:1420+kl_1_array*sum:num_acc:1421+kl_1_array*sum:num_acc:1422+kl_1_array*sum:num_acc:1423+kl_1_array*sum:num_acc:1424+kl_1_array*sum:num_acc:1425+kl_1_array*sum:num_acc:1429+kl_1_array*sum:num_acc:1461+kl_1_array*sum:num_acc:1462', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (20, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondIO', 'ass_pastduebal', 'kl_1_array*sum:num_acc:1424', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (21, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondIO', 'ass_prov', 'kl_1_array*sum:num_acc:1428', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (22, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondIO', 'ass_os', 'kl_1_array*sum:num_acc:1650_1690', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (23, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondIO', 'ass_other', 'ass_vsego-ass_money-ass_operacc-ass_cb-ass_dep-ass_zaimbank-ass_client-ass_prov-ass_os', 1, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (24, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondIO', 'surplus_earn', 'kl_1_array*sum:num_acc:3599', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (118, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudOT', 'mpuk', 'prud_ot_array*sum:num:1+prud_ot_array*sum:num:2+prud_ot_array*sum:num:3', null, 277480000, '>=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (119, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudOT', 'mpsk', 'prud_ot_array*sum:num:11', null, 277480000, '>=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (120, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudOT', 'k1', 'prud_ot_array*sum:num:17', null, .5, '>=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (121, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudOT', 'k2', 'prud_ot_array*sum:num:23', null, .015, '>=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (122, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudOT', 'result', 'prud_ot_array*sum:num:0000', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (62, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudIO', 'result', 'prud_array*sum:num:0000', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (63, to_date('01-11-2014', 'dd-mm-yyyy'), 'InfoNBO', 'assets', 'f1_apk_oo_kp_cdcb_array*endpr_sum:code:22', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (64, to_date('01-11-2014', 'dd-mm-yyyy'), 'InfoNBO', 'liability', 'f1_apk_oo_kp_cdcb_array*endpr_sum:code:35', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (65, to_date('01-11-2014', 'dd-mm-yyyy'), 'InfoNBO', 'cap', 'f1_apk_oo_kp_cdcb_array*endpr_sum:code:43', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (66, to_date('01-11-2014', 'dd-mm-yyyy'), 'InfoNBO', 'surplus_earn', 'f1_apk_oo_kp_cdcb_array*endpr_sum:code:41.2', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (67, to_date('01-11-2014', 'dd-mm-yyyy'), 'InfoNBO', 'zaim', 'f1_apk_oo_kp_cdcb_array*endpr_sum:code:12', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (68, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondNBO', 'ass_itogo', 'f1_apk_oo_kp_cdcb_array*endpr_sum:code:22', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (69, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondNBO', 'ass_mon', 'f1_apk_oo_kp_cdcb_array*endpr_sum:code:1', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (70, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondNBO', 'ass_dep', 'f1_apk_oo_kp_cdcb_array*endpr_sum:code:10', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (71, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondNBO', 'ass_cb', 'f1_apk_oo_kp_cdcb_array*endpr_sum:code:3+f1_array*endpr_sum:code:5+f1_array*endpr_sum:code:8', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (72, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondNBO', 'ass_zaim', 'f1_apk_oo_kp_cdcb_array*endpr_sum:code:12', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (73, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondNBO', 'ass_invest', 'f1_apk_oo_kp_cdcb_array*endpr_sum:code:14', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (74, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondNBO', 'ass_debtzadol', 'f1_apk_oo_kp_cdcb_array*endpr_sum:code:6', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (75, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondNBO', 'ass_other', 'ass_itogo-ass_mon-ass_dep-ass_cb-ass_zaim-ass_invest-ass_debtzadol', 1, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (76, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondNBO', 'liab_itogo', 'f1_apk_oo_kp_cdcb_array*endpr_sum:code:35', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (77, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondNBO', 'liab_dep', 'f1_apk_oo_kp_cdcb_array*endpr_sum:code:23', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (78, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondNBO', 'liab_zaim', 'f1_apk_oo_kp_cdcb_array*endpr_sum:code:27', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (79, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondNBO', 'liab_cb', 'f1_apk_oo_kp_cdcb_array*endpr_sum:code:25', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (80, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondNBO', 'liab_credzadol', 'f1_apk_oo_kp_cdcb_array*endpr_sum:code:28', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (81, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondNBO', 'liab_other', 'liab_itogo-liab_dep-liab_zaim-liab_cb-liab_credzadol', 1, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (82, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondNBO', 'cap_itogo', 'f1_apk_oo_kp_cdcb_array*endpr_sum:code:43', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (83, to_date('01-11-2014', 'dd-mm-yyyy'), 'FinCondNBO', 'cap_surplus_earn', 'f1_apk_oo_kp_cdcb_array*endpr_sum:code:41.2', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (84, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudKP', 'sk', 'prud_kp_array*value:num:1', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (85, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudKP', 'coeff_ss', 'prud_kp_array*value:num:3', null, .12, '>=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (86, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudKP', 'coeff_likv', 'prud_kp_array*value:num:6', null, .3, '>=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (91, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'uk', 'prud_apk_array*sum:num:1+prud_apk_array*sum:num:2', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (92, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'ik', 'prud_apk_array*sum:num:10', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (93, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'av', 'prud_apk_array*sum:num:12', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (94, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'uvo', 'prud_apk_array*sum:num:13', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (95, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'or', 'prud_apk_array*sum:num:7', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (96, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'k1', 'prud_apk_array*sum:num:11-1', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (97, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'k2', 'prud_apk_array*sum:num:11-2', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (98, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'sk', 'prud_apk_array*sum:num:11', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (99, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'assets', 'prud_apk_array*sum:num:13-1', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (100, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'opr', 'prud_apk_array*sum:num:14-3', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (101, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'kf_sk_k1', 'prud_apk_array*sum:num:14', null, .06, '>=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (102, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'kf_sk_k1-2', 'prud_apk_array*sum:num:14-1', null, .06, '>=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (103, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'kf_sk_k1-3', 'prud_apk_array*sum:num:14-2', null, .12, '>=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (104, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'max_risk_debtor', 'prud_apk_array*sum:num:15', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (105, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'max_risk_k2', 'prud_apk_array*sum:num:16', null, .25, '<=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (106, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'max_sp_org', 'prud_apk_array*sum:num:17', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (107, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'max_sp_k2', 'prud_apk_array*sum:num:18', null, 8, '<=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (108, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'likv_sum_ass', 'prud_apk_array*sum:num:19', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (109, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'likv_sum_liab', 'prud_apk_array*sum:num:20', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (110, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'likv_k3', 'prud_apk_array*sum:num:21', null, .5, '>=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (111, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'oh4', 'prud_apk_array*sum:num:22', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (112, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'k4', 'prud_apk_array*sum:num:23', null, 1, '<=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (113, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'oh5', 'prud_apk_array*sum:num:24', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (114, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'k5', 'prud_apk_array*sum:num:25', null, 2, '<=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (115, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'oh6', 'prud_apk_array*sum:num:26', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (116, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'k6', 'prud_apk_array*sum:num:27', null, 3, '<=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (117, to_date('01-11-2014', 'dd-mm-yyyy'), 'ExecPrudNBO', 'result', 'prud_apk_array*sum:num:0000', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (123, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudKP', 'open_cur_pos_A', 'currency_kp*a_pos::/currency_kp*equity_basis::*100', null, .15, '<=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (124, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudKP', 'open_cur_pos_other', 'currency_kp*b_pos::/currency_kp*equity_basis::*100', null, .075, '<=');
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (125, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudKP', 'open_cur_pos_result', 'prud_kp_array*value:num:0000', null, null, null);
insert into GUIDE_REPORTS_FORUMULA (ID, REPORT_DATE, FORMNAME, FIELDNAME, FORMULA, IS_CALC_OTHER_FIELD, COEFF, CONDITION)
values (126, to_date('01-11-2014', 'dd-mm-yyyy'), 'PrudKP', 'result', 'prud_kp_array*value:num:0000', null, null, null);
