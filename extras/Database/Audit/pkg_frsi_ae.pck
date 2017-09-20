create or replace package pkg_frsi_ae is

  -- Author  : AYUPOV.BAKHTIYAR
  -- Created : 24.11.2015 9:37:56 
  -- Пакет для аудита ФРСП
  
  E_Force_Exit exception;
  
  procedure ae_insert_main(
    ae_name_event_  in ae_main.ae_name_event  % type,
    code_object_    in ae_main.code_object    % type,
    name_object_    in ae_main.name_object    % type,    
    ae_kind_event_  in ae_main.ae_kind_event  % type,    
    date_event_     in ae_main.date_event     % type,
    ref_respondent_ in ae_main.ref_respondent % type,
    date_in_        in ae_main.date_in        % type,
    rec_id_         in ae_main.rec_id         % type,
    user_id_        in ae_main.user_id        % type,
    user_location_  in ae_main.user_location  % type,
    do_commit_      in Integer default 1,
    Err_Code        out number,
    Err_Msg         out varchar2 
  );
  
  procedure ae_read_main(
    begin_date_       in  Date,
    end_date_         in  Date,
    respondent_array_ in  NUMBER_ARRAY,    
    user_code_        in  f_users.screen_name % type,
    event_name_array_ in  NUMBER_ARRAY,
    event_kind_array_ in  NUMBER_ARRAY,    
    code_object_      in  ae_main.code_object % type,
    name_object_      in  ae_main.name_object % type,
    is_archive_       in  ae_main.is_archive % type,
    Cur               out sys_refcursor,
    Err_Code          out number,
    Err_Msg           out varchar2
  );
  
  procedure ae_move_to_from_archive(
    event_array_ in NUMBER_ARRAY,
    is_archive_  in ae_main.is_archive % type,
    do_commit_   in Integer default 1,
    Err_Code     out number,
    Err_Msg      out varchar2
  );

end pkg_frsi_ae;
/
create or replace package body pkg_frsi_ae is

  procedure ae_insert_main(
    ae_name_event_  in ae_main.ae_name_event  % type,
    code_object_    in ae_main.code_object    % type,
    name_object_    in ae_main.name_object    % type,    
    ae_kind_event_  in ae_main.ae_kind_event  % type,    
    date_event_     in ae_main.date_event     % type,
    ref_respondent_ in ae_main.ref_respondent % type,
    date_in_        in ae_main.date_in        % type,
    rec_id_         in ae_main.rec_id         % type,
    user_id_        in ae_main.user_id        % type,
    user_location_  in ae_main.user_location  % type,
    do_commit_      in Integer default 1,
    Err_Code        out number,
    Err_Msg         out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_AE.AE_INSERT_MAIN';
    v_name_object ae_main.name_object % type;
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    v_name_object := name_object_;
    
    if (name_object_ is null) and (code_object_ is not null) then
      if code_object_ like 'ref_%' then
        select name
          into v_name_object
          from ref_main
         where upper(code) = upper(code_object_);
      else
        select name
          into v_name_object
          from forms
         where upper(code) = upper(code_object_);
      end if;
    end if;    
    
    insert into ae_main
      (id,
       ae_name_event,
       code_object,
       name_object,       
       ae_kind_event,       
       date_event,
       ref_respondent,
       date_in,
       rec_id,
       user_id,
       user_location)
    values
      (seq_ae_main_id.nextval,
       ae_name_event_,
       code_object_,
       v_name_object,
       ae_kind_event_,
       date_event_,
       ref_respondent_,
       date_in_,
       rec_id_,
       user_id_,
       user_location_);
       
    if do_commit_ = 1 then
      Commit;
    end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' Ошибка добавления записи в таблицу аудита !';
  end;
  
  
  procedure ae_read_main(
    begin_date_       in  Date,
    end_date_         in  Date,
    respondent_array_ in  NUMBER_ARRAY,
    user_code_        in  f_users.screen_name % type,
    event_name_array_ in  NUMBER_ARRAY,
    event_kind_array_ in  NUMBER_ARRAY,
    code_object_      in  ae_main.code_object % type,
    name_object_      in  ae_main.name_object % type,
    is_archive_       in  ae_main.is_archive % type,
    Cur               out sys_refcursor,
    Err_Code          out number,
    Err_Msg           out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_AE.AE_READ_MAIN';
    v_user_code   varchar2(500 Char);
    v_code_object varchar2(500 Char);
    v_name_object varchar2(500 Char);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    v_user_code   := '%' || user_code_ || '%';
    v_code_object := '%' || code_object_ || '%';
    v_name_object := '%' || name_object_ || '%';
       
    Open Cur for 
      select m.id,             
             m.ae_name_event,
             ne.name as name_event,
             m.name_object,
             m.code_object,
             m.ae_kind_event,
             ke.name as kind_event,
             m.date_event,
             m.ref_respondent,
             lp.name_ru respondent_name,
             m.date_in,
             m.rec_id,
             m.user_id,             
             u.last_name || ' ' || u.first_name || '' || u.middle_name as user_name,
             m.user_location,
             m.datlast,
             m.is_archive
        from ae_main m,
             ae_name_event ne,
             ae_kind_event ke,
             f_users u,
             ref_respondent r,
             ref_legal_person lp
       where m.ae_name_event = ne.id
         and m.ae_kind_event = ke.id
         and m.user_id = u.user_id
         and m.ref_respondent = r.id
         and r.ref_legal_person = lp.id
         and (begin_date_ is null or begin_date_ <= m.date_event)
         and (end_date_ is null or end_date_ >= m.date_event)
         and m.ref_respondent in (select column_value
                                    from table(respondent_array_))
         and m.ae_name_event in (select column_value
                                   from table(event_name_array_))
         and m.ae_kind_event in (select column_value
                                   from table(event_kind_array_))
         and (user_code_ is null or upper(u.screen_name) like upper(v_user_code))
         and (code_object_ is null or upper(m.code_object) like upper(v_code_object))
         and (name_object_ is null or upper(m.name_object) like upper(v_name_object))         
--         and m.is_archive = is_archive_
       order by date_event;

  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' Ошибка получения курсора !';

      open Cur for
        select null from dual;
      rollback;
  end;
  
  procedure ae_move_to_from_archive(
    event_array_ in NUMBER_ARRAY,
    is_archive_  in ae_main.is_archive % type,
    do_commit_   in Integer default 1,
    Err_Code     out number,
    Err_Msg      out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_AE.AE_MOVE_TO_FROM_ARCHIVE';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    update
      (select *
        from ae_main
       where id in (select column_value
                      from table(event_array_))) d
      set d.is_archive = is_archive_;
    
  if do_commit_ = 1 then
    Commit;
  end if;
  
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' Ошибка изменение статуса записи аудит !';
  end;




end pkg_frsi_ae;
/
