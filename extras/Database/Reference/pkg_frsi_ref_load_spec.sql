create or replace package pkg_frsi_ref_load is

  -- Author  : AYUPOV.BAKHTIYAR
  -- Created : 24.05.2015 11:25:16
  -- �������� ������������ � ������� ������ (���, �����)
  
  E_Force_Exit exception;
  old_date Date := to_date('01.01.1900','dd.mm.yyyy');
  
  /* ������� ��� ���������� rec_id �� ���� � ���� ������ */
  function ref_get_rec_id(
    ref_code_   in ref_main.code   % type,
    code_       in varchar2
  )return number;
  
  /* ���������� ���� �������� � ref_main */
  procedure ref_update_main(
    ref_code_ in ref_main.code % type,
    sts_load_ in ref_main.sts_load % type
  );
  
  /* ��������� �� �������� ������������ */
  procedure ref_load(
    ref_code_      in  ref_main.code % type,
    id_usr_        in  f_users.user_id % type,
    user_location_ in  VARCHAR2,  
    Err_Code       out number,
    Err_Msg        out Varchar2
  );
  
  /* �������� ����������� ������ */
  procedure ref_bank_load(
    id_usr_        in  ref_bank.id_usr        % type,
    user_location_ in  ref_bank.user_location % type,
    Do_Commit_     in  integer default 1, 
    Err_Code       out number,
    Err_Msg        out varchar2
  );
  
  /* �������� ����������� ����������� ������� */
  procedure ref_rate_agency_load(
    id_usr_        in  ref_rate_agency.id_usr        % type,
    user_location_ in  ref_rate_agency.user_location % type,
    Do_Commit_     in  integer default 1, 
    Err_Code       out number,
    Err_Msg        out varchar2
  );
  
  /* �������� ����������� ����� */
  procedure ref_currency_load(
    id_usr_        in  ref_currency.id_usr        % type,
    user_location_ in  ref_currency.user_location % type,
    Do_Commit_     in  integer default 1, 
    Err_Code       out number,
    Err_Msg        out varchar2
  );
  
  /* �������� ����������� �������� ����� */
  procedure ref_currency_rate_load(
    id_usr_        in  ref_currency_rate.id_usr        % type,
    user_location_ in  ref_currency_rate.user_location % type,
    Do_Commit_     in  integer default 1, 
    Err_Code       out number,
    Err_Msg        out varchar2
  );
  
  /* �������� ����������� ������� � �������� */
  procedure ref_region_load(
    id_usr_        in  ref_region.id_usr        % type,
    user_location_ in  ref_region.user_location % type,
    Do_Commit_     in  integer default 1, 
    Err_Code       out number,
    Err_Msg        out varchar2
  );
  
  /* �������� ����������� ����� */
  procedure ref_country_load(
    id_usr_        in  ref_country.id_usr        % type,
    user_location_ in  ref_country.user_location % type,
    Do_Commit_     in  integer default 1, 
    Err_Code       out number,
    Err_Msg        out varchar2
  );
  
  /* �������� ����������� �������������� �� �� */
  procedure ref_department_load(
    id_usr_        in  ref_department.id_usr        % type,
    user_location_ in  ref_department.user_location % type,
    Do_Commit_     in  integer default 1, 
    Err_Code       out number,
    Err_Msg        out varchar2
  );
  
  /* �������� ����������� �� */
  procedure ref_securities_load(
    id_usr_        in  ref_securities.id_usr        % type,
    user_location_ in  ref_securities.user_location % type,
    Do_Commit_     in  integer default 1, 
    Err_Code       out number,
    Err_Msg        out varchar2
  );
  
  /* �������� ����������� ��������� */
  procedure ref_issuers_load(
    id_usr_        in  ref_issuers.id_usr        % type,
    user_location_ in  ref_issuers.user_location % type,
    Do_Commit_     in  integer default 1, 
    Err_Code       out number,
    Err_Msg        out varchar2
  );
  
  
end pkg_frsi_ref_load;
/
create or replace package body pkg_frsi_ref_load is

  function ref_get_rec_id(
    ref_code_   in ref_main.code   % type,
    code_       in varchar2
  )return number 
  is
    rec_id number;
  begin           
    execute immediate
      'select distinct(t.rec_id) ' ||
        'from ' || ref_code_  || ' t ' ||
        'where t.delfl = 0 ' ||
         'and upper(t.code) = upper(:code_) ' ||
         'and t.begin_date = (select max(t1.begin_date) ' ||
                               'from ' || ref_code_ || ' t1 ' ||
                              'where t1.rec_id = t.rec_id ' ||
                                'and t1.begin_date <= sysdate ' ||
                                'and t1.delfl = 0)'
    into rec_id
    using code_;
    
    return(rec_id);
    
  exception
    when others then
      return null;
  end;
  
  
  procedure ref_update_main(
    ref_code_ in ref_main.code % type,
    sts_load_ in ref_main.sts_load % type
  )
  is
  begin              
    update REF_MAIN
       set date_load = sysdate,
           sts_load  = sts_load_        
     where code      = ref_code_;    
  exception
    when others then
      null;
  end;
          
  procedure ref_load(
     ref_code_      in  ref_main.code % type,     
     id_usr_        in  f_users.user_id % type,
     user_location_ in  VARCHAR2,  
     Err_Code       out number,
     Err_Msg        out Varchar2
  ) 
  is
    ProcName     constant Varchar2(50) := 'PKG_FRSI_REF.REF_LOAD';
    E_Force_Exit exception;
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    if ref_code_ = 'ref_bank' then
      ref_bank_load(Id_Usr_        => id_usr_,
                    User_Location_ => user_location_,
                    Do_Commit_     => 0,
                    Err_Code       => Err_Code,
                    Err_Msg        => Err_Msg);                                
    elsif ref_code_ = 'ref_rate_agency' then
      ref_rate_agency_load(Id_Usr_        => id_usr_,
                           User_Location_ => user_location_,
                           Do_Commit_     => 0,
                           Err_Code       => Err_Code,
                           Err_Msg        => Err_Msg);
    elsif ref_code_ = 'ref_currency' then
      ref_currency_load(Id_Usr_        => id_usr_,
                        User_Location_ => user_location_,
                        Do_Commit_     => 0,
                        Err_Code       => Err_Code,
                        Err_Msg        => Err_Msg);
    elsif ref_code_ = 'ref_currency_rate' then
      ref_currency_rate_load(Id_Usr_        => id_usr_,
                             User_Location_ => user_location_,
                             Do_Commit_     => 0,
                             Err_Code       => Err_Code,
                             Err_Msg        => Err_Msg);
    elsif ref_code_ = 'ref_region' then
      ref_region_load(Id_Usr_        => id_usr_,
                      User_Location_ => user_location_,
                      Do_Commit_     => 0,
                      Err_Code       => Err_Code,
                      Err_Msg        => Err_Msg);
    elsif ref_code_ = 'ref_country' then
      ref_country_load(Id_Usr_        => id_usr_,
                       User_Location_ => user_location_,
                       Do_Commit_     => 0,
                       Err_Code       => Err_Code,
                       Err_Msg        => Err_Msg);
    elsif ref_code_ = 'ref_department' then
      ref_department_load(Id_Usr_        => id_usr_,
                          User_Location_ => user_location_,
                          Do_Commit_     => 0,
                          Err_Code       => Err_Code,
                          Err_Msg        => Err_Msg);
    elsif ref_code_ = 'ref_securities' then
      ref_securities_load(Id_Usr_        => id_usr_,
                          User_Location_ => user_location_,
                          Do_Commit_     => 0,
                          Err_Code       => Err_Code,
                          Err_Msg        => Err_Msg);
    elsif ref_code_ = 'ref_issuers' then
      ref_issuers_load(Id_Usr_        => id_usr_,
                       User_Location_ => user_location_,
                       Do_Commit_     => 0,
                       Err_Code       => Err_Code,
                       Err_Msg        => Err_Msg);            
    end if;
    
    ref_update_main(ref_code_,Err_Msg);
    
    Commit;
    
    if Err_Code <> 0 then
      Err_Msg  := ProcName || ' 001 -> ' || Err_Msg;
      raise E_Force_Exit;
    end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ������ ���������� ������ !';    
  end;
  
  
  procedure ref_bank_load(
    id_usr_        in  ref_bank.id_usr        % type,
    user_location_ in  ref_bank.user_location % type,
    Do_Commit_     in  integer default 1, 
    Err_Code       out number,
    Err_Msg        out varchar2
  ) 
  is
    ProcName     constant Varchar2(50) := 'PKG_FRSI_REF.REF_BANK_LOAD';
    id_          ref_bank.id % type;
  begin
    Err_Code := 0;
    Err_Msg  := '���������� �������� �������!';
    
    for Rec in (select gb.gs_bank,
                       gb.rec_id,
                       to_char(gb.rec_id) as code,
                       gb.bik,
                       gb.bik_head,
                       gb.bik_nbrk,
                       convert_win1251_to_utf8_kz(gb.name_kaz) as name_kaz,
                       gb.name,                     
                       gb.name_eng,
                       gb.bin idn,
                       gb.post_address,
--                       decode(gb.gl_oper_type, 3, 1, 0) delfl,
                       gb.phone_num,
                       gb.begin_date
                  from MAIN.gs_bank@nsi gb,
                       MAIN.gs_bank_state@nsi gbs
                 where gb.gs_bank = gbs.gs_bank
                   and gb.gs_client_type = 1
                   and gbs.is_last_state = 1
                   and gbs.confirm_step = 231
                   and gb.is_active = 1
                minus
                select rb.id,
                       rb.rec_id,
                       rb.code,
                       rb.bic,
                       rb.bic_head,
                       rb.bic_nbrk,
                       rb.name_kz,
                       rb.name_ru,                     
                       rb.name_en,
                       rb.idn,
                       rb.post_address,
--                       rb.delfl,
                       rb.phone_num,
                       rb.begin_date
                  from ref_bank rb)
    loop
               
      begin
        select id
          into id_
          from ref_bank 
         where id = Rec.Gs_Bank;
      exception
        when no_data_found then
          id_ := null;
      end;
      
      if id_ is null then
        insert into ref_bank
          (id,
           rec_id,
           code,
           bic,
           bic_head,
           bic_nbrk,
           name_kz,
           name_ru,
           name_en,
           idn,
           post_address,         
           phone_num,
           begin_date,
--           delfl,
           id_usr,
           user_location)
         values(
           Rec.Gs_Bank,
           Rec.Rec_id,
           Rec.Code,
           Rec.Bik,
           Rec.Bik_Head,
           Rec.Bik_Nbrk,
           Rec.Name_Kaz,
           Rec.Name,
           Rec.Name_Eng,
           Rec.Idn,
           Rec.Post_Address,         
           Rec.Phone_Num,
           Rec.Begin_Date,
--           Rec.Delfl,
           id_usr_,
           user_location_);
      else
        update ref_bank t
           set t.code = Rec.Code,
               t.bic = Rec.Bik,
               t.bic_head = Rec.Bik_Head,
               t.bic_nbrk = Rec.Bik_Nbrk,
               t.name_kz = Rec.Name_Kaz,
               t.name_ru = Rec.Name,
               t.name_en = Rec.Name_Eng,
               t.idn = Rec.Idn,
               t.post_address = Rec.Post_Address,
--               t.delfl = Rec.Delfl,
               t.phone_num = Rec.Phone_Num,
               t.begin_date = Rec.Begin_Date,
               t.id_usr = id_usr_,
               t.user_location = user_location_,
               t.sent_knd = 0
         where t.id = id_;
      end if;
    end loop;
    
    for Rec in (select rb.id
                  from ref_bank rb
                 where rb.delfl = 0
                minus
                select gb.gs_bank
                  from MAIN.gs_bank@nsi gb,
                       MAIN.gs_bank_state@nsi gbs
                 where gb.gs_bank = gbs.gs_bank
                   and gb.gs_client_type = 1
                   and gbs.is_last_state = 1
                   and gbs.confirm_step = 231
                   and gb.is_active = 1)
    loop
      update ref_bank   
         set delfl = 1
       where id = Rec.id; 
    end loop;
    
    if Do_Commit_ = 1 then
      Commit;
    end if;
    
  exception
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ������ ���������� ����������� "�����"!';
  end;
  
  
  procedure ref_rate_agency_load(
    id_usr_        in  ref_rate_agency.id_usr        % type,
    user_location_ in  ref_rate_agency.user_location % type,
    Do_Commit_     in  integer default 1, 
    Err_Code       out number,
    Err_Msg        out varchar2
  ) 
  is
    ProcName         constant Varchar2(50) := 'PKG_FRSI_REF.REF_RATE_AGENCY_LOAD';
    id_              ref_rate_agency.id % type;
  begin
    Err_Code := 0;
    Err_Msg  := '���������� �������� �������!';
    
    for Rec in (select ga.gd_rate_agency,
                       ga.rec_id,
                       ga.code,
                       ga.name,
                       ga.begin_date/*,
                       decode(ga.gl_oper_type, 3, 1, 0) delfl*/
                  from MAIN.gd_rate_agency@nsi ga,
                       MAIN.gd_rat_agency_state@nsi gas
                 where ga.gd_rate_agency = gas.gd_rate_agency
                   and gas.is_last_state = 1
                   and gas.confirm_step = 231
                   and ga.is_active = 1
                minus
                select ra.id,
                       ra.rec_id,
                       ra.code,
                       ra.name_ru,
                       ra.begin_date/*,
                       ra.delfl*/
                  from ref_rate_agency ra)
    loop
               
      begin
        select id
          into id_
          from ref_rate_agency 
         where id = Rec.gd_rate_agency;
      exception
        when no_data_found then
          id_ := null;
      end;
      
      if id_ is null then
        insert into ref_rate_agency
          (id,
           rec_id,
           code,
           name_ru,
           begin_date,
--           delfl,
           id_usr,
           user_location)
         values(
           Rec.gd_rate_agency,
           Rec.Rec_Id,
           Rec.Code,
           Rec.name,
           Rec.Begin_Date,
--           Rec.Delfl,
           id_usr_,
           user_location_);
      else
        update ref_rate_agency t
           set t.code = Rec.Code,             
               t.name_ru = Rec.name,
               t.begin_date = Rec.Begin_Date,
--               t.delfl = Rec.Delfl,
               t.id_usr = id_usr_,
               t.user_location = user_location_,
               t.sent_knd = 0
         where t.id = id_;
      end if;
    end loop;
    
    for Rec in (select ra.id
                  from ref_rate_agency ra
                 where ra.delfl = 0
                minus
                select ga.gd_rate_agency
                  from MAIN.gd_rate_agency@nsi ga,
                       MAIN.gd_rat_agency_state@nsi gas
                 where ga.gd_rate_agency = gas.gd_rate_agency
                   and gas.is_last_state = 1
                   and gas.confirm_step = 231
                   and ga.is_active = 1)
    loop
      update ref_rate_agency   
         set delfl = 1
       where id = Rec.id; 
    end loop;
    
    if Do_Commit_ = 1 then
      Commit;
    end if;
    
  exception
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ������ ���������� ����������� "����������� �������"!';
  end;
  
  procedure ref_currency_load(
    id_usr_        in  ref_currency.id_usr        % type,
    user_location_ in  ref_currency.user_location % type,
    Do_Commit_     in  integer default 1, 
    Err_Code       out number,
    Err_Msg        out varchar2
  ) 
  is
    ProcName      constant Varchar2(50) := 'PKG_FRSI_REF.REF_CURRENCY_LOAD';
    id_           ref_currency.id % type;
  begin
    Err_Code := 0;
    Err_Msg  := '���������� �������� �������!';
    
    for Rec in (select gc.gs_currency,
                       gc.rec_id,
                       gc.code_3a as code,
                       gc.small_unit,
                       convert_win1251_to_utf8_kz(gc.name_kaz) as name_kaz,
                       gc.name_rus,
                       gc.name_eng,
                       gc.gd_currency_rate,
                       gc.begin_date/*,
                       decode(gc.gl_oper_type, 3, 1, 0) delfl*/
                  from MAIN.gs_currency@nsi gc,
                       MAIN.gs_currency_state@nsi gcs
                 where gc.gs_currency = gcs.gs_currency
                   and gcs.is_last_state = 1
                   and gcs.confirm_step = 231
                   and gc.is_active = 1
                minus
                select rc.id,
                       rc.rec_id,
                       rc.code,
                       rc.minor_units,
                       rc.name_kz,
                       rc.name_ru,
                       rc.name_en,
                       rc.ref_currency_rate,
                       rc.begin_date/*,
                       rc.delfl*/
                  from ref_currency rc)
    loop
               
      begin
        select id
          into id_
          from ref_currency 
         where id = Rec.gs_currency;
      exception
        when no_data_found then
          id_ := null;
      end;
      
      if id_ is null then
        insert into ref_currency
          (id,
           rec_id,
           code,
           minor_units,         
           name_kz,
           name_ru,
           name_en,
           ref_currency_rate,
           begin_date,
--           delfl,
           id_usr,
           user_location)
         values(
           Rec.gs_currency,
           Rec.Rec_Id,
           Rec.Code,
           Rec.small_unit,
           Rec.name_kaz,
           Rec.name_rus,
           Rec.name_eng,
           Rec.gd_currency_rate,
           Rec.Begin_Date,
--           Rec.Delfl,
           id_usr_,
           user_location_);
      else
        update ref_currency t
           set t.code = Rec.Code,
               t.minor_units = Rec.small_unit,             
               t.name_kz = Rec.name_kaz,
               t.name_ru = Rec.name_rus,
               t.name_en = Rec.name_eng,
               t.ref_currency_rate = Rec.gd_currency_rate,                          
               t.begin_date = Rec.Begin_Date,
--               t.delfl = Rec.Delfl,
               t.id_usr = id_usr_,
               t.user_location = user_location_,
               t.sent_knd = 0
         where t.id = id_;
      end if;
    end loop;
    
    for Rec in (select rc.id
                  from ref_currency rc
                 where rc.delfl = 0
                minus
                select gc.gs_currency
                  from MAIN.gs_currency@nsi gc,
                       MAIN.gs_currency_state@nsi gcs
                 where gc.gs_currency = gcs.gs_currency
                   and gcs.is_last_state = 1
                   and gcs.confirm_step = 231
                   and gc.is_active = 1
                   )
    loop
      update ref_currency   
         set delfl = 1
       where id = Rec.id; 
    end loop;
    
    if Do_Commit_ = 1 then
      Commit;
    end if;
    
  exception
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ������ ���������� ����������� "������"!';
  end;
  
  
  procedure ref_currency_rate_load(
    id_usr_        in  ref_currency_rate.id_usr        % type,
    user_location_ in  ref_currency_rate.user_location % type,
    Do_Commit_     in  integer default 1, 
    Err_Code       out number,
    Err_Msg        out varchar2
  ) 
  is
    ProcName           constant Varchar2(50) := 'PKG_FRSI_REF.REF_CURRENCY_RATE_LOAD';
    id_                ref_currency_rate.id % type;
  begin
    Err_Code := 0;
    Err_Msg  := '���������� �������� �������!';
    
    for Rec in (select gr.gd_currency_rate,
                       gr.rec_id,
                       gr.code,
                       convert_win1251_to_utf8_kz(gr.name_kaz) as name_kaz,
                       gr.name as name_rus,                     
                       gr.gd_rate_agency,
                       gr.begin_date/*,
                       decode(gr.gl_oper_type, 3, 1, 0) delfl*/
                  from MAIN.gd_currency_rate@nsi gr,
                       MAIN.gd_currency_rate_state@nsi grs                     
                 where gr.gd_currency_rate = grs.gd_currency_rate
                   and grs.is_last_state = 1
                   and grs.confirm_step = 231
                   and gr.is_active = 1
                minus
                select cr.id,
                       cr.rec_id,
                       cr.code,
                       cr.name_kz,
                       cr.name_ru,
                       cr.ref_rate_agency,
                       cr.begin_date/*,
                       cr.delfl*/
                  from ref_currency_rate cr)
    loop
               
      begin
        select id
          into id_
          from ref_currency_rate 
         where id = Rec.gd_currency_rate;
      exception
        when no_data_found then
          id_ := null;
      end;
      
      if id_ is null then
        insert into ref_currency_rate
          (id,
           rec_id,
           code,                  
           name_kz,
           name_ru,
           ref_rate_agency,
           begin_date,
--           delfl,
           id_usr,
           user_location)
         values(
           Rec.gd_currency_rate,
           Rec.Rec_Id,
           Rec.Code,
           Rec.name_kaz,
           Rec.name_rus,
           Rec.gd_rate_agency,
           Rec.Begin_Date,
--           Rec.Delfl,
           id_usr_,
           user_location_);
      else
        update ref_currency_rate t
           set t.code = Rec.Code,
               t.name_kz = Rec.name_kaz,
               t.name_ru = Rec.name_rus,
               t.ref_rate_agency = Rec.gd_rate_agency,
               t.begin_date = Rec.Begin_Date,
--               t.delfl = Rec.Delfl,
               t.id_usr = id_usr_,
               t.user_location = user_location_,
               t.sent_knd = 0
         where t.id = id_;
      end if;
    end loop;
    
    for Rec in (select rc.id
                  from ref_currency_rate rc
                 where rc.delfl = 0
                minus
                select gr.gd_currency_rate
                  from MAIN.gd_currency_rate@nsi gr,
                       MAIN.gd_currency_rate_state@nsi grs
                 where gr.gd_currency_rate = grs.gd_currency_rate
                   and grs.is_last_state = 1
                   and grs.confirm_step = 231
                   and gr.is_active = 1)
    loop
      update ref_currency_rate   
         set delfl = 1
       where id = Rec.id; 
    end loop;
    
    if Do_Commit_ = 1 then
      Commit;
    end if;
    
  exception
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ������ ���������� ����������� "������� �����"!';
  end;
  
  
  procedure ref_region_load(
    id_usr_        in  ref_region.id_usr        % type,
    user_location_ in  ref_region.user_location % type,
    Do_Commit_     in  integer default 1, 
    Err_Code       out number,
    Err_Msg        out varchar2
  ) 
  is
    ProcName    constant Varchar2(50) := 'PKG_FRSI_REF.REF_REGION_LOAD';
    id_         ref_region.id % type;
  begin
    Err_Code := 0;
    Err_Msg  := '���������� �������� �������!';
    
    for Rec in (select gr.gs_region,
                       gr.rec_id,
                       gr.code,
                       convert_win1251_to_utf8_kz(gr.name_kaz) as name_kaz,
                       gr.name as name_rus,
                       go.name as oblast_name,
                       gr.begin_date/*,
                       decode(gr.gl_oper_type, 3, 1, 0) delfl*/
                  from MAIN.gs_region@nsi gr,
                       MAIN.gs_region_state@nsi grs,
                       MAIN.gs_oblast@nsi go
                 where gr.gs_region = grs.gs_region
                   and gr.gs_oblast = go.gs_oblast
                   and grs.is_last_state = 1
                   and grs.confirm_step = 231
                   and gr.is_active = 1
                minus
                select r.id,
                       r.rec_id,
                       r.code,
                       r.name_kz,
                       r.name_ru,
                       r.oblast_name,
                       r.begin_date/*,
                       r.delfl*/
                  from ref_region r)
    loop
               
      begin
        select id
          into id_
          from ref_region 
         where id = Rec.gs_region;
      exception
        when no_data_found then
          id_ := null;
      end;
      
      if id_ is null then
        insert into ref_region
          (id,
           rec_id,
           code,
           name_kz,
           name_ru,
           oblast_name,
           begin_date,
--           delfl,
           id_usr,
           user_location)
         values(
           Rec.gs_region,
           Rec.Rec_Id,
           Rec.Code,
           Rec.name_kaz,
           Rec.name_rus,
           Rec.oblast_name,
           Rec.Begin_Date,
--           Rec.Delfl,
           id_usr_,
           user_location_);
      else
        update ref_region t
           set t.code = Rec.Code,
               t.name_kz = Rec.name_kaz,
               t.name_ru = Rec.name_rus,
               t.oblast_name = Rec.oblast_name,
               t.begin_date = Rec.Begin_Date,
--               t.delfl = Rec.Delfl,
               t.id_usr = id_usr_,
               t.user_location = user_location_,
               t.sent_knd = 0
         where t.id = id_;
      end if;
    end loop;
    
    for Rec in (select rc.id
                  from ref_region rc
                 where rc.delfl = 0
                minus
                select gr.gs_region
                  from MAIN.gs_region@nsi gr,
                       MAIN.gs_region_state@nsi grs
                 where gr.gs_region = grs.gs_region
                   and grs.is_last_state = 1
                   and grs.confirm_step = 231
                   and gr.is_active = 1)
    loop
      update ref_region   
         set delfl = 1
       where id = Rec.id; 
    end loop;
    
    if Do_Commit_ = 1 then
      Commit;
    end if;
    
  exception
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ������ ���������� ����������� "������/�������"!';
  end;
  
  procedure ref_country_load(
    id_usr_        in  ref_country.id_usr        % type,
    user_location_ in  ref_country.user_location % type,
    Do_Commit_     in  integer default 1, 
    Err_Code       out number,
    Err_Msg        out varchar2
  ) 
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_COUNTRY_LOAD';
    id_      ref_country.id % type;
  begin
    Err_Code := 0;
    Err_Msg  := '���������� �������� �������!';
    
    for Rec in (select gc.gs_country,
                       gc.rec_id,
                       gc.code_2a as code,
                       convert_win1251_to_utf8_kz(gc.name_kaz) as name_kaz,
                       gc.name_rus,
                       gc.name_eng,
                       gc.begin_date/*,
                       decode(gc.gl_oper_type, 3, 1, 0) delfl*/
                  from MAIN.gs_country@nsi gc,
                       MAIN.gs_country_state@nsi gcs
                 where gc.gs_country = gcs.gs_country
                   and gcs.is_last_state = 1
                   and gcs.confirm_step = 231
                   and gc.is_active = 1
                minus
                select c.id,
                       c.rec_id,
                       c.code,
                       c.name_kz,
                       c.name_ru,
                       c.name_en,
                       c.begin_date/*,
                       c.delfl*/
                  from ref_country c)
    loop
               
      begin
        select id
          into id_
          from ref_country 
         where id = Rec.gs_country;
      exception
        when no_data_found then
          id_ := null;
      end;
      
      if id_ is null then
        insert into ref_country
          (id,
           rec_id,
           code,
           name_kz,
           name_ru,
           name_en,
           begin_date,
--           delfl,
           id_usr,
           user_location)
         values(
           Rec.gs_country,
           Rec.Rec_Id,
           Rec.Code,
           Rec.name_kaz,
           Rec.name_rus,
           Rec.name_eng,
           Rec.Begin_Date,
--           Rec.Delfl,
           id_usr_,
           user_location_);
      else
        update ref_country t
           set t.code = Rec.Code,
               t.name_kz = Rec.name_kaz,
               t.name_ru = Rec.name_rus,
               t.name_en = Rec.name_eng,
               t.begin_date = Rec.Begin_Date,
--               t.delfl = Rec.Delfl,
               t.id_usr = id_usr_,
               t.user_location = user_location_,
               t.sent_knd = 0
         where t.id = id_;
      end if;
    end loop;
    
    for Rec in (select rc.id
                  from ref_country rc
                 where rc.delfl = 0
                minus
                select gc.gs_country
                  from MAIN.gs_country@nsi gc,
                       MAIN.gs_country_state@nsi gcs
                 where gc.gs_country = gcs.gs_country
                   and gcs.is_last_state = 1
                   and gcs.confirm_step = 231
                   and gc.is_active = 1)
    loop
      update ref_country   
         set delfl = 1
       where id = Rec.id; 
    end loop;
    
    if Do_Commit_ = 1 then
      Commit;
    end if;
    
  exception
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ������ ���������� ����������� "������"!';
  end;
  
  procedure ref_department_load(
    id_usr_        in  ref_department.id_usr        % type,
    user_location_ in  ref_department.user_location % type,
    Do_Commit_     in  integer default 1, 
    Err_Code       out number,
    Err_Msg        out varchar2
  ) 
  is
    ProcName        constant Varchar2(50) := 'PKG_FRSI_REF.REF_DEPARTMENT_LOAD';
    id_             ref_department.id % type;
  begin
    Err_Code := 0;
    Err_Msg  := '���������� �������� �������!';
    
    for Rec in (select gd.gs_dept,
                       gd.rec_id,
                       to_char(gd.rec_id) as code,
                       convert_win1251_to_utf8_kz(gd.name_kaz) as name_kaz,
                       gd.name as name_rus,
                       gd.begin_date/*,
                       decode(gd.gl_oper_type, 3, 1, 0) delfl*/
                  from MAIN.gs_dept@nsi gd,
                       MAIN.gs_dept_state@nsi gds
                 where gd.gs_dept = gds.gs_dept
                   and gds.is_last_state = 1
                   and gds.confirm_step = 231
                   and gd.is_active = 1
                minus
                select d.id,
                       d.rec_id,
                       d.code,
                       d.name_kz,
                       d.name_ru,
                       d.begin_date/*,
                       d.delfl*/
                  from ref_department d)
    loop
               
      begin
        select id
          into id_
          from ref_department 
         where id = Rec.gs_dept;
      exception
        when no_data_found then
          id_ := null;
      end;
      
      if id_ is null then
        insert into ref_department
          (id,
           rec_id,
           code,
           name_kz,
           name_ru,         
           begin_date,
--           delfl,
           id_usr,
           user_location)
         values(
           Rec.gs_dept,
           Rec.Rec_Id,
           Rec.Code,
           Rec.name_kaz,
           Rec.name_rus,
           Rec.Begin_Date,
--           Rec.Delfl,
           id_usr_,
           user_location_);
      else
        update ref_department t
           set t.code = Rec.Code,
               t.name_kz = Rec.name_kaz,
               t.name_ru = Rec.name_rus,             
               t.begin_date = Rec.Begin_Date,
--               t.delfl = Rec.Delfl,
               t.id_usr = id_usr_,
               t.user_location = user_location_,
               t.sent_knd = 0
         where t.id = id_;
      end if;
    end loop;
    
    for Rec in (select rd.id
                  from ref_department rd
                 where rd.delfl = 0
                minus
                select gd.gs_dept
                  from MAIN.gs_dept@nsi gd,
                       MAIN.gs_dept_state@nsi gds
                 where gd.gs_dept = gds.gs_dept
                   and gds.is_last_state = 1
                   and gds.confirm_step = 231
                   and gd.is_active = 1)
    loop
      update ref_department   
         set delfl = 1
       where id = Rec.id; 
    end loop;
    
    if Do_Commit_ = 1 then
      Commit;
    end if;
    
  exception
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ������ ���������� ����������� "������������� �� ��"!';
  end;
  
  
  procedure ref_securities_load(
    id_usr_        in  ref_securities.id_usr        % type,
    user_location_ in  ref_securities.user_location % type,
    Do_Commit_     in  integer default 1, 
    Err_Code       out number,
    Err_Msg        out varchar2
  ) 
  is
    ProcName        constant Varchar2(50) := 'PKG_FRSI_REF.REF_SECURITIES_LOAD';
    id_             ref_securities.id % type;
  begin
    Err_Code := 0;
    Err_Msg  := '���������� �������� �������!';
    
    for Rec in (select ss.rec_id as id,
                       ss.id as rec_id,                     
                       ss.s_issuer,
                       si.name as issuer_name,
                       si.s_g_issuer_sign,
                       sis.name as sign_name,
                       sis.code as sign_code,
                       nvl(trim(si.is_resident),0) as is_resident,
                       nvl(trim(si.is_state),0) as is_state,
                       ss.nominal_value,
                       ss.nin,
                       ss.circul_date,
                       ss.maturity_date,
                       ss.security_cnt,       
                       ss.s_g_security_variety,
                       sv.code as variety_code,	
                       sv.name as variety_name,       
                       ss.s_g_security_type,
                       st.code as type_code,
                       st.name as type_name,
                       ss.nominal_currency,
                       kc.short_name as currency_code,
                       kc.name as currency_name,
                       ss.code,
                       ss.nin as name_ru,
                       trunc(nvl(ss.begindate,old_date)) as begindate
                  from bv.s_security@pfcbnv           ss,
                       bv.s_issuer@pfcbnv             si,
                       bv.s_g_issuer_sign@pfcbnv      sis,
                       bv.s_g_security_variety@pfcbnv sv,
                       bv.s_g_security_type@pfcbnv    st,
                       bv.k_g_currency@pfcbnv         kc
                 where ss.s_issuer = si.id
                   and si.s_g_issuer_sign = sis.id(+)
                   and ss.s_g_security_variety = sv.id(+)
                   and ss.s_g_security_type = st.id(+)
                   and ss.nominal_currency = kc.id(+)
                   and ss.status = 1
                   and si.status = 1
                   and sv.status = 1
                   and st.status = 1
                   and ss.rec_id not in (12695,13862) -- ����� ��� ������ � ��� ���������� ������.
                minus
                select s.id,
                       s.rec_id,
                       s.s_issuer,
                       s.issuer_name,
                       s.s_g_issuer_sign,
                       s.sign_name,
                       s.sign_code,
                       nvl(trim(s.is_resident),0) as is_resident,
                       nvl(trim(s.is_state),0) as is_state,
                       s.nominal_value,
                       s.nin,
                       s.circul_date,
                       s.maturity_date,
                       s.security_cnt,       
                       s.s_g_security_variety,
                       s.variety_code,
                       s.variety_name,      
                       s.s_g_security_type,
                       s.type_code,
                       s.type_name,
                       s.nominal_currency,
                       s.currency_code,
                       s.currency_name,
                       s.code,                     
                       s.name_ru,
                       s.begin_date                       
                  from ref_securities s
                 where s.delfl = 0)
    loop
               
      begin
        select id
          into id_
          from ref_securities 
         where id = Rec.id;
      exception
        when no_data_found then
          id_ := null;
      end;
      
      if id_ is null then
        insert into ref_securities
          (id,
           rec_id,
           s_issuer,
           issuer_name,
           s_g_issuer_sign,
           sign_name,
           sign_code,
           is_resident,
           is_state,
           nominal_value,
           nin,
           circul_date,
           maturity_date,
           security_cnt,       
           s_g_security_variety,
           variety_code,  
           variety_name,       
           s_g_security_type,
           type_code,
           type_name,
           nominal_currency,
           currency_code,
           currency_name,
           code,
           name_ru,
           begin_date,
           id_usr,
           user_location,
           sec_var_rec_id,
           currency_rec_id)
         values(
           Rec.id,
           Rec.Rec_Id,
           Rec.s_issuer,
           Rec.issuer_name,
           Rec.s_g_issuer_sign,
           Rec.sign_name,
           Rec.sign_code,
           Rec.is_resident,
           Rec.is_state,
           Rec.nominal_value,
           Rec.nin,
           Rec.circul_date,
           Rec.maturity_date,
           Rec.security_cnt,       
           Rec.s_g_security_variety,
           Rec.variety_code,  
           Rec.variety_name,       
           Rec.s_g_security_type,
           Rec.type_code,
           Rec.type_name,
           Rec.nominal_currency,
           Rec.currency_code,
           Rec.currency_name,         
           Rec.Code,         
           Rec.name_ru,
           Rec.BeginDate,         
           id_usr_,
           user_location_,
           ref_get_rec_id('ref_currency',Rec.Currency_Code)
           );
      else
        update ref_securities t
           set t.code = Rec.Code,             
               t.name_ru = Rec.name_ru,             
               t.begin_date = Rec.BeginDate,
               t.s_issuer = Rec.s_Issuer,
               t.issuer_name = Rec.issuer_name,
               t.s_g_issuer_sign = Rec.s_g_issuer_sign,
               t.sign_name = Rec.sign_name,
               t.sign_code = Rec.sign_code,
               t.is_resident = Rec.is_resident,
               t.is_state = Rec.is_state,
               t.nominal_value = Rec.nominal_value,
               t.nin = Rec.nin,
               t.circul_date = Rec.circul_date,
               t.maturity_date = Rec.maturity_date,
               t.security_cnt = Rec.security_cnt,
               t.s_g_security_variety = Rec.s_g_security_variety,
               t.variety_code = Rec.variety_code,
               t.variety_name = Rec.variety_name,
               t.s_g_security_type = Rec.s_g_security_type,
               t.type_code = Rec.type_code,
               t.type_name = Rec.type_name,
               t.nominal_currency = Rec.nominal_currency,
               t.currency_code = Rec.currency_code,
               t.currency_name = Rec.currency_name,
               t.id_usr = id_usr_,
               t.user_location = user_location_,
               t.sent_knd = 0,
               t.currency_rec_id = ref_get_rec_id('ref_currency',Rec.Currency_Code)
         where t.id = id_;
      end if;
    end loop;
    
    for Rec in (select rs.id
                  from ref_securities rs
                 where rs.delfl = 0
                minus
                select s.rec_id as id
                  from bv.s_security@pfcbnv s
                 where s.status = 1)
    loop
      update ref_securities   
         set delfl = 1
       where id = Rec.id; 
    end loop;
    
    if Do_Commit_ = 1 then
      Commit;
    end if;
    
  exception
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ������ ���������� ����������� "������ ������"!';
  end;
  
  procedure ref_issuers_load(
    id_usr_        in  ref_issuers.id_usr        % type,
    user_location_ in  ref_issuers.user_location % type,
    Do_Commit_     in  integer default 1, 
    Err_Code       out number,
    Err_Msg        out varchar2
  ) 
  is
    ProcName        constant Varchar2(50) := 'PKG_FRSI_REF.REF_ISSUERS_LOAD';
    id_             ref_issuers.id % type;
  begin
    Err_Code := 0;
    Err_Msg  := '���������� �������� �������!';
    
    for Rec in (select si.rec_id as id,
                       si.id as rec_id,                     
                       si.code,
                       si.name as name_ru,
                       convert_win1251_to_utf8_kz(si.name_kaz) as name_kaz,                       
                       trunc(si.begindate) as begindate
                  from bv.s_issuer@pfcbnv si                            
                 where si.status = 1  
                   and si.begindate is not null
                minus
                select i.id,
                       i.rec_id,
                       i.code,
                       i.name_ru,
                       i.name_kz,
                       i.begin_date
                  from ref_issuers i
                 where i.delfl = 0)
    loop
               
      begin
        select id
          into id_
          from ref_issuers 
         where id = Rec.id;
      exception
        when no_data_found then
          id_ := null;
      end;
      
      if id_ is null then
        insert into ref_issuers
          (id,
           rec_id,         
           code,
           name_ru,
           name_kz,
           begin_date,
           id_usr,
           user_location)
         values(
           Rec.id,
           Rec.Rec_Id,                  
           Rec.Code,
           Rec.name_ru,
           Rec.name_kaz,
           Rec.BeginDate,
           id_usr_,
           user_location_);
      else
        update ref_issuers t
           set t.code = Rec.Code,             
               t.name_ru = Rec.name_ru,
               t.name_kz = Rec.name_kaz,
               t.begin_date = Rec.BeginDate,
               t.id_usr = id_usr_,
               t.user_location = user_location_,
               t.sent_knd = 0
         where t.id = id_;
      end if;
    end loop;
    
    for Rec in (select ri.id
                  from ref_issuers ri
                 where ri.delfl = 0
                minus
                select i.rec_id as id
                  from bv.s_issuer@pfcbnv i
                 where i.status = 1)
    loop
      update ref_issuers   
         set delfl = 1
       where id = Rec.id; 
    end loop;
    
    if Do_Commit_ = 1 then
      Commit;
    end if;
    
  exception
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ������ ���������� ����������� "������ ������"!';
  end;  
  


end pkg_frsi_ref_load;
/
