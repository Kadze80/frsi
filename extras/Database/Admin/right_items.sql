prompt PL/SQL Developer import file
prompt Created on 3 Октябрь 2016 г. by Baurzhan.Baisholakov
set feedback off
set define off
prompt Loading RIGHT_ITEMS...
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (882, null, 'FRSI', null, 'АИП «ФИНАНСОВЫЕ РЕГУЛЯТОРНЫЕ СТАТИСТИЧЕСКИЕ ПОКАЗАТЕЛИ»', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (883, 882, 'PERMIS', null, 'Администрирование прав доступа', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (884, 883, 'PERMIS:UPDATE', null, 'Обновить', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (885, 883, 'PERMIS:GROUP_PERMIS', null, 'Права доступа группы', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (886, 883, 'PERMIS:USER_PERMIS', null, 'Права доступа пользователя', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (887, 882, 'RESP:FORM', null, 'Журнал форм', null, 4, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (888, 887, 'RESP:FORM:SIGN', null, 'Подписать', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (889, 887, 'RESP:FORM:SEND', null, 'Отправить', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (890, 887, 'RESP:FORM:HISTORY', null, 'История статусов', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21001, 882, 'ADM_FORMS', null, ' Администрирование форм', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (892, 887, 'RESP:FORM:PROPS', null, 'Свойства', null, 4, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (893, 887, 'RESP:FORM:COPY', null, 'Скопировать', null, 5, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (894, 887, 'RESP:FORM:DOWNLOAD_EXCEL', null, 'Выгрузить в Excel', null, 6, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2074, 914, 'SU:TEMPL:EDIT_XLS_OUT', null, 'Изменить вых. XLS', null, 6, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (896, 887, 'RESP:FORM:CONTROL', null, 'Контроль формы', null, 8, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2077, 947, 'SU:REF:REF_LISTING_ESTIMATION', null, 'Справочник листинговых оценок', null, 28, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (898, 882, 'RESP:UPLOAD', null, 'Загрузка файлов', null, 5, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (899, 898, 'RESP:UPLOAD:PROCESS', null, 'Загрузка файлов', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (900, 882, 'RESP:DOWNLOAD', null, 'Шаблоны форм (Респонденты)', null, 6, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (901, 900, 'RESP:DOWNLOAD:EXCEL', null, 'Выгрузить в Excel', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2078, 947, 'SU:REF:REF_RATING_ESTIMATION', null, 'Справочник рейтинговых оценок', null, 29, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2079, 947, 'SU:REF:REF_RATING_CATEGORY', null, 'Справочник категорий рейтинговых оценок', null, 30, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21002, 21001, 'ADM_FORMS:ADD', null, 'Добавить', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21003, 21001, 'ADM_FORMS:DELETE', null, 'Удалить', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21067, 887, 'RESP:FORM:ATTACH_FILE', null, 'Пояснительная записка', null, 9, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (907, 882, 'SU:FORMS', null, 'Входные формы', null, 8, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (908, 907, 'SU:FORMS:HISTORY', null, 'История статусов', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21068, 907, 'SU:FORMS:ATTACH_FILE', null, 'Пояснительная записка', null, 7, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (910, 907, 'SU:FORMS:PROPS', null, 'Свойства', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (911, 907, 'SU:FORMS:DOWNLOAD_EXCEL', null, 'Выгрузить в Excel', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (912, 907, 'SU:FORMS:CONTROL', null, 'Контроль формы', null, 5, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (914, 882, 'SU:TEMPL', null, 'Шаблоны форм', null, 9, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (915, 914, 'SU:TEMPL:PROPS', null, 'Свойства', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (916, 914, 'SU:TEMPL:VALIDATE', null, 'Проверить', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (917, 914, 'SU:TEMPL:VIEW', null, 'Просмотр', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (918, 914, 'SU:TEMPL:ADD', null, 'Добавить', null, 4, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (919, 914, 'SU:TEMPL:EDIT', null, 'Изменить', null, 5, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (920, 914, 'SU:TEMPL:EDIT_XLS', null, 'Изменить XLS', null, 6, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (921, 914, 'SU:TEMPL:DELETE', null, 'Удалить', null, 7, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (922, 914, 'SU:TEMPL:UPD_BA', null, 'Обновить шаблон балансовых счетов', null, 8, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (923, 914, 'SU:TEMPL:UPD_BA_OUT', null, 'Обновить шаблон балансовых счетов (свод)', null, 9, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (924, 882, 'REP_STAT', null, 'Статусы для входных форм', null, 10, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21117, 882, 'ADM:REF', null, 'Протокол справочников', null, 14, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (926, 924, 'ST:DRAFT', null, 'Черновик', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (927, 924, 'ST:SIGNED', null, 'Подписан', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21118, 21117, 'ADM:REF:REFRESH', null, 'Просмотр', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21145, 21144, 'SU:REF:REF_DEAL_BALANCE_ACC:VIEW', null, 'Просмотр справочника балансовых счетов для отчетов о сделках', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21146, 21144, 'SU:REF:REF_DEAL_BALANCE_ACC:EDIT', null, 'Ведение справочника балансовых счетов для отчетов о сделках', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (931, 924, 'ST:ERROR', null, 'Ошибка', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (932, 924, 'ST:COMPLETED', null, 'Не утвержден', null, 4, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21144, 947, 'SU:REF:REF_DEAL_BALANCE_ACC', null, 'Справочник балансовых счетов для отчетов о сделках', null, 49, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (934, 924, 'ST:APPROVED', null, 'Утвержден', null, 5, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (935, 924, 'ST:DISAPPROVED', null, 'Разутвержден', null, 6, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (936, 882, 'OUT_REP_STAT', null, 'Статусы для выходных/сводных', null, 11, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2094, 883, 'PERMIS:SITE', null, 'Сайт', null, 6, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (938, 936, 'OUT_ST:COMPLETED', null, 'Не утвержден', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (939, 936, 'OUT_ST:APPROVED', null, 'Утвержден', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2095, 2094, 'PERMIS:GROUP:ADD', null, 'Добавить сайт', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (941, null, 'F:SHOW', null, 'Показать', null, 1, 1);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (942, null, 'F:EDIT', null, 'Ведение', null, 2, 1);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (943, null, 'F:DELETE', null, 'Удаление', null, 3, 1);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (944, null, 'F:DECLINE', null, 'Отклонение', null, 4, 1);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (945, null, 'F:APPROVE', null, 'Утверждение', null, 5, 1);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (946, null, 'F:DISAPPROVE', null, 'Разутверждение', null, 6, 1);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2096, 2094, 'PERMIS:GROUP:DELETE', null, 'Удалить сайт', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2020, 948, 'SU:REF:REF_BALANCE_ACCOUNT:VIEW', null, 'Просмотр справочника балансовых счетов для отчетов о сделаках', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2021, 948, 'SU:REF:REF_BALANCE_ACCOUNT:EDIT', null, 'Ведение справочника балансовых счетов для отчетов о сделаках', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2022, 951, 'SU:REF:REF_BANK:VIEW', null, 'Просмотр справочника банков второго уровня', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2023, 951, 'SU:REF:REF_BANK:EDIT', null, 'Ведение справочника банков второго уровня', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2024, 954, 'SU:REF:REF_CURRENCY:VIEW', null, 'Просмотр справочника валют', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2025, 954, 'SU:REF:REF_CURRENCY:EDIT', null, 'Ведение справочника валют', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2026, 957, 'SU:REF:REF_TYPE_PROVIDE:VIEW', null, 'Просмотр справочника видов обеспечения', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2027, 957, 'SU:REF:REF_TYPE_PROVIDE:EDIT', null, 'Ведение справочника видов обеспечения', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2028, 960, 'SU:REF:REF_VID_OPER:VIEW', null, 'Просмотр справочника видов операций', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2029, 960, 'SU:REF:REF_VID_OPER:EDIT', null, 'Ведение справочника видов операций', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21063, 882, 'AUDIT', null, 'Журнал аудита', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21064, 21063, 'AUDIT:REFRESH', null, 'Обновить', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2032, 966, 'SU:REF:REF_REGION:VIEW', null, 'Просмотр справочника городов и регионов', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2033, 966, 'SU:REF:REF_REGION:EDIT', null, 'Ведение справочника городов и регионов', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2034, 972, 'SU:REF:REF_POST:VIEW', null, 'Просмотр справочника должностей', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2035, 972, 'SU:REF:REF_POST:EDIT', null, 'Ведение справочника должностей', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2036, 975, 'SU:REF:REF_CROSSCHECK:VIEW', null, 'Просмотр справочника межформенных и внутриформенных контролей', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2037, 975, 'SU:REF:REF_CROSSCHECK:EDIT', null, 'Ведение справочника межформенных и внутриформенных контролей', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2038, 978, 'SU:REF:REF_TYPE_BUS_ENTITY:VIEW', null, 'Просмотр справочника организационно-правовая форма', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2039, 978, 'SU:REF:REF_TYPE_BUS_ENTITY:EDIT', null, 'Ведение справочника организационно-правовая форма', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2040, 981, 'SU:REF:REF_BRANCH:VIEW', null, 'Просмотр справочника отраслей', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2041, 981, 'SU:REF:REF_BRANCH:EDIT', null, 'Ведение справочника отраслей', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2042, 984, 'SU:REF:REF_RESPONDENT:VIEW', null, 'Просмотр справочника подотчетных организаций', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2043, 984, 'SU:REF:REF_RESPONDENT:EDIT', null, 'Ведение справочника подотчетных организаций', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2044, 987, 'SU:REF:REF_DEPARTMENT:VIEW', null, 'Просмотр справочника подразделений НБ РК', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2045, 987, 'SU:REF:REF_DEPARTMENT:EDIT', null, 'Ведение справочника подразделений НБ РК', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2046, 990, 'SU:REF:REF_REPORTS_RULES:VIEW', null, 'Просмотр справочника правил выходных форм', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2047, 990, 'SU:REF:REF_REPORTS_RULES:EDIT', null, 'Ведение справочника правил выходных форм', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2048, 993, 'SU:REF:REF_CONN_ORG:VIEW', null, 'Просмотр справочника признаков связанности с подотчетной организацией особыми отношениями', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2049, 993, 'SU:REF:REF_CONN_ORG:EDIT', null, 'Ведение справочника признаков связанности с подотчетной организацией особыми отношениями', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2050, 996, 'SU:REF:REF_CURRENCY_RATE:VIEW', null, 'Просмотр справочника рейтинга валют', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2051, 996, 'SU:REF:REF_CURRENCY_RATE:EDIT', null, 'Ведение справочника рейтинга валют', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2052, 999, 'SU:REF:REF_RATE_AGENCY:VIEW', null, 'Просмотр справочника рейтинговых агенств', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2053, 999, 'SU:REF:REF_RATE_AGENCY:EDIT', null, 'Ведение справочника рейтинговых агенств', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2054, 1002, 'SU:REF:REF_MANAGERS:VIEW', null, 'Просмотр справочника работников НБРК', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2055, 1002, 'SU:REF:REF_MANAGERS:EDIT', null, 'Ведение справочника работников НБРК', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2056, 1005, 'SU:REF:REF_COUNTRY:VIEW', null, 'Просмотр справочника стран', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2057, 1005, 'SU:REF:REF_COUNTRY:EDIT', null, 'Ведение справочника стран', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2058, 1008, 'SU:REF:REF_DOC_TYPE:VIEW', null, 'Просмотр справочника типов документов', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2059, 1008, 'SU:REF:REF_DOC_TYPE:EDIT', null, 'Ведение справочника типов документов', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2060, 1011, 'SU:REF:REF_SUBJECT_TYPE:VIEW', null, 'Просмотр справочника типов субъектов', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2061, 1011, 'SU:REF:REF_SUBJECT_TYPE:EDIT', null, 'Ведение справочника типов субъектов', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2062, 1014, 'SU:REF:REF_TRANS_TYPES:VIEW', null, 'Просмотр справочника типов сделок', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2063, 1014, 'SU:REF:REF_TRANS_TYPES:EDIT', null, 'Ведение справочника типов сделок', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2064, 1017, 'SU:REF:REF_REQUIREMENT:VIEW', null, 'Просмотр справочника требований и обязательств', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2065, 1017, 'SU:REF:REF_REQUIREMENT:EDIT', null, 'Ведение справочника требований и обязательств', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2066, 1020, 'SU:REF:REF_PERSON:VIEW', null, 'Просмотр справочника физических лиц', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2067, 1020, 'SU:REF:REF_PERSON:EDIT', null, 'Ведение справочника физических лиц', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2068, 1023, 'SU:REF:REF_SECURITIES:VIEW', null, 'Просмотр справочника ценных бумаг', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2069, 1023, 'SU:REF:REF_SECURITIES:EDIT', null, 'Ведение справочника ценных бумаг', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2070, 1026, 'SU:REF:REF_ISSUERS:VIEW', null, 'Просмотр справочника эмитентов', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2071, 1026, 'SU:REF:REF_ISSUERS:EDIT', null, 'Ведение справочника эмитентов', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2072, 1029, 'SU:REF:REF_LEGAL_PERSON:VIEW', null, 'Просмотр справочника юридических лиц', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2073, 1029, 'SU:REF:REF_LEGAL_PERSON:EDIT', null, 'Ведение справочника юридических лиц', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21141, 947, 'SU:REF:REF_MFO_REG', null, 'Справочник реестра МФО', null, 48, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21142, 21141, 'SU:REF:REF_MFO_REG:VIEW', null, 'Просмотр справочника Реестра МФО', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21143, 21141, 'SU:REF:REF_MFO_REG:EDIT', null, 'Ведение справочника Реестра МФО', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1032, 900, 'RESP:DOWNLOAD:EXCEL_ID', null, 'Выгрузить идентификаторы в Excel', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21119, 951, 'SU:REF:REF_BANK:LOAD', null, 'Загрузка справочника банков второго уровня', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21126, 947, 'SU:REF:REF_ORG_TYPE', null, 'Справочник типов организаций', null, 47, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21127, 21126, 'SU:REF:REF_ORG_TYPE:VIEW', null, 'Просмотр справочника типов организаций', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21128, 21126, 'SU:REF:REF_ORG_TYPE:EDIT', null, 'Ведение справочника типов организаций', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2086, 883, 'PERMIS:USER_GROUP', null, 'Группа', null, 4, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2087, 2086, 'PERMIS:USER_GROUP:ADD', null, 'Добавить группу', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2088, 2086, 'PERMIS:USER_GROUP:EDIT', null, 'Изменить группу', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2089, 2086, 'PERMIS:USER_GROUP:DELETE', null, 'Удалить группу', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2090, 883, 'PERMIS:USER', null, 'Пользователь', null, 5, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2091, 2090, 'PERMIS:USER:ADD_TO_GROUP', null, 'Добавить пользователя в группу', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2092, 2090, 'PERMIS:USER:EDIT', null, 'Изменить пользователя', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2093, 2090, 'PERMIS:USER:DELETE_FROM_GROUP', null, 'Удалить пользователя из группы', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2000, 882, 'SU:OUT', null, 'Выходные и сводные формы', null, 7, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2001, 2000, 'SU:OUT:NEW', null, 'Сформировать', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2002, 2000, 'SU:OUT:INPUT_REPORTS', null, 'Входные отчеты', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2003, 2000, 'SU:OUT:HISTORY', null, 'История статусов', null, 4, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21020, 2000, 'SU:OUT:NEW:DRAFT', null, 'Сформировать черновик', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2005, 2000, 'SU:OUT:PROPS', null, 'Свойства', null, 5, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2006, 2000, 'SU:OUT:DOWNLOAD_EXCEL', null, 'Выгрузить в Excel', null, 6, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2080, 2077, 'SU:REF:REF_LISTING_ESTIMATION:VIEW', null, 'Просмотр справочника листинговых оценок', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2008, 2000, 'SU:OUT:CONTROL', null, 'Контроль формы', null, 8, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2081, 2077, 'SU:REF:REF_LISTING_ESTIMATION:EDIT', null, 'Ведение справочника листинговых оценок', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2082, 2078, 'SU:REF:REF_RATING_ESTIMATION:VIEW', null, 'Просмотр справочника рейтинговых оценок', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2083, 2078, 'SU:REF:REF_RATING_ESTIMATION:EDIT', null, 'Ведение справочника рейтинговых оценок', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2084, 2079, 'SU:REF:REF_RATING_CATEGORY:VIEW', null, 'Просмотр справочника категорий рейтинговых оценок', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (2085, 2079, 'SU:REF:REF_RATING_CATEGORY:EDIT', null, 'Ведение справочника категорий рейтинговых оценок', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21114, 947, 'SU:REF:REF_MRP', null, 'Справочник МРП', null, 46, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21115, 21114, 'SU:REF:REF_MRP:VIEW', null, 'Просмотр справочника МРП', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21116, 21114, 'SU:REF:REF_MRP:EDIT', null, 'Ведение справочника МРП', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21129, 887, 'RESP:FORM:ATTACH_LETTER', null, 'Сопроводительное письмо', null, 10, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21130, 907, 'SU:FORMS:ATTACH_LETTER', null, 'Сопроводительное письмо', null, 8, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21132, 21131, 'DESIGNER:INPUT', null, 'Входные формы', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21133, 21131, 'DESIGNER:OUTPUT', null, 'Выходные формы', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21134, 21131, 'DESIGNER:CROSSCHECK_CONTROL', null, 'Внутриформенные и межформенные контроли', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21135, 21132, 'DESIGNER:INPUT:DOWNLOAD_TEMPLATE', null, 'Выгрузка шаблонов', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21136, 21132, 'DESIGNER:INPUT:UPLOAD_TEMPLATE', null, 'Загрузка шаблонов', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21137, 21133, 'DESIGNER:OUTPUT:DOWNLOAD_TEMPLATE', null, 'Выгрузка шаблонов', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21138, 21133, 'DESIGNER:OUTPUT:UPLOAD_TEMPLATE', null, 'Загрузка шаблонов', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21139, 21134, 'DESIGNER:CROSSCHECK_CONTROL:DOWNLOAD', null, 'Выгрузка', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21140, 21134, 'DESIGNER:CROSSCHECK_CONTROL:UPLOAD', null, 'Загрузка', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21131, 882, 'DESIGNER', null, 'Дизайнер форм', null, 17, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (947, 882, 'SU:REF', null, 'Справочники', null, 12, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21040, 2000, 'SU:OUT:SIGN:EDIT', null, 'Добавление/удаление подписи', null, 10, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21060, 887, 'RESP:FORM:PRINT', null, 'Печать', null, 7, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (948, 947, 'SU:REF:REF_BALANCE_ACCOUNT', null, 'Справочник детализации балансовых счетов для составления обзора финансового сектора', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21061, 2000, 'SU:OUT:PRINT', null, 'Печать', null, 7, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21062, 907, 'SU:FORMS:PRINT', null, 'Печать', null, 4, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (951, 947, 'SU:REF:REF_BANK', null, 'Справочник банков второго уровня', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (954, 947, 'SU:REF:REF_CURRENCY', null, 'Справочник валют', null, 3, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (957, 947, 'SU:REF:REF_TYPE_PROVIDE', null, 'Справочник видов обеспечения', null, 4, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (960, 947, 'SU:REF:REF_VID_OPER', null, 'Справочник видов операций', null, 5, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (966, 947, 'SU:REF:REF_REGION', null, 'Справочник городов и регионов', null, 7, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21120, 882, 'INFO', null, 'Справка', null, 15, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21121, 21120, 'INFO:GENERATE', null, 'Формирование справки', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21122, 21120, 'INFO:DOWN_PRINT', null, 'Выгрузка в Excel и печать', null, 2, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21123, 882, 'TEMPLATE', null, 'Шаблоны', null, 16, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (972, 947, 'SU:REF:REF_POST', null, 'Справочник должностей', null, 8, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21124, 21123, 'TEMPLATE:VIEW', null, 'Просмотр', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (21125, 21123, 'TEMPLATE:EDIT', null, 'Изменение', null, 1, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (975, 947, 'SU:REF:REF_CROSSCHECK', null, 'Справочник межформенных и внутриформенных контролей', null, 9, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (978, 947, 'SU:REF:REF_TYPE_BUS_ENTITY', null, 'Справочник организационно-правовых форм', null, 10, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (981, 947, 'SU:REF:REF_BRANCH', null, 'Справочник отраслей', null, 11, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (984, 947, 'SU:REF:REF_RESPONDENT', null, 'Справочник подотчетных организаций', null, 12, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (987, 947, 'SU:REF:REF_DEPARTMENT', null, 'Справочник подразделений НБ РК', null, 13, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (990, 947, 'SU:REF:REF_REPORTS_RULES', null, 'Справочник правил выходных форм', null, 14, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (993, 947, 'SU:REF:REF_CONN_ORG', null, 'Справочник признаков связанности с подотчетной организацией особыми отношениями', null, 15, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (996, 947, 'SU:REF:REF_CURRENCY_RATE', null, 'Справочник рейтинга валют', null, 16, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (999, 947, 'SU:REF:REF_RATE_AGENCY', null, 'Справочник рейтинговых агенств', null, 17, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1002, 947, 'SU:REF:REF_MANAGERS', null, 'Справочник работников НБРК', null, 18, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1005, 947, 'SU:REF:REF_COUNTRY', null, 'Справочник стран', null, 19, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1008, 947, 'SU:REF:REF_DOC_TYPE', null, 'Справочник типов документов', null, 20, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1011, 947, 'SU:REF:REF_SUBJECT_TYPE', null, 'Справочник типов субъектов', null, 21, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1014, 947, 'SU:REF:REF_TRANS_TYPES', null, 'Справочник типов сделок', null, 22, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1017, 947, 'SU:REF:REF_REQUIREMENT', null, 'Справочник требований и обязательств', null, 23, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1020, 947, 'SU:REF:REF_PERSON', null, 'Справочник физических лиц', null, 24, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1023, 947, 'SU:REF:REF_SECURITIES', null, 'Справочник ценных бумаг', null, 25, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1026, 947, 'SU:REF:REF_ISSUERS', null, 'Справочник эмитентов', null, 26, 0);
insert into RIGHT_ITEMS (id, parent, name, title_kaz, title_rus, title_eng, ord, for_forms)
values (1029, 947, 'SU:REF:REF_LEGAL_PERSON', null, 'Справочник юридических лиц', null, 27, 0);
prompt 201 records loaded
set feedback on
set define on
prompt Done.
