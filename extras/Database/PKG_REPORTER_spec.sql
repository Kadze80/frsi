create or replace package PKG_REPORTER is

-- Author  : ALIBEK.DABEROV
-- Created : 04.12.2014 9:19:10
-- Purpose : ¿¿¿¿¿¿¿¿¿ ¿¿¿¿¿¿¿ "¿¿¿¿-¿¿¿¿¿¿¿¿" ¿¿ ¿¿¿¿¿¿¿ ¿¿¿¿¿¿ ¿¿¿ ¿¿¿¿¿¿¿¿¿¿ ¿¿¿¿¿¿¿¿ ¿¿¿¿
--           ¿¿¿¿¿ ¿¿¿¿¿¿¿¿ ¿¿¿¿¿¿¿¿¿, ¿¿¿¿¿¿¿¿¿¿¿ ¿ ¿¿¿¿¿¿¿¿ ¿¿¿¿¿¿¿¿¿¿ ¿¿¿¿¿¿¿¿¿¿¿¿¿ ¿¿¿¿¿¿¿¿¿¿¿,
--           ¿¿¿¿¿¿¿¿ ¿¿¿¿, ¿¿¿¿¿¿¿¿¿¿¿¿ ¿¿¿¿¿¿¿¿ ¿¿¿¿¿ ¿ ¿¿¿¿¿¿¿¿¿¿¿¿¿ ¿¿¿¿¿¿ ¿ ¿¿¿¿¿¿¿¿¿¿¿¿ ¿¿¿¿¿¿ KEY-VALUE ¿¿¿¿¿¿¿¿ ¿
--           ¿¿¿¿¿¿¿¿¿ ¿¿¿¿¿¿¿:
--                     "¿¿¿_¿¿¿¿¿"*¿¿¿_¿¿¿¿¿¿¿:¿¿¿_¿¿¿¿¿¿:¿¿¿¿¿¿¿¿_¿¿¿¿_¿¿¿¿¿¿": "¿¿¿¿¿¿¿¿_¿¿¿¿¿¿"
--           ¿¿¿ ¿¿¿_¿¿¿¿¿ = ¿¿¿¿¿¿¿¿¿¿¿¿ ¿¿¿¿¿ + '_array', ¿¿¿¿¿¿¿¿ "f1_array"
--               ¿¿¿_¿¿¿¿¿¿¿ = ¿¿¿¿¿¿¿ ¿¿¿¿¿¿ - ¿¿¿ ¿¿¿¿¿¿¿, ¿¿¿¿¿¿¿¿ "code"
--               ¿¿¿_¿¿¿¿¿¿ = ¿¿¿¿¿¿¿ ¿¿¿¿¿¿ - ¿¿¿ ¿¿¿¿¿¿, ¿¿¿¿¿¿¿¿ "endpr_sum"
--               ¿¿¿¿¿¿¿¿_¿¿¿¿_¿¿¿¿¿¿ = ¿¿¿¿¿¿¿¿ ¿¿¿¿ ¿¿¿¿¿¿, ¿¿¿¿¿¿¿¿ "1.1"
--               ¿¿¿¿¿¿¿¿_¿¿¿¿¿¿ = ¿¿¿¿¿¿¿¿ ¿¿¿¿¿¿ ¿¿ ¿¿¿¿¿¿¿ ¿¿¿¿¿.
--           ¿¿¿¿¿¿ ¿¿¿¿¿¿¿¿¿¿ ¿¿¿¿¿¿ ¿¿¿¿¿¿¿¿¿:
--                     "f1_array*endpr_sum:code:1.1": "0"
--                     "f1_array*endpr_sum:code:1.2": "7170617"
--                     "f1_array*endprevpr_sum:code:1.1": "0"
--                     "f1_array*endprevpr_sum:code:1.2": "563615"
--                     "f1_array*endpr_sum:code:7.1": "0"
--                     "f1_array*endprevpr_sum:code:40": "-465207"
--                     "f1_array*endprevpr_sum:code:41": "-19610929"
  procedure get_data(p_creditor_id     in eav_a_creditor_user.creditor_id%TYPE,
                     p_report_date     in eav_batches.rep_date%TYPE,
                     p_input_form_name in eav_m_classes.name%TYPE,
                     p_code_fld        in eav_m_simple_attributes.name%TYPE,
                     p_cursor_         OUT SYS_REFCURSOR);

end PKG_REPORTER;
