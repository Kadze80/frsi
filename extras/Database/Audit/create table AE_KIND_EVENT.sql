-- Create table
create table AE_KIND_EVENT
(
  id   NUMBER not null,
  name VARCHAR2(500 CHAR)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255;
-- Add comments to the table 
comment on table AE_KIND_EVENT
  is 'Таблица видов событий аудита';
-- Add comments to the columns 
comment on column AE_KIND_EVENT.id
  is 'Id Таблцы';
comment on column AE_KIND_EVENT.name
  is 'Наименование';
-- Create/Recreate primary, unique and foreign key constraints 
alter table AE_KIND_EVENT
  add constraint PK_AE_KIND_EVENT primary key (ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255;
