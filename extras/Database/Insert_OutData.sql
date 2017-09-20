CREATE or replace Procedure Insert_OutData
   (
     p_resps_array   in  resps_array,
     p_user_id       in number,
     p_report_date   in date,
     p_received_date in date,
     p_is_approved   in number,
     p_form_name     in varchar2,
     p_couchbase_id  in number,
     Err_Code        OUT NUMBER,
     Err_Msg         OUT VARCHAR2
 )

IS
    Procnum CONSTANT VARCHAR2(24) := 'Insert_OutData';
    id_out_date number;
BEGIN
    id_out_date := SEQ_OUT_DATA.nextval;
    insert into out_data
      (id, user_id, report_date, is_approved, form_name, received_date, couchbase_id)
    values
      (id_out_date, p_user_id, p_report_date, p_is_approved, p_form_name, p_received_date, p_couchbase_id);



    FOR i IN 1..p_resps_array.COUNT
      LOOP
          insert into out_data_resps
            (id, respname, out_data_id)
          values
            (SEQ_OUT_DATA_RESPS.nextval, p_resps_array(i), id_out_date);
      END LOOP;


     commit;
EXCEPTION
  WHEN OTHERS THEN
     Err_Code := SQLCODE;
     Err_Msg := ProcNum || ' ' || SQLERRM;
     raise_application_error(-20001,'An error was encountered - '||SQLCODE||' -ERROR- '||SQLERRM);
END Insert_OutData;


