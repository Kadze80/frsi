prompt PL/SQL Developer import file
prompt Created on 3 ������� 2016 �. by Baurzhan.Baisholakov
set feedback off
set define off
prompt Loading RIGHT_ITEMS...
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (882, null, 'FRSI', null, '��� ����������� ������������ �������������� ���������Ȼ', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (883, 882, 'PERMIS', null, '����������������� ���� �������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (884, 883, 'PERMIS:UPDATE', null, '��������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (885, 883, 'PERMIS:GROUP_PERMIS', null, '����� ������� ������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (886, 883, 'PERMIS:USER_PERMIS', null, '����� ������� ������������', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (887, 882, 'RESP:FORM', null, '������ ����', null, 4, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (888, 887, 'RESP:FORM:SIGN', null, '���������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (889, 887, 'RESP:FORM:SEND', null, '���������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (890, 887, 'RESP:FORM:HISTORY', null, '������� ��������', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21001, 882, 'ADM_FORMS', null, ' ����������������� ����', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (892, 887, 'RESP:FORM:PROPS', null, '��������', null, 4, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (893, 887, 'RESP:FORM:COPY', null, '�����������', null, 5, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (894, 887, 'RESP:FORM:DOWNLOAD_EXCEL', null, '��������� � Excel', null, 6, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2074, 914, 'SU:TEMPL:EDIT_XLS_OUT', null, '�������� ���. XLS', null, 6, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (896, 887, 'RESP:FORM:CONTROL', null, '�������� �����', null, 8, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2077, 947, 'SU:REF:REF_LISTING_ESTIMATION', null, '���������� ����������� ������', null, 28, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (898, 882, 'RESP:UPLOAD', null, '�������� ������', null, 5, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (899, 898, 'RESP:UPLOAD:PROCESS', null, '�������� ������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (900, 882, 'RESP:DOWNLOAD', null, '������� ���� (�����������)', null, 6, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (901, 900, 'RESP:DOWNLOAD:EXCEL', null, '��������� � Excel', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2078, 947, 'SU:REF:REF_RATING_ESTIMATION', null, '���������� ����������� ������', null, 29, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2079, 947, 'SU:REF:REF_RATING_CATEGORY', null, '���������� ��������� ����������� ������', null, 30, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21002, 21001, 'ADM_FORMS:ADD', null, '��������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21003, 21001, 'ADM_FORMS:DELETE', null, '�������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21067, 887, 'RESP:FORM:ATTACH_FILE', null, '������������� �������', null, 9, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (907, 882, 'SU:FORMS', null, '������� �����', null, 8, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (908, 907, 'SU:FORMS:HISTORY', null, '������� ��������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21068, 907, 'SU:FORMS:ATTACH_FILE', null, '������������� �������', null, 7, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (910, 907, 'SU:FORMS:PROPS', null, '��������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (911, 907, 'SU:FORMS:DOWNLOAD_EXCEL', null, '��������� � Excel', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (912, 907, 'SU:FORMS:CONTROL', null, '�������� �����', null, 5, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (914, 882, 'SU:TEMPL', null, '������� ����', null, 9, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (915, 914, 'SU:TEMPL:PROPS', null, '��������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (916, 914, 'SU:TEMPL:VALIDATE', null, '���������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (917, 914, 'SU:TEMPL:VIEW', null, '��������', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (918, 914, 'SU:TEMPL:ADD', null, '��������', null, 4, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (919, 914, 'SU:TEMPL:EDIT', null, '��������', null, 5, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (920, 914, 'SU:TEMPL:EDIT_XLS', null, '�������� XLS', null, 6, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (921, 914, 'SU:TEMPL:DELETE', null, '�������', null, 7, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (922, 914, 'SU:TEMPL:UPD_BA', null, '�������� ������ ���������� ������', null, 8, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (923, 914, 'SU:TEMPL:UPD_BA_OUT', null, '�������� ������ ���������� ������ (����)', null, 9, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (924, 882, 'REP_STAT', null, '������� ��� ������� ����', null, 10, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21117, 882, 'ADM:REF', null, '�������� ������������', null, 14, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (926, 924, 'ST:DRAFT', null, '��������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (927, 924, 'ST:SIGNED', null, '��������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21118, 21117, 'ADM:REF:REFRESH', null, '��������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21145, 21144, 'SU:REF:REF_DEAL_BALANCE_ACC:VIEW', null, '�������� ����������� ���������� ������ ��� ������� � �������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21146, 21144, 'SU:REF:REF_DEAL_BALANCE_ACC:EDIT', null, '������� ����������� ���������� ������ ��� ������� � �������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (931, 924, 'ST:ERROR', null, '������', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (932, 924, 'ST:COMPLETED', null, '�� ���������', null, 4, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21144, 947, 'SU:REF:REF_DEAL_BALANCE_ACC', null, '���������� ���������� ������ ��� ������� � �������', null, 49, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (934, 924, 'ST:APPROVED', null, '���������', null, 5, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (935, 924, 'ST:DISAPPROVED', null, '������������', null, 6, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (936, 882, 'OUT_REP_STAT', null, '������� ��� ��������/�������', null, 11, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2094, 883, 'PERMIS:SITE', null, '����', null, 6, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (938, 936, 'OUT_ST:COMPLETED', null, '�� ���������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (939, 936, 'OUT_ST:APPROVED', null, '���������', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2095, 2094, 'PERMIS:GROUP:ADD', null, '�������� ����', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (941, null, 'F:SHOW', null, '��������', null, 1, 1);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (942, null, 'F:EDIT', null, '�������', null, 2, 1);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (943, null, 'F:DELETE', null, '��������', null, 3, 1);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (944, null, 'F:DECLINE', null, '����������', null, 4, 1);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (945, null, 'F:APPROVE', null, '�����������', null, 5, 1);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (946, null, 'F:DISAPPROVE', null, '��������������', null, 6, 1);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2096, 2094, 'PERMIS:GROUP:DELETE', null, '������� ����', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2020, 948, 'SU:REF:REF_BALANCE_ACCOUNT:VIEW', null, '�������� ����������� ���������� ������ ��� ������� � ��������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2021, 948, 'SU:REF:REF_BALANCE_ACCOUNT:EDIT', null, '������� ����������� ���������� ������ ��� ������� � ��������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2022, 951, 'SU:REF:REF_BANK:VIEW', null, '�������� ����������� ������ ������� ������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2023, 951, 'SU:REF:REF_BANK:EDIT', null, '������� ����������� ������ ������� ������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2024, 954, 'SU:REF:REF_CURRENCY:VIEW', null, '�������� ����������� �����', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2025, 954, 'SU:REF:REF_CURRENCY:EDIT', null, '������� ����������� �����', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2026, 957, 'SU:REF:REF_TYPE_PROVIDE:VIEW', null, '�������� ����������� ����� �����������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2027, 957, 'SU:REF:REF_TYPE_PROVIDE:EDIT', null, '������� ����������� ����� �����������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2028, 960, 'SU:REF:REF_VID_OPER:VIEW', null, '�������� ����������� ����� ��������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2029, 960, 'SU:REF:REF_VID_OPER:EDIT', null, '������� ����������� ����� ��������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21063, 882, 'AUDIT', null, '������ ������', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21064, 21063, 'AUDIT:REFRESH', null, '��������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2032, 966, 'SU:REF:REF_REGION:VIEW', null, '�������� ����������� ������� � ��������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2033, 966, 'SU:REF:REF_REGION:EDIT', null, '������� ����������� ������� � ��������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2034, 972, 'SU:REF:REF_POST:VIEW', null, '�������� ����������� ����������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2035, 972, 'SU:REF:REF_POST:EDIT', null, '������� ����������� ����������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2036, 975, 'SU:REF:REF_CROSSCHECK:VIEW', null, '�������� ����������� ������������ � ��������������� ���������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2037, 975, 'SU:REF:REF_CROSSCHECK:EDIT', null, '������� ����������� ������������ � ��������������� ���������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2038, 978, 'SU:REF:REF_TYPE_BUS_ENTITY:VIEW', null, '�������� ����������� ��������������-�������� �����', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2039, 978, 'SU:REF:REF_TYPE_BUS_ENTITY:EDIT', null, '������� ����������� ��������������-�������� �����', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2040, 981, 'SU:REF:REF_BRANCH:VIEW', null, '�������� ����������� ��������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2041, 981, 'SU:REF:REF_BRANCH:EDIT', null, '������� ����������� ��������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2042, 984, 'SU:REF:REF_RESPONDENT:VIEW', null, '�������� ����������� ����������� �����������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2043, 984, 'SU:REF:REF_RESPONDENT:EDIT', null, '������� ����������� ����������� �����������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2044, 987, 'SU:REF:REF_DEPARTMENT:VIEW', null, '�������� ����������� ������������� �� ��', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2045, 987, 'SU:REF:REF_DEPARTMENT:EDIT', null, '������� ����������� ������������� �� ��', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2046, 990, 'SU:REF:REF_REPORTS_RULES:VIEW', null, '�������� ����������� ������ �������� ����', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2047, 990, 'SU:REF:REF_REPORTS_RULES:EDIT', null, '������� ����������� ������ �������� ����', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2048, 993, 'SU:REF:REF_CONN_ORG:VIEW', null, '�������� ����������� ��������� ����������� � ����������� ������������ ������� �����������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2049, 993, 'SU:REF:REF_CONN_ORG:EDIT', null, '������� ����������� ��������� ����������� � ����������� ������������ ������� �����������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2050, 996, 'SU:REF:REF_CURRENCY_RATE:VIEW', null, '�������� ����������� �������� �����', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2051, 996, 'SU:REF:REF_CURRENCY_RATE:EDIT', null, '������� ����������� �������� �����', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2052, 999, 'SU:REF:REF_RATE_AGENCY:VIEW', null, '�������� ����������� ����������� �������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2053, 999, 'SU:REF:REF_RATE_AGENCY:EDIT', null, '������� ����������� ����������� �������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2054, 1002, 'SU:REF:REF_MANAGERS:VIEW', null, '�������� ����������� ���������� ����', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2055, 1002, 'SU:REF:REF_MANAGERS:EDIT', null, '������� ����������� ���������� ����', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2056, 1005, 'SU:REF:REF_COUNTRY:VIEW', null, '�������� ����������� �����', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2057, 1005, 'SU:REF:REF_COUNTRY:EDIT', null, '������� ����������� �����', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2058, 1008, 'SU:REF:REF_DOC_TYPE:VIEW', null, '�������� ����������� ����� ����������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2059, 1008, 'SU:REF:REF_DOC_TYPE:EDIT', null, '������� ����������� ����� ����������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2060, 1011, 'SU:REF:REF_SUBJECT_TYPE:VIEW', null, '�������� ����������� ����� ���������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2061, 1011, 'SU:REF:REF_SUBJECT_TYPE:EDIT', null, '������� ����������� ����� ���������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2062, 1014, 'SU:REF:REF_TRANS_TYPES:VIEW', null, '�������� ����������� ����� ������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2063, 1014, 'SU:REF:REF_TRANS_TYPES:EDIT', null, '������� ����������� ����� ������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2064, 1017, 'SU:REF:REF_REQUIREMENT:VIEW', null, '�������� ����������� ���������� � ������������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2065, 1017, 'SU:REF:REF_REQUIREMENT:EDIT', null, '������� ����������� ���������� � ������������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2066, 1020, 'SU:REF:REF_PERSON:VIEW', null, '�������� ����������� ���������� ���', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2067, 1020, 'SU:REF:REF_PERSON:EDIT', null, '������� ����������� ���������� ���', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2068, 1023, 'SU:REF:REF_SECURITIES:VIEW', null, '�������� ����������� ������ �����', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2069, 1023, 'SU:REF:REF_SECURITIES:EDIT', null, '������� ����������� ������ �����', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2070, 1026, 'SU:REF:REF_ISSUERS:VIEW', null, '�������� ����������� ���������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2071, 1026, 'SU:REF:REF_ISSUERS:EDIT', null, '������� ����������� ���������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2072, 1029, 'SU:REF:REF_LEGAL_PERSON:VIEW', null, '�������� ����������� ����������� ���', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2073, 1029, 'SU:REF:REF_LEGAL_PERSON:EDIT', null, '������� ����������� ����������� ���', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21141, 947, 'SU:REF:REF_MFO_REG', null, '���������� ������� ���', null, 48, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21142, 21141, 'SU:REF:REF_MFO_REG:VIEW', null, '�������� ����������� ������� ���', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21143, 21141, 'SU:REF:REF_MFO_REG:EDIT', null, '������� ����������� ������� ���', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1032, 900, 'RESP:DOWNLOAD:EXCEL_ID', null, '��������� �������������� � Excel', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21119, 951, 'SU:REF:REF_BANK:LOAD', null, '�������� ����������� ������ ������� ������', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21126, 947, 'SU:REF:REF_ORG_TYPE', null, '���������� ����� �����������', null, 47, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21127, 21126, 'SU:REF:REF_ORG_TYPE:VIEW', null, '�������� ����������� ����� �����������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21128, 21126, 'SU:REF:REF_ORG_TYPE:EDIT', null, '������� ����������� ����� �����������', null, 2, 0);
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
values (2000, 882, 'SU:OUT', null, '�������� � ������� �����', null, 7, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2001, 2000, 'SU:OUT:NEW', null, '������������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2002, 2000, 'SU:OUT:INPUT_REPORTS', null, '������� ������', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2003, 2000, 'SU:OUT:HISTORY', null, '������� ��������', null, 4, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21020, 2000, 'SU:OUT:NEW:DRAFT', null, '������������ ��������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2005, 2000, 'SU:OUT:PROPS', null, '��������', null, 5, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2006, 2000, 'SU:OUT:DOWNLOAD_EXCEL', null, '��������� � Excel', null, 6, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2080, 2077, 'SU:REF:REF_LISTING_ESTIMATION:VIEW', null, '�������� ����������� ����������� ������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2008, 2000, 'SU:OUT:CONTROL', null, '�������� �����', null, 8, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2081, 2077, 'SU:REF:REF_LISTING_ESTIMATION:EDIT', null, '������� ����������� ����������� ������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2082, 2078, 'SU:REF:REF_RATING_ESTIMATION:VIEW', null, '�������� ����������� ����������� ������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2083, 2078, 'SU:REF:REF_RATING_ESTIMATION:EDIT', null, '������� ����������� ����������� ������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2084, 2079, 'SU:REF:REF_RATING_CATEGORY:VIEW', null, '�������� ����������� ��������� ����������� ������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2085, 2079, 'SU:REF:REF_RATING_CATEGORY:EDIT', null, '������� ����������� ��������� ����������� ������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21114, 947, 'SU:REF:REF_MRP', null, '���������� ���', null, 46, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21115, 21114, 'SU:REF:REF_MRP:VIEW', null, '�������� ����������� ���', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21116, 21114, 'SU:REF:REF_MRP:EDIT', null, '������� ����������� ���', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21129, 887, 'RESP:FORM:ATTACH_LETTER', null, '���������������� ������', null, 10, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21130, 907, 'SU:FORMS:ATTACH_LETTER', null, '���������������� ������', null, 8, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21132, 21131, 'DESIGNER:INPUT', null, '������� �����', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21133, 21131, 'DESIGNER:OUTPUT', null, '�������� �����', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21134, 21131, 'DESIGNER:CROSSCHECK_CONTROL', null, '��������������� � ������������ ��������', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21135, 21132, 'DESIGNER:INPUT:DOWNLOAD_TEMPLATE', null, '�������� ��������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21136, 21132, 'DESIGNER:INPUT:UPLOAD_TEMPLATE', null, '�������� ��������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21137, 21133, 'DESIGNER:OUTPUT:DOWNLOAD_TEMPLATE', null, '�������� ��������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21138, 21133, 'DESIGNER:OUTPUT:UPLOAD_TEMPLATE', null, '�������� ��������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21139, 21134, 'DESIGNER:CROSSCHECK_CONTROL:DOWNLOAD', null, '��������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21140, 21134, 'DESIGNER:CROSSCHECK_CONTROL:UPLOAD', null, '��������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21131, 882, 'DESIGNER', null, '�������� ����', null, 17, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (947, 882, 'SU:REF', null, '�����������', null, 12, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21040, 2000, 'SU:OUT:SIGN:EDIT', null, '����������/�������� �������', null, 10, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21060, 887, 'RESP:FORM:PRINT', null, '������', null, 7, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (948, 947, 'SU:REF:REF_BALANCE_ACCOUNT', null, '���������� ����������� ���������� ������ ��� ����������� ������ ����������� �������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21061, 2000, 'SU:OUT:PRINT', null, '������', null, 7, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21062, 907, 'SU:FORMS:PRINT', null, '������', null, 4, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (951, 947, 'SU:REF:REF_BANK', null, '���������� ������ ������� ������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (954, 947, 'SU:REF:REF_CURRENCY', null, '���������� �����', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (957, 947, 'SU:REF:REF_TYPE_PROVIDE', null, '���������� ����� �����������', null, 4, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (960, 947, 'SU:REF:REF_VID_OPER', null, '���������� ����� ��������', null, 5, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (966, 947, 'SU:REF:REF_REGION', null, '���������� ������� � ��������', null, 7, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21120, 882, 'INFO', null, '�������', null, 15, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21121, 21120, 'INFO:GENERATE', null, '������������ �������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21122, 21120, 'INFO:DOWN_PRINT', null, '�������� � Excel � ������', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21123, 882, 'TEMPLATE', null, '�������', null, 16, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (972, 947, 'SU:REF:REF_POST', null, '���������� ����������', null, 8, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21124, 21123, 'TEMPLATE:VIEW', null, '��������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21125, 21123, 'TEMPLATE:EDIT', null, '���������', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (975, 947, 'SU:REF:REF_CROSSCHECK', null, '���������� ������������ � ��������������� ���������', null, 9, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (978, 947, 'SU:REF:REF_TYPE_BUS_ENTITY', null, '���������� ��������������-�������� ����', null, 10, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (981, 947, 'SU:REF:REF_BRANCH', null, '���������� ��������', null, 11, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (984, 947, 'SU:REF:REF_RESPONDENT', null, '���������� ����������� �����������', null, 12, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (987, 947, 'SU:REF:REF_DEPARTMENT', null, '���������� ������������� �� ��', null, 13, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (990, 947, 'SU:REF:REF_REPORTS_RULES', null, '���������� ������ �������� ����', null, 14, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (993, 947, 'SU:REF:REF_CONN_ORG', null, '���������� ��������� ����������� � ����������� ������������ ������� �����������', null, 15, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (996, 947, 'SU:REF:REF_CURRENCY_RATE', null, '���������� �������� �����', null, 16, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (999, 947, 'SU:REF:REF_RATE_AGENCY', null, '���������� ����������� �������', null, 17, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1002, 947, 'SU:REF:REF_MANAGERS', null, '���������� ���������� ����', null, 18, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1005, 947, 'SU:REF:REF_COUNTRY', null, '���������� �����', null, 19, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1008, 947, 'SU:REF:REF_DOC_TYPE', null, '���������� ����� ����������', null, 20, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1011, 947, 'SU:REF:REF_SUBJECT_TYPE', null, '���������� ����� ���������', null, 21, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1014, 947, 'SU:REF:REF_TRANS_TYPES', null, '���������� ����� ������', null, 22, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1017, 947, 'SU:REF:REF_REQUIREMENT', null, '���������� ���������� � ������������', null, 23, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1020, 947, 'SU:REF:REF_PERSON', null, '���������� ���������� ���', null, 24, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1023, 947, 'SU:REF:REF_SECURITIES', null, '���������� ������ �����', null, 25, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1026, 947, 'SU:REF:REF_ISSUERS', null, '���������� ���������', null, 26, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1029, 947, 'SU:REF:REF_LEGAL_PERSON', null, '���������� ����������� ���', null, 27, 0);
prompt 201 records loaded
set feedback on
set define on
prompt Done.
