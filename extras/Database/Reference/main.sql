pkg_frsi_util
eav_m_classes
eav_be_entities
eav_m_simple_attributes

eav_be_integer_values
eav_be_string_values
eav_be_boolean_values
-----------------------------
ref_main
pkg_frsi_ref
pkg_frsi_ref_load

convert_win1251_to_utf8_kz

type_change
rep_per_dur_months
sent_knd
crosscheck_type
type_post

f_users 10196
---------------------------------------------------------------------------------------------------------------
-- Должности  + 
ref_post
ref_post_hst
seq_ref_post_id
seq_ref_post_hst_id
trg_aiu_ref_post
-- Физические лица
ref_person
ref_person_hst
seq_ref_person_id
seq_ref_person_hst_id
trg_aiu_ref_person
-- Юридические лица
ref_legal_person
ref_legal_person_hst
seq_ref_legal_person_id
seq_ref_legal_person_hst_id
trg_aiu_ref_legal_person
-- Руководящие работники
ref_managers
ref_managers_hst
seq_ref_managers_id
seq_ref_managers_hst_id
trg_aiu_ref_managers
-- Организационно-правовая форма +
ref_type_bus_entity
ref_type_bus_entity_hst
seq_ref_type_bus_entity_id
seq_ref_type_bus_entity_hst_id
trg_aiu_ref_type_bus_entity
-- Требования и обязательства +
ref_requirement
ref_requirement_hst
seq_ref_requirement_id
seq_ref_requirement_hst_id
trg_aiu_ref_requirement
-- Виды  обеспечения +
ref_type_provide
ref_type_provide_hst
seq_ref_type_provide_id
seq_ref_type_provide_hst_id
trg_aiu_ref_type_provide
-- Типы сделок +
ref_trans_types
ref_trans_types_hst
seq_ref_trans_types_id
seq_ref_trans_types_hst_id
trg_aiu_ref_trans_types
-- Балансовые счета для отчетов о сделках +
ref_balance_account
ref_balance_account_hst
seq_ref_balance_acc_id
seq_ref_balance_acc_hst_id
trg_aiu_ref_balance_account
-- Признаки связанности с подотчетной организацией особыми отношениями +
ref_conn_org
ref_conn_org_hst
seq_ref_conn_org_id
seq_ref_conn_org_hst_id
trg_aiu_ref_conn_org
------------------------------------------------------------------
-- Подразделения НБ РК (Загрузка) +
ref_department
-- Банки второго уровня (Загрузка) +
ref_bank
-- Страны (Загрузка) +
ref_country
-- Регионы/Города (Загрузка) +
ref_region
-- Рейтинг агенств (Загрузка) +
ref_rate_agency
-- Валюты (Загрузка) +
ref_currency
-- Рейтинг Валют (Загрузка) +
ref_currency_rate
-- Эмитенты (Загрузка) +
ref_issuers
-- Ценные бумаги (Загрузка) +
ref_securities
----------------------------------------------------------------
-- Список подотчетных организаций (Респондентов)
ref_respondent
ref_respondent_hst
seq_ref_respondent_id
seq_ref_respondent_hst_id
trg_aiu_ref_respondent 
-- Тип организации +
ref_subject_type
ref_subject_type_hst
seq_ref_subject_type_id
seq_ref_subject_type_hst_id
trg_aiu_ref_subject_type
-- Тип документов +
ref_doc_type
ref_doc_type_hst
seq_ref_doc_type_id
seq_ref_doc_type_hst_id
trg_aiu_ref_doc_type
-- Документ +
ref_document
ref_document_hst
seq_ref_document_id
seq_ref_document_hst_id
trg_aiu_ref_document
---------------------------------------------------------------
-- Вид операций +
ref_vid_oper
ref_vid_oper_hst
seq_ref_vid_oper_id
seq_ref_vid_oper_hst_id
trg_aiu_ref_vid_oper
--Отрасли +
ref_branch
ref_branch_hst
seq_ref_branch_id
seq_ref_branch_hst_id
trg_aiu_ref_branch
-- Межформенный контроль +
ref_crosscheck
ref_crosscheck_hst
seq_ref_crosscheck_id
seq_ref_crosscheck_hst_id
trg_aiu_ref_crosscheck
-- Правила выходных форм +
ref_reports_rules
ref_reports_rules_hst
seq_ref_reports_rules_id
seq_ref_reports_rules_hst_id
trg_aiu_ref_reports_rules
