CREATE OR REPLACE TRIGGER TRG_AIU_REF_EXTIND
AFTER INSERT OR UPDATE ON REF_EXTIND
REFERENCING
  NEW AS NEW
FOR EACH ROW
  DECLARE
    Type_Change_ type_change.type_change%TYPE;
  BEGIN

    IF INSERTING
    THEN
      Type_Change_ := 1;
    ELSIF UPDATING
      THEN
        IF :new.delfl = 1
        THEN
          Type_Change_ := 3;
        ELSE
          Type_Change_ := 2;
        END IF;
    END IF;
    IF :new.sent_knd = 0
    THEN
      INSERT INTO REF_EXTIND_HST
      (ID_HST,
       ID,
       REC_ID,
       CODE,
       EXTSYS_ID,
       NAME_KZ,
       NAME_RU,
       NAME_EN,
       ALG,
       VALUE_TYPE,
       BEGIN_DATE,
       END_DATE,
       DELFL,
       DATLAST,
       ID_USR,
       USER_LOCATION,
       TYPE_CHANGE
      )
      VALUES
        (seq_ref_extind_hst_id.nextval,
          :new.ID,
          :new.REC_ID,
          :new.CODE,
          :new.EXTSYS_ID,
          :new.NAME_KZ,
          :new.NAME_RU,
          :new.NAME_EN,
          :new.ALG,
          :new.VALUE_TYPE,
          :new.BEGIN_DATE,
         :new.END_DATE,
         :new.DELFL,
         :new.DATLAST,
         :new.ID_USR,
         :new.USER_LOCATION,
         Type_Change_
        );
    END IF;
  END TRG_AIU_REF_EXTIND;

CREATE OR REPLACE TRIGGER TRG_AIU_REF_EXTIND_PARAMS
AFTER INSERT OR UPDATE ON REF_EXTIND_PARAMS
REFERENCING
  NEW AS NEW
FOR EACH ROW
  DECLARE
    Type_Change_ type_change.type_change%TYPE;
  BEGIN

    IF INSERTING
    THEN
      Type_Change_ := 1;
    ELSIF UPDATING
      THEN
        Type_Change_ := 2;
    END IF;
    INSERT INTO REF_EXTIND_PARAMS_HST
    (ID_HST,
     REF_EXTIND_ID,
     NAME,
     VALUE_TYPE,
     TYPE_CHANGE
    )
    VALUES
      (seq_ref_extind_params_hst_id.nextval,
       :new.REF_EXTIND_ID,
       :new.NAME,
       :new.VALUE_TYPE,
       Type_Change_
      );
  END TRG_AIU_REF_EXTIND_PARAMS;

CREATE OR REPLACE TRIGGER "TRG_AIU_REF_PERIOD_ALG"
AFTER INSERT OR UPDATE ON REF_PERIOD_ALG
REFERENCING
  NEW AS NEW
FOR EACH ROW
  DECLARE
    Type_Change_ type_change.type_change%TYPE;
  BEGIN

    IF INSERTING
    THEN
      Type_Change_ := 1;
    ELSIF UPDATING
      THEN
        IF :new.delfl = 1
        THEN
          Type_Change_ := 3;
        ELSE
          Type_Change_ := 2;
        END IF;
    END IF;

    INSERT INTO REF_PERIOD_ALG_HST
    (ID_HST,
     ID,
     REC_ID,
     NAME_KZ,
     NAME_RU,
     NAME_EN,
     ALG,
     BEGIN_DATE,
     DELFL,
     DATLAST,
     ID_USR,
     TYPE_CHANGE,
     USER_LOCATION,
     SENT_KND
    )
    VALUES
      (SEQ_REF_PERIOD_ALG_HST_ID.nextval,
        :new.ID,
        :new.REC_ID,
        :new.NAME_KZ,
        :new.NAME_RU,
        :new.NAME_EN,
        :new.ALG,
        :new.BEGIN_DATE,
        :new.DELFL,
        :new.DATLAST,
        :new.ID_USR,
       Type_Change_,
       :new.USER_LOCATION,
       :new.SENT_KND
      );
  END TRG_AIU_REF_PERIOD_ALG;


CREATE OR REPLACE TRIGGER "TRG_AIU_REF_PERIOD"
AFTER INSERT OR UPDATE ON REF_PERIOD
REFERENCING
  NEW AS NEW
FOR EACH ROW
  DECLARE
    Type_Change_ type_change.type_change%TYPE;
  BEGIN

    IF INSERTING
    THEN
      Type_Change_ := 1;
    ELSIF UPDATING
      THEN
        IF :new.delfl = 1
        THEN
          Type_Change_ := 3;
        ELSE
          Type_Change_ := 2;
        END IF;
    END IF;

    INSERT INTO REF_PERIOD_HST
    (ID_HST,
     ID,
     REC_ID,
     NAME_KZ,
     NAME_RU,
     NAME_EN,
     SHORT_NAME_KZ,
     SHORT_NAME_RU,
     SHORT_NAME_EN,
     REF_PERIOD_ALG,
     AUTO_APPROVE,
     BEGIN_DATE,
     DELFL,
     DATLAST,
     ID_USR,
     TYPE_CHANGE,
     USER_LOCATION,
     SENT_KND
    )
    VALUES
      (SEQ_REF_PERIOD_HST_ID.nextval,
        :new.ID,
        :new.REC_ID,
        :new.NAME_KZ,
        :new.NAME_RU,
        :new.NAME_EN,
        :new.SHORT_NAME_KZ,
        :new.SHORT_NAME_RU,
        :new.SHORT_NAME_EN,
        :new.REF_PERIOD_ALG,
       :new.AUTO_APPROVE,
       :new.BEGIN_DATE,
       :new.DELFL,
       :new.DATLAST,
       :new.ID_USR,
       Type_Change_,
       :new.USER_LOCATION,
       :new.SENT_KND
      );
  END TRG_AIU_REF_PERIOD;

CREATE OR REPLACE TRIGGER TRG_AIU_REF_PERIOD_ARGS
AFTER INSERT OR UPDATE ON REF_PERIOD_ARGS
REFERENCING
  NEW AS NEW
FOR EACH ROW
  DECLARE
    Type_Change_ type_change.type_change%TYPE;
  BEGIN

    IF INSERTING
    THEN
      Type_Change_ := 1;
    ELSIF UPDATING
      THEN
        Type_Change_ := 2;
    END IF;
    INSERT INTO REF_PERIOD_ARGS_HST
    (ID_HST,
     REF_PERIOD_ID,
     NAME,
     VALUE_TYPE,
      INTEGER_VALUE,
      REAL_VALUE,
      BOOLEAN_VALUE,
      STRING_VALUE,
      DATE_VALUE,
     TYPE_CHANGE
    )
    VALUES
      (seq_ref_period_args_hst_id.nextval,
       :new.REF_PERIOD_ID,
       :new.NAME,
       :new.VALUE_TYPE,
       :new.INTEGER_VALUE,
       :new.REAL_VALUE,
       :new.BOOLEAN_VALUE,
       :new.STRING_VALUE,
       :new.DATE_VALUE,
       Type_Change_
      );
  END TRG_AIU_REF_PERIOD_ARGS;