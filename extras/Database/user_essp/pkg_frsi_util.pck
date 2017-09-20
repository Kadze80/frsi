CREATE OR REPLACE PACKAGE pkg_frsi_util AS

  type respondent_item is record
  (
    id number,
    bin varchar2(12),
    name_en varchar2(250),
    name_kz varchar2(250),
    name_ru varchar2(250),
    subject_type varchar2(250)
  );

  type respondent_table is table of respondent_item index by binary_integer;
  
  type subject_type_code_list is table of varchar2(250) index by binary_integer;
  
  type form_code_list is table of varchar2(255) index by binary_integer;

/* очистка пакета
  FUNCTION get_respondent_bin
  (
    p_respondent_id IN NUMBER,
    p_report_date IN DATE
  ) RETURN VARCHAR2;*/

/* очистка пакета
  FUNCTION get_respondents
  (
    p_report_date IN DATE
  ) RETURN SYS_REFCURSOR;*/

/* очистка пакета
  FUNCTION get_subjecttype_byresp
  (
    --p_report_date IN DATE,
    resp_id in number
  ) RETURN varchar2;*/
  
  FUNCTION get_user_resp
  (
    p_user_id in number,
    p_report_date IN DATE
  ) RETURN SYS_REFCURSOR;
  
  FUNCTION get_user_resp_by_subjecttype
  (
    p_user_id in number,
    p_st_rec_id in number,
    p_report_date IN DATE
  ) RETURN SYS_REFCURSOR;

/* очистка пакета
  FUNCTION get_user_resp_by_subjecttypes
  (
    p_user_id in number,
    p_st_codes in subject_type_code_list,
    p_report_date IN DATE
  ) RETURN SYS_REFCURSOR;*/

/* очистка пакета
  FUNCTION get_crosscheck
  (
    p_report_date IN VARCHAR2,
    form_name_   in VARCHAR2
  ) RETURN SYS_REFCURSOR;
  */
  
  FUNCTION check_period
  (
    period_code in rep_per_dur_months.code%type, 
  report_date in Date
  ) RETURN number;
  
  FUNCTION get_report_value
  (
    p_key in varchar2,
    p_report_history_id in report_history.id%type
  ) RETURN varchar2;
  
  FUNCTION get_last_status_code
  (
    p_report_history_id      IN NUMBER
  ) RETURN VARCHAR2; 


END pkg_frsi_util;
/
CREATE OR REPLACE PACKAGE BODY "PKG_FRSI_UTIL" AS
-- *****************************************************

/* очистка пакета
FUNCTION get_respondent_bin
(
  p_respondent_id IN NUMBER,
  p_report_date IN DATE
) RETURN VARCHAR2
AS
  v_doucment_type_bin_id NUMBER;
  v_bin VARCHAR2(12 CHAR);

BEGIN
  begin
    select e.id into v_doucment_type_bin_id
    from eav_be_entities e, eav_m_classes c
    where
      c.id = e.class_id and
      c.name = 'ref_doc_type' and
      pkg_eav_util.get_string_value(e.id, 'code', p_report_date) = '07';
  exception
    when others then v_doucment_type_bin_id := null;
  end;

  begin
    select pkg_eav_util.get_string_value(csvn.entity_value_id, 'no', p_report_date) into v_bin
    from (select rank() over(order by csv.report_date desc) as num_pp, csv.entity_value_id, csv.is_closed
          from (select tn.attribute_id, tn.set_id
                from (select rank() over(partition by t.attribute_id order by t.report_date desc) as num_pp, t.attribute_id, t.set_id, t.is_closed
                      from eav_be_entity_complex_sets t
                      where t.entity_id = p_respondent_id and t.report_date <= p_report_date) tn
                where tn.num_pp = 1 and tn.is_closed = 0) ecs,
                eav_be_complex_set_values csv, eav_m_complex_set cs
          where ecs.attribute_id = cs.id and cs.name = 'docs' and ecs.set_id = csv.set_id and csv.report_date <= p_report_date) csvn
    where csvn.num_pp = 1 and csvn.is_closed = 0 and pkg_eav_util.get_complex_value_id(csvn.entity_value_id, 'doc_type') = v_doucment_type_bin_id;
  exception when others then v_bin := null;
  end;

  return v_bin;
END get_respondent_bin;*/
-- *****************************************************

/* очистка пакета
FUNCTION get_respondents
(
  p_report_date IN DATE
) RETURN SYS_REFCURSOR
AS
  v_respondents SYS_REFCURSOR;

BEGIN
  open v_respondents for
  select
    e.id as id,
    --pkg_frsi_util.get_respondent_bin(e.id, p_report_date) as bin,
    pkg_eav_util.get_string_value(e.id, 'ref_legal_person.idn') as bin, --веременно
    pkg_eav_util.get_string_value(e.id, 'ref_legal_person.name_ru') as name,
    pkg_eav_util.get_string_value(e.id, 'subject_type.name_ru') as subject_type_name_ru,
    pkg_eav_util.get_string_value(e.id, 'subject_type.code') as subject_type_code
  from
    eav_be_entities e,
    eav_m_classes c
  where
   c.id = e.class_id and
   c.name = 'ref_respondent';

  return v_respondents;
END get_respondents;*/
-- *****************************************************
/* Очистка пакета
FUNCTION get_subjecttype_byresp
(
  --p_report_date IN DATE,
  resp_id in number
) RETURN varchar2
AS
  id number;
  bin varchar2(64);
  name_ varchar2(250);
  subject_type_name_ru varchar2(250);
  subject_type_code_ varchar2(64);
BEGIN
--  open v_respondents for
  select
    e.id as id,
    --pkg_frsi_util.get_respondent_bin(e.id, p_report_date) as bin,
    pkg_eav_util.get_string_value(e.id, 'name') as name,
    pkg_eav_util.get_string_value(e.id, 'subject_type.name_ru') as subject_type_name_ru,
    pkg_eav_util.get_string_value(e.id, 'subject_type.code') as subject_type_code
  into
    id,
    --bin,
    name_,
    subject_type_name_ru,
    subject_type_code_
  from
    eav_be_entities e,
    eav_m_classes c
  where
   c.id = e.class_id and
   c.name = 'ref_respondent' and
   e.id = resp_id;

  return subject_type_code_;

END get_subjecttype_byresp;*/
-- *****************************************************

FUNCTION get_user_resp(
  p_user_id in number, 
  p_report_date in date
) return sys_refcursor 
AS  
  v_respondents SYS_REFCURSOR;
begin
   
   open v_respondents for
   select r.id,
             r.rec_id,
             r.code,
             r.ref_legal_person,
             lp.name_ru as LEGAL_PERSON_NAME,
             lp.idn,
             lp.rec_id as ref_legal_person_rec_id,
             r.ref_subject_type,
             st.rec_id as ref_subject_type_rec_id,
             st.name_ru as ref_subject_type_name,
             r.REF_DEPARTMENT,
             d.rec_id as REF_DEPARTMENT_REC_ID,
             d.name_ru as REF_DEPARTMENT_NAME,
             r.nokbdb_code,
             r.main_buh,
             r.date_begin_lic,
             r.date_end_lic,
             r.stop_lic,
             r.vid_activity,              
             r.begin_date,
             r.end_date,
             r.datlast,
             r.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             r.user_location,
             sk.name as sent_knd
        from v_ref_respondent r,             
             v_ref_legal_person lp,
             v_ref_subject_type st,
             v_ref_department d,
             f_users u,
             sent_knd sk,
             f_session_creditors s
       where r.id_usr = u.user_id
         and r.rec_id = s.creditor_id
         and s.user_id = p_user_id
         and r.REF_DEPARTMENT = d.id(+)
         and r.ref_legal_person = lp.id
         and r.ref_subject_type = st.id
         and r.sent_knd = sk.sent_knd         
         and (p_report_date is null or r.begin_date = (select max(t.begin_date)
                                                         from v_ref_respondent t
                                                        where t.rec_id = r.rec_id
                                                          and t.begin_date <= p_report_date))
         and (r.end_date is null or r.end_date > p_report_date);

  return v_respondents;
end get_user_resp;
-- *****************************************************


FUNCTION get_user_resp_by_subjecttype(
  p_user_id in number, 
  p_st_rec_id in number, 
  p_report_date in date
) return sys_refcursor 
AS
  v_respondents SYS_REFCURSOR;
begin
   open v_respondents for
   select r.id,
             r.rec_id,
             r.code,
             r.ref_legal_person,
             lp.name_ru as LEGAL_PERSON_NAME,
             nvl(lp.short_name_ru, lp.name_ru) as LEGAL_PERSON_SHORT_NAME,
             lp.idn,
             lp.rec_id as ref_legal_person_rec_id,
             r.ref_subject_type,
             st.rec_id as ref_subject_type_rec_id,
             st.name_ru as ref_subject_type_name,
             r.REF_DEPARTMENT,
             d.rec_id as REF_DEPARTMENT_REC_ID,
             d.name_ru as REF_DEPARTMENT_NAME,
             r.nokbdb_code,
             r.main_buh,
             r.date_begin_lic,
             r.date_end_lic,
             r.stop_lic,
             r.vid_activity,              
             r.begin_date,
             r.end_date,
             r.datlast,
             r.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             r.user_location,
             sk.name as sent_knd
        from v_ref_respondent r,             
             v_ref_legal_person lp,
             v_ref_subject_type st,
             v_ref_department d,
             f_users u,
             sent_knd sk,
             f_session_creditors s
       where r.id_usr = u.user_id
         and r.rec_id = s.creditor_id
         and s.user_id = p_user_id
         and r.REF_DEPARTMENT = d.id(+)
         and r.ref_legal_person = lp.id
         and r.ref_subject_type = st.id
         and st.rec_id = p_st_rec_id
         and r.sent_knd = sk.sent_knd         
         and (p_report_date is null or r.begin_date = (select max(t.begin_date)
                                                         from v_ref_respondent t
                                                        where t.rec_id = r.rec_id
                                                          and t.begin_date <= p_report_date))
         and (r.end_date is null or r.end_date > p_report_date);

  return v_respondents;
end get_user_resp_by_subjecttype;
-- *****************************************************
/* очистка пакета
FUNCTION get_user_resp_by_subjecttypes(
  p_user_id in number, 
  p_st_codes in subject_type_code_list, 
  p_report_date in date
) return sys_refcursor 
AS
  v_respondents SYS_REFCURSOR;
  v_codes varchar2(250) := '''0''';
begin
  
  FOR i IN p_st_codes.FIRST .. p_st_codes.LAST
  LOOP
      v_codes := v_codes || ',' || '''' || p_st_codes(i) || '''';
  END LOOP;

  open v_respondents for
  'select * from (select
    e.id as id,
    pkg_frsi_util.get_respondent_bin(e.id, to_date(''' || to_char(p_report_date, 'dd.mm.yyyy') || ''',''dd.mm.yyyy'')) as bin,
    pkg_eav_util.get_string_value(e.id, ''name'') as name,
    pkg_eav_util.get_string_value(e.id, ''subject_type.name_ru'') as subject_type_name_ru,
    pkg_eav_util.get_string_value(e.id, ''subject_type.code'') as subject_type_code
  from
    eav_be_entities e
    inner join f_session_creditors s on e.id=s.creditor_id
  where
   s.user_id = ' || p_user_id ||
   ') where  subject_type_code in (' || v_codes || ')';

  return v_respondents;
end get_user_resp_by_subjecttypes;*/
-- *****************************************************

/* очистка пакета
FUNCTION get_crosscheck
(
  p_report_date IN VARCHAR2,
  form_name_   in VARCHAR2
) RETURN SYS_REFCURSOR
AS
  v_crosscheck SYS_REFCURSOR;

BEGIN
  open v_crosscheck for
  select * from (
    select
      e.id as id,
      --pkg_frsi_util.get_respondent_bin(e.id, p_report_date) as bin,
      pkg_eav_util.get_string_value(e.id, 'internal_formula', to_date(p_report_date, 'dd.MM.yyyy')) as internal_formula,
      pkg_eav_util.get_string_value(e.id, 'external_formula', to_date(p_report_date, 'dd.MM.yyyy')) as external_formula,
      pkg_eav_util.get_string_value(e.id, 'formname', to_date(p_report_date, 'dd.MM.yyyy')) as formname,
      pkg_eav_util.get_string_value(e.id, 'condition', to_date(p_report_date, 'dd.MM.yyyy')) as condition,
      pkg_eav_util.get_string_value(e.id, 'expression', to_date(p_report_date, 'dd.MM.yyyy')) as expression
    from
      eav_be_entities e,
      eav_m_classes c
    where
     c.id = e.class_id and
     c.name = 'ref_crosscheck') c
   where c.formname='f1';

  return v_crosscheck;
END get_crosscheck;*/
-- *****************************************************

FUNCTION check_period
(
  period_code in rep_per_dur_months.code%type, 
  report_date in Date
) RETURN number
AS
  Result NUMBER := 0;
  v_day NUMBER; 
  v_month NUMBER;
BEGIN
  v_day := extract(day from report_date);
  v_month := extract(month from report_date);
  
  CASE period_code
    WHEN 'm' THEN
      IF v_day = 1 THEN Result := 1; ELSE Result := 0; END IF;
    WHEN 'q' THEN
      IF v_day = 1 AND v_month in (1,4,7,10) THEN Result := 1; ELSE Result := 0; END IF;   
    WHEN 'h' THEN
      IF v_day = 1 AND v_month in (1,7) THEN Result := 1; ELSE Result := 0; END IF; 
    WHEN 'y' THEN
      IF v_day = 1 AND v_month = 1 THEN Result := 1; ELSE Result := 0; END IF;
    WHEN 'd' THEN
      Result := 1;  
    ELSE
      Result := 0;
   END CASE;     
  
  RETURN(Result);
END check_period;
-- *****************************************************

FUNCTION get_report_value
  (
    p_key in varchar2,
    p_report_history_id in report_history.id%type
  ) RETURN varchar2
AS
  result varchar2(400) := '';
  v_is_exist_list NUMBER;
  v_ind NUMBER; 
  v_str varchar2(400):=null;
  v_last_quote_index number;
  v_key varchar2(400);
BEGIN
  select is_exist_list 
         into v_is_exist_list
    from report_history
   where id = p_report_history_id;
  
  if v_is_exist_list = 1 then
    select value
      into result
      from report_history_list
     where report_history_id = p_report_history_id
       and key = p_key;
  else
    v_key := p_key || '"';
    select dbms_lob.instr(t.data, v_key) 
           into v_ind
    from report_history t 
    where t.id = p_report_history_id;
    
    if v_ind>0 then
      select dbms_lob.substr(t.data, 25, v_ind + length(v_key) + 2) into v_str
             from report_history t 
             where t.id = p_report_history_id;

      if v_str is not null then
            v_last_quote_index := instr(v_str, '"');
            if(v_last_quote_index>0) then
              result := substr(v_str, 1, v_last_quote_index-1);
            end if;
      end if;
    end if;
  end if;
  
  RETURN(result);
END get_report_value;
-- *****************************************************
  
  FUNCTION get_last_status_code
    (
      p_report_history_id IN NUMBER
    )
    RETURN VARCHAR2
  AS
    Result VARCHAR2(400) := '';
    BEGIN


      SELECT s.STATUS_CODE
      INTO Result
      FROM REPORT_HISTORY_STATUSES s
      WHERE s.ID = (SELECT max(s2.ID)
                    FROM REPORT_HISTORY_STATUSES s2
                    WHERE s2.REPORT_HISTORY_ID = p_report_history_id);

      RETURN (Result);
    END get_last_status_code;
-- *****************************************************  

end pkg_frsi_util;
/
