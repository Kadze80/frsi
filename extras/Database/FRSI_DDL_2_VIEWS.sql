CREATE MATERIALIZED VIEW mv_rate_agency
BUILD IMMEDIATE
REFRESH FORCE ON DEMAND
AS
  SELECT *
  FROM gd_rate_agency@nsi;

CREATE MATERIALIZED VIEW mv_currency_rate
BUILD IMMEDIATE
REFRESH FORCE ON DEMAND
AS
  SELECT *
  FROM gd_currency_rate@nsi;

CREATE MATERIALIZED VIEW mv_currency
BUILD IMMEDIATE
REFRESH FORCE ON DEMAND
AS
  SELECT *
  FROM gs_currency@nsi;

CREATE MATERIALIZED VIEW mv_bank
BUILD IMMEDIATE
REFRESH FORCE ON DEMAND
AS
  SELECT *
  FROM gs_bank@nsi;


CREATE MATERIALIZED VIEW mv_issuer
BUILD IMMEDIATE
REFRESH FORCE ON DEMAND
AS
  SELECT *
  FROM bv.s_issuer@pfcbnv;

CREATE MATERIALIZED VIEW mv_security
BUILD IMMEDIATE
REFRESH FORCE ON DEMAND
AS
  SELECT
    s.rec_id        id,
    s.nin,
    s.nominal_value,
    c.short_name    currency_code,
    c.name          currency_name,
    v.code          variety_code,
    v.name          variety_name,
    t.code          type_code,
    t.name          type_name,
    i.rec_id        issuer_id,
    i.name          issuer_name,
    i.is_state,
    i.is_resident,
    g.code          sign_code,
    g.name          sign_name,
    n.code          country_code,
    n.name          country_name,
    s.circul_date   circul_date,
    s.maturity_date maturity_date
  FROM bv.s_security@pfcbnv s,
    bv.k_g_currency@pfcbnv c,
    bv.s_g_security_variety@pfcbnv v,
    bv.s_g_security_type@pfcbnv t,
    bv.s_issuer@pfcbnv i,
    bv.s_g_issuer_sign@pfcbnv g,
    bv.k_g_country@pfcbnv n
  WHERE s.nominal_currency = c.rec_id (+)
        AND s.s_g_security_variety = v.rec_id (+)
        AND s.s_g_security_type = t.rec_id (+)
        AND s.s_issuer = i.rec_id (+)
        AND i.s_g_issuer_sign = g.rec_id (+)
        AND i.k_g_country = n.rec_id (+);

CREATE INDEX mv_security_idx
  ON mv_security (issuer_id, nin);


CREATE MATERIALIZED VIEW mv_legal_person
BUILD IMMEDIATE
REFRESH FORCE ON DEMAND
AS
  SELECT
    k_g_jur_person  id,
    bin,
    jur_unique_code code,
    short_name,
    name            name_ru,
    kaz_name        name_kz,
    eng_name        name_en,
    is_subsidiary,
    parent_id,
    legal_address,
    fact_address
  FROM k_g_jur_person;

CREATE OR REPLACE VIEW REF_RATING_ESTIMATION_V AS
  SELECT
    e.ID,
    e.REC_ID,
    e.REF_RATING_CATEGORY,
    c.name_ru || ' - ' || e.name_ru AS name_ru,
    c.name_kz || ' - ' || e.name_kz AS name_kz,
    c.name_en || ' - ' || e.name_en AS name_en,
    e.PRIORITY,
    e.BEGIN_DATE,
    e.DELFL,
    e.DATLAST,
    e.ID_USR,
    e.USER_LOCATION,
    e.SENT_KND,
    e.CODE
  FROM REF_RATING_ESTIMATION e, ref_rating_category c
  WHERE e.ref_rating_category = c.rec_id
        AND c.begin_date = (SELECT max(c2.begin_date)
                            FROM ref_rating_category c2
                            WHERE c2.rec_id = c.rec_id AND c.delfl = 0)
  ORDER BY c.id, e.priority;

CREATE OR REPLACE VIEW REF_RESPONDENT_V AS
  SELECT
    r.*,
    lp.name_ru,
    lp.name_kz,
    lp.name_en
  FROM ref_respondent r,
    ref_legal_person lp,
    f_users u,
    sent_knd sk
  WHERE r.ref_legal_person = lp.id;


CREATE OR REPLACE VIEW V_HISTORY_LAST_STATUS AS
  SELECT
    h."ID",
    h."REPORT_ID",
    h."SAVE_DATE",
    h."DATA",
    h."DATA_SIZE",
    h."COMMENTS",
    h."ATTACHMENT",
    h."ATTACHMENT_SIZE",
    h."ATTACHMENT_FILE_NAME",
    h."HASH",
    h."DELIVERY_WAY_CODE",
    h."USER_ID",
    h."USER_INFO",
    h."SU_USER_ID",
    h."SU_USER_INFO",
    h."SU_COMMENTS",
    h.CONTROL_RESULT_CODE,
    h.CONTROL_RESULT_CODE2,
    s.ID            STATUS_ID,
    s.MESSAGE       STATUS_MESSAGE,
    s.STATUS_CODE,
    s.STATUS_DATE,
    s.USER_ID       STATUS_USER_ID,
    s.USER_INFO     STATUS_USER_INFO,
    s.USER_LOCATION STATUS_USER_LOCATION
  FROM REPORT_HISTORY h INNER JOIN REPORT_HISTORY_STATUSES s ON s.ID = (SELECT max(s2.ID)
                                                                        FROM REPORT_HISTORY_STATUSES s2
                                                                        WHERE s2.REPORT_HISTORY_ID = h.ID);


CREATE OR REPLACE VIEW V_REF_CURRENCY_R AS
  SELECT
    c.ID,
    c.REC_ID,
    c.CODE,
    c.MINOR_UNITS,
    c.RATE,
    c.NAME_KZ,
    c.NAME_EN,
    c.NAME_RU,
    c.REF_CURRENCY_RATE,
    c.BEGIN_DATE,
    c.DELFL,
    c.DATLAST,
    c.ID_USR,
    c.USER_LOCATION,
    c.SENT_KND,
    c.END_DATE,
    (SELECT r.CODE
     FROM REF_CURRENCY_RATE r
     WHERE c.REF_CURRENCY_RATE = r.ID) rate_code
  FROM ref_currency c
  WHERE c.rec_id NOT IN (SELECT c2.rec_id
                         FROM ref_currency c2
                         WHERE c2.delfl = 1);

CREATE OR REPLACE VIEW v_report_history_short AS
  SELECT
    fh1.form_id,
    fh1.name,
    fh1.short_name
  FROM form_history fh1
  WHERE fh1.begin_date = (SELECT max(fh2.begin_date)
                          FROM form_history fh2
                          WHERE fh2.form_id = fh1.form_id);


CREATE OR REPLACE VIEW V_REF_EXTIND AS
  SELECT
    "ID",
    "REC_ID",
    "CODE",
    "EXTSYS_ID",
    "NAME_KZ",
    "NAME_RU",
    "NAME_EN",
    "ALG",
    "VALUE_TYPE",
    "BEGIN_DATE",
    "DELFL",
    "DATLAST",
    "ID_USR",
    "USER_LOCATION",
    "SENT_KND",
    "END_DATE"
  FROM REF_EXTIND
  WHERE rec_id NOT IN (SELECT rec_id
                       FROM REF_EXTIND
                       WHERE delfl = 1);

CREATE OR REPLACE VIEW V_REF_PERIOD_ALG AS
  SELECT
    "ID",
    "REC_ID",
    "NAME_KZ",
    "NAME_RU",
    "NAME_EN",
    "ALG",
    "BEGIN_DATE",
    "DELFL",
    "DATLAST",
    "ID_USR",
    "USER_LOCATION",
    "SENT_KND",
    "END_DATE"
  FROM REF_PERIOD_ALG
  WHERE rec_id NOT IN (SELECT rec_id
                       FROM REF_PERIOD_ALG
                       WHERE delfl = 1);

CREATE OR REPLACE VIEW V_REF_PERIOD AS
  SELECT
    "ID",
    "REC_ID",
    "REF_PERIOD_ALG",
    "NAME_KZ",
    "NAME_RU",
    "NAME_EN",
    "SHORT_NAME_KZ",
    "SHORT_NAME_RU",
    "SHORT_NAME_EN",
    "AUTO_APPROVE",
    "BEGIN_DATE",
    "DELFL",
    "DATLAST",
    "ID_USR",
    "USER_LOCATION",
    "SENT_KND",
    "END_DATE"
  FROM REF_PERIOD
  WHERE rec_id NOT IN (SELECT rec_id
                       FROM REF_PERIOD
                       WHERE delfl = 1);