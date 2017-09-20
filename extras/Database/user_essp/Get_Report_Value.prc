create or replace procedure Get_Report_Value
------------------------------------------------------------
-- Процедура возвращает занчение идентификатора в отчете. --
-- Входные параметры:                                     --
-- 1. v_form_code - код формы;                            --
-- 2. v_idn - БИН подотчетной организаций;                --
-- 3. v_report_date - отчетная дата.                      --
-- Выходные параметры:                                    --
-- 1. v_result - значение идентифиатора;                  --
-- 2. Err_Code - код ошибки (0 - нет ошибок)              --
-- 3. Err_Msg - сообщение об ошибке                       --
------------------------------------------------------------
(
  v_form_code   in  reports.form_code % type,
  v_idn         in  reports.idn % type,
  v_report_date in  reports.report_date % type,
  v_id          in  varchar2,
  v_result      out number,
  Err_Code      out number,
  Err_Msg       out varchar2
  )
is
  v_report_history_id report_history.id % type;
  E_Force_Exit        exception;
  E_Error_Exit        exception;
begin
  Err_Code := 0;
  Err_Msg := '';
  
  if v_form_code is null then
    Err_Code := 001;
    Err_Msg  := ' v_form_code is null!';
    raise E_Error_Exit;
  end if;

  if v_idn is null then
    Err_Code := 002;
    Err_Msg  := ' v_idn is null!';
    raise E_Error_Exit;
  end if;

  if v_report_date is null then
    Err_Code := 003;
    Err_Msg  := ' v_report_date is null!';
    raise E_Error_Exit;
  end if;
  
  begin
    select rh.id
      into v_report_history_id
      from reports r,
           report_history rh,
           report_history_statuses rhs
     where r.idn = v_idn
       and r.report_date = v_report_date
       and upper(r.form_code) = upper(v_form_code)
       and r.id = rh.report_id
       and rh.id = rhs.report_history_id
       and upper(rhs.status_code) = 'APPROVED'
       and rhs.id = (select max(rhs1.id)
                       from report_history_statuses rhs1
                      where rhs1.report_history_id = rh.id);
  exception
    when no_data_found then
      v_result := 0;
      raise E_Force_Exit;
  end;
  
  v_result := to_number(Pkg_Frsi_Util.Get_Report_Value(lower(v_id), v_report_history_id));
    
exception
  when E_Force_Exit then
    null;
  when E_Error_Exit then
    null;
  when others then
    Err_Code := SqlCode;
    Err_Msg  := SqlErrM;
end;
/
