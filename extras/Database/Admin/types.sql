CREATE TYPE PERMISSION_FORM_ROW AS OBJECT (
  id                    NUMBER,
  permission_name       VARCHAR2(100),
  form_code             VARCHAR2(100),
  form_name             VARCHAR2(250),
  ref_respondent_rec_id NUMBER,
  is_active             NUMBER(1),
  is_init_active        NUMBER(1),
  is_inh_active         NUMBER(1),
  is_for_group          NUMBER(1)
);

CREATE TYPE PERMISSION_FORM_TABLE AS TABLE OF PERMISSION_FORM_ROW;