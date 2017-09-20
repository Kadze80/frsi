-- Create table
create table AE_NAME_EVENT
(
  id   NUMBER not null,
  name VARCHAR2(500 CHAR)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255;
-- Add comments to the table 
comment on table AE_NAME_EVENT
  is 'Таблица наименования аудируемого события';
-- Add comments to the columns 
comment on column AE_NAME_EVENT.id
  is 'ID таблицы';
comment on column AE_NAME_EVENT.name
  is 'Наименование';
-- Create/Recreate primary, unique and foreign key constraints 
alter table AE_NAME_EVENT
  add constraint PK_AE_NAME_EVENT primary key (ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255;
