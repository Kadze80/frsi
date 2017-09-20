-- Create table
create table GUIDE_REPORTS_FORUMULA
(
  ID                  NUMBER not null,
  REPORT_DATE         DATE not null,
  FORMNAME            VARCHAR2(255) not null,
  FIELDNAME           VARCHAR2(255) not null,
  FORMULA             VARCHAR2(4000) not null,
  IS_CALC_OTHER_FIELD NUMBER
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
-- Create/Recreate primary, unique and foreign key constraints
alter table GUIDE_REPORTS_FORUMULA
  add constraint GUIDE_REPORTS_FORUMULA_PK primary key (ID)
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
