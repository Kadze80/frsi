CREATE or REPLACE PACKAGE pkg_frsi_util AS

  type respondent_item is legalPersonItem
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

  FUNCTION floor_date
    (
      date_ IN DATE,
      pt    IN VARCHAR2
    )
    RETURN DATE;

  FUNCTION plus_period
    (
      date_ IN DATE,
      pt    IN VARCHAR2,
      period_count IN INT
    )
    RETURN DATE;

END pkg_frsi_util;
