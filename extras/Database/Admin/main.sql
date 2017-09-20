-- Create table
create table ROLE
(
  id     NUMBER(14) not null,
  name   VARCHAR2(50) not null
);
-- Create/Recreate primary, unique and foreign key constraints 
alter table ROLE
  add constraint PK_ROLE primary key (ID);

-- Add/modify columns 
alter table GROUPS add ref_department_rec_id NUMBER(14);
alter table GROUPS add ref_subject_type_rec_id NUMBER(14);
alter table GROUPS add role_id NUMBER(14);
-- Add comments to the columns 
comment on column GROUPS.ref_department_rec_id
  is 'Id ������� ��� ������������� - �������';
comment on column GROUPS.ref_subject_type_rec_id
  is 'Id ������� ��� ����� �����������';
comment on column GROUPS.role_id
  is '���� ������';
-- Create/Recreate primary, unique and foreign key constraints 
alter table GROUPS
  add constraint FK_GROUPS_RO foreign key (ROLE_ID)
  references role (ID);

-- Drop columns 
alter table F_USERS drop column user_type;

-- Create/Recreate primary, unique and foreign key constraints 
alter table GROUP_USERS
  add constraint GROUP_USERS_UN unique (GROUP_ID, USER_ID);

-- Create sequence 
create sequence SEQ_GROUP_DEPARTMENTS_ID
minvalue 1
maxvalue 9999999999999999999999999999
start with 1
increment by 1
cache 20;

-- Create sequence 
create sequence SEQ_USER_DEPARTMENTS_ID
minvalue 1
maxvalue 9999999999999999999999999999
start with 1
increment by 1
cache 20;

-- Create table
create table GROUP_DEPARTMENTS
(
  id                    NUMBER(14) not null,
  group_id              NUMBER(14) not null,
  ref_department_rec_id NUMBER(14) not null,
  is_active             NUMBER(1) not null
);
alter table GROUP_DEPARTMENTS
  add constraint GROUP_DEPARTMENTS_PK primary key (ID);
alter table GROUP_DEPARTMENTS
  add constraint GROUP_DEPARTMENTS_UK unique (GROUP_ID, REF_DEPARTMENT_REC_ID);

create table USER_DEPARTMENTS
(
  id                    NUMBER(14) not null,
  user_id              NUMBER(14) not null,
  ref_department_rec_id NUMBER(14) not null,
  is_active             NUMBER(1) not null
);
alter table USER_DEPARTMENTS
  add constraint USER_DEPARTMENTS_PK primary key (ID);
alter table USER_DEPARTMENTS
  add constraint USER_DEPARTMENTS_UK unique (USER_ID, REF_DEPARTMENT_REC_ID);

-- Create table
create table F_SESSION_DEPARTMENTS
(
  user_id     NUMBER(13) not null,
  department_id NUMBER(13) not null
);
-- Create/Recreate primary, unique and foreign key constraints 
alter table F_SESSION_DEPARTMENTS
  add constraint F_SESSION_DEPARTMENTS_UK unique (USER_ID, DEPARTMENT_ID);

insert into ROLE (id, name)
values (1, '�������������');
insert into ROLE (id, name)
values (2, '������������� (������)');
insert into ROLE (id, name)
values (3, '������������ �� ��');
insert into ROLE (id, name)
values (4, '������������ �� �� (������)');
insert into ROLE (id, name)
values (5, '����������');

insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2086, 883, 'PERMIS:USER_GROUP', null, '������', null, 4, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2087, 2086, 'PERMIS:USER_GROUP:ADD', null, '�������� ������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2088, 2086, 'PERMIS:USER_GROUP:EDIT', null, '�������� ������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2089, 2086, 'PERMIS:USER_GROUP:DELETE', null, '������� ������', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2090, 883, 'PERMIS:USER', null, '������������', null, 5, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2091, 2090, 'PERMIS:USER:ADD_TO_GROUP', null, '�������� ������������ � ������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2092, 2090, 'PERMIS:USER:EDIT', null, '�������� ������������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2093, 2090, 'PERMIS:USER:DELETE_FROM_GROUP', null, '������� ������������ �� ������', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2094, 883, 'PERMIS:GROUP', null, '����', null, 6, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2095, 2094, 'PERMIS:GROUP:ADD', null, '�������� ������ �� ����', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2096, 2094, 'PERMIS:GROUP:DELETE', null, '������� ������ �� ����', null, 2, 0);
commit;
