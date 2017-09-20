Актуализировать !!!
create or replace trigger "TRG_AIU_REF_BALANCE_ACCOUNT"
  after insert or update on REF_BALANCE_ACCOUNT
REFERENCING
  NEW AS NEW
  for each row
declare
  Type_Change_ type_change.type_change % type;
BEGIN

  if inserting then
    Type_Change_ := 1;
  elsif updating then
    if :new.delfl = 1 then
      Type_Change_ := 3;
    else
      Type_Change_ := 2;
    end if;
  end if;

  INSERT INTO REF_BALANCE_ACCOUNT_HST
    (ID_HST,
     ID,
     REC_ID,
     CODE,
     PARENT_CODE,
     LEVEL_CODE,
     NAME_KZ,
     NAME_RU,
     NAME_EN,
     BEGIN_DATE,
     END_DATE,
     DELFL,
     DATLAST,
     ID_USR,
     USER_LOCATION,
     TYPE_CHANGE,
     SENT_KND,
     ENTITY_ID
     )
  VALUES
    (seq_ref_balance_acc_hst_id.nextval,
     :new.ID,
     :new.REC_ID,
     :new.CODE,
     :new.PARENT_CODE,
     :new.LEVEL_CODE,
     :new.NAME_KZ,
     :new.NAME_RU,
     :new.NAME_EN,
     :new.BEGIN_DATE,
     :new.END_DATE,
     :new.DELFL,
     :new.DATLAST,
     :new.ID_USR,
     :new.USER_LOCATION,
     Type_Change_,
     :new.SENT_KND,
     :new.ENTITY_ID
     );
END TRG_AIU_REF_BALANCE_ACCOUNT;
/


create or replace trigger "TRG_AIU_REF_BRANCH"
  after insert or update on REF_BRANCH
REFERENCING
  NEW AS NEW
  for each row
declare
  Type_Change_ type_change.type_change % type;
BEGIN

  if inserting then
    Type_Change_ := 1;
  elsif updating then
    if :new.delfl = 1 then
      Type_Change_ := 3;
    else
      Type_Change_ := 2;
    end if;
  end if;

  INSERT INTO REF_BRANCH_HST
    (ID_HST,
     ID,
     REC_ID,
     CODE,
     NAME_KZ,
     NAME_RU,
     NAME_EN,
     BEGIN_DATE,
     DELFL,
     DATLAST,
     ID_USR,
     USER_LOCATION,
     TYPE_CHANGE,
     SENT_KND,
     ENTITY_ID
     )
  VALUES
    (seq_ref_branch_hst_id.nextval,
     :new.ID,
     :new.REC_ID,
     :new.CODE,
     :new.NAME_KZ,
     :new.NAME_RU,
     :new.NAME_EN,
     :new.BEGIN_DATE,
     :new.DELFL,
     :new.DATLAST,
     :new.ID_USR,
     :new.USER_LOCATION,
     Type_Change_,
     :new.SENT_KND,
     :new.ENTITY_ID
     );
END TRG_AIU_REF_BRANCH;
/

create or replace trigger "TRG_AIU_REF_CONN_ORG"
  after insert or update on REF_CONN_ORG
REFERENCING
  NEW AS NEW
  for each row
declare
  Type_Change_ type_change.type_change % type;
BEGIN

  if inserting then
    Type_Change_ := 1;
  elsif updating then
    if :new.delfl = 1 then
      Type_Change_ := 3;
    else
      Type_Change_ := 2;
    end if;
  end if;

  INSERT INTO REF_CONN_ORG_HST
    (ID_HST,
     ID,
     REC_ID,
     CODE,
     NAME_KZ,
     NAME_RU,
     NAME_EN,
     SHORT_NAME,
     BEGIN_DATE,
     DELFL,
     DATLAST,
     ID_USR,
     USER_LOCATION,
     TYPE_CHANGE,
     SENT_KND,
     ENTITY_ID
     )
  VALUES
    (seq_ref_conn_org_hst_id.nextval,
     :new.ID,
     :new.REC_ID,
     :new.CODE,
     :new.NAME_KZ,
     :new.NAME_RU,
     :new.NAME_EN,
     :new.SHORT_NAME,
     :new.BEGIN_DATE,
     :new.DELFL,
     :new.DATLAST,
     :new.ID_USR,
     :new.USER_LOCATION,
     Type_Change_,
     :new.SENT_KND,
     :new.ENTITY_ID
     );
END TRG_AIU_REF_CONN_ORG;
/


create or replace trigger "TRG_AIU_REF_CROSSCHECK"
  after insert or update on REF_CROSSCHECK
REFERENCING
  NEW AS NEW
  for each row
declare
  Type_Change_ type_change.type_change % type;
BEGIN

  if inserting then
    Type_Change_ := 1;
  elsif updating then
    if :new.delfl = 1 then
      Type_Change_ := 3;
    else
      Type_Change_ := 2;
    end if;
  end if;

  INSERT INTO REF_CROSSCHECK_HST
    (ID_HST,
     ID,
     REC_ID,     
     FORMULA,     
     FORM_CODE,
     DESCR_RUS,
     CROSSCHECK_TYPE,
     BEGIN_DATE,
     END_DATE,
     DELFL,
     DATLAST,
     ID_USR,
     USER_LOCATION,
     TYPE_CHANGE     
     )
  VALUES
    (seq_ref_crosscheck_hst_id.nextval,
     :new.ID,
     :new.REC_ID,     
     :new.FORMULA,
     :new.FORM_CODE,     
     :new.DESCR_RUS,
     :new.CROSSCHECK_TYPE,
     :new.BEGIN_DATE,
     :new.END_DATE,     
     :new.DELFL,
     :new.DATLAST,
     :new.ID_USR,
     :new.USER_LOCATION,
     Type_Change_
     );
END TRG_AIU_REF_CROSSCHECK;
/




create or replace trigger "TRG_AIU_REF_DOC_TYPE"
  after insert or update on REF_DOC_TYPE
REFERENCING
  NEW AS NEW
  for each row
declare
  Type_Change_ type_change.type_change % type;
BEGIN

  if inserting then
    Type_Change_ := 1;
  elsif updating then
    if :new.delfl = 1 then
      Type_Change_ := 3;
    else
      Type_Change_ := 2;
    end if;
  end if;

  INSERT INTO REF_DOC_TYPE_HST
    (ID_HST,
     ID,
     REC_ID,
     CODE,
     NAME_KZ,
     NAME_RU,
     NAME_EN,
     IS_IDENTIFICATION,
     IS_ORGANIZATION_DOC,
     IS_PERSON_DOC,
     SIGN_COUNT,
     WEIGHT,
     BEGIN_DATE,
     DELFL,
     DATLAST,
     ID_USR,
     USER_LOCATION,
     TYPE_CHANGE,
     SENT_KND,
     ENTITY_ID
     )
  VALUES
    (seq_ref_doc_type_hst_id.nextval,
     :new.ID,
     :new.REC_ID,
     :new.CODE,
     :new.NAME_KZ,
     :new.NAME_RU,
     :new.NAME_EN,
     :new.IS_IDENTIFICATION,
     :new.IS_ORGANIZATION_DOC,
     :new.IS_PERSON_DOC,
     :new.SIGN_COUNT,
     :new.WEIGHT,
     :new.BEGIN_DATE,
     :new.DELFL,
     :new.DATLAST,
     :new.ID_USR,
     :new.USER_LOCATION,
     Type_Change_,
     :new.SENT_KND,
     :new.ENTITY_ID
     );
END TRG_AIU_REF_DOC_TYPE;
/

create or replace trigger "TRG_AIU_REF_DOCUMENT"
  after insert or update on REF_DOCUMENT
REFERENCING
  NEW AS NEW
  for each row
declare
  Type_Change_ type_change.type_change % type;
BEGIN

  if inserting then
    Type_Change_ := 1;
  elsif updating then
    if :new.delfl = 1 then
      Type_Change_ := 3;
    else
      Type_Change_ := 2;
    end if;
  end if;

  INSERT INTO REF_DOCUMENT_HST
    (ID_HST,
     ID,
     REC_ID,
     CODE,
     NAME_KZ,
     NAME_RU,
     NAME_EN,
     REF_DOC_TYPE,
     REF_RESPONDENT,
     BEGIN_DATE,
     DELFL,
     DATLAST,
     ID_USR,
     USER_LOCATION,
     TYPE_CHANGE,
     SENT_KND,
     ENTITY_ID
     )
  VALUES
    (seq_ref_document_hst_id.nextval,
     :new.ID,
     :new.REC_ID,
     :new.CODE,
     :new.NAME_KZ,
     :new.NAME_RU,
     :new.NAME_EN,
     :new.REF_DOC_TYPE,
     :new.REF_RESPONDENT,
     :new.BEGIN_DATE,
     :new.DELFL,
     :new.DATLAST,
     :new.ID_USR,
     :new.USER_LOCATION,
     Type_Change_,
     :new.SENT_KND,
     :new.ENTITY_ID
     );
END TRG_AIU_REF_DOCUMENT;
/

create or replace trigger "TRG_AIU_REF_LEGAL_PERSON"
  after insert or update on REF_LEGAL_PERSON
REFERENCING
  NEW AS NEW
  for each row
declare
  Type_Change_ type_change.type_change % type;
BEGIN

  if inserting then
    Type_Change_ := 1;
  elsif updating then
    if :new.delfl = 1 then
      Type_Change_ := 3;
    else
      Type_Change_ := 2;
    end if;
  end if;

  INSERT INTO REF_LEGAL_PERSON_HST
    (ID_HST,
     ID,
     REC_ID,
     CODE,
     IDN,
     NAME_KZ,
     NAME_RU,
     NAME_EN,
     SHORT_NAME_KZ,
     SHORT_NAME_RU,
     SHORT_NAME_EN,
     REF_SUBJECT_TYPE,
     REF_TYPE_BUS_ENTITY,
     REF_COUNTRY,
     REF_REGION,
     POSTAL_INDEX,
     ADDRESS_STREET,
     ADDRESS_NUM_HOUSE,
     REF_MANAGERS,
     LEGAL_ADDRESS,
     FACT_ADDRESS,
     NOTE,
     BEGIN_DATE,
     DELFL,
     DATLAST,
     ID_USR,
     TYPE_CHANGE,
     USER_LOCATION,
     SENT_KND,
     ENTITY_ID
     )
  VALUES
    (seq_ref_legal_person_hst_id.nextval,
     :new.ID,
     :new.REC_ID,
     :new.CODE,
     :new.IDN,
     :new.NAME_KZ,
     :new.NAME_RU,
     :new.NAME_EN,
     :new.SHORT_NAME_KZ,
     :new.SHORT_NAME_RU,
     :new.SHORT_NAME_EN,
     :new.REF_SUBJECT_TYPE,
     :new.REF_TYPE_BUS_ENTITY,
     :new.REF_COUNTRY,
     :new.REF_REGION,
     :new.POSTAL_INDEX,
     :new.ADDRESS_STREET,
     :new.ADDRESS_NUM_HOUSE,
     :new.REF_MANAGERS,
     :new.LEGAL_ADDRESS,
     :new.FACT_ADDRESS,
     :new.NOTE,
     :new.BEGIN_DATE,
     :new.DELFL,
     :new.DATLAST,
     :new.ID_USR,
     Type_Change_,
     :new.USER_LOCATION,
     :new.SENT_KND,
     :new.ENTITY_ID
     );
END TRG_AIU_REF_LEGAL_PERSON;
/

create or replace trigger "TRG_AIU_REF_MANAGERS"
  after insert or update on REF_MANAGERS
REFERENCING
  NEW AS NEW
  for each row
declare
  Type_Change_ type_change.type_change % type;
BEGIN

  if inserting then
    Type_Change_ := 1;
  elsif updating then
    if :new.delfl = 1 then
      Type_Change_ := 3;
    else
      Type_Change_ := 2;
    end if;
  end if;

  INSERT INTO REF_MANAGERS_HST
    (ID_HST,
     ID,
     REC_ID,
     CODE,
     FM,
     NM,
     FT,
     FIO_KZ,
     FIO_EN,
     BEGIN_DATE,
     DELFL,
     DATLAST,
     ID_USR,
     USER_LOCATION,
     TYPE_CHANGE,
     SENT_KND,
     ENTITY_ID
     )
  VALUES
    (seq_ref_managers_hst_id.nextval,
     :new.ID,
     :new.REC_ID,
     :new.CODE,
     :new.FM,
     :new.NM,
     :new.FT,
     :new.FIO_KZ,
     :new.FIO_EN,
     :new.BEGIN_DATE,
     :new.DELFL,
     :new.DATLAST,
     :new.ID_USR,
     :new.USER_LOCATION,
     Type_Change_,
     :new.SENT_KND,
     :new.ENTITY_ID
     );
END TRG_AIU_REF_MANAGERS;
/

create or replace trigger "TRG_AIU_REF_PERSON"
  after insert or update on REF_PERSON
REFERENCING
  NEW AS NEW
  for each row
declare
  Type_Change_ type_change.type_change % type;
BEGIN

  if inserting then
    Type_Change_ := 1;
  elsif updating then
    if :new.delfl = 1 then
      Type_Change_ := 3;
    else
      Type_Change_ := 2;
    end if;
  end if;

  INSERT INTO REF_PERSON_HST
    (ID_HST,
     ID,
     REC_ID,
     IDN,
     CODE,
     FM,
     NM,
     FT,
     FIO_KZ,
     FIO_EN,
     REF_COUNTRY,
     PHONE_WORK,
     FAX,
     ADDRESS_WORK,
     NOTE,
     BEGIN_DATE,
     DELFL,
     DATLAST,
     ID_USR,
     TYPE_CHANGE,
     USER_LOCATION,
     SENT_KND,
     ENTITY_ID
     )
  VALUES
    (seq_ref_person_hst_id.nextval,
     :new.ID,
     :new.REC_ID,
     :new.IDN,
     :new.CODE,
     :new.FM,
     :new.NM,
     :new.FT,
     :new.FIO_KZ,
     :new.FIO_EN,
     :new.REF_COUNTRY,
     :new.PHONE_WORK,
     :new.FAX,
     :new.ADDRESS_WORK,
     :new.NOTE,
     :new.BEGIN_DATE,
     :new.DELFL,
     :new.DATLAST,
     :new.ID_USR,
     Type_Change_,
     :new.USER_LOCATION,
     :new.SENT_KND,
     :new.ENTITY_ID
     );
END TRG_AIU_REF_PERSON;
/

create or replace trigger "TRG_AIU_REF_POST"
  after insert or update on REF_POST
REFERENCING
  NEW AS NEW
  for each row
declare
  Type_Change_ type_change.type_change % type;
BEGIN

  if inserting then
    Type_Change_ := 1;
  elsif updating then
    if :new.delfl = 1 then
      Type_Change_ := 3;
    else
      Type_Change_ := 2;
    end if;
  end if;

  INSERT INTO REF_POST_HST
    (ID_HST,
     ID,
     REC_ID,
     CODE,
     NAME_KZ,
     NAME_RU,
     NAME_EN,
     TYPE_POST,
     IS_ACTIVITY,
     IS_MAIN_RUK,
     BEGIN_DATE,
     DELFL,
     DATLAST,
     ID_USR,
     TYPE_CHANGE,
     USER_LOCATION,
     SENT_KND,
     ENTITY_ID
     )
  VALUES
    (seq_ref_post_hst_id.nextval,
     :new.ID,
     :new.REC_ID,
     :new.CODE,
     :new.NAME_KZ,
     :new.NAME_RU,
     :new.NAME_EN,
     :new.TYPE_POST,
     :new.IS_ACTIVITY,
     :new.IS_MAIN_RUK,
     :new.BEGIN_DATE,
     :new.DELFL,
     :new.DATLAST,
     :new.ID_USR,
     Type_Change_,
     :new.USER_LOCATION,
     :new.SENT_KND,
     :new.ENTITY_ID
     );
END TRG_AIU_REF_POST;
/

create or replace trigger "TRG_AIU_REF_REPORTS_RULES"
  after insert or update on REF_REPORTS_RULES
REFERENCING
  NEW AS NEW
  for each row
declare
  Type_Change_ type_change.type_change % type;
BEGIN

  if inserting then
    Type_Change_ := 1;
  elsif updating then
    if :new.delfl = 1 then
      Type_Change_ := 3;
    else
      Type_Change_ := 2;
    end if;
  end if;

  INSERT INTO REF_REPORTS_RULES_HST
    (ID_HST,
     ID,
     REC_ID,
     CODE,
     NAME_KZ,
     NAME_RU,
     NAME_EN,
     FORMNAME,
     FIELDNAME,
     FORMULA,
     IS_CALC_OTHER_FIELD,
     COEFF,
     CONDITION,
     BEGIN_DATE,
     DELFL,
     DATLAST,
     ID_USR,
     USER_LOCATION,
     TYPE_CHANGE,
     SENT_KND,
     ENTITY_ID
     )
  VALUES
    (seq_ref_reports_rules_hst_id.nextval,
     :new.ID,
     :new.REC_ID,
     :new.CODE,
     :new.NAME_KZ,
     :new.NAME_RU,
     :new.NAME_EN,
     :new.FORMNAME,
     :new.FIELDNAME,
     :new.FORMULA,
     :new.IS_CALC_OTHER_FIELD,
     :new.COEFF,
     :new.CONDITION,
     :new.BEGIN_DATE,
     :new.DELFL,
     :new.DATLAST,
     :new.ID_USR,
     :new.USER_LOCATION,
     Type_Change_,
     :new.SENT_KND,
     :new.ENTITY_ID
     );
END TRG_AIU_REF_REPORTS_RULES;
/

create or replace trigger "TRG_AIU_REF_REQUIREMENT"
  after insert or update on REF_REQUIREMENT
REFERENCING
  NEW AS NEW
  for each row
declare
  Type_Change_ type_change.type_change % type;
BEGIN

  if inserting then
    Type_Change_ := 1;
  elsif updating then
    if :new.delfl = 1 then
      Type_Change_ := 3;
    else
      Type_Change_ := 2;
    end if;
  end if;

  INSERT INTO REF_REQUIREMENT_HST
    (ID_HST,
     ID,
     REC_ID,
     CODE,
     NAME_KZ,
     NAME_RU,
     NAME_EN,
     BEGIN_DATE,
     DELFL,
     DATLAST,
     ID_USR,
     USER_LOCATION,
     TYPE_CHANGE,
     SENT_KND,
     ENTITY_ID
     )
  VALUES
    (seq_ref_requirement_hst_id.nextval,
     :new.ID,
     :new.REC_ID,
     :new.CODE,
     :new.NAME_KZ,
     :new.NAME_RU,
     :new.NAME_EN,
     :new.BEGIN_DATE,
     :new.DELFL,
     :new.DATLAST,
     :new.ID_USR,
     :new.USER_LOCATION,
     Type_Change_,
     :new.SENT_KND,
     :new.ENTITY_ID
     );
END TRG_AIU_REF_REQUIREMENT;
/

create or replace trigger "TRG_AIU_REF_RESPONDENT"
  after insert or update on REF_RESPONDENT
REFERENCING
  NEW AS NEW
  for each row
declare
  Type_Change_ type_change.type_change % type;
BEGIN

  if inserting then
    Type_Change_ := 1;
  elsif updating then
    if :new.delfl = 1 then
      Type_Change_ := 3;
    else
      Type_Change_ := 2;
    end if;
  end if;

  INSERT INTO REF_RESPONDENT_HST
    (ID_HST,
     ID,
     REC_ID,
     CODE,
     REF_LEGAL_PERSON,
     NOKBDB_CODE,
     MAIN_BUH,
     DATE_BEGIN_LIC,
     DATE_END_LIC,
     STOP_LIC,
     VID_ACTIVITY,
     BEGIN_DATE,
     DELFL,
     DATLAST,
     ID_USR,
     USER_LOCATION,
     TYPE_CHANGE,
     SENT_KND,
     ENTITY_ID
     )
  VALUES
    (seq_ref_respondent_hst_id.nextval,
     :new.ID,
     :new.REC_ID,
     :new.CODE,
     :new.REF_LEGAL_PERSON,
     :new.NOKBDB_CODE,
     :new.MAIN_BUH,
     :new.DATE_BEGIN_LIC,
     :new.DATE_END_LIC,
     :new.STOP_LIC,
     :new.VID_ACTIVITY,
     :new.BEGIN_DATE,
     :new.DELFL,
     :new.DATLAST,
     :new.ID_USR,
     :new.USER_LOCATION,
     Type_Change_,
     :new.SENT_KND,
     :new.ENTITY_ID
     );
END TRG_AIU_REF_RESPONDENT;
/

create or replace trigger "TRG_AIU_REF_SUBJECT_TYPE"
  after insert or update on REF_SUBJECT_TYPE
REFERENCING
  NEW AS NEW
  for each row
declare
  Type_Change_ type_change.type_change % type;
BEGIN

  if inserting then
    Type_Change_ := 1;
  elsif updating then
    if :new.delfl = 1 then
      Type_Change_ := 3;
    else
      Type_Change_ := 2;
    end if;
  end if;

  INSERT INTO REF_SUBJECT_TYPE_HST
    (ID_HST,
     ID,
     REC_ID,
     CODE,
     NAME_KZ,
     NAME_RU,
     NAME_EN,
     KIND_ID,
     REP_PER_DUR_MONTHS,
     IS_ADVANCE,
     BEGIN_DATE,
     DELFL,
     DATLAST,
     ID_USR,
     USER_LOCATION,
     TYPE_CHANGE,
     SENT_KND,
     ENTITY_ID
     )
  VALUES
    (seq_ref_subject_type_hst_id.nextval,
     :new.ID,
     :new.REC_ID,
     :new.CODE,
     :new.NAME_KZ,
     :new.NAME_RU,
     :new.NAME_EN,
     :new.KIND_ID,
     :new.REP_PER_DUR_MONTHS,
     :new.IS_ADVANCE,
     :new.BEGIN_DATE,
     :new.DELFL,
     :new.DATLAST,
     :new.ID_USR,
     :new.USER_LOCATION,
     Type_Change_,
     :new.SENT_KND,
     :new.ENTITY_ID
     );
END TRG_AIU_REF_SUBJECT_TYPE;
/


create or replace trigger "TRG_AIU_REF_TRANS_TYPES"
  after insert or update on REF_TRANS_TYPES
REFERENCING
  NEW AS NEW
  for each row
declare
  Type_Change_ type_change.type_change % type;
BEGIN

  if inserting then
    Type_Change_ := 1;
  elsif updating then
    if :new.delfl = 1 then
      Type_Change_ := 3;
    else
      Type_Change_ := 2;
    end if;
  end if;

  INSERT INTO REF_TRANS_TYPES_HST
    (ID_HST,
     ID,
     REC_ID,
     CODE,
     NAME_KZ,
     NAME_RU,
     NAME_EN,
     KIND_OF_ACTIVITY,
     SHORT_NAME,
     BEGIN_DATE,
     DELFL,
     DATLAST,
     ID_USR,
     USER_LOCATION,
     TYPE_CHANGE,
     SENT_KND,
     ENTITY_ID
     )
  VALUES
    (seq_ref_trans_types_hst_id.nextval,
     :new.ID,
     :new.REC_ID,
     :new.CODE,
     :new.NAME_KZ,
     :new.NAME_RU,
     :new.NAME_EN,
     :new.KIND_OF_ACTIVITY,
     :new.SHORT_NAME,
     :new.BEGIN_DATE,
     :new.DELFL,
     :new.DATLAST,
     :new.ID_USR,
     :new.USER_LOCATION,
     Type_Change_,
     :new.SENT_KND,
     :new.ENTITY_ID
     );
END TRG_AIU_REF_TRANS_TYPES;
/

create or replace trigger "TRG_AIU_REF_TYPE_BUS_ENTITY"
  after insert or update on REF_TYPE_BUS_ENTITY
REFERENCING
  NEW AS NEW
  for each row
declare
  Type_Change_ type_change.type_change % type;
BEGIN

  if inserting then
    Type_Change_ := 1;
  elsif updating then
    if :new.delfl = 1 then
      Type_Change_ := 3;
    else
      Type_Change_ := 2;
    end if;
  end if;

  INSERT INTO REF_TYPE_BUS_ENTITY_HST
    (ID_HST,
     ID,
     REC_ID,
     CODE,
     NAME_KZ,
     NAME_RU,
     NAME_EN,
     BEGIN_DATE,
     DELFL,
     DATLAST,
     ID_USR,
     USER_LOCATION,
     TYPE_CHANGE,
     SENT_KND,
     ENTITY_ID
     )
  VALUES
    (seq_ref_type_bus_entity_hst_id.nextval,
     :new.ID,
     :new.REC_ID,
     :new.CODE,
     :new.NAME_KZ,
     :new.NAME_RU,
     :new.NAME_EN,
     :new.BEGIN_DATE,
     :new.DELFL,
     :new.DATLAST,
     :new.ID_USR,
     :new.USER_LOCATION,
     Type_Change_,
     :new.SENT_KND,
     :new.ENTITY_ID
     );
END TRG_AIU_REF_TYPE_BUS_ENTITY;
/

create or replace trigger "TRG_AIU_REF_TYPE_PROVIDE"
  after insert or update on REF_TYPE_PROVIDE
REFERENCING
  NEW AS NEW
  for each row
declare
  Type_Change_ type_change.type_change % type;
BEGIN

  if inserting then
    Type_Change_ := 1;
  elsif updating then
    if :new.delfl = 1 then
      Type_Change_ := 3;
    else
      Type_Change_ := 2;
    end if;
  end if;

  INSERT INTO REF_TYPE_PROVIDE_HST
    (ID_HST,
     ID,
     REC_ID,
     CODE,
     NAME_KZ,
     NAME_RU,
     NAME_EN,
     BEGIN_DATE,
     DELFL,
     DATLAST,
     ID_USR,
     USER_LOCATION,
     TYPE_CHANGE,
     SENT_KND,
     ENTITY_ID
     )
  VALUES
    (seq_ref_type_provide_hst_id.nextval,
     :new.ID,
     :new.REC_ID,
     :new.CODE,
     :new.NAME_KZ,
     :new.NAME_RU,
     :new.NAME_EN,
     :new.BEGIN_DATE,
     :new.DELFL,
     :new.DATLAST,
     :new.ID_USR,
     :new.USER_LOCATION,
     Type_Change_,
     :new.SENT_KND,
     :new.ENTITY_ID
     );
END TRG_AIU_REF_TYPE_PROVIDE;
/

create or replace trigger "TRG_AIU_REF_VID_OPER"
  after insert or update on REF_VID_OPER
REFERENCING
  NEW AS NEW
  for each row
declare
  Type_Change_ type_change.type_change % type;
BEGIN

  if inserting then
    Type_Change_ := 1;
  elsif updating then
    if :new.delfl = 1 then
      Type_Change_ := 3;
    else
      Type_Change_ := 2;
    end if;
  end if;

  INSERT INTO REF_VID_OPER_HST
    (ID_HST,
     ID,
     REC_ID,
     CODE,
     NAME_KZ,
     NAME_RU,
     NAME_EN,
     BEGIN_DATE,
     DELFL,
     DATLAST,
     ID_USR,
     USER_LOCATION,
     TYPE_CHANGE,
     SENT_KND,
     ENTITY_ID
     )
  VALUES
    (seq_ref_vid_oper_hst_id.nextval,
     :new.ID,
     :new.REC_ID,
     :new.CODE,
     :new.NAME_KZ,
     :new.NAME_RU,
     :new.NAME_EN,
     :new.BEGIN_DATE,
     :new.DELFL,
     :new.DATLAST,
     :new.ID_USR,
     :new.USER_LOCATION,
     Type_Change_,
     :new.SENT_KND,
     :new.ENTITY_ID
     );
END TRG_AIU_REF_VID_OPER;
/

