create or replace package PKG_FRSI_REF is

  -- Author  : AYUPOV.BAKHTIYAR
  -- Created : 15.04.2015 18:27:04
  -- Purpose : 
  
  E_Force_Exit exception;

  /* Проверка на наличие прав по коду справочника и id пользователя */
  function ref_have_right(
    ref_code_ in ref_main.code   % type,
    user_id_  in f_users.user_id % type,
    type_     in varchar2
  )return number;   
  
  /* Проверка на код по всем записям */
  function ref_check_code(
    ref_code_ in ref_main.code % type,
    rec_id_   in ref_main.id % type,
    code_     in ref_main.code % type
  )return number;
  
  
  /* Проверка на уникальность наименования по всем записям */
  function ref_check_name(
    ref_code_ in ref_main.code % type,
    rec_id_   in ref_main.id % type,
    name_     in ref_main.name % type
  )return number;
 
  
  /* Проверка на уникальность справочника правил выходных форм по всем записям по наим формы и поля */
  function ref_check_rr_param(
    rec_id_    in ref_reports_rules.rec_id % type,
    formname_  in ref_reports_rules.formname % type,
    fieldname_ in ref_reports_rules.fieldname % type
  )return number;  
  
  /* Проверка на дату в пределах одной сущности */
  function ref_check_date(
    ref_code_   in ref_main.code % type,
    id_         in ref_main.id % type,
    rec_id_     in ref_main.id % type,    
    begin_date_ in Date default null
  )return number;
  
  /* Проверка на пустую дату окончания в пределах одной сущности */
  function ref_check_end_date(
    ref_code_   in ref_main.code % type,
    id_         in ref_main.id   % type,
    rec_id_     in ref_main.id   % type,
    b_date_     in date,
    e_date_     in date
  )return number;
  
  /* Проверка на имеющийся период в пределах сущости */
  function ref_check_date_period(
    ref_code_   in ref_main.code % type,
    id_         in ref_main.id   % type,
    rec_id_     in ref_main.id   % type,
    begin_date_ in Date default null,
    end_date_   in Date default null
  )return number;
    
  /* Статус отправки в ЕССП */
  function ref_get_sent_sts(
    ref_code_ in ref_main.code % type,
    sent_knd_ in number
  )return number;
  
  /* Проверка на ИН*/
  function ref_check_idn(
    ref_code_ in ref_main.code % type,
    idn_      in varchar2,
    rec_id_   in number
  )return number;
  
  /* Проверка на уникальность респондента*/
  function ref_check_respondent(
    ref_legal_person_ in varchar2,
    rec_id_           in number
  )return number;
  
  /* Проверка БИН у Юр.лица подвязанного к респонденту */
  function ref_check_respondent_idn(
    ref_legal_person_ in varchar2
  )return number;
  
  /* Проверка связей между таблицами */
  function ref_check_record(
    table_name_  in varchar2,
    column_name_ in varchar2,
    id_          in varchar2
  )return number;    
  
  /* Проверка уникальности записи за указанный год */
  function ref_check_year(
    table_name_ in varchar2,
    begin_date_ in date,
    rec_id_     in number
  ) return number;
  
  /* Получить Entity_id по наименованию справочника и id из ЕССП */
  function ref_get_entity_id(    
    ref_code_   in varchar2,
    id_         in number
  )return number;
  
  function ref_update_end_date(
    ref_code_   in ref_main.code % type,    
    rec_id_     in ref_main.id   % type,     
    b_date_     in date
  )return number;
  
  procedure report_ref_link_check(
    rec_id_     in ref_main.id % type,
    id_         in ref_main.id % type,    
    ref_code_   in ref_main.code % type,
    /*begin_date_ in Date,
    end_date_   in Date,
    kind_event_ in varchar2,*/
    err_code    out number,
    err_msg     out varchar2
  );
  
  function ref_get_rec_id(
    ref_code_ in ref_main.code % type,
    id_        in ref_main.id % type
  ) return number;
  
  function get_condition (
    cond_symbol_ ref_crosscheck.formula_symbol % type
  ) return varchar2;
  
  ------------------------------------------------------------------------------------------------------------------------
  /* Список справочников */  
  procedure read_ref_list(
    user_id_ in  f_users.user_id  % type,
    ref_knd_ in  ref_main.ref_knd % type,
    name_    in  ref_main.name    % type,
    code_    in  ref_main.code    % type,
    cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2);
    
  /* Список для простых справочников */ 
  procedure ref_read_simple_list_by_params(
    ref_code_       in  ref_main.code % type,
    date_           in  date,
    id_             in  number,
    rec_id_         in  number,    
    name_ru_        in  varchar2,
    code_           in  varchar2,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    Err_Code        out number,
    Err_Msg         out varchar2
  );
  
  procedure ref_read_simple_hst_list(
    ref_code_    in ref_main.code % type,    
    id_          in number,    
    Cur          out sys_refcursor,
    Err_Code     out number,
    Err_Msg      out varchar2
  );
  
  procedure ref_insert_simple(
    ref_code_      in  varchar2,
    rec_id_        in  number,
    code_          in  varchar2,
    name_kz_       in  varchar2,
    name_ru_       in  varchar2,
    name_en_       in  varchar2,
    begin_date_    in  date,
    end_date_      in  date,
    id_usr_        in  number,
    user_location_ in  varchar2,
    datlast_       in  date,
    do_commit_     in  Integer default 1,
    id_            out number,
    Err_Code       out number,
    Err_Msg        out varchar2
  );
  
  procedure ref_update_simple(    
    ref_code_      in  varchar2,
    id_            in  number,
    rec_id_        in  number,
    code_          in  varchar2,
    name_kz_       in  varchar2,
    name_ru_       in  varchar2,
    name_en_       in  varchar2,
    begin_date_    in  date,
    end_date_      in  date,
    id_usr_        in  number,
    user_location_ in  varchar2,
    datlast_       in  date,
    do_commit_     in  Integer default 1,    
    Err_Code       out number,
    Err_Msg        out varchar2
  );
  
  procedure ref_delete_simple(
    ref_code_	 in  ref_main.code % type,
    id_        in  number,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );
    
  /* Справочник должностей */
  procedure ref_read_post_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2 
  );
  
  procedure ref_read_post_list_by_params(
    id_             in  ref_post.id % type,
    date_           in  Date,
    name_ru_        in  ref_post.name_ru % type,
    rec_id_         in  ref_post.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2 
  );
  
  procedure ref_read_post_hst_list( 
    id_       in  ref_post.id % type,   
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2 
  );
  
  procedure ref_insert_post(
    rec_id_        in  ref_post.rec_id        % type,
    code_          in  ref_post.code          % type,
    name_kz_       in  ref_post.name_kz       % type,
    name_ru_       in  ref_post.name_ru       % type,
    name_en_       in  ref_post.name_en       % type,
    type_post_id_  in  ref_post.type_post     % type,
    is_activity_   in  ref_post.is_activity   % type,
    is_main_ruk_   in  ref_post.is_main_ruk   % type,
    begin_date_    in  ref_post.begin_date    % type,
    end_date_      in  ref_post.end_date      % type,
    id_usr_        in  ref_post.id_usr        % type,
    user_location_ in  ref_post.user_location % type,
    datlast_       in  ref_post.datlast       % type,
    do_commit_     in  Integer default 1,
    id_            out ref_post.id            % type,
    Err_Code       out number,
    Err_Msg        out varchar2
  );
  
  procedure ref_update_post(
    id_            in  ref_post.id            % type,
    rec_id_        in  ref_post.rec_id        % type,
    code_          in  ref_post.code          % type,
    name_kz_       in  ref_post.name_kz       % type,
    name_ru_       in  ref_post.name_ru       % type,
    name_en_       in  ref_post.name_en       % type,
    type_post_id_  in  ref_post.type_post     % type,
    is_activity_   in  ref_post.is_activity   % type,
    is_main_ruk_   in  ref_post.is_main_ruk   % type,    
    begin_date_    in  ref_post.begin_date    % type,
    end_date_      in  ref_post.end_date      % type,
    id_usr_        in  ref_post.id_usr        % type,
    user_location_ in  ref_post.user_location % type,
    datlast_       in  ref_post.datlast       % type,
    do_commit_     in  integer default 1,     
    err_code       out number,
    err_msg        out varchar2
  );
    
  procedure ref_delete_post(
    id_        in  ref_post.id % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );
  
  procedure ref_read_simple_post_lis(
    type_post_ in ref_post.type_post % type,
    date_      in ref_post.begin_date % type,
    Cur        out sys_refcursor,
    err_code   out number,
    err_msg    out varchar2 
  );
  
  
  /* Справочник физических лиц*/  
  procedure ref_read_person_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2 
  );
  
  procedure ref_read_person_list_by_params(
    id_             in  ref_person.id % type,
    date_           in  Date,
    idn_            in  ref_person.idn % type,
    fm_             in  ref_person.fm % type,
    nm_             in  ref_person.nm % type,
    ft_             in  ref_person.ft % type,
    rec_id_         in  ref_person.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2 
  );
  
  procedure ref_read_person_hst_list(
    id_         in  ref_person.id % type,
    Cur         out sys_refcursor,
    err_code    out number,
    err_msg     out varchar2 
  );
  
  procedure ref_insert_person(
    rec_id_        in  ref_person.rec_id        % type,
    code_          in  ref_person.code          % type,
    idn_           in  ref_person.idn           % type,
    fm_            in  ref_person.fm            % type,
    nm_            in  ref_person.nm            % type,
    ft_            in  ref_person.ft            % type,
    fio_kz_        in  ref_person.fio_kz        % type,
    fio_en_        in  ref_person.fio_en        % type,
    ref_country_   in  ref_person.ref_country   % type,
    phone_work_    in  ref_person.phone_work    % type,
    fax_           in  ref_person.fax           % type,
    address_work_  in  ref_person.address_work  % type,
    note_          in  ref_person.note          % type,    
    begin_date_    in  ref_person.begin_date    % type,
    end_date_      in  ref_person.end_date      % type,
    id_usr_        in  ref_person.id_usr        % type,
    user_location_ in  ref_person.user_location % type,
    datlast_       in  ref_person.datlast       % type,
    do_commit_     in  integer default 1,
    id_            out ref_person.id            % type,
    err_code       out number,
    err_msg        out varchar2
  );
  
  procedure ref_update_person(
    id_            in  ref_person.id            % type,
    rec_id_        in  ref_person.rec_id        % type,
    code_          in  ref_person.code          % type,
    idn_           in  ref_person.idn           % type,
    fm_            in  ref_person.fm            % type,
    nm_            in  ref_person.nm            % type,
    ft_            in  ref_person.ft            % type,
    fio_kz_        in  ref_person.fio_kz        % type,
    fio_en_        in  ref_person.fio_en        % type,
    ref_country_   in  ref_person.ref_country   % type,
    phone_work_    in  ref_person.phone_work    % type,
    fax_           in  ref_person.fax           % type,
    address_work_  in  ref_person.address_work  % type,
    note_          in  ref_person.note          % type,    
    begin_date_    in  ref_person.begin_date    % type,
    end_date_      in  ref_person.end_date      % type,
    id_usr_        in  ref_person.id_usr        % type,    
    user_location_ in  ref_person.user_location % type,
    datlast_       in  ref_person.datlast       % type,
    do_commit_     in  integer default 1,     
    err_code       out number,
    err_msg        out varchar2
  );
  
  procedure ref_delete_person(
    id_         in  ref_person.id % type,
    do_commit_  in  integer default 1,     
    err_code    out number,
    err_msg     out varchar2 
  );

  
  /* Справочник юридических лиц  */  
  procedure ref_read_legal_person_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2 
  );
  
  procedure ref_read_lp_list_by_params(
    id_             in  ref_legal_person.id % type,
    date_           in  Date,
    name_           in  ref_legal_person.name_ru % type,
    idn_            in  ref_legal_person.idn % type,
    rec_id_         in  ref_legal_person.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2 
  );
  
  procedure ref_read_legal_person_hst_list(
    id_      in  ref_legal_person.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  );
  
  /*procedure ref_get_legal_person(
    id_                in  ref_legal_person.id                % type,
    code_              out ref_legal_person.code              % type,
    name_kz_           out ref_legal_person.name_kz           % type,
    name_ru_           out ref_legal_person.name_ru           % type,
    name_en_           out ref_legal_person.name_en           % type,
    short_name_kz_     out ref_legal_person.short_name_kz     % type,
    short_name_ru_     out ref_legal_person.short_name_ru     % type,
    short_name_en_     out ref_legal_person.short_name_en     % type,
    is_non_rezident_   out ref_legal_person.is_non_rezident   % type,    
    idn_               out ref_legal_person.idn               % type,
    org_type_name_     out ref_org_type.name_ru               % type,
    type_be_name_      out ref_type_bus_entity.name_ru        % type,
    country_name_      out ref_country.name_ru                % type,
    region_name_       out ref_region.name_ru                 % type,
    postal_index_      out ref_legal_person.postal_index      % type,
    address_street_    out ref_legal_person.address_street    % type,
    address_num_house_ out ref_legal_person.address_num_house % type,
    manager_           out ref_legal_person.manager           % type,
    legal_address_     out ref_legal_person.legal_address     % type,
    fact_address_      out ref_legal_person.fact_address      % type,
    note_              out ref_legal_person.note              % type,
    begin_date_        out ref_legal_person.begin_date        % type,
    end_date_          out ref_legal_person.end_date          % type,
    is_inv_fund_       out ref_legal_person.is_inv_fund       % type,
    inv_idn_           out ref_legal_person.inv_idn           % type,
    user_name_         out f_users.first_name                 % type,    
    user_location_     out ref_legal_person.user_location     % type,
    err_code           out number,
    err_msg            out varchar2
  );*/
  
  procedure ref_insert_legal_person(
    rec_id_              in  ref_legal_person.rec_id              % type,
    code_                in  ref_legal_person.code                % type,    
    name_kz_             in  ref_legal_person.name_kz             % type,
    name_ru_             in  ref_legal_person.name_ru             % type,
    name_en_             in  ref_legal_person.name_en             % type,
    short_name_kz_       in  ref_legal_person.short_name_kz       % type,
    short_name_ru_       in  ref_legal_person.short_name_ru       % type,
    short_name_en_       in  ref_legal_person.short_name_en       % type,
    is_non_rezident_     in  ref_legal_person.is_non_rezident     % type,
    idn_                 in  ref_legal_person.idn                 % type,
    ref_org_type_        in  ref_legal_person.ref_org_type        % type,
    ref_type_bus_entity_ in  ref_legal_person.ref_type_bus_entity % type,
    ref_country_         in  ref_legal_person.ref_country         % type,
    ref_region_          in  ref_legal_person.ref_region          % type,
    postal_index_        in  ref_legal_person.postal_index        % type,
    address_street_      in  ref_legal_person.address_street      % type,
    address_num_house_   in  ref_legal_person.address_num_house   % type,
    manager_             in  ref_legal_person.manager             % type,
    legal_address_       in  ref_legal_person.legal_address       % type,
    fact_address_        in  ref_legal_person.fact_address        % type,
    note_                in  ref_legal_person.note                % type,    
    begin_date_          in  ref_legal_person.begin_date          % type,
    end_date_            in  ref_legal_person.end_date            % type,
    is_inv_fund_         in  ref_legal_person.is_inv_fund         % type,
    inv_idn_             in  ref_legal_person.inv_idn             % type,
    is_akimat_           in  ref_legal_person.is_akimat           % type,
    id_usr_              in  ref_legal_person.id_usr              % type,    
    user_location_       in  ref_legal_person.user_location       % type,
    datlast_             in  ref_legal_person.datlast             % type,
    do_commit_           in  integer default 1,
    id_                  out ref_legal_person.id % type,
    err_code             out number,
    err_msg              out varchar2
  );
  
  procedure ref_update_legal_person(
    id_                  in  ref_legal_person.id                  % type,
    rec_id_              in  ref_legal_person.rec_id              % type,
    code_                in  ref_legal_person.code                % type,
    name_kz_             in  ref_legal_person.name_kz             % type,
    name_ru_             in  ref_legal_person.name_ru             % type,
    name_en_             in  ref_legal_person.name_en             % type,
    short_name_kz_       in  ref_legal_person.short_name_kz       % type,
    short_name_ru_       in  ref_legal_person.short_name_ru       % type,
    short_name_en_       in  ref_legal_person.short_name_en       % type,
    is_non_rezident_     in  ref_legal_person.is_non_rezident     % type,
    idn_                 in  ref_legal_person.idn                 % type,
    ref_org_type_        in  ref_legal_person.ref_org_type        % type,
    ref_type_bus_entity_ in  ref_legal_person.ref_type_bus_entity % type,
    ref_country_         in  ref_legal_person.ref_country         % type,
    ref_region_          in  ref_legal_person.ref_region          % type,
    postal_index_        in  ref_legal_person.postal_index        % type,
    address_street_      in  ref_legal_person.address_street      % type,
    address_num_house_   in  ref_legal_person.address_num_house   % type,
    manager_             in  ref_legal_person.manager        	    % type,
    legal_address_       in  ref_legal_person.legal_address       % type,
    fact_address_        in  ref_legal_person.fact_address        % type,
    note_                in  ref_legal_person.note                % type,    
    begin_date_          in  ref_legal_person.begin_date          % type,
    end_date_            in  ref_legal_person.end_date            % type,
    is_inv_fund_         in  ref_legal_person.is_inv_fund         % type,
    inv_idn_             in  ref_legal_person.inv_idn             % type,
    is_akimat_           in  ref_legal_person.is_akimat           % type,
    id_usr_              in  ref_legal_person.id_usr              % type,    
    user_location_       in  ref_legal_person.user_location       % type,
    datlast_             in  ref_legal_person.datlast             % type,
    do_commit_           in  integer default 1,     
    err_code             out number,
    err_msg              out varchar2
  );
  
  procedure ref_delete_legal_person(
    id_        in  ref_legal_person.id % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );
  
    
  /* Справочник стран */ 
  procedure ref_read_country_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_country_l_by_params(
    id_             in  ref_country.id % type,
    date_           in  Date,
    code_           in  ref_country.code % type,
    name_ru_        in  ref_country.name_ru % type,
    rec_id_         in  ref_country.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  
  /* Справочник работников */
  procedure ref_read_managers_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_managers_l_by_params(
    id_             in  ref_managers.id % type,
    date_           in  Date,
    fm_             in  ref_managers.fm % type,
    nm_             in  ref_managers.nm % type,
    ft_             in  ref_managers.ft % type,
    rec_id_         in  ref_managers.rec_id % type,
    ref_post_       in  ref_managers.ref_post % type,
    is_executor_    in  Integer default 0,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  procedure ref_read_managers_hst_list(
    id_      in  ref_managers.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  );
  
  procedure ref_insert_managers(
    rec_id_        in  ref_managers.rec_id        % type,
    code_          in  ref_managers.code          % type,    
    fm_            in  ref_managers.fm            % type,
    nm_            in  ref_managers.nm            % type,
    ft_            in  ref_managers.ft            % type,
    fio_kz_        in  ref_managers.fio_kz        % type,
    fio_en_        in  ref_managers.fio_en        % type,        
    ref_post_      in  ref_managers.ref_post      % type,
    phone_         in  ref_managers.phone         % type,
    begin_date_    in  ref_managers.begin_date    % type,
    end_date_      in  ref_managers.end_date      % type,
    id_usr_        in  ref_managers.id_usr        % type,    
    user_location_ in  ref_managers.user_location % type,
    datlast_       in  ref_managers.datlast       % type,
    do_commit_     in  integer default 1,
    id_            out ref_managers.id % type,
    err_code       out number,
    err_msg        out varchar2
  );
  
  procedure ref_update_managers(
    id_            in  ref_managers.id            % type,
    rec_id_        in  ref_managers.rec_id        % type,
    code_          in  ref_managers.code          % type,    
    fm_            in  ref_managers.fm            % type,
    nm_            in  ref_managers.nm            % type,
    ft_            in  ref_managers.ft            % type,
    fio_kz_        in  ref_managers.fio_kz        % type,
    fio_en_        in  ref_managers.fio_en        % type,        
    ref_post_      in  ref_managers.ref_post      % type,    
    phone_         in  ref_managers.phone         % type,
    begin_date_    in  ref_managers.begin_date    % type,
    end_date_      in  ref_managers.end_date      % type,
    id_usr_        in  ref_managers.id_usr        % type,    
    user_location_ in  ref_managers.user_location % type,
    datlast_       in  ref_managers.datlast       % type,
    do_commit_     in  integer default 1,     
    err_code       out number,
    err_msg        out varchar2
  );
  
  procedure ref_delete_managers(
    id_        in  ref_managers.id % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );
  
  
  /* Справочник организационно-правовая форма */   
  procedure ref_read_type_bus_entity_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_t_b_e_list_by_params(
    id_             in  ref_type_bus_entity.id % type,
    date_           in  Date,
    name_ru_        in  ref_type_bus_entity.name_ru % type,
    rec_id_         in  ref_type_bus_entity.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  procedure ref_read_t_b_e_hst_list(
    id_      in  ref_type_bus_entity.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  );
  
  procedure ref_insert_type_bus_entity(
    rec_id_        in  ref_type_bus_entity.rec_id        % type,
    code_          in  ref_type_bus_entity.code          % type,    
    name_kz_       in  ref_type_bus_entity.name_kz       % type,
    name_ru_       in  ref_type_bus_entity.name_ru       % type,
    name_en_       in  ref_type_bus_entity.name_en       % type,                        
    begin_date_    in  ref_type_bus_entity.begin_date    % type,
    end_date_      in  ref_type_bus_entity.end_date      % type,
    id_usr_        in  ref_type_bus_entity.id_usr        % type,    
    user_location_ in  ref_type_bus_entity.user_location % type,
    datlast_       in  ref_type_bus_entity.datlast       % type,
    do_commit_     in  integer default 1,
    id_            out ref_type_bus_entity.id % type,
    err_code       out number,
    err_msg        out varchar2
  );
  
  procedure ref_update_type_bus_entity(
    id_            in  ref_type_bus_entity.id                   % type,
    rec_id_        in  ref_type_bus_entity.rec_id               % type,
    code_          in  ref_type_bus_entity.code                 % type,    
    name_kz_       in  ref_type_bus_entity.name_kz              % type,
    name_ru_       in  ref_type_bus_entity.name_ru              % type,
    name_en_       in  ref_type_bus_entity.name_en              % type,            
    begin_date_    in  ref_type_bus_entity.begin_date           % type,
    end_date_      in  ref_type_bus_entity.end_date             % type,
    id_usr_        in  ref_type_bus_entity.id_usr               % type,    
    user_location_ in  ref_type_bus_entity.user_location        % type,    
    datlast_       in  ref_type_bus_entity.datlast              % type,
    do_commit_     in  integer default 1,     
    err_code       out number,
    err_msg        out varchar2
  );
  
  procedure ref_delete_type_bus_entity(
    id_        in  ref_type_bus_entity.id  % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );
  
  
  /* Справочник регионов */ 
  procedure ref_read_region_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_region_list_by_params(
    id_             in  ref_region.id % type,
    date_           in  Date,
    code_           in  ref_region.code % type,
    name_ru_        in  ref_region.name_ru % type,
    rec_id_         in  ref_region.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  
  /* Справочник требований и обязательств*/  
  procedure ref_read_requirement_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_req_list_by_params(
    id_             in  ref_requirement.id % type,
    date_           in  Date,
    name_ru_        in  ref_requirement.name_ru % type,
    rec_id_         in  ref_requirement.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  procedure ref_read_requirement_hst_list(
    id_      in  ref_requirement.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  );
  
  procedure ref_insert_requirement(
    rec_id_        in  ref_requirement.rec_id        % type,
    code_          in  ref_requirement.code          % type,    
    name_kz_       in  ref_requirement.name_kz       % type,
    name_ru_       in  ref_requirement.name_ru       % type,
    name_en_       in  ref_requirement.name_en       % type,            
    begin_date_    in  ref_requirement.begin_date    % type,
    end_date_      in  ref_requirement.end_date      % type,
    id_usr_        in  ref_requirement.id_usr        % type,    
    user_location_ in  ref_requirement.user_location % type,
    datlast_       in  ref_requirement.datlast       % type,
    do_commit_     in  integer default 1,     
    id_            out ref_requirement.id % type,
    err_code       out number,
    err_msg        out varchar2
  );
  
  procedure ref_update_requirement(
    id_              in  ref_requirement.id               % type,
    rec_id_          in  ref_requirement.rec_id           % type,
    code_            in  ref_requirement.code             % type,    
    name_kz_         in  ref_requirement.name_kz          % type,
    name_ru_         in  ref_requirement.name_ru          % type,
    name_en_         in  ref_requirement.name_en          % type,            
    begin_date_      in  ref_requirement.begin_date       % type,
    end_date_        in  ref_requirement.end_date         % type,
    id_usr_          in  ref_requirement.id_usr           % type,    
    user_location_   in  ref_requirement.user_location    % type,
    datlast_         in  ref_requirement.datlast          % type,
    do_commit_       in  integer default 1,     
    err_code         out number,
    err_msg          out varchar2
  );
  
  procedure ref_delete_requirement(
    id_        in  ref_requirement.id  % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );
  
  
  /* Виды  обеспечения */
  procedure ref_read_type_provide_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_t_p_list_by_params(
    id_             in  ref_type_provide.id % type,
    date_           in  Date,
    code_           in  ref_type_provide.code % type,
    name_ru_        in  ref_type_provide.name_ru % type,
    rec_id_         in  ref_type_provide.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  procedure ref_read_type_provide_hst_list(
    id_      in  ref_type_provide.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  );
  
  procedure ref_insert_type_provide(
    rec_id_        in  ref_type_provide.rec_id        % type,
    code_          in  ref_type_provide.code          % type,    
    name_kz_       in  ref_type_provide.name_kz       % type,
    name_ru_       in  ref_type_provide.name_ru       % type,
    name_en_       in  ref_type_provide.name_en       % type,            
    begin_date_    in  ref_type_provide.begin_date    % type,
    end_date_      in  ref_type_provide.end_date      % type,
    id_usr_        in  ref_type_provide.id_usr        % type,    
    user_location_ in  ref_type_provide.user_location % type,
    datlast_       in  ref_type_provide.datlast       % type,
    do_commit_     in  integer default 1,
    id_            out ref_type_provide.id % type,
    err_code       out number,
    err_msg        out varchar2
  );
  
  procedure ref_update_type_provide(
    id_            in  ref_type_provide.id                % type,
    rec_id_        in  ref_type_provide.rec_id            % type,
    code_          in  ref_type_provide.code              % type,    
    name_kz_       in  ref_type_provide.name_kz           % type,
    name_ru_       in  ref_type_provide.name_ru           % type,
    name_en_       in  ref_type_provide.name_en           % type,            
    begin_date_    in  ref_type_provide.begin_date        % type,
    end_date_      in  ref_type_provide.end_date          % type,
    id_usr_        in  ref_type_provide.id_usr            % type,    
    user_location_ in  ref_type_provide.user_location     % type,    
    datlast_       in  ref_type_provide.datlast           % type,
    do_commit_     in  integer default 1,     
    err_code       out number,
    err_msg        out varchar2
  );
  
  procedure ref_delete_type_provide(
    id_        in  ref_type_provide.id  % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );
  
  
  /* Типы сделок */   
  procedure ref_read_trans_types_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_t_t_list_by_params(
    id_               in  ref_trans_types.id % type,
    date_             in  Date,
    name_ru_          in  ref_trans_types.name_ru % type,
    kind_of_activity_ in  ref_trans_types.kind_of_activity % type,
    rec_id_           in  ref_trans_types.rec_id % type,
    search_all_ver_   in  Integer default 0,
    Cur               out sys_refcursor,
    err_code          out number,
    err_msg           out varchar2    
  );
  
  procedure ref_read_trans_types_hst_list(
    id_      in  ref_trans_types.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  );
  
  procedure ref_insert_trans_types(
    rec_id_           in  ref_trans_types.rec_id           % type,
    code_             in  ref_trans_types.code             % type,    
    name_kz_          in  ref_trans_types.name_kz          % type,
    name_ru_          in  ref_trans_types.name_ru          % type,
    name_en_          in  ref_trans_types.name_en          % type,            
    kind_of_activity_ in  ref_trans_types.kind_of_activity % type,
    short_name_       in  ref_trans_types.short_name       % type,
    begin_date_       in  ref_trans_types.begin_date       % type,
    end_date_         in  ref_trans_types.end_date         % type,
    id_usr_           in  ref_trans_types.id_usr           % type,    
    user_location_    in  ref_trans_types.user_location    % type,
    datlast_          in  ref_trans_types.datlast          % type,
    do_commit_        in  integer default 1,
    id_               out ref_trans_types.id % type,
    err_code          out number,
    err_msg           out varchar2
  );
  
  procedure ref_update_trans_types(
    id_               in  ref_trans_types.id               % type,
    rec_id_           in  ref_trans_types.rec_id           % type,
    code_             in  ref_trans_types.code             % type,    
    name_kz_          in  ref_trans_types.name_kz          % type,
    name_ru_          in  ref_trans_types.name_ru          % type,
    name_en_          in  ref_trans_types.name_en          % type,            
    kind_of_activity_ in  ref_trans_types.kind_of_activity % type,
    short_name_       in  ref_trans_types.short_name       % type,
    begin_date_       in  ref_trans_types.begin_date       % type,
    end_date_         in  ref_trans_types.end_date         % type,
    id_usr_           in  ref_trans_types.id_usr           % type,    
    user_location_    in  ref_trans_types.user_location    % type,    
    datlast_          in  ref_trans_types.datlast          % type,
    do_commit_        in  integer default 1,     
    err_code          out number,
    err_msg           out varchar2
  );
  
  procedure ref_delete_trans_types(
    id_        in  ref_trans_types.id  % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );
  
  
  /* Балансовые счета для отчетов о сделках */   
  procedure ref_read_balance_acc_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_b_acc_l_by_params(
    id_             in  ref_balance_account.id % type,
    date_           in  Date,
    name_ru_        in  ref_balance_account.name_ru % type,
    code_           in  ref_balance_account.code % type,
    parent_code_    in  ref_balance_account.parent_code % type,
    rec_id_         in  ref_balance_account.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  procedure ref_read_balance_acc_hst_list(
    id_      in  ref_balance_account.id  % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  );
  
  procedure ref_read_bal_acc_last_rec_list(
    date_    in  Date,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  );
  
  procedure ref_insert_balance_acc(
    rec_id_           in  ref_balance_account.rec_id           % type,
    code_             in  ref_balance_account.code             % type,    
    parent_code_      in  ref_balance_account.parent_code      % type,
    level_code_       in  ref_balance_account.level_code       % type,
    name_kz_          in  ref_balance_account.name_kz          % type,
    name_ru_          in  ref_balance_account.name_ru          % type,
    name_en_          in  ref_balance_account.name_en          % type,                    
    short_name_kz_    in  ref_balance_account.short_name_kz    % type,
    short_name_ru_    in  ref_balance_account.short_name_ru    % type,
    short_name_en_    in  ref_balance_account.short_name_en    % type,
    begin_date_       in  ref_balance_account.begin_date       % type,    
    end_date_         in  ref_balance_account.end_date         % type,
    id_usr_           in  ref_balance_account.id_usr           % type,    
    user_location_    in  ref_balance_account.user_location    % type,
    datlast_          in  ref_balance_account.datlast          % type,
    do_commit_        in  integer default 1,
    id_               out ref_balance_account.id % type,
    err_code          out number,
    err_msg           out varchar2
  );
  
  procedure ref_update_balance_acc(
    id_               in  ref_balance_account.id               % type,
    rec_id_           in  ref_balance_account.rec_id           % type,
    code_             in  ref_balance_account.code             % type,        
    name_kz_          in  ref_balance_account.name_kz          % type,
    name_ru_          in  ref_balance_account.name_ru          % type,
    name_en_          in  ref_balance_account.name_en          % type,                    
    short_name_kz_    in  ref_balance_account.short_name_kz    % type,
    short_name_ru_    in  ref_balance_account.short_name_ru    % type,
    short_name_en_    in  ref_balance_account.short_name_en    % type,
    begin_date_       in  ref_balance_account.begin_date       % type,    
    end_date_         in  ref_balance_account.end_date         % type,
    id_usr_           in  ref_balance_account.id_usr           % type,    
    user_location_    in  ref_balance_account.user_location    % type,     
    datlast_          in  ref_balance_account.datlast          % type,
    do_commit_        in  integer default 1,     
    err_code          out number,
    err_msg           out varchar2
  );
  
  procedure ref_delete_balance_acc(
    id_        in  ref_balance_account.id  % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );
  
    
  /* Признаки связанности с подотчетной организацией особыми отношениями*/ 
  procedure ref_read_conn_org_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_conn_org_l_by_params(
    id_             in  ref_conn_org.id % type,
    date_           in  Date,
    code_           in  ref_conn_org.code % type,
    name_ru_        in  ref_conn_org.name_ru % type,
    rec_id_         in  ref_conn_org.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  procedure ref_read_conn_org_hst_list(
    id_      in  ref_conn_org.id  % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  );
  
  procedure ref_insert_conn_org(
    rec_id_        in  ref_conn_org.rec_id           % type,
    code_          in  ref_conn_org.code             % type,    
    name_kz_       in  ref_conn_org.name_kz          % type,
    name_ru_       in  ref_conn_org.name_ru          % type,
    name_en_       in  ref_conn_org.name_en          % type,            
    short_name_    in  ref_conn_org.short_name       % type,
    begin_date_    in  ref_conn_org.begin_date       % type,
    end_date_      in  ref_conn_org.end_date         % type,
    id_usr_        in  ref_conn_org.id_usr           % type,    
    user_location_ in  ref_conn_org.user_location    % type,
    datlast_       in  ref_conn_org.datlast          % type,
    do_commit_     in  integer default 1,
    id_            out ref_conn_org.id % type,
    err_code       out number,
    err_msg        out varchar2
  );
  
  procedure ref_update_conn_org(
    id_            in  ref_conn_org.id            % type,
    rec_id_        in  ref_conn_org.rec_id        % type,
    code_          in  ref_conn_org.code          % type,    
    name_kz_       in  ref_conn_org.name_kz       % type,
    name_ru_       in  ref_conn_org.name_ru       % type,
    name_en_       in  ref_conn_org.name_en       % type,            
    short_name_    in  ref_conn_org.short_name    % type,
    begin_date_    in  ref_conn_org.begin_date    % type,
    end_date_      in  ref_conn_org.end_date         % type,
    id_usr_        in  ref_conn_org.id_usr        % type,    
    user_location_ in  ref_conn_org.user_location % type,    
    datlast_       in  ref_conn_org.datlast          % type,
    do_commit_     in  integer default 1,     
    err_code       out number,
    err_msg        out varchar2
  );
  
  procedure ref_delete_conn_org(
    id_        in  ref_conn_org.id  % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );
  
  
  /* Подразделения НБ РК*/  
  procedure ref_read_department_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_dep_list_by_params(
    id_             in  ref_department.id % type,
    date_           in  Date,
    name_ru_        in  ref_department.name_ru % type,
    rec_id_         in  ref_department.rec_id % type,
    dept_type_      in  ref_department.ref_department_type % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  procedure ref_read_dep_type(
    date_    in Date,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2
  );
  
  
  /* Банки второго уровня */
  procedure ref_read_bank_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_bank_list_by_params(
    id_              in  ref_bank.id % type,
    date_            in  Date,
    idn_             in  ref_bank.idn % type,
    name_ru_         in  ref_bank.name_ru % type,
    rec_id_          in  ref_bank.rec_id % type,
    is_load_         in  ref_bank.is_load % type,    
    is_non_rezident_ in  ref_bank.is_non_rezident % type,
    search_all_ver_  in  Integer default 0,
    Cur              out sys_refcursor,
    err_code         out number,
    err_msg          out varchar2    
  );
  
  procedure ref_read_bank_hst_list(
    id_      in  ref_bank.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  );
  
  procedure ref_insert_bank(
    rec_id_          in  ref_bank.rec_id          % type,
    code_            in  ref_bank.code            % type,    
    name_kz_         in  ref_bank.name_kz         % type,
    name_ru_         in  ref_bank.name_ru         % type,
    name_en_         in  ref_bank.name_en         % type,
    idn_             in  ref_bank.idn             % type,
    post_address_    in  ref_bank.post_address    % type,
    phone_num_       in  ref_bank.phone_num       % type,
    ref_country_     in  ref_bank.ref_country     % type,
    is_non_rezident_ in  ref_bank.is_non_rezident % type,
    begin_date_      in  ref_bank.begin_date      % type,
    end_date_        in  ref_bank.end_date        % type,
    id_usr_          in  ref_bank.id_usr          % type,    
    user_location_   in  ref_bank.user_location   % type,
    datlast_         in  ref_bank.datlast         % type,
    do_commit_       in  integer default 1,     
    id_              out ref_bank.id % type,
    err_code         out number,
    err_msg          out varchar2
  );
  
  procedure ref_update_bank(
    id_              in  ref_bank.id               % type,
    rec_id_          in  ref_bank.rec_id           % type,
    code_            in  ref_bank.code             % type,    
    name_kz_         in  ref_bank.name_kz          % type,
    name_ru_         in  ref_bank.name_ru          % type,
    name_en_         in  ref_bank.name_en          % type,            
    idn_             in  ref_bank.idn              % type,
    post_address_    in  ref_bank.post_address     % type,
    phone_num_       in  ref_bank.phone_num        % type,
    ref_country_     in  ref_bank.ref_country      % type,
    is_non_rezident_ in  ref_bank.is_non_rezident  % type,
    begin_date_      in  ref_bank.begin_date       % type,
    end_date_        in  ref_bank.end_date         % type,
    id_usr_          in  ref_bank.id_usr           % type,    
    user_location_   in  ref_bank.user_location    % type,
    datlast_         in  ref_bank.datlast          % type,
    do_commit_       in  integer default 1,     
    err_code         out number,
    err_msg          out varchar2
  );
  
  procedure ref_delete_bank(
    id_        in  ref_bank.id  % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );

  
  /*Рейтинг агенств*/
  procedure ref_read_rate_agency_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_rat_agen_l_by_params(
    id_             in  ref_rate_agency.id % type,
    date_           in  Date,
    name_ru_        in  ref_rate_agency.name_ru % type,
    rec_id_         in  ref_rate_agency.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  /* Валюты */
  procedure ref_read_currency_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_curr_list_by_params(
    id_             in  ref_currency.id % type,
    date_           in  Date,
    code_           in  ref_currency.code % type,
    cur_rate_name_  in  ref_currency_rate.name_ru % type,
    rate_agency_    in  ref_rate_agency.name_ru % type,
    name_ru_        in  ref_currency.name_ru % type,
    rec_id_         in  ref_currency.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  
  /* Рейтинг валют */
  procedure ref_read_currency_rate_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_curr_rat_l_by_params(
    id_             in  ref_currency_rate.id % type,
    date_           in  Date,
    rate_agency_    in  ref_rate_agency.name_ru % type,
    name_ru_        in  ref_currency_rate.name_ru % type,
    rec_id_         in  ref_currency_rate.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  
  /* Тип организации */ 
  procedure ref_read_subject_type_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_subj_type_l_by_params(
    id_             in  ref_subject_type.id % type,
    date_           in  Date,
    name_ru_        in  ref_subject_type.name_ru % type,
    rec_id_         in  ref_subject_type.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  procedure ref_read_subject_type_hst_list(
    id_      in  ref_subject_type.id  % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  );
  
  procedure ref_insert_subject_type(
    rec_id_             in  ref_subject_type.rec_id             % type,
    code_               in  ref_subject_type.code               % type,    
    name_kz_            in  ref_subject_type.name_kz            % type,
    name_ru_            in  ref_subject_type.name_ru            % type,
    name_en_            in  ref_subject_type.name_en            % type,                
    short_name_kz_      in  ref_subject_type.short_name_kz      % type,
    short_name_ru_      in  ref_subject_type.short_name_ru      % type,
    short_name_en_      in  ref_subject_type.short_name_en      % type,                
--    rep_per_dur_months_ in  ref_subject_type.rep_per_dur_months % type,
    is_advance_         in  ref_subject_type.is_advance         % type,
    begin_date_         in  ref_subject_type.begin_date         % type,
    end_date_           in  ref_subject_type.end_date           % type,
    id_usr_             in  ref_subject_type.id_usr             % type,    
    user_location_      in  ref_subject_type.user_location      % type,
    datlast_            in  ref_subject_type.datlast            % type,
    do_commit_          in  integer default 1,
    id_                 out ref_subject_type.id % type,
    err_code            out number,
    err_msg             out varchar2
  );
  
  procedure ref_update_subject_type(
    id_                 in  ref_subject_type.id                 % type,
    rec_id_             in  ref_subject_type.rec_id             % type,
    code_               in  ref_subject_type.code               % type,    
    name_kz_            in  ref_subject_type.name_kz            % type,
    name_ru_            in  ref_subject_type.name_ru            % type,
    name_en_            in  ref_subject_type.name_en            % type,                
    short_name_kz_      in  ref_subject_type.short_name_kz      % type,
    short_name_ru_      in  ref_subject_type.short_name_ru      % type,
    short_name_en_      in  ref_subject_type.short_name_en      % type,
--    rep_per_dur_months_ in  ref_subject_type.rep_per_dur_months % type,
    is_advance_         in  ref_subject_type.is_advance         % type,
    begin_date_         in  ref_subject_type.begin_date         % type,
    end_date_           in  ref_subject_type.end_date           % type,
    id_usr_             in  ref_subject_type.id_usr             % type,    
    user_location_      in  ref_subject_type.user_location      % type,
    datlast_            in  ref_subject_type.datlast            % type,
    do_commit_          in  integer default 1,     
    err_code            out number,
    err_msg             out varchar2
  );
  
  procedure ref_delete_subject_type(
    id_        in  ref_subject_type.id   % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );
  
  
  /* Респонденты */  
  procedure ref_read_respondent_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_resp_list_by_params(
    id_               in ref_respondent.id % type,
    date_             in  Date,
    name_ru_          in  ref_legal_person.name_ru % type,
    rec_id_           in  ref_legal_person.rec_id % type,
    idn_              in  ref_legal_person.idn % type,
    ref_department_   in  ref_respondent.ref_department % type,
    ref_subject_type_ in  ref_respondent.ref_subject_type % type,
    search_all_ver_   in  Integer default 0,
    Cur               out sys_refcursor,
    err_code          out number,
    err_msg           out varchar2    
  );
  
  procedure ref_read_respondent_hst_list(
    id_      in  ref_respondent.id  % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  );
  
  procedure ref_insert_respondent(
    rec_id_             in  ref_respondent.rec_id             % type,
    code_               in  ref_respondent.code               % type,    
    ref_legal_person_   in  ref_respondent.ref_legal_person   % type,
    nokbdb_code_        in  ref_respondent.nokbdb_code        % type,    
    main_buh_           in  ref_respondent.main_buh           % type,
    date_begin_lic_     in  ref_respondent.date_begin_lic     % type,
    date_end_lic_       in  ref_respondent.date_end_lic       % type,
    stop_lic_           in  ref_respondent.stop_lic           % type,
    vid_activity_       in  ref_respondent.vid_activity       % type,    
    ref_department_     in  ref_respondent.ref_department     % type,
    ref_subject_type_   in  ref_respondent.ref_subject_type   % type,
    begin_date_         in  ref_respondent.begin_date         % type,
    end_date_           in  ref_respondent.end_date           % type,
    id_usr_             in  ref_respondent.id_usr             % type,    
    user_location_      in  ref_respondent.user_location      % type,
    datlast_            in  ref_respondent.datlast            % type,
    do_commit_          in  integer default 1,
    id_                 out ref_respondent.id % type,
    err_code            out number,
    err_msg             out varchar2
  );
  
  procedure ref_update_respondent(
    id_               in  ref_respondent.id               % type,
    rec_id_           in  ref_respondent.rec_id           % type,
    code_             in  ref_respondent.code             % type,    
    ref_legal_person_ in  ref_respondent.ref_legal_person % type,
    nokbdb_code_      in  ref_respondent.nokbdb_code      % type,    
    main_buh_         in  ref_respondent.main_buh         % type,
    date_begin_lic_   in  ref_respondent.date_begin_lic   % type,
    date_end_lic_     in  ref_respondent.date_end_lic     % type,
    stop_lic_         in  ref_respondent.stop_lic         % type,
    vid_activity_     in  ref_respondent.vid_activity     % type,
    ref_department_   in  ref_respondent.ref_department   % type,
    ref_subject_type_ in  ref_respondent.ref_subject_type % type,
    begin_date_       in  ref_respondent.begin_date       % type,
    end_date_         in  ref_respondent.end_date         % type,
    id_usr_           in  ref_respondent.id_usr           % type,    
    user_location_    in  ref_respondent.user_location    % type,
    datlast_          in  ref_respondent.datlast          % type,
    do_commit_        in  integer default 1,     
    err_code          out number,
    err_msg           out varchar2
  );
  
  procedure ref_delete_respondent(
    id_        in  ref_respondent.id   % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );
  
  procedure ref_read_respondent_by_rec_id(
    rec_id_   in ref_respondent.rec_id % type,
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  
  /* Типы документов */
  procedure ref_read_doc_type_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_doc_type_l_by_params(
    id_             in  ref_doc_type.id % type,
    date_           in  Date,
    name_ru_        in  ref_doc_type.name_ru % type,
    rec_id_         in  ref_doc_type.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  procedure ref_read_doc_type_hst_list(
    id_      in  ref_doc_type.id  % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  );
  
  procedure ref_insert_doc_type(
    rec_id_              in  ref_doc_type.rec_id              % type,
    code_                in  ref_doc_type.code                % type,    
    name_kz_             in  ref_doc_type.name_kz             % type,
    name_ru_             in  ref_doc_type.name_ru             % type,
    name_en_             in  ref_doc_type.name_en             % type,                
    is_identification_   in  ref_doc_type.is_identification   % type,
    is_organization_doc_ in  ref_doc_type.is_organization_doc % type,
    is_person_doc_       in  ref_doc_type.is_person_doc       % type,    
    begin_date_          in  ref_doc_type.begin_date          % type,
    end_date_            in  ref_doc_type.end_date            % type,
    id_usr_              in  ref_doc_type.id_usr              % type,    
    user_location_       in  ref_doc_type.user_location       % type,
    datlast_             in  ref_doc_type.datlast             % type,
    do_commit_           in  integer default 1,
    id_                  out ref_doc_type.id % type,
    err_code             out number,
    err_msg              out varchar2
  );
  
  procedure ref_update_doc_type(
    id_                  in  ref_doc_type.id                  % type,
    rec_id_              in  ref_doc_type.rec_id              % type,
    code_                in  ref_doc_type.code                % type,    
    name_kz_             in  ref_doc_type.name_kz             % type,
    name_ru_             in  ref_doc_type.name_ru             % type,
    name_en_             in  ref_doc_type.name_en             % type,                
    is_identification_   in  ref_doc_type.is_identification   % type,
    is_organization_doc_ in  ref_doc_type.is_organization_doc % type,
    is_person_doc_       in  ref_doc_type.is_person_doc       % type,    
    begin_date_          in  ref_doc_type.begin_date          % type,
    end_date_            in  ref_doc_type.end_date            % type,
    id_usr_              in  ref_doc_type.id_usr              % type,    
    user_location_       in  ref_doc_type.user_location       % type,
    datlast_             in  ref_doc_type.datlast             % type,
    do_commit_           in  integer default 1,     
    err_code             out number,
    err_msg              out varchar2
  );
  
  procedure ref_delete_doc_type(
    id_        in  ref_doc_type.id   % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );
  
  
  /* Документы */   
  procedure ref_read_document_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_doc_list_by_params(
    id_             in  ref_document.id % type,
    date_           in  Date,
    name_ru_        in  ref_document.name_ru % type,
    rec_id_         in  ref_document.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  procedure ref_read_document_hst_list(
    id_      in  ref_document.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  );
  
  procedure ref_insert_document(
    rec_id_              in  ref_document.rec_id         % type,
    code_                in  ref_document.code           % type,    
    name_kz_             in  ref_document.name_kz        % type,
    name_ru_             in  ref_document.name_ru        % type,
    name_en_             in  ref_document.name_en        % type,                
    ref_doc_type_        in  ref_document.ref_doc_type   % type,
    ref_respondent_      in  ref_document.ref_respondent % type,
    begin_date_          in  ref_document.begin_date     % type,
    end_date_            in  ref_document.end_Date       % type,
    id_usr_              in  ref_document.id_usr         % type,    
    user_location_       in  ref_document.user_location  % type,
    datlast_             in  ref_document.datlast        % type,
    do_commit_           in  integer default 1,
    id_                  out ref_document.id % type,
    err_code             out number,
    err_msg              out varchar2
  );
  
  procedure ref_update_document(
    id_                  in  ref_document.id             % type,
    rec_id_              in  ref_document.rec_id         % type,
    code_                in  ref_document.code           % type,    
    name_kz_             in  ref_document.name_kz        % type,
    name_ru_             in  ref_document.name_ru        % type,
    name_en_             in  ref_document.name_en        % type,                
    ref_doc_type_        in  ref_document.ref_doc_type   % type,
    ref_respondent_      in  ref_document.ref_respondent % type,
    begin_date_          in  ref_document.begin_date     % type,
    end_date_            in  ref_document.end_Date       % type,
    id_usr_              in  ref_document.id_usr         % type,    
    user_location_       in  ref_document.user_location  % type,
    datlast_             in  ref_document.datlast        % type,
    do_commit_           in  integer default 1,     
    err_code             out number,
    err_msg              out varchar2
  );
  
  procedure ref_delete_document(
    id_        in  ref_document.id % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );
  
  
  /* Эмитенты */  
  procedure ref_read_issuers_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_issuers_l_by_params(
    id_             in  ref_issuers.id % type,
    date_           in  Date,
    name_ru_        in  ref_issuers.name_ru % type,
    rec_id_         in  ref_issuers.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  
  /* Ценные бумаги */
  procedure ref_read_securities_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_sec_list_by_params(
    id_             in  ref_securities.id % type,
    date_           in  Date,
    issuer_name_    in  ref_securities.issuer_name % type,
    nin_            in  ref_securities.nin % type,
    rec_id_         in  ref_securities.rec_id % type,    
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  
  /* Вид операций */ 
  procedure ref_read_vid_oper_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_vid_oper_l_by_params(
    id_             in  ref_vid_oper.id % type,
    date_           in  Date,
    name_ru_        in  ref_vid_oper.name_ru % type,
    rec_id_         in  ref_vid_oper.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  procedure ref_read_vid_oper_hst_list(
    id_      in  ref_vid_oper.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  );
  
  procedure ref_insert_vid_oper(
    rec_id_        in  ref_vid_oper.rec_id         % type,
    code_          in  ref_vid_oper.code           % type,    
    name_kz_       in  ref_vid_oper.name_kz        % type,
    name_ru_       in  ref_vid_oper.name_ru        % type,
    name_en_       in  ref_vid_oper.name_en        % type,                    
    begin_date_    in  ref_vid_oper.begin_date     % type,
    end_date_      in  ref_vid_oper.end_date       % type,
    id_usr_        in  ref_vid_oper.id_usr         % type,    
    user_location_ in  ref_vid_oper.user_location  % type,
    datlast_       in  ref_vid_oper.datlast        % type,
    do_commit_     in  integer default 1,
    id_            out ref_vid_oper.id % type,
    err_code       out number,
    err_msg        out varchar2
  );
  
  procedure ref_update_vid_oper(
    id_            in  ref_vid_oper.id             % type,
    rec_id_        in  ref_vid_oper.rec_id         % type,
    code_          in  ref_vid_oper.code           % type,    
    name_kz_       in  ref_vid_oper.name_kz        % type,
    name_ru_       in  ref_vid_oper.name_ru        % type,
    name_en_       in  ref_vid_oper.name_en        % type,                    
    begin_date_    in  ref_vid_oper.begin_date     % type,
    end_date_      in  ref_vid_oper.end_date       % type,
    id_usr_        in  ref_vid_oper.id_usr         % type,    
    user_location_ in  ref_vid_oper.user_location  % type,
    datlast_       in  ref_vid_oper.datlast        % type,
    do_commit_     in  integer default 1,     
    err_code       out number,
    err_msg        out varchar2
  );
  
  procedure ref_delete_vid_oper(
    id_        in  ref_vid_oper.id % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );
    
  
  /* Вид ЦБ */
/*  procedure ref_read_vid_cb_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_vid_cb_l_by_params(
    id_             in  ref_vid_cb.id % type,
    date_           in  Date,
    name_ru_        in  ref_vid_cb.name_ru % type,
    rec_id_         in  ref_vid_cb.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  procedure ref_read_vid_cb_hst_list(
    id_      in  ref_vid_cb.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  );
  
  procedure ref_insert_vid_cb(
    rec_id_        in  ref_vid_cb.rec_id         % type,
    code_          in  ref_vid_cb.code           % type,    
    name_kz_       in  ref_vid_cb.name_kz        % type,
    name_ru_       in  ref_vid_cb.name_ru        % type,
    name_en_       in  ref_vid_cb.name_en        % type,                    
    begin_date_    in  ref_vid_cb.begin_date     % type,
    end_date_      in  ref_vid_cb.end_date       % type,
    id_usr_        in  ref_vid_cb.id_usr         % type,    
    user_location_ in  ref_vid_cb.user_location  % type,
    datlast_       in  ref_vid_cb.datlast        % type,
    do_commit_     in  integer default 1,
    id_            out ref_vid_cb.id % type,
    err_code       out number,
    err_msg        out varchar2
  );
  
  procedure ref_update_vid_cb(
    id_            in  ref_vid_cb.id             % type,
    rec_id_        in  ref_vid_cb.rec_id         % type,
    code_          in  ref_vid_cb.code           % type,    
    name_kz_       in  ref_vid_cb.name_kz        % type,
    name_ru_       in  ref_vid_cb.name_ru        % type,
    name_en_       in  ref_vid_cb.name_en        % type,                    
    begin_date_    in  ref_vid_cb.begin_date     % type,
    end_date_      in  ref_vid_cb.end_date       % type,
    id_usr_        in  ref_vid_cb.id_usr         % type,    
    user_location_ in  ref_vid_cb.user_location  % type,
    datlast_       in  ref_vid_cb.datlast        % type,
    do_commit_     in  integer default 1,     
    err_code       out number,
    err_msg        out varchar2
  );
  
  procedure ref_delete_vid_cb(
    id_        in  ref_vid_cb.id % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );*/
        
 
  /* Отрасли */
  procedure ref_read_branch_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_branch_list_by_params(
    id_             in  ref_branch.id % type,
    date_           in  Date,
    code_           in  ref_branch.code % type,
    name_ru_        in  ref_branch.name_ru % type,
    rec_id_         in  ref_branch.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  /*procedure ref_read_branch_hst_list(
    id_      in  ref_branch.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  );
  
  procedure ref_insert_branch(
    rec_id_        in  ref_branch.rec_id         % type,
    code_          in  ref_branch.code           % type,    
    name_kz_       in  ref_branch.name_kz        % type,
    name_ru_       in  ref_branch.name_ru        % type,
    name_en_       in  ref_branch.name_en        % type,                    
    begin_date_    in  ref_branch.begin_date     % type,
    end_date_      in  ref_branch.end_date       % type,
    id_usr_        in  ref_branch.id_usr         % type,    
    user_location_ in  ref_branch.user_location  % type,
    datlast_       in  ref_branch.datlast        % type,
    do_commit_     in  integer default 1,
    id_            out ref_branch.id % type,
    err_code       out number,
    err_msg        out varchar2
  );
  
  procedure ref_update_branch(
    id_            in  ref_branch.id             % type,
    rec_id_        in  ref_branch.rec_id         % type,
    code_          in  ref_branch.code           % type,    
    name_kz_       in  ref_branch.name_kz        % type,
    name_ru_       in  ref_branch.name_ru        % type,
    name_en_       in  ref_branch.name_en        % type,                    
    begin_date_    in  ref_branch.begin_date     % type,
    end_date_      in  ref_branch.end_date       % type,
    id_usr_        in  ref_branch.id_usr         % type,    
    user_location_ in  ref_branch.user_location  % type,
    datlast_       in  ref_branch.datlast        % type,
    do_commit_     in  integer default 1,     
    err_code       out number,
    err_msg        out varchar2
  );
  
  procedure ref_delete_branch(
    id_        in  ref_branch.id % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );*/
    
  
  /* Межформенный контроль */ 
  procedure ref_read_crosscheck_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_cross_list_by_params(
    id_             in  ref_crosscheck.id % type,
    date_           in  Date,
    l_src_formula_  in  ref_crosscheck.l_src_formula % type,
    r_src_formula_  in  ref_crosscheck.r_src_formula % type,
    l_desc_         in  ref_crosscheck.l_desc % type,
    r_desc_         in  ref_crosscheck.r_desc % type,
    cf_type_        in  ref_crosscheck.crosscheck_type % type,
    l_src_cond_     in  ref_crosscheck.l_src_cond % type,
    r_src_cond_     in  ref_crosscheck.r_src_cond % type,
    is_available_   in  ref_crosscheck.is_available % type,
    rec_id_         in  ref_crosscheck.rec_id % type,
    form_codes_     in  form_code_array,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  procedure ref_read_crosscheck_hst_list(
    id_      in  ref_crosscheck.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  );
  
  procedure ref_insert_crosscheck(
    rec_id_           in  ref_crosscheck.rec_id           % type,                   
    l_src_formula_    in  ref_crosscheck.l_src_formula    % type,
    r_src_formula_    in  ref_crosscheck.r_src_formula    % type,
    l_rel_formula_    in  ref_crosscheck.l_rel_formula    % type,
    r_rel_formula_    in  ref_crosscheck.r_rel_formula    % type,
    l_desc_           in  ref_crosscheck.l_desc           % type,
    r_desc_           in  ref_crosscheck.r_desc           % type,
    l_src_cond_       in  ref_crosscheck.l_src_cond       % type,
    r_src_cond_       in  ref_crosscheck.r_src_cond       % type,
    l_rel_cond_       in  ref_crosscheck.l_rel_cond       % type,
    r_rel_cond_       in  ref_crosscheck.r_rel_cond       % type,
    formula_symbol_   in  ref_crosscheck.formula_symbol   % type,
    cond_symbol_      in  ref_crosscheck.cond_symbol      % type,  	
    crosscheck_type_  in  ref_crosscheck.crosscheck_type  % type,
    num_              in  ref_crosscheck.num              % type,
    is_available_     in  ref_crosscheck.is_available     % type,
    begin_date_       in  ref_crosscheck.begin_date       % type,
    end_date_         in  ref_crosscheck.end_date         % type,
    id_usr_           in  ref_crosscheck.id_usr           % type,    
    user_location_    in  ref_crosscheck.user_location    % type,
    datlast_          in  ref_crosscheck.datlast          % type,
    form_codes_       in  form_code_array,    
    do_commit_        in  integer default 1,
    id_               out ref_crosscheck.id % type,
    err_code          out number,
    err_msg           out varchar2
  );
  
  procedure ref_update_crosscheck(
    id_                  in  ref_crosscheck.id               % type,
    rec_id_              in  ref_crosscheck.rec_id           % type,                    
    l_src_formula_       in  ref_crosscheck.l_src_formula    % type,
    r_src_formula_       in  ref_crosscheck.r_src_formula    % type,
    l_rel_formula_       in  ref_crosscheck.l_rel_formula    % type,
    r_rel_formula_       in  ref_crosscheck.r_rel_formula    % type,
    l_desc_              in  ref_crosscheck.l_desc           % type,
    r_desc_              in  ref_crosscheck.r_desc           % type,
    l_src_cond_          in  ref_crosscheck.l_src_cond       % type,
    r_src_cond_          in  ref_crosscheck.r_src_cond       % type,
    l_rel_cond_          in  ref_crosscheck.l_rel_cond       % type,
    r_rel_cond_          in  ref_crosscheck.r_rel_cond       % type,
    formula_symbol_      in  ref_crosscheck.formula_symbol   % type,
    cond_symbol_         in  ref_crosscheck.cond_symbol      % type,
    crosscheck_type_     in  ref_crosscheck.crosscheck_type  % type,
    num_                 in  ref_crosscheck.num              % type,
    is_available_        in  ref_crosscheck.is_available     % type,
    begin_date_          in  ref_crosscheck.begin_date       % type,
    end_date_            in  ref_crosscheck.end_date         % type,
    id_usr_              in  ref_crosscheck.id_usr           % type,    
    user_location_       in  ref_crosscheck.user_location    % type,
    datlast_             in  ref_crosscheck.datlast          % type,
    form_codes_          in  form_code_array,
    do_commit_           in  integer default 1,     
    err_code             out number,
    err_msg              out varchar2
  );  
  
  procedure ref_delete_crosscheck(
    id_        in  ref_crosscheck.id   % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );
  
  procedure ref_update_crosscheck_forms(
    id_                  in  ref_crosscheck.id % type,
    form_codes_          in  form_code_array,
    do_commit_           in  integer default 1,     
    err_code             out number,
    err_msg              out varchar2
  );
  
    
  /* Правила выходных форм */
  procedure ref_read_reports_rules_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  );
  
  procedure ref_read_rep_rules_l_by_params(    
    id_             in  ref_reports_rules.id % type,
    date_           in  Date,
    form_name_      in  ref_reports_rules.formname % type,
    formula_        in  ref_reports_rules.formula % type,
    rec_id_         in  ref_reports_rules.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,    
    err_code        out number,
    err_msg         out varchar2    
  );
  
  procedure ref_read_reps_rul_hst_list(
    id_      in  ref_reports_rules.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  );
  
  procedure ref_insert_reports_rules(
    rec_id_              in  ref_reports_rules.rec_id              % type,
    code_                in  ref_reports_rules.code                % type,    
    name_kz_             in  ref_reports_rules.name_kz             % type,
    name_ru_             in  ref_reports_rules.name_ru             % type,
    name_en_             in  ref_reports_rules.name_en             % type,                    
    formname_            in  ref_reports_rules.formname            % type,
    fieldname_           in  ref_reports_rules.fieldname           % type,
    formula_             in  ref_reports_rules.formula             % type,
    coeff_               in  ref_reports_rules.coeff               % type,
    condition_           in  ref_reports_rules.condition           % type,
    priority_            in  ref_reports_rules.priority            % type,
    report_type_         in  ref_reports_rules.report_type         % type,
    keyvalue_            in  ref_reports_rules.keyvalue            % type,
    report_kind_         in  ref_reports_rules.report_kind         % type,
    rep_per_dur_months_  in  ref_reports_rules.rep_per_dur_months  % type,
    table_name_          in  ref_reports_rules.table_name          % type,
    begin_date_          in  ref_reports_rules.begin_date          % type,
    end_date_            in  ref_reports_rules.end_date            % type,
    id_usr_              in  ref_reports_rules.id_usr              % type,    
    user_location_       in  ref_reports_rules.user_location       % type,
    datlast_             in  ref_reports_rules.datlast             % type,
    do_commit_           in  integer default 1,   
    id_                  out ref_reports_rules.id % type,
    err_code             out number,
    err_msg              out varchar2
  );
  
  procedure ref_update_reports_rules(
    id_                  in  ref_reports_rules.id                % type,
    rec_id_              in  ref_reports_rules.rec_id            % type,
    code_                in  ref_reports_rules.code              % type,    
    name_kz_             in  ref_reports_rules.name_kz           % type,
    name_ru_             in  ref_reports_rules.name_ru           % type,
    name_en_             in  ref_reports_rules.name_en           % type,                
    formname_            in  ref_reports_rules.formname          % type,
    fieldname_           in  ref_reports_rules.fieldname         % type,
    formula_             in  ref_reports_rules.formula           % type,
    coeff_               in  ref_reports_rules.coeff             % type,
    condition_           in  ref_reports_rules.condition         % type,
    priority_            in  ref_reports_rules.priority          % type,
    report_type_         in  ref_reports_rules.report_type       % type,
    keyvalue_            in  ref_reports_rules.keyvalue          % type,
    report_kind_         in  ref_reports_rules.report_kind       % type,
    rep_per_dur_months_  in  ref_reports_rules.rep_per_dur_months% type,
    table_name_          in  ref_reports_rules.table_name        % type,
    begin_date_          in  ref_reports_rules.begin_date        % type,
    end_date_            in  ref_reports_rules.end_date          % type,
    id_usr_              in  ref_reports_rules.id_usr            % type,    
    user_location_       in  ref_reports_rules.user_location     % type,
    datlast_             in  ref_reports_rules.datlast           % type,
    do_commit_           in  integer default 1,     
    err_code             out number,
    err_msg              out varchar2
  );
  
  procedure ref_delete_reports_rules(
    id_        in  ref_reports_rules.id % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );
  
  
  /* Листинговые оценки */
  procedure ref_read_listing_est_list(    
    date_    in ref_listing_estimation.begin_date % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2    
  );
  
  procedure ref_read_lis_est_l_by_params(    
    id_             in  ref_listing_estimation.id % type,
    date_           in  ref_listing_estimation.begin_date % type,
    name_ru_        in  ref_listing_estimation.name_ru % type,
    rec_id_         in  ref_listing_estimation.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  /* Рейтинговые оценки */
  procedure ref_read_rating_est_list(    
    date_    in ref_rating_estimation.begin_date % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2    
  );
  
  procedure ref_read_rat_est_l_by_params(    
    id_             in  ref_rating_estimation.id % type,
    date_           in  ref_rating_estimation.begin_date % type,
    name_ru_        in  ref_rating_estimation.name_ru % type,
    rat_cat_name_   in  ref_rating_category.name_ru % type,
    rec_id_         in  ref_rating_category.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  /* Категории рейтинговых оценок */
  procedure ref_read_rating_category_list(    
    date_    in ref_rating_category.begin_date % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2    
  );
  
  procedure ref_read_rat_cat_l_by_params(
    id_             in  ref_rating_category.id % type,
    date_           in  ref_rating_category.begin_date % type,
    name_ru_        in  ref_rating_category.name_ru % type,
    code_           in  ref_rating_category.code % type,
    rec_id_         in  ref_rating_category.rec_id % type, 
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  );
  
  /* Справочник МРП*/  
  procedure ref_read_mrp_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2 
  );
  
  procedure ref_read_mrp_list_by_params(
    id_             in  ref_mrp.id      % type,
    date_           in  Date,
    name_ru_        in  ref_mrp.name_ru % type,
    rec_id_         in  ref_mrp.rec_id  % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2 
  );
  
  procedure ref_read_mrp_hst_list( 
    id_       in  ref_mrp.id % type,   
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2 
  );
  
  procedure ref_insert_mrp(
    rec_id_        in  ref_mrp.rec_id        % type,
    code_          in  ref_mrp.code          % type,
    name_kz_       in  ref_mrp.name_kz       % type,
    name_ru_       in  ref_mrp.name_ru       % type,
    name_en_       in  ref_mrp.name_en       % type,
    value_         in  ref_mrp.value         % type,
    begin_date_    in  ref_mrp.begin_date    % type,
    end_date_      in  ref_mrp.end_date      % type,
    id_usr_        in  ref_mrp.id_usr        % type,
    user_location_ in  ref_mrp.user_location % type,
    datlast_       in  ref_mrp.datlast       % type,
    do_commit_     in  Integer default 1,
    id_            out ref_mrp.id            % type,
    Err_Code       out number,
    Err_Msg        out varchar2
  );
  
  procedure ref_update_mrp(
    id_            in  ref_mrp.id            % type,
    rec_id_        in  ref_mrp.rec_id        % type,
    code_          in  ref_mrp.code          % type,
    name_kz_       in  ref_mrp.name_kz       % type,
    name_ru_       in  ref_mrp.name_ru       % type,
    name_en_       in  ref_mrp.name_en       % type,
    value_         in  ref_mrp.value         % type,
    begin_date_    in  ref_mrp.begin_date    % type,
    end_date_      in  ref_mrp.end_date      % type,
    id_usr_        in  ref_mrp.id_usr        % type,
    user_location_ in  ref_mrp.user_location % type,
    datlast_       in  ref_mrp.datlast       % type,
    do_commit_     in  integer default 1,     
    err_code       out number,
    err_msg        out varchar2
  );
    
  procedure ref_delete_mrp(
    id_        in  ref_mrp.id % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );
  
  /* Справочник Реестр МФО*/  
  procedure ref_read_mfo_reg_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2 
  );
  
  procedure ref_read_mfo_reg_list_by_p(
    id_             in  ref_mfo_reg.id      % type,
    date_           in  Date,
    name_ru_        in  ref_mfo_reg.name_ru % type,
    rec_id_         in  ref_mfo_reg.rec_id  % type,
    ref_department_ in  ref_mfo_reg.ref_department % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2 
  );
  
  procedure ref_read_mfo_reg_hst_list( 
    id_       in  ref_mfo_reg.id % type,   
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2 
  );
  
  procedure ref_insert_mfo_reg(
    rec_id_           in  ref_mfo_reg.rec_id           % type,
    code_             in  ref_mfo_reg.code             % type,
    name_kz_          in  ref_mfo_reg.name_kz          % type,
    name_ru_          in  ref_mfo_reg.name_ru          % type,
    name_en_          in  ref_mfo_reg.name_en          % type,
    ref_department_   in  ref_mfo_reg.ref_department   % type,
    ref_legal_person_ in  ref_mfo_reg.ref_legal_person % type,    
    base_             in  ref_mfo_reg.base             % type,
    num_reg_          in  ref_mfo_reg.num_reg          % type,
    fio_manager_      in  ref_mfo_reg.fio_manager      % type,
    address_          in  ref_mfo_reg.address          % type,
    contact_details_  in  ref_mfo_reg.contact_details  % type,    
    begin_date_       in  ref_mfo_reg.begin_date       % type,
    end_date_         in  ref_mfo_reg.end_date         % type,
    id_usr_           in  ref_mfo_reg.id_usr           % type,
    user_location_    in  ref_mfo_reg.user_location    % type,
    datlast_          in  ref_mfo_reg.datlast          % type,
    do_commit_        in  Integer default 1,
    id_               out ref_mfo_reg.id               % type,
    Err_Code          out number,
    Err_Msg           out varchar2
  );
  
  procedure ref_update_mfo_reg(
    id_               in  ref_mfo_reg.id               % type,
    rec_id_           in  ref_mfo_reg.rec_id           % type,
    code_             in  ref_mfo_reg.code             % type,
    name_kz_          in  ref_mfo_reg.name_kz          % type,
    name_ru_          in  ref_mfo_reg.name_ru          % type,
    name_en_          in  ref_mfo_reg.name_en          % type,
    ref_department_   in  ref_mfo_reg.ref_department   % type,
    ref_legal_person_ in  ref_mfo_reg.ref_legal_person % type,    
    base_             in  ref_mfo_reg.base             % type,
    num_reg_          in  ref_mfo_reg.num_reg          % type,
    fio_manager_      in  ref_mfo_reg.fio_manager      % type,
    address_          in  ref_mfo_reg.address          % type,
    contact_details_  in  ref_mfo_reg.contact_details  % type,    
    begin_date_       in  ref_mfo_reg.begin_date       % type,
    end_date_         in  ref_mfo_reg.end_date         % type,
    id_usr_           in  ref_mfo_reg.id_usr           % type,
    user_location_    in  ref_mfo_reg.user_location    % type,
    datlast_          in  ref_mfo_reg.datlast          % type,
    do_commit_        in  integer default 1,     
    err_code          out number,
    err_msg           out varchar2
  );
    
  procedure ref_delete_mfo_reg(
    id_        in  ref_mfo_reg.id % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  );
  
  /* Справочник балансовых счетов для отчетов о сделках*/  
  procedure ref_read_deal_balance_acc_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2 
  );
  
  procedure ref_read_deal_ba_l_by_params(
    id_             in  ref_deal_balance_acc.id      % type,
    date_           in  Date,
    name_ru_        in  ref_deal_balance_acc.name_ru % type,
    rec_id_         in  ref_deal_balance_acc.rec_id  % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2 
  );
  
  procedure ref_read_deal_ba_hst_list( 
    id_       in  ref_deal_balance_acc.id % type,   
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2 
  );
  
  procedure ref_insert_deal_balance_acc(
    rec_id_        in  ref_deal_balance_acc.rec_id        % type,
    code_          in  ref_deal_balance_acc.code          % type,
    name_kz_       in  ref_deal_balance_acc.name_kz       % type,
    name_ru_       in  ref_deal_balance_acc.name_ru       % type,
    name_en_       in  ref_deal_balance_acc.name_en       % type,
    short_name_kz_ in  ref_deal_balance_acc.name_kz       % type,
    short_name_ru_ in  ref_deal_balance_acc.name_ru       % type,
    short_name_en_ in  ref_deal_balance_acc.name_en       % type,
    num_acc_	     in  ref_deal_balance_acc.num_acc       % type,
    begin_date_    in  ref_deal_balance_acc.begin_date    % type,
    end_date_      in  ref_deal_balance_acc.end_date      % type,
    id_usr_        in  ref_deal_balance_acc.id_usr        % type,
    user_location_ in  ref_deal_balance_acc.user_location % type,
    datlast_       in  ref_deal_balance_acc.datlast       % type,
    do_commit_     in  Integer default 1,
    id_            out ref_deal_balance_acc.id            % type,
    Err_Code       out number,
    Err_Msg        out varchar2
  );
  
  procedure ref_update_deal_balance_acc(
    id_            in  ref_deal_balance_acc.id            % type,
    rec_id_        in  ref_deal_balance_acc.rec_id        % type,
    code_          in  ref_deal_balance_acc.code          % type,
    name_kz_       in  ref_deal_balance_acc.name_kz       % type,
    name_ru_       in  ref_deal_balance_acc.name_ru       % type,
    name_en_       in  ref_deal_balance_acc.name_en       % type,
    short_name_kz_ in  ref_deal_balance_acc.name_kz       % type,
    short_name_ru_ in  ref_deal_balance_acc.name_ru       % type,
    short_name_en_ in  ref_deal_balance_acc.name_en       % type,
    num_acc_	     in  ref_deal_balance_acc.num_acc       % type,
    begin_date_    in  ref_deal_balance_acc.begin_date    % type,
    end_date_      in  ref_deal_balance_acc.end_date      % type,
    id_usr_        in  ref_deal_balance_acc.id_usr        % type,
    user_location_ in  ref_deal_balance_acc.user_location % type,
    datlast_       in  ref_deal_balance_acc.datlast       % type,
    do_commit_     in  integer default 1,
    err_code       out number,
    err_msg        out varchar2
  );
    
  procedure ref_delete_deal_balance_acc(
    id_        in  ref_deal_balance_acc.id % type,
    do_commit_ in  integer default 1,
    err_code   out number,
    err_msg    out varchar2 
  );
  
  
end PKG_FRSI_REF;
/
create or replace package body PKG_FRSI_REF is

  -- Author  : AYUPOV.BAKHTIYAR
  -- Created : 15.04.2015 18:27:04
  -- Новый портлет для работы со справочниками ФРСП

  function ref_have_right(
    ref_code_ in ref_main.code   % type,
    user_id_  in f_users.user_id % type,
    type_     in varchar2
  )return number 
  is
    cnt number;
    code varchar2(255);
  begin       
    
    code := 'SU:REF:' || ref_code_ || ':' || type_;
    
    execute immediate
      'select count(*) ' ||
        'from f_session_right_items sri ' ||
       'where sri.user_id = :user_id_ ' ||
         'and exists (select ri.id ' ||
                       'from right_items ri ' ||
                     'where upper(ri.name) = upper(:code) ' ||
                       'and ri.id = sri.right_item_id)'
    into cnt
    using user_id_,code;
    
    return(cnt);
  exception
    when others then
      return 0;
  end;
  
  function ref_check_code(
    ref_code_ in ref_main.code     % type,
    rec_id_   in ref_main.id       % type,
    code_     in ref_main.code     % type
  )return number 
  is
    cnt number;
  begin       
    execute immediate
      'select count(*)       
         from v_' || ref_code_ || '
        where rec_id != nvl(:rec_id_,0)
          and trim(code) = trim(:code_)'
          
    into cnt
    using rec_id_, code_;
    
     return (cnt);
  end;
  
  function ref_check_name(
    ref_code_ in ref_main.code     % type,
    rec_id_   in ref_main.id       % type,
    name_     in ref_main.name     % type
  )return number 
  is
    cnt number;
  begin       
    execute immediate
      'select count(*)       
         from v_' || ref_code_ || '
        where rec_id != nvl(:rec_id_,0)
          and trim(name_ru) = trim(:name_)'
    into cnt
    using rec_id_, name_;
    
     return (cnt);
  end;
  
  function ref_check_rr_param(
    rec_id_    in ref_reports_rules.rec_id % type,
    formname_  in ref_reports_rules.formname % type,
    fieldname_ in ref_reports_rules.fieldname % type
  ) return number
  is
    cnt number;
  begin
    execute immediate
      'select count(*)       
         from v_ref_reports_rules 
        where rec_id != nvl(:rec_id_,0)
          and trim(formname) = trim(:formname_)
          and trim(fieldname) = trim(:fieldname_)'
    into cnt
    using rec_id_, formname_, fieldname_;
    
     return (cnt);
    
  end; 
       
  function ref_check_date(
    ref_code_   in ref_main.code     % type,
    id_         in ref_main.id       % type,
    rec_id_     in ref_main.id       % type,
    begin_date_ in Date default null
  )return number
  is
    cnt number;
  begin
    execute immediate
      'select count(*)
         from v_' || ref_code_ || '
        where rec_id = nvl(:rec_id_,0)
          and id != nvl(:id_,0)
          and begin_date = :begin_date_'
    into cnt
    using rec_id_, id_, begin_date_;
    
     return (cnt);
  end;
  
  function ref_check_end_date(
    ref_code_   in ref_main.code % type,
    id_         in ref_main.id   % type,
    rec_id_     in ref_main.id   % type,
    b_date_     in date,
    e_date_     in date
  )return number
  is
    cnt number;
    v_b_date date := null;
  begin
    execute immediate      
    ' select max(begin_date)
        from v_' || ref_code_ || ' t
       where rec_id = nvl(:rec_id_,0)
         and id != nvl(:id_,0)'
         
    into v_b_date
    using rec_id_, id_;

    if (v_b_date is not null) and (b_date_ < v_b_date) and (e_date_ is null) then
      return 1;
    end if;

    execute immediate
      'select count(*)
         from v_' || ref_code_ || ' t
        where rec_id = nvl(:rec_id_,0)
          and ((begin_date <> nvl(:v_b_date,begin_date) and begin_date > :b_date_) or (:b_date_ > begin_date))
          and id != nvl(:id_,0)
          and end_date is null'

    into cnt
    using rec_id_, v_b_date, b_date_, b_date_, id_;

     return (cnt);
  end;
  
  function ref_check_date_period(
    ref_code_   in ref_main.code % type,
    id_         in ref_main.id   % type,
    rec_id_     in ref_main.id   % type,
    begin_date_ in Date default null,
    end_date_   in Date default null
  )return number
  is
    cnt number;
    b_date Date;
    e_date Date;
  begin
    b_date := to_date('01.01.1900','dd.mm.yyyy');
    e_date := to_date('01.01.3333','dd.mm.yyyy');
    execute immediate
      'select count(*)       
         from v_' || ref_code_ || '
        where rec_id = nvl(:rec_id_,0)
          and id != nvl(:id_,0)
          and begin_date < nvl(:end_date_,:e_date)
          and nvl(end_date,:e_date) > nvl(:begin_date_, :b_date)'
          
    into cnt
    using rec_id_, id_, end_date_, e_date, e_date, begin_date_,b_date;

     return (cnt);
  end;  
  
  function ref_get_sent_sts(
    ref_code_ in ref_main.code % type,
    sent_knd_ in number
  )return number
  is
    cnt number;
  begin
    execute immediate
      'select count(*)' ||
       ' from v_' || ref_code_ ||
      ' where sent_knd = :sent_knd_ '
    into cnt
    using sent_knd_;
    
     return (cnt);        
  end;
  
  function ref_check_idn(
    ref_code_ in ref_main.code % type,
    idn_      in varchar2,
    rec_id_   in number
  )return number
  is
    cnt number;
  begin
    execute immediate
      ' select count(*) ' ||
        ' from v_' || ref_code_ ||
       ' where rec_id != nvl(:rec_id_,0) ' ||
         ' and idn = :idn_'
    into cnt
    using rec_id_,idn_;
    
     return (cnt);        
  end;
  
  function ref_check_respondent(    
    ref_legal_person_ in varchar2,
    rec_id_           in number
  )return number
  is
    cnt number;
  begin
    execute immediate
      'select count(*) ' ||
        'from v_ref_respondent ' ||
       'where rec_id != nvl(:rec_id_,0) ' ||
         'and ref_legal_person = :ref_legal_person_'
    into cnt
    using rec_id_,ref_legal_person_;
    
     return (cnt);        
  end;
  
  function ref_check_respondent_idn(
    ref_legal_person_ in varchar2
  )return number
  is
    cnt number;
  begin
    execute immediate
      'select count(*) ' ||
        'from v_ref_legal_person ' ||
       'where id = :ref_legal_person_ ' ||
         'and trim(idn) is null'
    into cnt
    using ref_legal_person_;
    
     return (cnt);        
  end;
    
  function ref_check_record(
    table_name_  in varchar2,
    column_name_ in varchar2,
    id_          in varchar2
  )return number
  is
    cnt number;
  begin
    execute immediate
      'select count(*) ' ||
        'from v_' || table_name_ || ' ' ||
       'where ' || column_name_ || ' = :id_ '         
    into cnt
    using id_;
    
    return (cnt);        
  end;  
  
  function ref_check_year(
    table_name_ in varchar2,
    begin_date_ in date,
    rec_id_     in number
  ) return number
  is 
    cnt number;
  begin
    execute immediate
      'select count(*) ' ||
        'from v_' || table_name_  || ' ' ||
       'where rec_id != nvl(:rec_id_,0) ' ||
         'and trunc(begin_date,''yyyy'') = trunc(:begin_date_,''yyyy'')'
    into cnt
    using rec_id_,begin_date_;
    
    return (cnt);    
  end;
  
  function ref_get_entity_id(    
    ref_code_   in varchar2,
    id_         in number    
  )return number
  is
    entity_id     number;
    v_rec_id_     number;
    v_begin_date_ date;
  begin
    execute immediate
      'select rec_id, ' ||
             'begin_date ' ||
        'from v_' ||  ref_code_ || ' ' ||
       'where id = :id_'
       
    into v_rec_id_, v_begin_date_
    using id_;
       
    execute immediate      
      'select e.id as entity_id ' ||
        'from usci.eav_m_classes c, ' ||
             'usci.eav_be_entities e ' ||
       'where c.id = e.class_id ' ||
         'and c.name = :ref_code_ ' ||
         ' and pkg_eav_util.get_integer_value(e.id, ''rec_id'',:v_begin_date_) = :v_rec_id_'

    into entity_id
    using ref_code_,v_begin_date_,v_rec_id_;
    
     return (entity_id);
  exception
    when others then
      return 0;
  end;
    
  function ref_update_end_date(
    ref_code_   in ref_main.code % type,    
    rec_id_     in ref_main.id   % type,     
    b_date_     in date
  )return number
  is
    v_id ref_main.id % type;
  begin
    begin
      execute immediate
      ' select t.id 
         from v_' || ref_code_ || ' t
         where t.rec_id = :rec_id_         
           and t.begin_date = (select max(t1.begin_date) 
                                from v_' || ref_code_ || ' t1
                               where t1.rec_id = :rec_id_
                                 and t1.begin_date <= :b_date_                               
                                 and t1.end_date is null)'

      into v_id
      
      using rec_id_, rec_id_, b_date_;
    
    exception
      when NO_DATA_FOUND then
        return 0;
    end;
        
    execute immediate
      'update ' || ref_code_ ||
        ' set end_date = :b_date_
        where id = :v_id_'
    
    using b_date_, v_id;
    
    return 0;
  end;
  
  procedure report_ref_link_check(
    rec_id_     in ref_main.id % type,
    id_         in ref_main.id % type,    
    ref_code_   in ref_main.code % type,
    -- begin_date_ in Date,
    -- end_date_   in Date,
    -- kind_event_ in varchar2,
    Err_Code    out number, 
    Err_Msg     out varchar2
  )
  is
    v_report_history report_history.id % type;
    v_form_name      form_history.name % type;
    v_report_date    reports.report_date % type;
    v_rec_id         ref_main.id % type;
    v_end_date       date;
  begin
    Err_Code := 0;
    Err_Msg := ' ';
    
    if ref_code_ is null then
      Err_Code := 550;
      Err_Msg := ' Не задан ref_code!';
      raise E_Force_Exit;
    end if;
    
    if rec_id_ is not null then  
      v_rec_id := rec_id_;
    else       
      v_rec_id := ref_get_rec_id(ref_code_,id_);
    end if;
    
    if v_rec_id is null then
      Err_Code := 550;
      Err_Msg := ' Не задан rec_id!';
      raise E_Force_Exit;
    end if;
    
    for Rec in (select t2.form_code,
                       t2.report_date,
                       t2.idn
                  from report_ref_link t,
                       report_history t1,
                       reports t2
                 where t.rec_id = v_rec_id
                   and t.ref_code = ref_code_
                   and t.delfl = 0
                   and t.report_history = t1.id
                   and t1.report_id = t2.id
                   -- and t2.report_date between begin_date_ and nvl(end_date_, t2.report_date)
                   and rownum = 1)
    loop         
      Err_Code := 550;
      Err_Msg := 'Ошибка удаления! Запись используется в форме с кодом: ' || Rec.Form_Code ||
                 ' за отчетную дату ' || To_Char(Rec.Report_Date, 'dd.mm.yyyy') || ' БИН организаций ' || Rec.Idn;
      raise E_Force_Exit;
    end loop;
  exception 
   when E_Force_Exit then
      null;
    when others then
      Err_Code := 777;
      Err_Msg := 'Ошибка проверки данных!';    
  end;
  
  function ref_get_rec_id(
    ref_code_ in ref_main.code % type,
    id_        in ref_main.id % type
    ) return number
  is
    v_rec_id ref_main.id % type;
  begin
    execute immediate
      'select rec_id
         from ' || ref_code_ ||
        ' where id = :id_
          and delfl = 0'
     into v_rec_id
    using id_;
    
    return v_rec_id;
  end;
  
  function get_condition (
    cond_symbol_ ref_crosscheck.formula_symbol % type
    ) return varchar2
  is  
  begin
      if cond_symbol_ = 'EQ' then 
        return '='; 
      elsif cond_symbol_ = 'NE' then 
        return '!='; 
      elsif cond_symbol_ = 'LT' then 
        return '<'; 
      elsif cond_symbol_ = 'GT' then 
        return '>'; 
      elsif cond_symbol_ = 'LE' then 
        return '<='; 
      elsif cond_symbol_ = 'GE' then 
        return '>='; 
      else
        return '';
      end if;
  end;
  
  /* Список справочников */  
  procedure read_ref_list(
    user_id_ in  f_users.user_id  % type,
    ref_knd_ in  ref_main.ref_knd % type,
    name_    in  ref_main.name    % type,
    code_    in  ref_main.code    % type,
    Cur      out sys_refcursor,
    Err_Code out number,
    Err_Msg  out varchar2 
  )
  is  
    ProcName constant varchar2(50) := 'PKG_FRSI_REF.READ_REF_LIST';
    v_name   ref_main.name % type;
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    v_name := '%' || name_ || '%';    
    
    Open Cur for
      select t.ID,
             t.NAME,
             t.CODE,                          
             t.REF_KND,
             k.name as REF_KND_NAME,
             t.DATE_LOAD,
             t.STS_LOAD,
             ref_get_sent_sts(t.CODE, 0) CNT_NOT_SENT,             
             ref_get_sent_sts(t.CODE, 1) CNT_WAIT
        from REF_MAIN t,
             REF_KND k
       where ref_have_right(t.code,user_id_,'view') > 0
         and t.ref_knd = k.id
         and (ref_knd_ is null or t.ref_knd in (ref_knd_,decode(ref_knd_,1,4,2,4)))
         and (trim(name_) is null or upper(t.name) like upper(v_name))
         and (trim(code_) is null or upper(t.code) like upper(code_))
       order by NAME;
        
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback; 
  end read_ref_list;
  
  procedure ref_read_simple_list_by_params(
    ref_code_       in  ref_main.code % type,
    date_           in  date,
    id_             in  number,
    rec_id_         in  number,    
    name_ru_        in  varchar2,
    code_           in  varchar2,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    Err_Code        out number,
    Err_Msg         out varchar2
  )
  is
    ProcName     constant varchar2(50) := 'PKG_FRSI_REF.REF_READ_SIMPLE_LIST_BY_PARAMS';
    v_ref_code   ref_main.code % type;
    Sql_Text     VarChar2(12000);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_ref_code := 'v_' || ref_code_;
    Sql_Text :=  'select t.id,
                         t.rec_id,
                         t.code,
                         t.name_kz,
                         t.name_ru,
                         t.name_en,
                         t.begin_date,
                         t.end_date,
                         t.datlast,
                         t.id_usr,
                         t.user_location,
                         u.last_name || '' '' || u.first_name || '' '' || u.middle_name as USER_NAME,
                         sk.name as sent_knd
                    from '  || v_ref_code || ' t,
                         f_users u,
                         sent_knd sk
                   where t.id_usr = u.user_id 
                     and t.delfl = 0                     
                     and t.sent_knd = sk.sent_knd';
                     
    if name_ru_ is not null then
      Sql_Text := Sql_Text || ' and t.rec_id = (select max(t2.rec_id)
                                                 from ' || v_ref_code || ' t2
                                                where (((' || search_all_ver_ || ' = 1) and
                                                          (upper(t2.name_ru) like upper(trim(''%' || name_ru_ || '%'')))) or 
                                                       ((' || search_all_ver_ || ' is null or ' || search_all_ver_ || ' = 0) and
                                                          (upper(t.name_ru) like upper(trim(''%' || name_ru_ || '%''))))
                                                      )
                                                  and t2.rec_id = t.rec_id
                                                )';      
    end if;

    if date_ is not null then
      Sql_Text := Sql_Text || ' and (t.begin_date = (select max(t1.begin_date) 
                                                     from ' || v_ref_code || ' t1 
                                                    where t1.rec_id = t.rec_id 
                                                      and t1.begin_date <= ''' || date_ || '''))';
    end if;

    if id_ is not null then
      Sql_Text := Sql_Text || ' and t.id = ' || id_;
    end if;
    
    if rec_id_ is not null then
      Sql_Text := Sql_Text || ' and t.rec_id = ' || rec_id_;
    end if;
    
    /*if trim(name_ru_) is not null then
      Sql_Text := Sql_Text || ' and upper(t.name_ru) like upper(trim(''%' || name_ru_ || '%'')) ';
    end if;*/
    
    if trim(code_) is not null then
      Sql_Text := Sql_Text || ' and upper(t.code) like upper(trim(''' || code_ || ''')) ' ;
    end if;
    
    Sql_Text := Sql_Text || ' order by t.id';
    open Cur for Sql_Text;
           
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' Ошибка получения курсора ' || ref_code_ ;
      
      open Cur for
        select null from dual;
      rollback; 
  end;
  
  procedure ref_read_simple_hst_list(
    ref_code_    in ref_main.code % type,    
    id_          in number,    
    Cur          out sys_refcursor,
    Err_Code     out number,
    Err_Msg      out varchar2
  )
  is
    ProcName     constant varchar2(50) := 'PKG_FRSI_REF.REF_READ_SIMPLE_HST_LIST';
    Sql_Text     VarChar2(12000);
    v_ref_code   ref_main.code % type;
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_ref_code := ref_code_ || '_hst';
    Sql_Text :=  'select t.id_hst,
                         t.id,                         
                         t.rec_id,
                         t.code,
                         t.name_kz,
                         t.name_ru,
                         t.name_en,
                         t.begin_date,
                         t.end_date,
                         t.datlast,
                         t.id_usr,
                         t.user_location,
                         u.last_name || '' '' || u.first_name || '' '' || u.middle_name as USER_NAME,
                         sk.name as sent_knd,
                         t.type_change,
                         tc.name as type_change_name
                    from '  || v_ref_code || ' t,
                         f_users u,
                         sent_knd sk,
                         type_change tc
                   where t.id_usr = u.user_id
                     and t.sent_knd = sk.sent_knd 
                     and t.type_change = tc.type_change
                     and t.id = ' || id_ || ' ' ||
                    'order by t.id_hst';

    open Cur for Sql_Text;

  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' Ошибка получения курсора ' || v_ref_code;
      
      open Cur for
        select null from dual;
      rollback; 
  end;
  
  procedure ref_insert_simple(
    ref_code_      in  varchar2,
    rec_id_        in  number,
    code_          in  varchar2,
    name_kz_       in  varchar2,
    name_ru_       in  varchar2,
    name_en_       in  varchar2,
    begin_date_    in  date,
    end_date_      in  date,
    id_usr_        in  number,
    user_location_ in  varchar2,
    datlast_       in  date,
    do_commit_     in  Integer default 1,
    id_            out number,
    Err_Code       out number,
    Err_Msg        out varchar2
  )
  is
    ProcName     constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_SIMPLE';    
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    if (trim(name_ru_) is null) or
       (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
                   
    if ref_check_name(ref_code_,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code_,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;    
    
    if rec_id_ is not null then
      if ref_update_end_date(ref_code_,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 05 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code_,null,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code_,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 07 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
           
    if code_ is not null then
      if ref_check_code(ref_code_,rec_id_,code_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Уже имеется запись с данным кодом!';
        raise E_Force_Exit;
      end if;
    end if;
    
    execute immediate
    'select seq_' || ref_code_ || '_id.nextval' || ' ' ||
      'from dual'
    into id_;    
    
    execute immediate
      'insert into ' || ref_code_ || ' ' ||
       '(id,
        rec_id,
        code,
        name_kz,
        name_ru,
        name_en,            
        begin_date,
        end_date,
        id_usr,
        user_location,
        datlast
      )
      values (
        :id_,
        nvl(:rec_id_,:id_),
        :code_,
        :name_kz_,
        :name_ru_,
        :name_en_,      
        :begin_date_,
        :end_date_,
        :id_usr_,
        :user_location_,
        :datlast_)'
    
     using id_,rec_id_,id_,nvl(code_,id_),name_kz_,name_ru_,name_en_,begin_date_,end_date_,id_usr_,user_location_,datlast_;
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи ' || ref_code_;
  end;
  
  procedure ref_update_simple(
    ref_code_      in  varchar2,
    id_            in  number,
    rec_id_        in  number,
    code_          in  varchar2,
    name_kz_       in  varchar2,
    name_ru_       in  varchar2,
    name_en_       in  varchar2,
    begin_date_    in  date,
    end_date_      in  date,
    id_usr_        in  number,
    user_location_ in  varchar2,
    datlast_       in  date,
    do_commit_     in  Integer default 1,
    Err_Code       out number,
    Err_Msg        out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_SIMPLE';
    v_have_chg        boolean;
    v_ref_data        ref_org_type % rowtype;
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_have_chg := false;
    
    if (trim(name_ru_) is null) or
       (begin_date_ is null) or
       (trim(code_) is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_code(ref_code_,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_name(ref_code_,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code_,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code_,id_,rec_id_,begin_date_, end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code_,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;    
    
    /*report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code_,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;*/
            
    execute immediate
      'select * ' ||
        'from ' || ref_code_ ||
       ' where id = :id_'           
    into v_ref_data
    using id_;
    
    if v_ref_data.id <> 0 then
      if nvl(v_ref_data.code,' ') <> nvl(code_,' ') or
         nvl(v_ref_data.name_kz,' ') <> nvl(name_kz_,' ') or 
         nvl(v_ref_data.name_ru,' ') <> nvl(name_ru_,' ') or
         nvl(v_ref_data.name_en,' ') <> nvl(name_en_,' ') or         
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) then
         
        v_have_chg := true;
        
      end if;
    end if;
    
    if v_have_chg = true then
      execute immediate
        'update ' || ref_code_ || ' ' ||
           'set code = :code_,
               name_kz = :name_kz_,
               name_ru = :name_ru_,
               name_en = :name_en_,
               user_location = :user_location_,
               begin_date = :begin_date_,
               end_date = :end_date_,
               id_usr = :id_usr_,
               sent_knd = 0,
               datlast = :datlast_
         where id = :id_'

         using code_,name_kz_,name_ru_,name_en_,user_location_,begin_date_,end_date_,id_usr_,datlast_,id_;

      if do_commit_ = 1 then
        Commit;
      end if;
    end if;

  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи ' || ref_code_;
  end;
    
  procedure ref_delete_simple(
    ref_code_	 in  ref_main.code % type,
    id_        in  number,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_SIMPLE';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code_,
                          /*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
    
    if ref_code_ like 'ref_org_type' then    
      if ref_check_record('ref_legal_person','ref_org_type', id_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется дочерняя запись!';
        raise E_Force_Exit;
      end if;
    end if;
                         
    execute immediate 
      'update ' || ref_code_ || ' ' || 
         'set delfl = 1,
              sent_knd = 0
       where id = :id_'
       using id_;
       
    if do_commit_ = 1 then
      Commit;
    end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи ' || ref_code_;
  end;
  
  /* Справочник должностей */
  procedure ref_read_post_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    Err_Code  out number,
    Err_Msg   out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_POST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select p.id,
             p.rec_id,
             p.code,
             p.name_kz,
             p.name_ru,
             p.name_en,
             p.type_post as type_post_id,
             tp.name as type_post_name,             
             p.is_activity,
             p.is_main_ruk,
             p.begin_date,
             p.end_date,
             p.datlast,
             p.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             p.user_location,
             sk.name as sent_knd
        from v_ref_post p,
             f_users u,
             sent_knd sk,
             type_post tp
       where p.id_usr = u.user_id
         and p.sent_knd = sk.sent_knd
         and p.type_post = tp.id         
         and (date_ is null or p.begin_date = (select max(t.begin_date)
                                                 from v_ref_post t
                                                where t.rec_id = p.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (p.end_date is null or p.end_date > date_))
       order by p.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_post_list;
  
  procedure ref_read_post_list_by_params(
    id_             in  ref_post.id % type,
    date_           in  Date,
    name_ru_        in  ref_post.name_ru % type,
    rec_id_         in  ref_post.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    Err_Code        out number,
    Err_Msg         out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_POST_LIST_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    Open Cur for
      select p.id,
             p.rec_id,
             p.code,
             p.name_kz,
             p.name_ru,
             p.name_en,
             p.type_post as type_post_id,
             tp.name as type_post_name,             
             p.is_activity,
             p.is_main_ruk,
             p.begin_date,
             p.end_date,
             p.datlast,
             p.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             p.user_location,
             sk.name as sent_knd
        from v_ref_post p,
             f_users u,
             sent_knd sk,
             type_post tp
       where p.id_usr = u.user_id
         and p.sent_knd = sk.sent_knd
         and p.type_post = tp.id
         and p.rec_id = (select max(t1.rec_id)
                          from v_ref_post t1
                         where (((search_all_ver_ = 1) and 
                                   (name_ru_ is null or upper(t1.name_ru) like upper(trim(v_name)))) or 
                                ((search_all_ver_ is null or search_all_ver_ = 0) and 
                                   (name_ru_ is null or upper(p.name_ru) like upper(trim(v_name))))
                               )
                           and t1.rec_id = p.rec_id
                         )
         and (id_ is null or p.id = id_)
--         and (trim(name_ru_) is null or upper(p.name_ru) like upper(trim(v_name)))
         and (rec_id_ is null or p.rec_id = rec_id_)
         and (date_ is null or p.begin_date = (select max(t.begin_date)
                                                 from v_ref_post t
                                                where t.rec_id = p.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (p.end_date is null or p.end_date > date_))
       order by p.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_post_list_by_params;
  
  
  procedure ref_read_post_hst_list(
    id_       in  ref_post.id % type,
    Cur       out sys_refcursor,
    Err_Code  out number,
    Err_Msg   out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_POST_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select ph.id_hst,
             ph.id,
             ph.rec_id,
             ph.code,
             ph.name_kz,
             ph.name_ru,
             ph.name_en,
             ph.type_post as type_post_id,
             tp.name as type_post_name,
             ph.is_activity,
             ph.is_main_ruk,
             ph.begin_date,
             ph.end_date,
             ph.datlast,
             ph.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             ph.type_change,
             tc.name as type_change_name,
             ph.user_location,
             sk.name as sent_knd
        from ref_post_hst ph,
             f_users u,
             type_change tc,
             sent_knd sk,
             type_post tp
       where ph.id_usr = u.user_id         
         and ph.sent_knd = sk.sent_knd
         and ph.type_change = tc.type_change
         and ph.type_post = tp.id
         and ph.id = id_
       order by ph.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_post_hst_list;
  
  
  procedure ref_insert_post(
    rec_id_        in  ref_post.rec_id        % type,
    code_          in  ref_post.code          % type,
    name_kz_       in  ref_post.name_kz       % type,
    name_ru_       in  ref_post.name_ru       % type,
    name_en_       in  ref_post.name_en       % type,
    type_post_id_  in  ref_post.type_post     % type,
    is_activity_   in  ref_post.is_activity   % type,
    is_main_ruk_   in  ref_post.is_main_ruk   % type,        
    begin_date_    in  ref_post.begin_date    % type,
    end_date_      in  ref_post.end_date      % type,
    id_usr_        in  ref_post.id_usr        % type,
    user_location_ in  ref_post.user_location % type,
    datlast_       in  ref_post.datlast       % type,
    do_commit_     in  Integer default 1,
    id_            out ref_post.id            % type,
    Err_Code       out number,
    Err_Msg        out varchar2
  )
  is
    ProcName     constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_POST';
    ref_code     Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_post';
    
    if /*(trim(code_) is null) or*/ (trim(name_ru_) is null) or (begin_date_ is null)
      or (type_post_id_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
                
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if; */
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;    
    
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,null,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
       
    id_ := seq_ref_post_id.nextval;
    
    insert into ref_post(
      id,
      rec_id,
      code,
      name_kz,
      name_ru,
      name_en,
      type_post,
      is_activity, 
      is_main_ruk,      
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),
      nvl(rec_id_,id_),--code_,
      name_kz_,
      name_ru_,
      name_en_,
      type_post_id_,
      nvl(is_activity_,1),
      nvl(is_main_ruk_,0),
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_
    );
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';   
  end ref_insert_post;
  
  
  procedure ref_update_post(
    id_            in  ref_post.id             % type,
    rec_id_        in  ref_post.rec_id        % type,
    code_          in  ref_post.code          % type,
    name_kz_       in  ref_post.name_kz       % type,
    name_ru_       in  ref_post.name_ru       % type,
    name_en_       in  ref_post.name_en       % type,  
    type_post_id_  in  ref_post.type_post     % type,
    is_activity_   in  ref_post.is_activity   % type,
    is_main_ruk_   in  ref_post.is_main_ruk   % type,        
    begin_date_    in  ref_post.begin_date    % type,
    end_date_      in  ref_post.end_date      % type,
    id_usr_        in  ref_post.id_usr        % type,
    user_location_ in  ref_post.user_location % type,
    datlast_       in  ref_post.datlast       % type,
    do_commit_     in  integer default 1,     
    err_code       out number,
    err_msg        out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_POST';
    v_have_chg           boolean;
    v_ref_data           ref_post %rowtype;
    ref_code             Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_post';
    v_have_chg := false;
    
    
    if (trim(name_ru_) is null) or (begin_date_ is null) or 
       (trim(code_) is null) or (type_post_id_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;*/
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_, end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    /*report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;*/
    
    begin
      select *
        into v_ref_data
        from ref_post
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;
    
    if v_ref_data.id <> 0 then
      if nvl(v_ref_data.name_kz,' ') <> nvl(name_kz_,' ') or 
         nvl(v_ref_data.name_ru,' ') <> nvl(name_ru_,' ') or
         nvl(v_ref_data.name_en,' ') <> nvl(name_en_,' ') or
         nvl(v_ref_data.type_post,0) <> nvl(type_post_id_,0) or
         nvl(v_ref_data.is_activity,0) <> nvl(is_activity_,0) or
         nvl(v_ref_data.is_main_ruk,0) <> nvl(is_main_ruk_,0) or
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) then
         
        v_have_chg := true;
      end if;
    end if;
    
    if v_have_chg = true then
      update ref_post 
         set --code          = code_,
             name_kz       = name_kz_,
             name_ru       = name_ru_,
             name_en       = name_en_,
             type_post     = type_post_id_,
             is_activity   = is_activity_,
             is_main_ruk   = is_main_ruk_,           
             user_location = user_location_,
             begin_date    = begin_date_,
             end_date      = end_date_,
             id_usr        = id_usr_,
             sent_knd      = 0,
             datlast       = datlast_
       where id = id_;
             
      if do_commit_ = 1 then
        Commit;
      end if;
    end if;
    
  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';  
  end ref_update_post;
  
  
  procedure ref_delete_post(
    id_        in  ref_post.id % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_POST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    if ref_check_record('ref_managers','ref_post', id_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется дочерняя запись!';
        raise E_Force_Exit;
    end if;
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_post',
                          /*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
  
    update ref_post 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_post;
  
  procedure ref_read_simple_post_lis(
    type_post_ in ref_post.type_post % type,
    date_      in ref_post.begin_date % type,
    Cur        out sys_refcursor,
    err_code   out number,
    err_msg    out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_SIMPLE_POST_LIS';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select p.id,
             p.rec_id,
             p.code,
             p.name_kz,
             p.name_ru,
             p.name_en,
             p.type_post as type_post_id,
             tp.name as type_post_name,
             p.is_activity,
             p.is_main_ruk,
             p.begin_date,
             p.end_date,
             p.datlast,
             p.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             p.user_location,
             sk.name as sent_knd
        from v_ref_post p,
             f_users u,
             sent_knd sk,
             type_post tp
       where p.id_usr = u.user_id
         and p.sent_knd = sk.sent_knd
         and (type_post_ is null or p.type_post = type_post_)
         and p.type_post = tp.id
         and p.begin_date = (select max(ps.begin_date)
                               from v_ref_post ps
                              where ps.rec_id = p.rec_id
                                and ps.begin_date <= nvl(date_,sysdate))
         and (date_ is null or (p.end_date is null or p.end_date > date_))         
       order by p.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_simple_post_lis;
  
  /* Справочник физических лиц */ 
  
  procedure ref_read_person_list(
    date_     in  Date, 
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_PERSON_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select p.id,
             p.rec_id,
             p.code,
             p.idn,
             p.fm,
             p.nm,
             p.ft,
             p.fio_kz,
             p.fio_en,
             p.ref_country,
             c.rec_id as REF_COUNTRY_REC_ID,
             c.name_ru as country_name,
             p.phone_work,
             p.fax,
             p.address_work,
             p.note,
             p.begin_date,
             p.end_date,
             p.datlast,
             p.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             p.user_location,
             sk.name as sent_knd
        from v_ref_person p,
             f_users u,
             v_ref_country c,
             sent_knd sk
       where p.id_usr = u.user_id
         and p.ref_country = c.id
         and p.sent_knd = sk.sent_knd         
         and (date_ is null or p.begin_date = (select max(t.begin_date)
                                                 from v_ref_person t
                                                where t.rec_id = p.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (p.end_date is null or p.end_date > date_))
       order by p.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_person_list;
  
  procedure ref_read_person_list_by_params(
    id_             in  ref_person.id % type,
    date_           in  Date, 
    idn_            in  ref_person.idn % type,
    fm_             in  ref_person.fm % type,
    nm_             in  ref_person.nm % type,
    ft_             in  ref_person.ft % type,
    rec_id_         in  ref_person.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_PERSON_LIST_BY_PARAMS';    
    v_fm varchar2(255);
    v_nm varchar2(255);
    v_ft varchar2(255);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';   
    v_fm := '%' || fm_ || '%';
    v_nm := '%' || nm_ || '%';
    v_ft := '%' || ft_ || '%'; 
    
    Open Cur for
      select p.id,
             p.rec_id,
             p.code,
             p.idn,
             p.fm,
             p.nm,
             p.ft,
             p.fio_kz,
             p.fio_en,
             p.ref_country,
             c.rec_id as REF_COUNTRY_REC_ID,
             c.name_ru as country_name,
             p.phone_work,
             p.fax,
             p.address_work,
             p.note,
             p.begin_date,
             p.end_date,
             p.datlast,
             p.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             p.user_location,
             sk.name as sent_knd
        from v_ref_person p,
             f_users u,
             v_ref_country c,
             sent_knd sk
       where p.id_usr = u.user_id
         and p.ref_country = c.id
         and p.sent_knd = sk.sent_knd
         and (id_ is null or p.id = id_)
         and p.rec_id = (select max(t1.rec_id)
                          from v_ref_person t1
                         where ((
                                  (search_all_ver_ = 1) and 
                                  (trim(idn_) is null or upper(t1.idn) like upper(trim(idn_)) || '%') and 
                                  (trim(fm_) is null or upper(t1.fm) like upper(trim(v_fm))) and 
                                  (trim(nm_) is null or upper(t1.nm) like upper(trim(v_nm))) and 
                                  (trim(ft_) is null or upper(t1.ft) like upper(trim(v_ft)))
                                 ) or 
                                 (
                                  (search_all_ver_ is null or search_all_ver_ = 0) and 
                                  (trim(idn_) is null or upper(p.idn) like upper(trim(idn_)) || '%') and 
                                  (trim(fm_) is null or upper(p.fm) like upper(trim(v_fm))) and 
                                  (trim(nm_) is null or upper(p.nm) like upper(trim(v_nm))) and 
                                  (trim(ft_) is null or upper(p.ft) like upper(trim(v_ft)))
                                 ) 
                               )
                           and t1.rec_id = p.rec_id
                         )
/*         and ((search_all_ver_ = 0)
               and (trim(idn_) is null or upper(p.idn) like upper(trim(idn_)) || '%')
               and (trim(fm_) is null or upper(p.fm) like upper(trim(v_fm)))
               and (trim(nm_) is null or upper(p.nm) like upper(trim(v_nm)))
               and (trim(ft_) is null or upper(p.ft) like upper(trim(v_ft)))
             )*/
         and (rec_id_ is null or p.rec_id = rec_id_ )
         and (date_ is null or p.begin_date = (select max(t.begin_date)
                                                 from v_ref_person t
                                                where t.rec_id = p.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (p.end_date is null or p.end_date > date_))
       order by p.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_person_list_by_params;
  
  
  procedure ref_read_person_hst_list(
    id_         in  ref_person.id % type,
    Cur         out sys_refcursor,
    err_code    out number,
    err_msg     out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_PERSON_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select ph.id_hst,
             ph.id,
             ph.rec_id,
             ph.code,
             ph.idn,
             ph.fm,
             ph.nm,
             ph.ft,
             ph.fio_kz,
             ph.fio_en,
             ph.ref_country,
             c.name_ru as country_name,
             ph.phone_work,
             ph.fax,
             ph.address_work,
             ph.note,
             ph.begin_date,
             ph.end_date,
             ph.datlast,
             ph.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             ph.type_change,
             tc.name as type_change_name,
             ph.user_location,
             sk.name as sent_knd
        from ref_person_hst ph,
             f_users u,
             ref_country c,
             type_change tc,
             sent_knd sk
       where ph.id_usr = u.user_id
         and ph.sent_knd = sk.sent_knd
         and ph.ref_country = c.id
         and ph.type_change = tc.type_change
         and ph.id = id_
       order by ph.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_person_hst_list;
  
  
  procedure ref_insert_person(
    rec_id_        in  ref_person.rec_id        % type,
    code_          in  ref_person.code          % type,
    idn_           in  ref_person.idn           % type,
    fm_            in  ref_person.fm            % type,
    nm_            in  ref_person.nm            % type,
    ft_            in  ref_person.ft            % type,
    fio_kz_        in  ref_person.fio_kz        % type,
    fio_en_        in  ref_person.fio_en        % type,
    ref_country_   in  ref_person.ref_country   % type,
    phone_work_    in  ref_person.phone_work    % type,
    fax_           in  ref_person.fax           % type,
    address_work_  in  ref_person.address_work  % type,
    note_          in  ref_person.note          % type,    
    begin_date_    in  ref_person.begin_date    % type,
    end_date_      in  ref_person.end_date      % type,
    id_usr_        in  ref_person.id_usr        % type,
    user_location_ in  ref_person.user_location % type,
    datlast_       in  ref_person.datlast       % type,
    do_commit_     in  integer default 1,
    id_            out ref_person.id            % type,
    err_code       out number,
    err_msg        out varchar2
  )
  is
    ProcName       constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_PERSON';
    ref_code       Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_person';
    
    if /*(trim(code_) is null) or */
       (trim(idn_) is null) or 
       (trim(fm_) is null) or 
       (trim(nm_) is null) or 
       (begin_date_ is null) or 
       (trim(address_work_) is null) or 
       (ref_country_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if; 
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом/наименованием!';
      raise E_Force_Exit;
    end if; */
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;   
    
    if ref_check_idn(ref_code,idn_,rec_id_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется такой ИН!';
      raise E_Force_Exit;
    end if;
    
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_, end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    id_ := seq_ref_person_id.nextval;
                      
    insert into ref_person(    
      id,
      rec_id,
      code,
      idn,
      fm,
      nm,
      ft,
      fio_kz,
      fio_en,
      ref_country,
      phone_work,
      fax,
      address_work,
      note,      
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),
      nvl(rec_id_,id_),--code_,
      idn_,
      fm_,
      nm_,
      ft_,
      fio_kz_,
      fio_en_,
      ref_country_,
      phone_work_,
      fax_,
      address_work_,
      note_,      
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_
    );
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';         
  end ref_insert_person;
  
  
  procedure ref_update_person(
    id_            in  ref_person.id            % type,
    rec_id_        in  ref_person.rec_id        % type,
    code_          in  ref_person.code          % type,
    idn_           in  ref_person.idn           % type,
    fm_            in  ref_person.fm            % type,
    nm_            in  ref_person.nm            % type,
    ft_            in  ref_person.ft            % type,
    fio_kz_        in  ref_person.fio_kz        % type,
    fio_en_        in  ref_person.fio_en        % type,
    ref_country_   in  ref_person.ref_country   % type,
    phone_work_    in  ref_person.phone_work    % type,
    fax_           in  ref_person.fax           % type,
    address_work_  in  ref_person.address_work  % type,
    note_          in  ref_person.note          % type,        
    begin_date_    in  ref_person.begin_date    % type,
    end_date_      in  ref_person.end_date      % type,
    id_usr_        in  ref_person.id_usr        % type,
    user_location_ in  ref_person.user_location % type,
    datlast_       in  ref_person.datlast       % type,
    do_commit_     in  integer default 1,     
    err_code       out number,
    err_msg        out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_PERSON';
    v_have_chg        boolean;
    v_ref_data        ref_person %rowtype;
    ref_code          Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_have_chg := false;
    ref_code := 'ref_person';
    
    if (trim(code_) is null) or 
       (trim(idn_) is null) or 
       (trim(fm_) is null) or 
       (trim(nm_) is null) or 
       (begin_date_ is null) or 
       (trim(address_work_) is null) or 
       (ref_country_ is null) then 
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
   /* if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;*/
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_idn(ref_code,idn_,rec_id_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется такой ИН!';
      raise E_Force_Exit;
    end if;  
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_, end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    /*report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;*/
    
    begin
      select *
        into v_ref_data
        from ref_person
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;
    
    if v_ref_data.id <> 0 then
      if nvl(v_ref_data.fm,' ') <> nvl(fm_,' ') or 
         nvl(v_ref_data.nm,' ') <> nvl(nm_,' ') or
         nvl(v_ref_data.ft,' ') <> nvl(ft_,' ') or 
         nvl(v_ref_data.fio_kz,' ') <> nvl(fio_kz_,' ') or
         nvl(v_ref_data.fio_en,' ') <> nvl(fio_en_,' ') or
         nvl(v_ref_data.ref_country,0) <> nvl(ref_country_,0) or
         nvl(v_ref_data.phone_work,' ') <> nvl(phone_work_,' ') or
         nvl(v_ref_data.fax,' ') <> nvl(fax_,' ') or
         nvl(v_ref_data.address_work,' ') <> nvl(address_work_,' ') or
         nvl(v_ref_data.note,' ') <> nvl(note_,' ') or         
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or 
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) then

        v_have_chg := true;

      end if;
    end if;
    
    
    if v_have_chg = true then
      update ref_person
         set 
             --code          = code_,
             idn           = idn_,
             fm            = fm_,
             nm            = nm_,
             ft            = ft_,
             fio_kz        = fio_kz_,
             fio_en        = fio_en_,
             ref_country   = ref_country_,
             phone_work    = phone_work_,
             fax           = fax_,
             address_work  = address_work_,
             note          = note_,                      
             begin_date    = begin_date_,
             end_date      = end_date_,
             id_usr        = id_usr_,
             user_location = user_location_,
             sent_knd      = 0,
             datlast       = datlast_
       where id = id_;
             
      if do_commit_ = 1 then
        Commit;
      end if;           
    end if;
    
  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';  
  end ref_update_person;
  
  
  procedure ref_delete_person(
    id_         in  ref_person.id % type,
    do_commit_  in  integer default 1,     
    err_code    out number,
    err_msg     out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_PERSON';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_person',
                          /*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
  
    update ref_person 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_person;
  
  
   /* Справочник юридических лиц  */  
   procedure ref_read_legal_person_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_LEGAL_PERSON_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select lp.id,
             lp.rec_id,
             lp.code,             
             lp.name_kz,
             lp.name_ru,
             lp.name_en,
             lp.short_name_kz,
             lp.short_name_ru,
             lp.short_name_en,
             lp.is_non_rezident,
             lp.idn,
             lp.ref_org_type,
             st.rec_id as REF_ORG_TYPE_REC_ID,
             st.name_ru as ORG_TYPE_NAME,
             lp.ref_type_bus_entity,
             be.rec_id as REF_TYPE_BUS_ENTITY_REC_ID,
             be.name_ru as TYPE_BE_NAME,
             lp.ref_country,
             c.rec_id as REF_COUNTRY_REC_ID,
             c.name_ru as COUNTRY_NAME,
             lp.ref_region,
             cy.rec_id as REF_REGION_REC_ID,
             cy.name_ru as REGION_NAME,
             lp.postal_index,
             lp.address_street,
             lp.address_num_house,
             lp.manager,
             /*lp.ref_managers,             
             m.rec_id as REF_MANAGERS_REC_ID,
             m.fm || ' ' || m.nm || ' ' || m.ft as MANAGERS_NAME,*/
             lp.legal_address,
             lp.fact_address,
             lp.note,
             lp.begin_date,
             lp.end_date,
             lp.datlast,
             lp.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,             
             lp.user_location,
             sk.name as sent_knd,
             lp.is_inv_fund,
             lp.inv_idn,
             lp.is_akimat
        from v_ref_legal_person lp,
             f_users u,
             v_ref_country c,
             v_ref_region cy,
             v_ref_type_bus_entity be,
             --ref_managers m,
             v_ref_org_type st,
             sent_knd sk
       where lp.id_usr = u.user_id
         and lp.sent_knd = sk.sent_knd
         and lp.ref_country = c.id(+)
         and lp.ref_region = cy.id(+)
         and lp.ref_type_bus_entity = be.id(+)
         --and lp.ref_managers = m.id(+)
         and lp.ref_org_type = st.id(+) 
         and (date_ is null or lp.begin_date = (select max(t.begin_date)
                                                 from v_ref_legal_person t
                                                where t.rec_id = lp.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (lp.end_date is null or lp.end_date > date_))
       order by lp.name_ru;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_legal_person_list;
  
  procedure ref_read_lp_list_by_params(
    id_             in  ref_legal_person.id % type,
    date_           in  Date,
    name_           in  ref_legal_person.name_ru % type,
    idn_            in  ref_legal_person.idn % type,
    rec_id_         in  ref_legal_person.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_LP_LIST_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ || '%';
    
    Open Cur for
      select lp.id,
             lp.rec_id,
             lp.code,             
             lp.name_kz,
             lp.name_ru,
             lp.name_en,
             lp.short_name_kz,
             lp.short_name_ru,
             lp.short_name_en,
             lp.is_non_rezident,
             lp.idn,
             lp.ref_org_type,
             st.rec_id as REF_ORG_TYPE_REC_ID,
             st.name_ru as ORG_TYPE_NAME,
             lp.ref_type_bus_entity,
             be.rec_id as REF_TYPE_BUS_ENTITY_REC_ID,
             be.name_ru as TYPE_BE_NAME,
             lp.ref_country,
             c.rec_id as REF_COUNTRY_REC_ID,
             c.name_ru as COUNTRY_NAME,
             lp.ref_region,
             cy.rec_id as REF_REGION_REC_ID,
             cy.name_ru as REGION_NAME,
             lp.postal_index,
             lp.address_street,
             lp.address_num_house,
             lp.manager,
             /*lp.ref_managers,
             m.rec_id as REF_MANAGERS_REC_ID,
             m.fm || ' ' || m.nm || ' ' || m.ft as MANAGERS_NAME,*/
             lp.legal_address,
             lp.fact_address,
             lp.note,
             lp.begin_date,
             lp.end_date,
             lp.datlast,
             lp.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,             
             lp.user_location,
             sk.name as sent_knd,
             lp.is_inv_fund,
             lp.inv_idn,
             lp.is_akimat
        from v_ref_legal_person lp,
             f_users u,
             v_ref_country c,
             v_ref_region cy,
             v_ref_type_bus_entity be,
             --ref_managers m,
             v_ref_org_type st,
             sent_knd sk
       where lp.id_usr = u.user_id
         and lp.sent_knd = sk.sent_knd
         and lp.ref_country = c.id(+)
         and lp.ref_region = cy.id(+)
         and lp.ref_type_bus_entity = be.id(+)
         and lp.rec_id = (select max(t1.rec_id)
                            from v_ref_legal_person t1
                           where ((
                                    (search_all_ver_ = 1) and 
                                    (name_ is null or upper(t1.name_ru) like upper(trim(v_name))) and
                                    (trim(idn_) is null or upper(t1.idn) like upper(trim(idn_)) || '%' )
                                   ) or 
                                   (
                                    (search_all_ver_ is null or search_all_ver_ = 0) and 
                                    (name_ is null or upper(lp.name_ru) like upper(trim(v_name))) and
                                    (trim(idn_) is null or upper(lp.idn) like upper(trim(idn_)) || '%' )
                                   ) 
                                 )
                             and t1.rec_id = lp.rec_id
                           )
         --and lp.ref_managers = m.id(+)
         and lp.ref_org_type = st.id(+)         
         and (id_ is null or lp.id = id_)
         /*and (trim(name_) is null or upper(lp.name_ru) like  upper(trim(v_name)))
         and (trim(idn_) is null or upper(lp.idn) like upper(trim(idn_)) || '%' )*/
         and (rec_id_ is null or lp.rec_id = rec_id_)
         and (date_ is null or lp.begin_date = (select max(t.begin_date)
                                                 from v_ref_legal_person t
                                                where t.rec_id = lp.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (lp.end_date is null or  lp.end_date > date_))
       order by lp.name_ru;

  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_lp_list_by_params;
  
  
  procedure ref_read_legal_person_hst_list(
    id_      in  ref_legal_person.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_LEGAL_PERSON_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select lph.id_hst,
             lph.id,
             lph.rec_id,
             lph.code,
             lph.name_kz,
             lph.name_ru,
             lph.name_en,
             lph.short_name_kz,
             lph.short_name_ru,
             lph.short_name_en,
             lph.is_non_rezident,             
             lph.idn,
             lph.ref_org_type,
             st.rec_id as REF_ORG_TYPE_REC_ID,
             st.name_ru as ORG_TYPE_NAME,
             lph.ref_type_bus_entity,
             be.rec_id as REF_TYPE_BUS_ENTITY_REC_ID,
             be.name_ru as TYPE_BE_NAME,
             lph.ref_country,
             c.rec_id as REF_COUNTRY_REC_ID,
             c.name_ru as COUNTRY_NAME,
             lph.ref_region,
             cy.rec_id as REF_REGION_REC_ID,
             cy.name_ru as REGION_NAME,
             lph.postal_index,
             lph.address_street,
             lph.address_num_house,
             lph.manager,
             /*lph.ref_managers,
             m.fm || ' ' || m.nm || ' ' || m.ft as MANAGERS_NAME,*/             
             lph.legal_address,
             lph.fact_address,
             lph.note,
             lph.begin_date,
             lph.end_date,
             lph.datlast,
             lph.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             lph.type_change,
             tc.name as type_change_name,
             lph.user_location,
             sk.name as sent_knd,
             lph.is_inv_fund,
             lph.inv_idn,
             lph.is_akimat
        from ref_legal_person_hst lph,
             f_users u,
             ref_country c,
             ref_region cy,
             ref_type_bus_entity be,
             --ref_managers m,
             type_change tc,
             ref_org_type st,
             sent_knd sk
       where lph.id_usr = u.user_id
         and lph.sent_knd = sk.sent_knd
         and lph.ref_country = c.id(+)
         and lph.type_change = tc.type_change
         and lph.ref_region = cy.id(+)
         and lph.ref_type_bus_entity = be.id(+)
         --and lph.ref_managers = m.id(+)
         and lph.ref_org_type = st.id
         and lph.id = id_
       order by lph.id_hst, lph.name_ru;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_legal_person_hst_list;
  
  /*procedure ref_get_legal_person(
    id_                in  ref_legal_person.id            	   % type,
    code_              out ref_legal_person.code               % type,
    name_kz_           out ref_legal_person.name_kz            % type,
    name_ru_           out ref_legal_person.name_ru            % type,
    name_en_           out ref_legal_person.name_en            % type,
    short_name_kz_     out ref_legal_person.short_name_kz      % type,
    short_name_ru_     out ref_legal_person.short_name_ru      % type,
    short_name_en_     out ref_legal_person.short_name_en      % type,
    is_non_rezident_   out ref_legal_person.is_non_rezident    % type,
    idn_               out ref_legal_person.idn                % type,
    org_type_name_     out ref_org_type.name_ru                % type,
    type_be_name_      out ref_type_bus_entity.name_ru         % type,
    country_name_      out ref_country.name_ru                 % type,
    region_name_       out ref_region.name_ru                  % type,
    postal_index_      out ref_legal_person.postal_index       % type,
    address_street_    out ref_legal_person.address_street     % type,
    address_num_house_ out ref_legal_person.address_num_house  % type,
    manager_           out ref_legal_person.manager            % type,
    legal_address_     out ref_legal_person.legal_address      % type,
    fact_address_      out ref_legal_person.fact_address       % type,
    note_              out ref_legal_person.note               % type,
    begin_date_        out ref_legal_person.begin_date         % type,
    end_date_          out ref_legal_person.end_date           % type,
    is_inv_fund_       out ref_legal_person.is_inv_fund        % type,
    inv_idn_           out ref_legal_person.inv_idn            % type,
    is_akimat_         out ref_legal_person
    user_name_         out f_users.first_name                  % type,    
    user_location_     out ref_legal_person.user_location      % type,
    err_code           out number,
    err_msg            out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_GET_LEGAL_PERSON';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    select lp.code,
           lp.name_kz,
           lp.name_ru,
           lp.name_en,
           lp.short_name_kz,
           lp.short_name_ru,
           lp.short_name_en,
           lp.is_non_rezident,
           lp.idn,
           st.name_ru as ORG_TYPE_NAME,
           be.name_ru as TYPE_BE_NAME,
           c.name_ru as COUNTRY_NAME,
           cy.name_ru as REGION_NAME,
           lp.postal_index,
           lp.address_street,
           lp.address_num_house,
           lp.manager,
           --m.fm || ' ' || m.nm || ' ' || m.ft as MANAGERS_NAME,
           lp.legal_address,
           lp.fact_address,
           lp.note,
           lp.begin_date,
           lp.end_date,
           lp.is_inv_fund,
           lp.inv_idn,
           lp.is_akimat,
           u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,             
           lp.user_location
      into code_,
           name_kz_,
           name_ru_,
           name_en_,
           short_name_kz_,
           short_name_ru_,
           short_name_en_,
           is_non_rezident_,
           idn_,
           org_type_name_,
           type_be_name_,
           country_name_,
           region_name_,
           postal_index_,
           address_street_,
           address_num_house_,
           manager_,
           legal_address_,
           fact_address_,
           note_,
           begin_date_,
           end_date_,
           is_inv_fund_,
           inv_idn_,
           is_akimat_,
           user_name_,
           user_location_
      from ref_legal_person lp,
           f_users u,
           ref_country c,
           ref_region cy,
           ref_type_bus_entity be,
           --ref_managers m,
           ref_org_type st
     where lp.id_usr = u.user_id
       and lp.ref_country = c.id(+)
       and lp.ref_region = cy.id(+)
       and lp.ref_type_bus_entity = be.id(+)
       --and lp.ref_managers = m.id(+)
       and lp.ref_org_type = st.id(+)
       and lp.id = id_;    
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' Ошибка получения данных !';       
  end ref_get_legal_person;*/
  
  
  procedure ref_insert_legal_person(
    rec_id_              in  ref_legal_person.rec_id              % type,
    code_                in  ref_legal_person.code                % type,    
    name_kz_             in  ref_legal_person.name_kz             % type,
    name_ru_             in  ref_legal_person.name_ru             % type,
    name_en_             in  ref_legal_person.name_en             % type,
    short_name_kz_       in  ref_legal_person.short_name_kz       % type,
    short_name_ru_       in  ref_legal_person.short_name_ru       % type,
    short_name_en_       in  ref_legal_person.short_name_en       % type,
    is_non_rezident_     in  ref_legal_person.is_non_rezident     % type,
    idn_                 in  ref_legal_person.idn                 % type,
    ref_org_type_        in  ref_legal_person.ref_org_type        % type,
    ref_type_bus_entity_ in  ref_legal_person.ref_type_bus_entity % type,
    ref_country_         in  ref_legal_person.ref_country         % type,
    ref_region_          in  ref_legal_person.ref_region          % type,
    postal_index_        in  ref_legal_person.postal_index        % type,
    address_street_      in  ref_legal_person.address_street      % type,
    address_num_house_   in  ref_legal_person.address_num_house   % type,
    manager_             in  ref_legal_person.manager             % type,
    legal_address_       in  ref_legal_person.legal_address       % type,
    fact_address_        in  ref_legal_person.fact_address        % type,
    note_                in  ref_legal_person.note                % type,    
    begin_date_          in  ref_legal_person.begin_date          % type,
    end_date_            in  ref_legal_person.end_date            % type,
    is_inv_fund_         in  ref_legal_person.is_inv_fund         % type,
    inv_idn_             in  ref_legal_person.inv_idn             % type,
    is_akimat_           in  ref_legal_person.is_akimat           % type,
    id_usr_              in  ref_legal_person.id_usr              % type,    
    user_location_       in  ref_legal_person.user_location       % type,
    datlast_             in  ref_legal_person.datlast             % type,
    do_commit_           in  integer default 1,
    id_                  out ref_legal_person.id % type,
    err_code             out number,
    err_msg              out varchar2
  )
  is
    ProcName     constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_LEGAL_PERSON';
    ref_code     Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_legal_person';
    
    if /*(trim(code_) is null) or*/ 
       ((trim(idn_) is null) and (is_non_rezident_ = 0 and is_inv_fund_ = 0 and is_akimat_ = 0)) or
       (trim(name_ru_) is null) or 
       --(trim(name_kz_) is null) or
       (begin_date_ is null) or
       (ref_org_type_ is null) or
       (ref_type_bus_entity_ is null) or       
       (trim(legal_address_) is null) or         
       (ref_country_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;     
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if; */
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if is_non_rezident_ = 0 then
      if ref_check_idn(ref_code,idn_,rec_id_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 05 ' || 'Внимание! Уже имеется такой ИН!';
        raise E_Force_Exit;
      end if;      
    end if;
        
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
     
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 07 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 08 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;  
        
    id_ := seq_ref_legal_person_id.nextval;
                    
    insert into ref_legal_person(
      id,
      rec_id,
      code,
      name_kz,
      name_ru,
      name_en,
      short_name_kz,
      short_name_ru,
      short_name_en,
      is_non_rezident,
      idn,
      ref_org_type,
      ref_type_bus_entity,
      ref_country,
      ref_region,
      postal_index,
      address_street,
      address_num_house,
      manager,
      legal_address,
      fact_address,
      note,      
      begin_date,
      end_date,
      is_inv_fund,
      inv_idn,
      is_akimat,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),
      nvl(rec_id_,id_),--code_,
      name_kz_,
      name_ru_,
      name_en_,
      short_name_kz_,
      short_name_ru_,
      short_name_en_,
      is_non_rezident_,
      idn_,
      ref_org_type_,
      ref_type_bus_entity_,
      ref_country_,
      ref_region_,
      postal_index_,
      address_street_,
      address_num_house_,
      manager_,
      legal_address_,
      fact_address_,
      note_,   
      begin_date_,
      end_date_,
      is_inv_fund_,
      inv_idn_,
      is_akimat_,
      id_usr_,
      user_location_,
      datlast_
    );
                   
    if Do_Commit_ = 1 then
      Commit;
    end if;

                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';
  end ref_insert_legal_person;
  
  
  procedure ref_update_legal_person(
    id_                  in  ref_legal_person.id                  % type,
    rec_id_              in  ref_legal_person.rec_id              % type,
    code_                in  ref_legal_person.code                % type,
    name_kz_             in  ref_legal_person.name_kz             % type,
    name_ru_             in  ref_legal_person.name_ru             % type,
    name_en_             in  ref_legal_person.name_en             % type,
    short_name_kz_       in  ref_legal_person.short_name_kz       % type,
    short_name_ru_       in  ref_legal_person.short_name_ru       % type,
    short_name_en_       in  ref_legal_person.short_name_en       % type,
    is_non_rezident_     in  ref_legal_person.is_non_rezident     % type,
    idn_                 in  ref_legal_person.idn                 % type,
    ref_org_type_        in  ref_legal_person.ref_org_type        % type,
    ref_type_bus_entity_ in  ref_legal_person.ref_type_bus_entity % type,
    ref_country_         in  ref_legal_person.ref_country         % type,
    ref_region_          in  ref_legal_person.ref_region          % type,
    postal_index_        in  ref_legal_person.postal_index        % type,
    address_street_      in  ref_legal_person.address_street      % type,
    address_num_house_   in  ref_legal_person.address_num_house   % type,
    manager_             in  ref_legal_person.manager             % type,
    legal_address_       in  ref_legal_person.legal_address       % type,
    fact_address_        in  ref_legal_person.fact_address        % type,
    note_                in  ref_legal_person.note                % type,    
    begin_date_          in  ref_legal_person.begin_date          % type,
    end_date_            in  ref_legal_person.end_date            % type,
    is_inv_fund_         in  ref_legal_person.is_inv_fund         % type,
    inv_idn_             in  ref_legal_person.inv_idn             % type,
    is_akimat_           in  ref_legal_person.is_akimat           % type,
    id_usr_              in  ref_legal_person.id_usr              % type,    
    user_location_       in  ref_legal_person.user_location       % type,
    datlast_             in  ref_legal_person.datlast             % type,
    do_commit_           in  integer default 1,     
    err_code             out number,
    err_msg              out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_LEGAL_PERSON';
    v_have_chg        boolean;
    v_ref_data        ref_legal_person %rowtype;
    ref_code          Varchar2(64);    
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_have_chg := false;
    ref_code := 'ref_legal_person';
    
    if (trim(code_) is null) or 
       ((trim(idn_) is null) and (is_non_rezident_ = 0 and is_inv_fund_ = 0 and is_akimat_ = 0)) or
       (trim(name_ru_) is null) or 
       --(trim(name_kz_) is null) or
       (begin_date_ is null) or
       (ref_org_type_ is null) or
       (ref_type_bus_entity_ is null) or       
       (trim(legal_address_) is null) or         
       (ref_country_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;
    */
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if is_non_rezident_ = 0 then
      if ref_check_idn(ref_code,idn_,rec_id_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 05 ' || 'Внимание! Уже имеется такой ИН!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 07 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    /*report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;*/
    
    begin
      select *
        into v_ref_data
        from ref_legal_person
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;
    
    if v_ref_data.id <> 0 then
      if nvl(v_ref_data.name_kz,' ') <> nvl(name_kz_,' ') or 
         nvl(v_ref_data.name_ru,' ') <> nvl(name_ru_,' ') or
         nvl(v_ref_data.name_en,' ') <> nvl(name_en_,' ') or 
         nvl(v_ref_data.short_name_kz,' ') <> nvl(short_name_kz_,' ') or
         nvl(v_ref_data.short_name_ru,' ') <> nvl(short_name_ru_,' ') or 
         nvl(v_ref_data.short_name_en,' ') <> nvl(short_name_en_,' ') or
         nvl(v_ref_data.is_non_rezident,0) <> nvl(is_non_rezident_,0) or 
         nvl(v_ref_data.idn,' ') <> nvl(idn_,' ') or 
         nvl(v_ref_data.ref_org_type,0) <> nvl(ref_org_type_,0) or
         nvl(v_ref_data.ref_type_bus_entity,0) <> nvl(ref_type_bus_entity_,0) or 
         nvl(v_ref_data.ref_country,0) <> nvl(ref_country_,0) or
         nvl(v_ref_data.ref_region,0) <> nvl(ref_region_,0) or 
         nvl(v_ref_data.postal_index,' ') <> nvl(postal_index_,' ') or
         nvl(v_ref_data.address_street,' ') <> nvl(address_street_,' ') or 
         nvl(v_ref_data.address_num_house,' ') <> nvl(address_num_house_,' ') or
         nvl(v_ref_data.manager,' ') <> nvl(manager_,' ') or 
         nvl(v_ref_data.legal_address,' ') <> nvl(legal_address_,' ') or
         nvl(v_ref_data.fact_address,' ') <> nvl(fact_address_,' ') or 
         nvl(v_ref_data.note,' ') <> nvl(note_,' ') or         
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or 
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) or 
         nvl(v_ref_data.is_inv_fund,0) <> nvl(is_inv_fund_,0) or 
         nvl(v_ref_data.inv_idn,' ') <> nvl(inv_idn_,' ') or
         nvl(v_ref_data.is_akimat,0) <> nvl(is_akimat_,0) then

        v_have_chg := true;

      end if;
    end if;
    
    if v_have_chg = true then
      update ref_legal_person
       set 
           --code                = code_,
           name_kz             = name_kz_,
           name_ru             = name_ru_,
           name_en             = name_en_,
           short_name_kz       = short_name_kz_,
           short_name_ru       = short_name_ru_,
           short_name_en       = short_name_en_,
           is_non_rezident     = is_non_rezident_,
           idn                 = idn_,
           ref_org_type        = ref_org_type_,
           ref_type_bus_entity = ref_type_bus_entity_,
           ref_country         = ref_country_,
           ref_region          = ref_region_,
           postal_index        = postal_index_,
           address_street      = address_street_,
           address_num_house   = address_num_house_,
           manager             = manager_,
           legal_address       = legal_address_,
           fact_address        = fact_address_,
           note                = note_,                      
           begin_date          = begin_date_,
           end_date            = end_date_,
           is_inv_fund         = is_inv_fund_,
           inv_idn             = inv_idn_,
           is_akimat           = is_akimat_,
           id_usr              = id_usr_,
           user_location       = user_location_,
           sent_knd            = 0,
           datlast             = datlast_
     where id = id_;
           
      if do_commit_ = 1 then
        Commit;
      end if;
    end if;
    
  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';  
  end ref_update_legal_person;
  
  
  procedure ref_delete_legal_person(
    id_         in  ref_legal_person.id % type,
    do_commit_  in  integer default 1,     
    err_code    out number,
    err_msg     out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_LEGAL_PERSON';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    if ref_check_record('ref_respondent','ref_legal_person', id_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || ' Внимание! Имеется дочерняя запись!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_record('ref_mfo_reg','ref_legal_person', id_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || ' Внимание! Имеется дочерняя запись!';
      raise E_Force_Exit;
    end if;
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_legal_person',
                          /*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
  
    update ref_legal_person 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_legal_person;
   
   
  /* Справочник стран */  
  procedure ref_read_country_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    Err_Code  out number,
    Err_Msg   out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_COUNTRY_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select c.id,
             c.rec_id,
             c.code,
             c.name_kz,
             c.name_ru,
             c.name_en,
             c.begin_date,
             c.end_date,
             c.datlast,
             c.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             c.user_location,
             sk.name as sent_knd
        from v_ref_country c,
             f_users u,
             sent_knd sk
       where c.id_usr = u.user_id         
         and c.sent_knd = sk.sent_knd
         and (date_ is null or c.begin_date = (select max(t.begin_date)
                                                 from v_ref_country t
                                                where t.rec_id = c.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (c.end_date is null or c.end_date > date_))
       order by c.name_ru;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_country_list;
  
  procedure ref_read_country_l_by_params(
    id_             in  ref_country.id % type,
    date_           in  Date,
    code_           in  ref_country.code % type,
    name_ru_        in  ref_country.name_ru % type,
    rec_id_         in  ref_country.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    Err_Code        out number,
    Err_Msg         out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_COUNTRY_L_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    Open Cur for
      select c.id,
             c.rec_id,
             c.code,
             c.name_kz,
             c.name_ru,
             c.name_en,
             c.begin_date,
             c.end_date,
             c.datlast,
             c.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             c.user_location,
             sk.name as sent_knd
        from v_ref_country c,
             f_users u,
             sent_knd sk
       where c.id_usr = u.user_id         
         and c.sent_knd = sk.sent_knd         
         and c.rec_id = (select max(t1.rec_id)
                           from v_ref_country t1
                          where ((
                                  (search_all_ver_ = 1) and 
                                  (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name))) and
                                  (trim(code_) is null or upper(t1.code) like upper(trim(code_)) || '%' )
                                 ) or 
                                 (
                                  (search_all_ver_ is null or search_all_ver_ = 0) and 
                                  (trim(name_ru_) is null or upper(c.name_ru) like upper(trim(v_name))) and
                                  (trim(code_) is null or upper(c.code) like upper(trim(code_)) || '%' )
                                 ) 
                                )
                            and t1.rec_id = c.rec_id
                         )
         and (id_ is null or c.id = id_)
         /*and (trim(code_) is null or upper(c.code) like upper(trim(code_)) || '%' )
         and (trim(name_ru_) is null or upper(c.name_ru) like upper(trim(v_name)))*/
         and (rec_id_ is null or c.rec_id = rec_id_ )
         and (date_ is null or c.begin_date = (select max(t.begin_date)
                                                 from v_ref_country t
                                                where t.rec_id = c.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (c.end_date is null or c.end_date > date_))
       order by c.name_ru;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_country_l_by_params;
  
  
  /* Справочник Руководящие работники */    
  procedure ref_read_managers_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    Err_Code  out number,
    Err_Msg   out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_MANAGERS_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select m.id,
             m.rec_id,
             m.code,
             m.fm,
             m.nm,
             m.ft,
             m.fm || ' ' || m.nm || ' ' || m.ft as FIO_RU,
             m.fio_kz,
             m.fio_en,
             m.ref_post,
             p.NAME_RU as post_name_ru,
             m.phone,
             decode(p.type_post,4,1,0) is_executor,
             m.begin_date,
             m.end_date,
             m.datlast,
             m.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             m.user_location,
             sk.name as sent_knd
        from v_ref_managers m,
             v_ref_post p,
             f_users u,
             sent_knd sk
       where m.id_usr = u.user_id         
         and m.sent_knd = sk.sent_knd
         and m.ref_post = p.id
         and (date_ is null or m.begin_date = (select max(t.begin_date)
                                                 from v_ref_managers t
                                                where t.rec_id = m.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (m.end_date is null or m.end_date > date_))
       order by m.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_managers_list;  
  
  procedure ref_read_managers_l_by_params(
    id_             in  ref_managers.id % type,
    date_           in  Date,
    fm_             in  ref_managers.fm % type,
    nm_             in  ref_managers.nm % type,
    ft_             in  ref_managers.ft % type,
    rec_id_         in  ref_managers.rec_id % type,
    ref_post_       in  ref_managers.ref_post % type,
    is_executor_    in  Integer default 0,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    Err_Code        out number,
    Err_Msg         out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_MANAGERS_L_BY_PARAMS';
    v_fm varchar2(255);
    v_nm varchar2(255);
    v_ft varchar2(255);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_fm := '%' || fm_ || '%';
    v_nm := '%' || nm_ || '%';
    v_ft := '%' || ft_ || '%';
    
    Open Cur for
      select m.id,
             m.rec_id,
             m.code,
             m.fm,
             m.nm,
             m.ft,
             m.fm || ' ' || m.nm || ' ' || m.ft as FIO_RU,
             m.fio_kz,
             m.fio_en,
             m.ref_post,
             p.NAME_RU as post_name_ru,
             m.phone,
             decode(p.type_post,4,1,0) is_executor,
             m.begin_date,
             m.end_date,
             m.datlast,
             m.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             m.user_location,
             sk.name as sent_knd
        from v_ref_managers m,
             v_ref_post p,
             f_users u,
             sent_knd sk
       where m.id_usr = u.user_id
         and m.sent_knd = sk.sent_knd
         and m.ref_post = p.id
         and m.rec_id = (select max(t1.rec_id)
                          from v_ref_managers t1,
                               v_ref_post p1
                         where ((
                                  (search_all_ver_ = 1) and 
                                  (trim(fm_) is null or upper(t1.fm) like upper(trim(v_fm))) and
                                  (trim(nm_) is null or upper(t1.nm) like upper(trim(v_nm))) and
                                  (trim(ft_) is null or upper(t1.ft) like upper(trim(v_ft))) and
                                  (ref_post_ is null or t1.ref_post = ref_post_) and
                                  (is_executor_ is null or (is_executor_ != 0 and p1.type_post = 8) or (is_executor_ = 0 and p1.TYPE_POST != 8))
                                 ) or 
                                 (
                                  (search_all_ver_ is null or search_all_ver_ = 0) and 
                                  (trim(fm_) is null or upper(m.fm) like upper(trim(v_fm))) and
                                  (trim(nm_) is null or upper(m.nm) like upper(trim(v_nm))) and
                                  (trim(ft_) is null or upper(m.ft) like upper(trim(v_ft))) and
                                  (ref_post_ is null or m.ref_post = ref_post_) and
                                  (is_executor_ is null or (is_executor_ != 0 and p.type_post = 8) or (is_executor_ = 0 and p1.TYPE_POST != 8))
                                 ) 
                               )
                           and t1.rec_id = m.rec_id
                           and t1.ref_post = p1.id
                         )
         and (id_ is null or m.id = id_)
         /*and (trim(fm_) is null or upper(m.fm) like upper(trim(v_fm)))
         and (trim(nm_) is null or upper(m.nm) like upper(trim(v_nm)))
         and (trim(ft_) is null or upper(m.ft) like upper(trim(v_ft)))*/
         and (rec_id_ is null or m.rec_id = rec_id_)
         and (date_ is null or m.begin_date = (select max(t.begin_date)
                                                 from v_ref_managers t
                                                where t.rec_id = m.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (m.end_date is null or  m.end_date > date_))
       order by m.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_managers_l_by_params;  
  
  
  procedure ref_read_managers_hst_list(
    id_      in  ref_managers.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_MANAGERS_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select mh.id_hst,
             mh.id,
             mh.rec_id,
             mh.code,
             mh.fm,
             mh.nm,
             mh.ft,
             mh.fm || ' ' || mh.nm || ' ' || mh.ft as FIO_RU,
             mh.fio_kz,
             mh.fio_en, 
             mh.ref_post,
             p.NAME_RU as post_name_ru,
             mh.phone,
             decode(p.type_post,4,1,0) is_executor,
             mh.begin_date,
             mh.end_date,
             mh.datlast,
             mh.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             mh.type_change,
             tc.name as type_change_name,
             mh.user_location,
             sk.name as sent_knd
        from ref_managers_hst mh,
             ref_post p,
             f_users u,
             type_change tc,
             sent_knd sk
       where mh.id_usr = u.user_id
         and mh.type_change = tc.type_change
         and mh.sent_knd = sk.sent_knd
         and mh.ref_post = p.id(+)
         and mh.id = id_         
       order by mh.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_managers_hst_list;
  
  
  procedure ref_insert_managers(
    rec_id_        in  ref_managers.rec_id        % type,
    code_          in  ref_managers.code          % type,    
    fm_            in  ref_managers.fm            % type,
    nm_            in  ref_managers.nm            % type,
    ft_            in  ref_managers.ft            % type,
    fio_kz_        in  ref_managers.fio_kz        % type,
    fio_en_        in  ref_managers.fio_en        % type,        
    ref_post_      in  ref_managers.ref_post      % type,
    phone_         in  ref_managers.phone         % type,    
    begin_date_    in  ref_managers.begin_date    % type,
    end_date_      in  ref_managers.end_date      % type,
    id_usr_        in  ref_managers.id_usr        % type,    
    user_location_ in  ref_managers.user_location % type,
    datlast_       in  ref_managers.datlast       % type,
    do_commit_     in  integer default 1,
    id_            out ref_managers.id % type,
    err_code       out number,
    err_msg        out varchar2
  )
  is
    ProcName         constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_MANAGERS';
    ref_code         Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_managers';
    
    if /*(trim(code_) is null) or*/ 
       (trim(fm_) is null) or 
       (trim(nm_) is null) or 
       (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if; 
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code('ref_managers',rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if; */
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    id_ := seq_ref_managers_id.nextval;
                  
    insert into ref_managers(
      id,
      rec_id,
      code,
      fm,
      nm,
      ft,
      fio_kz,
      fio_en,
      ref_post,
      phone,      
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),
      nvl(rec_id_,id_),--code_,
      fm_,
      nm_,
      ft_,
      fio_kz_,
      fio_en_,
      ref_post_,
      phone_,      
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_
    );
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';      
  end ref_insert_managers;
  
  
  procedure ref_update_managers(
    id_            in  ref_managers.id            % type,
    rec_id_        in  ref_managers.rec_id        % type,
    code_          in  ref_managers.code          % type,    
    fm_            in  ref_managers.fm            % type,
    nm_            in  ref_managers.nm            % type,
    ft_            in  ref_managers.ft            % type,
    fio_kz_        in  ref_managers.fio_kz        % type,
    fio_en_        in  ref_managers.fio_en        % type,        
    ref_post_      in  ref_managers.ref_post      % type,
    phone_         in  ref_managers.phone         % type,    
    begin_date_    in  ref_managers.begin_date    % type,
    end_date_      in  ref_managers.end_date      % type,
    id_usr_        in  ref_managers.id_usr        % type,    
    user_location_ in  ref_managers.user_location % type,
    datlast_       in  ref_managers.datlast       % type,
    do_commit_     in  integer default 1,     
    err_code       out number,
    err_msg        out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_MANAGERS';
    v_have_chg        boolean;
    v_ref_data        ref_managers %rowtype;
    ref_code          Varchar2(64);
  begin    
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_managers';
    v_have_chg := false;
    
    if (trim(code_) is null) or 
       (trim(fm_) is null) or 
       (trim(nm_) is null) or 
       (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code('ref_managers',rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;*/
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    /*report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;*/
    
    begin
      select *
        into v_ref_data
        from ref_managers
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;
    
    if v_ref_data.id <> 0 then
      if nvl(v_ref_data.fm,' ') <> nvl(fm_,' ') or 
         nvl(v_ref_data.nm,' ') <> nvl(nm_,' ') or
         nvl(v_ref_data.ft,' ') <> nvl(ft_,' ') or 
         nvl(v_ref_data.fio_kz,' ') <> nvl(fio_kz_,' ') or
         nvl(v_ref_data.fio_en,' ') <> nvl(fio_en_,' ') or
         nvl(v_ref_data.ref_post,0) <> nvl(ref_post_,0) or         
         nvl(v_ref_data.phone,' ') <> nvl(phone_,' ') or
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or 
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) then

        v_have_chg := true;

      end if;
    end if;
    
    if v_have_chg = true then
      update ref_managers
         set --code          = code_,
             fm            = fm_,
             nm            = nm_,
             ft            = ft_,
             fio_kz        = fio_kz_,
             fio_en        = fio_en_,
             ref_post      = ref_post_,
             phone         = phone_,             
             begin_date    = begin_date_,
             end_date      = end_date_,
             id_usr        = id_usr_,
             user_location = user_location_,
             sent_knd      = 0,
             datlast       = datlast_
       where id = id_;
             
      if do_commit_ = 1 then
        Commit;
      end if;           
    end if;
    
  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';  
  end ref_update_managers;
  
  
  procedure ref_delete_managers(
    id_        in  ref_managers.id % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_MANAGERS';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_managers',
                          /*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
  
    update ref_managers 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;      
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_managers;
  
  
  /* Справочник организационно-правовая форма */
  procedure ref_read_type_bus_entity_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    Err_Code  out number,
    Err_Msg   out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_TYPE_BUS_ENTITY_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select tb.id,
             tb.rec_id,
             tb.code,
             tb.name_kz,
             tb.name_ru,
             tb.name_en,
             tb.begin_date,
             tb.end_date,
             tb.datlast,
             tb.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             tb.user_location,
             sk.name as sent_knd
        from v_ref_type_bus_entity tb,
             f_users u,
             sent_knd sk
       where tb.id_usr = u.user_id         
         and tb.sent_knd = sk.sent_knd
         and (date_ is null or tb.begin_date = (select max(t.begin_date)
                                                 from v_ref_type_bus_entity t
                                                where t.rec_id = tb.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (tb.end_date is null or tb.end_date > date_))
       order by tb.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_type_bus_entity_list;  
  
  procedure ref_read_t_b_e_list_by_params(
    id_             in  ref_type_bus_entity.id % type,
    date_           in  Date,
    name_ru_        in  ref_type_bus_entity.name_ru % type,
    rec_id_         in  ref_type_bus_entity.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    Err_Code        out number,
    Err_Msg         out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_T_B_E_LIST_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    Open Cur for
      select tb.id,
             tb.rec_id,
             tb.code,
             tb.name_kz,
             tb.name_ru,
             tb.name_en,
             tb.begin_date,
             tb.end_date,
             tb.datlast,
             tb.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             tb.user_location,
             sk.name as sent_knd
        from v_ref_type_bus_entity tb,
             f_users u,
             sent_knd sk
       where tb.id_usr = u.user_id         
         and tb.sent_knd = sk.sent_knd
         and tb.rec_id = (select max(t1.rec_id)
                            from v_ref_type_bus_entity t1
                           where ((
                                    (search_all_ver_ = 1) and 
                                    (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name)))
                                   ) or 
                                   (
                                    (search_all_ver_ is null or search_all_ver_ = 0) and 
                                    (trim(name_ru_) is null or upper(tb.name_ru) like upper(trim(v_name)))
                                   ) 
                                 )
                             and t1.rec_id = tb.rec_id
                           )
         and (id_ is null or tb.id = id_)         
--         and (trim(name_ru_) is null or upper(tb.name_ru) like upper(trim(v_name)))
         and (rec_id_ is null or tb.rec_id = rec_id_)         
         and (date_ is null or tb.begin_date = (select max(t.begin_date)
                                                 from v_ref_type_bus_entity t
                                                where t.rec_id = tb.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (tb.end_date is null or tb.end_date > date_))
       order by tb.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_t_b_e_list_by_params;  
  
  
  procedure ref_read_t_b_e_hst_list(
    id_      in  ref_type_bus_entity.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_T_B_E_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select tbh.id_hst,
             tbh.id,
             tbh.rec_id,
             tbh.code,
             tbh.name_kz,
             tbh.name_ru,
             tbh.name_en,
             tbh.begin_date,
             tbh.end_date,
             tbh.datlast,
             tbh.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             tbh.type_change,
             tc.name as type_change_name,
             tbh.user_location,
             sk.name as sent_knd
        from ref_type_bus_entity_hst tbh,
             f_users u,
             type_change tc,
             sent_knd sk
       where tbh.id_usr = u.user_id
         and tbh.sent_knd = sk.sent_knd
         and tbh.type_change = tc.type_change
         and tbh.id = id_
       order by tbh.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_t_b_e_hst_list;
  
  
  procedure ref_insert_type_bus_entity(
    rec_id_        in  ref_type_bus_entity.rec_id        % type,
    code_          in  ref_type_bus_entity.code          % type,    
    name_kz_       in  ref_type_bus_entity.name_kz       % type,
    name_ru_       in  ref_type_bus_entity.name_ru       % type,
    name_en_       in  ref_type_bus_entity.name_en       % type,                        
    begin_date_    in  ref_type_bus_entity.begin_date    % type,
    end_date_      in  ref_type_bus_entity.end_date      % type,
    id_usr_        in  ref_type_bus_entity.id_usr        % type,    
    user_location_ in  ref_type_bus_entity.user_location % type,
    datlast_       in  ref_type_bus_entity.datlast       % type,
    do_commit_     in  integer default 1,
    id_            out ref_type_bus_entity.id % type,
    err_code       out number,
    err_msg        out varchar2
  )
  is
    ProcName    constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_TYPE_BUS_ENTITY';
    ref_code    Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_type_bus_entity';
    
    if /*(trim(code_) is null) or */(trim(name_ru_) is null) or (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if; 
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code('ref_type_bus_entity',rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if; */
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    id_ := seq_ref_type_bus_entity_id.nextval;    
                  
    insert into ref_type_bus_entity(
      id,
      rec_id,
      code,
      name_kz,
      name_ru,
      name_en,
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),
      nvl(rec_id_,id_),--code_,
      name_kz_,                 
      name_ru_, 
      name_en_,
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_
    );
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';
  end ref_insert_type_bus_entity;
  
  
  procedure ref_update_type_bus_entity(
    id_                  in  ref_type_bus_entity.id                   % type,
    rec_id_              in  ref_type_bus_entity.rec_id               % type,
    code_                in  ref_type_bus_entity.code                 % type,    
    name_kz_             in  ref_type_bus_entity.name_kz              % type,
    name_ru_             in  ref_type_bus_entity.name_ru              % type,
    name_en_             in  ref_type_bus_entity.name_en              % type,            
    begin_date_          in  ref_type_bus_entity.begin_date           % type,
    end_date_            in  ref_type_bus_entity.end_date             % type,
    id_usr_              in  ref_type_bus_entity.id_usr               % type,    
    user_location_       in  ref_type_bus_entity.user_location        % type,    
    datlast_             in  ref_type_bus_entity.datlast              % type,
    do_commit_           in  integer default 1,     
    err_code             out number,
    err_msg              out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_TYPE_BUS_ENTITY';
    v_have_chg        boolean;
    v_ref_data        ref_type_bus_entity %rowtype;
    ref_code          Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_type_bus_entity';
    v_have_chg := false;
        
    if (trim(code_) is null) or (trim(name_ru_) is null) or (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code('ref_type_bus_entity',rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;*/
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    /*report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;*/
    
    begin
      select *
        into v_ref_data
        from ref_type_bus_entity
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;

   if v_ref_data.id <> 0 then
      if nvl(v_ref_data.name_kz,' ') <> nvl(name_kz_,' ') or 
         nvl(v_ref_data.name_ru,' ') <> nvl(name_ru_,' ') or
         nvl(v_ref_data.name_en,' ') <> nvl(name_en_,' ') or           
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or 
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) then

        v_have_chg := true;

      end if;
    end if;
    
    if v_have_chg = true then
      update ref_type_bus_entity
         set --code          = code_,
             name_kz       = name_kz_,
             name_ru       = name_ru_,
             name_en       = name_en_,
             begin_date    = begin_date_,
             end_date      = end_date_,
             id_usr        = id_usr_,
             user_location = user_location_,
             sent_knd      = 0,
             datlast       = datlast_
       where id = id_;
             
      if do_commit_ = 1 then
        Commit;
      end if;
    end if;
      
    
  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';  
  end ref_update_type_bus_entity;
  
  
  procedure ref_delete_type_bus_entity(
    id_        in  ref_type_bus_entity.id  % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_TYPE_BUS_ENTITY';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_type_bus_entity',
                          /*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
  
    update ref_type_bus_entity 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_type_bus_entity;
  
  /* Справочник регионов */   
  procedure ref_read_region_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    Err_Code  out number,
    Err_Msg   out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_REGION_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select r.id,
             r.rec_id,
             r.code,
             r.name_kz,
             r.name_ru,
             r.name_en,
             r.oblast_name,
             r.begin_date,
             r.end_date,
             r.datlast,
             r.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             r.user_location,
             sk.name as sent_knd
        from v_ref_region r,
             f_users u,
             sent_knd sk
       where r.id_usr = u.user_id         
         and r.sent_knd = sk.sent_knd
         and (date_ is null or r.begin_date = (select max(t.begin_date)
                                                 from v_ref_region t
                                                where t.rec_id = r.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (r.end_date is null or r.end_date > date_))
       order by r.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_region_list;
  
  procedure ref_read_region_list_by_params(
    id_             in  ref_region.id % type,
    date_           in  Date,
    code_           in  ref_region.code % type,
    name_ru_        in  ref_region.name_ru % type,
    rec_id_         in  ref_region.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    Err_Code        out number,
    Err_Msg         out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_REGION_LIST_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    Open Cur for
      select r.id,
             r.rec_id,
             r.code,
             r.name_kz,
             r.name_ru,
             r.name_en,
             r.oblast_name,
             r.begin_date,
             r.end_date,
             r.datlast,
             r.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             r.user_location,
             sk.name as sent_knd
        from v_ref_region r,
             f_users u,
             sent_knd sk
       where r.id_usr = u.user_id         
         and r.sent_knd = sk.sent_knd         
         and r.rec_id = (select max(t1.rec_id)
                          from v_ref_region t1
                         where ((
                                  (search_all_ver_ = 1) and 
                                  (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name))) and
                                  (trim(code_) is null or upper(t1.code) like upper(trim(code_)) || '%')
                                 ) or 
                                 (
                                  (search_all_ver_ is null or search_all_ver_ = 0) and 
                                  (trim(name_ru_) is null or upper(r.name_ru) like upper(trim(v_name))) and
                                  (trim(code_) is null or upper(r.code) like upper(trim(code_)) || '%')
                                 ) 
                               )
                           and t1.rec_id = r.rec_id
                         )
         and (id_ is null or r.id = id_)
--         and (trim(code_) is null or upper(r.code) like upper(trim(code_)) || '%')
--         and (trim(name_ru_) is null or upper(r.name_ru) like upper(trim(v_name)))
         and (rec_id_ is null or r.rec_id = rec_id_)
         and (date_ is null or r.begin_date = (select max(t.begin_date)
                                                 from v_ref_region t
                                                where t.rec_id = r.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (r.end_date is null or r.end_date > date_))
       order by r.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_region_list_by_params;
  
  
  /* Справочник требований и обязательств*/  
  procedure ref_read_requirement_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    Err_Code  out number,
    Err_Msg   out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_REQUIREMENT_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select r.id,
             r.rec_id,
             r.code,
             r.name_kz,
             r.name_ru,
             r.name_en,
             r.begin_date,
             r.end_date,
             r.datlast,
             r.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             r.user_location,
             sk.name as sent_knd
        from v_ref_requirement r,
             f_users u,
             sent_knd sk
       where r.id_usr = u.user_id
         and r.sent_knd = sk.sent_knd         
         and (date_ is null or r.begin_date = (select max(t.begin_date)
                                                 from v_ref_requirement t
                                                where t.rec_id = r.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (r.end_date is null or r.end_date > date_))
       order by r.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_requirement_list;  
  
  procedure ref_read_req_list_by_params(
    id_             in  ref_requirement.id % type,
    date_           in  Date,
    name_ru_        in  ref_requirement.name_ru % type,
    rec_id_         in  ref_requirement.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    Err_Code        out number,
    Err_Msg         out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_REQ_LIST_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    Open Cur for
      select r.id,
             r.rec_id,
             r.code,
             r.name_kz,
             r.name_ru,
             r.name_en,
             r.begin_date,
             r.end_date,
             r.datlast,
             r.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             r.user_location,
             sk.name as sent_knd
        from v_ref_requirement r,
             f_users u,
             sent_knd sk
       where r.id_usr = u.user_id
         and r.sent_knd = sk.sent_knd
         and r.rec_id = (select max(t1.rec_id)
                          from v_ref_requirement t1
                         where ((
                                  (search_all_ver_ = 1) and 
                                  (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name)))
                                 ) or 
                                 (
                                  (search_all_ver_ is null or search_all_ver_ = 0) and 
                                  (trim(name_ru_) is null or upper(r.name_ru) like upper(trim(v_name)))
                                 ) 
                               )
                           and t1.rec_id = r.rec_id
                          )
         and (id_ is null or r.id = id_)
--         and (trim(name_ru_) is null or upper(r.name_ru) like upper(trim(v_name)))
         and (rec_id_ is null or r.rec_id = rec_id_)
         and (date_ is null or r.begin_date = (select max(t.begin_date)
                                                 from v_ref_requirement t
                                                where t.rec_id = r.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (r.end_date is null or r.end_date > date_))
       order by r.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_req_list_by_params;
  
  
  procedure ref_read_requirement_hst_list(
    id_      in  ref_requirement.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_REQUIREMENT_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select rh.id_hst,
             rh.id,
             rh.rec_id,
             rh.code,
             rh.name_kz,
             rh.name_ru,
             rh.name_en,
             rh.begin_date,
             rh.end_date,
             rh.datlast,
             rh.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             rh.type_change,
             tc.name as type_change_name,
             rh.user_location,
             sk.name as sent_knd
        from ref_requirement_hst rh,
             f_users u,
             type_change tc,
             sent_knd sk
       where rh.id_usr = u.user_id
         and rh.sent_knd = sk.sent_knd
         and rh.type_change = tc.type_change
         and rh.id = id_
       order by rh.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_requirement_hst_list;
  
  
  procedure ref_insert_requirement(
    rec_id_        in  ref_requirement.rec_id        % type,
    code_          in  ref_requirement.code          % type,    
    name_kz_       in  ref_requirement.name_kz       % type,
    name_ru_       in  ref_requirement.name_ru       % type,
    name_en_       in  ref_requirement.name_en       % type,            
    begin_date_    in  ref_requirement.begin_date    % type,
    end_date_      in  ref_requirement.end_date      % type,
    id_usr_        in  ref_requirement.id_usr        % type,    
    user_location_ in  ref_requirement.user_location % type,
    datlast_       in  ref_requirement.datlast       % type,
    do_commit_     in  integer default 1,
    id_            out ref_requirement.id % type,
    err_code       out number,
    err_msg        out varchar2
  )
  is
    ProcName    constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_REQUIREMENT';    
    ref_code    Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_requirement';
    
    if /*(trim(code_) is null) or */(trim(name_ru_) is null) or (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if; 
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code('ref_requirement',rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if; */
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
        
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    id_ := seq_ref_requirement_id.nextval;
                  
    insert into ref_requirement(
      id,
      rec_id,
      code,
      name_kz,
      name_ru,
      name_en,
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),
      nvl(rec_id_,id_),--code_,
      name_kz_,                 
      name_ru_, 
      name_en_,
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_
    );
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';
  end ref_insert_requirement;
  
  
  procedure ref_update_requirement(
    id_              in  ref_requirement.id                   % type,
    rec_id_          in  ref_requirement.rec_id               % type,
    code_            in  ref_requirement.code                 % type,    
    name_kz_         in  ref_requirement.name_kz              % type,
    name_ru_         in  ref_requirement.name_ru              % type,
    name_en_         in  ref_requirement.name_en              % type,            
    begin_date_      in  ref_requirement.begin_date           % type,
    end_date_        in  ref_requirement.end_date             % type,
    id_usr_          in  ref_requirement.id_usr               % type,    
    user_location_   in  ref_requirement.user_location        % type,
    datlast_         in  ref_requirement.datlast              % type,
    do_commit_       in  integer default 1,     
    err_code         out number,
    err_msg          out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_REQUIREMENT';
    v_have_chg        boolean;
    v_ref_data        ref_requirement %rowtype;
    ref_code          Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_requirement';
    
    if (trim(code_) is null) or (trim(name_ru_) is null) or (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code('ref_requirement',rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;*/
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    /*report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;*/
    
    begin
      select *
        into v_ref_data
        from ref_requirement
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;


  if v_ref_data.id <> 0 then
      if nvl(v_ref_data.name_kz,' ') <> nvl(name_kz_,' ') or 
         nvl(v_ref_data.name_ru,' ') <> nvl(name_ru_,' ') or
         nvl(v_ref_data.name_en,' ') <> nvl(name_en_,' ') or           
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or 
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) then

        v_have_chg := true;

      end if;
    end if;
    
    if v_have_chg = true then
      update ref_requirement
         set --code          = code_,
             name_kz       = name_kz_,
             name_ru       = name_ru_,
             name_en       = name_en_,
             begin_date    = begin_date_,
             end_date      = end_date_,
             id_usr        = id_usr_,
             user_location = user_location_,
             sent_knd      = 0,
             datlast       = datlast_
       where id = id_;
             
      if do_commit_ = 1 then
        Commit;
      end if;
    end if;
    
  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';  
  end ref_update_requirement;
  
  
  procedure ref_delete_requirement(
    id_        in  ref_requirement.id  % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_REQUIREMENT';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_requirement',
                          /*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
  
    update ref_requirement 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_requirement;
  
  
  /* Виды  обеспечения */  
  procedure ref_read_type_provide_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    Err_Code  out number,
    Err_Msg   out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_TYPE_PROVIDE_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select tp.id,
             tp.rec_id,
             tp.code,
             tp.name_kz,
             tp.name_ru,
             tp.name_en,
             tp.begin_date,
             tp.end_date,
             tp.datlast,
             tp.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             tp.user_location,
             sk.name as sent_knd
        from v_ref_type_provide tp,
             f_users u,
             sent_knd sk
       where tp.id_usr = u.user_id
         and tp.sent_knd = sk.sent_knd
         and (date_ is null or tp.begin_date = (select max(t.begin_date)
                                                 from v_ref_type_provide t
                                                where t.rec_id = tp.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (tp.end_date is null or tp.end_date > date_))
       order by tp.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_type_provide_list;  
  
  procedure ref_read_t_p_list_by_params(
    id_             in  ref_type_provide.id % type,
    date_           in  Date,
    code_           in  ref_type_provide.code % type,
    name_ru_        in  ref_type_provide.name_ru % type,
    rec_id_         in  ref_type_provide.rec_id % type, 
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    Err_Code        out number,
    Err_Msg         out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_T_P_LIST_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    Open Cur for
      select tp.id,
             tp.rec_id,
             tp.code,
             tp.name_kz,
             tp.name_ru,
             tp.name_en,
             tp.begin_date,
             tp.end_date,
             tp.datlast,
             tp.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             tp.user_location,
             sk.name as sent_knd
        from v_ref_type_provide tp,
             f_users u,
             sent_knd sk
       where tp.id_usr = u.user_id
         and tp.sent_knd = sk.sent_knd
         and tp.rec_id = (select max(t1.rec_id)
                            from v_ref_type_provide t1
                           where ((
                                    (search_all_ver_ = 1) and 
                                    (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name))) and
                                    (trim(code_) is null or upper(t1.code) like upper(trim(code_)) || '%')
                                   ) or 
                                   (
                                    (search_all_ver_ is null or search_all_ver_ = 0) and 
                                    (trim(name_ru_) is null or upper(tp.name_ru) like upper(trim(v_name))) and
                                    (trim(code_) is null or upper(tp.code) like upper(trim(code_)) || '%')
                                   ) 
                                 )
                             and t1.rec_id = tp.rec_id
                           )
         and (id_ is null or tp.id = id_)
/*         and (trim(code_) is null or upper(tp.code) like upper(trim(code_)) || '%')
         and (trim(name_ru_) is null or upper(tp.name_ru) like upper(trim(v_name)))*/
         and (rec_id_ is null or tp.rec_id = rec_id_)
         and (date_ is null or tp.begin_date = (select max(t.begin_date)
                                                 from v_ref_type_provide t
                                                where t.rec_id = tp.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (tp.end_date is null or tp.end_date > date_))
       order by tp.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_t_p_list_by_params;
  
  
  procedure ref_read_type_provide_hst_list(
    id_      in  ref_type_provide.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_TYPE_PROVIDE_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select tph.id_hst,
             tph.id,
             tph.rec_id,
             tph.code,
             tph.name_kz,
             tph.name_ru,
             tph.name_en,
             tph.begin_date,
             tph.end_date,
             tph.datlast,
             tph.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             tph.type_change,
             tc.name as type_change_name,
             tph.user_location,
             sk.name as sent_knd
        from ref_type_provide_hst tph,
             f_users u,
             type_change tc,
             sent_knd sk
       where tph.id_usr = u.user_id
         and tph.type_change = tc.type_change
         and tph.sent_knd = sk.sent_knd
         and tph.id = id_
       order by tph.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_type_provide_hst_list;
  
  
  procedure ref_insert_type_provide(
    rec_id_        in  ref_type_provide.rec_id        % type,
    code_          in  ref_type_provide.code          % type,    
    name_kz_       in  ref_type_provide.name_kz       % type,
    name_ru_       in  ref_type_provide.name_ru       % type,
    name_en_       in  ref_type_provide.name_en       % type,            
    begin_date_    in  ref_type_provide.begin_date    % type,
    end_date_      in  ref_type_provide.end_date      % type,
    id_usr_        in  ref_type_provide.id_usr        % type,    
    user_location_ in  ref_type_provide.user_location % type,
    datlast_       in  ref_type_provide.datlast       % type,
    do_commit_     in  integer default 1,     
    id_            out ref_type_provide.id % type,
    err_code       out number,
    err_msg        out varchar2
  )
  is
    ProcName   constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_TYPE_PROVIDE';    
    ref_code   Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_type_provide';
    
    if (trim(code_) is null) or (trim(name_ru_) is null) or (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;        
    
    /*if ref_check_code('ref_type_provide',rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;*/
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    id_ := seq_ref_type_provide_id.nextval;    
                  
    insert into ref_type_provide(
      id,
      rec_id,
      code,
      name_kz,
      name_ru,
      name_en,
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),
      code_,
      name_kz_,                 
      name_ru_, 
      name_en_,
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_
    );
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';      
  end ref_insert_type_provide;
  
  
 procedure ref_update_type_provide(
    id_               in  ref_type_provide.id                   % type,
    rec_id_           in  ref_type_provide.rec_id               % type,
    code_             in  ref_type_provide.code                 % type,    
    name_kz_          in  ref_type_provide.name_kz              % type,
    name_ru_          in  ref_type_provide.name_ru              % type,
    name_en_          in  ref_type_provide.name_en              % type,            
    begin_date_       in  ref_type_provide.begin_date           % type,
    end_date_         in  ref_type_provide.end_date             % type,
    id_usr_           in  ref_type_provide.id_usr               % type,    
    user_location_    in  ref_type_provide.user_location        % type,    
    datlast_          in  ref_type_provide.datlast              % type,
    do_commit_        in  integer default 1,     
    err_code          out number,
    err_msg           out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_TYPE_PROVIDE';
    v_have_chg        boolean;
    v_ref_data        ref_type_provide %rowtype;
    ref_code          Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_type_provide';
    v_have_chg := false;
    
    if (code_ is null) or (name_ru_ is null) or (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;

    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;*/
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          /*begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
    
    begin
      select *
        into v_ref_data
        from ref_type_provide
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;


    if v_ref_data.id <> 0 then
      if nvl(v_ref_data.name_kz,' ') <> nvl(name_kz_,' ') or 
         nvl(v_ref_data.name_ru,' ') <> nvl(name_ru_,' ') or
         nvl(v_ref_data.name_en,' ') <> nvl(name_en_,' ') or           
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or 
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) then

        v_have_chg := true;

      end if;
    end if;
    
    if v_have_chg = true then
      update ref_type_provide
         set code          = code_,
             name_kz       = name_kz_,
             name_ru       = name_ru_,
             name_en       = name_en_,
             begin_date    = begin_date_,
             end_date      = end_date_,
             id_usr        = id_usr_,
             user_location = user_location_,
             sent_knd      = 0,
             datlast       = datlast_
       where id = id_;
             
      if do_commit_ = 1 then
        Commit;
      end if;
    end if;     

  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';  
  end ref_update_type_provide;
  
  
  procedure ref_delete_type_provide(
    id_        in  ref_type_provide.id  % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_TYPE_PROVIDE';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_type_provide',
                          /*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
  
    update ref_type_provide 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_type_provide;
  
  /* Типы сделок */
  procedure ref_read_trans_types_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_TRANS_TYPES_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select tt.id,
             tt.rec_id,
             tt.code,
             tt.name_kz,
             tt.name_ru,
             tt.name_en,
             tt.kind_of_activity,
             tt.short_name,
             tt.begin_date,
             tt.end_date,
             tt.datlast,
             tt.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             tt.user_location,
             sk.name as sent_knd
        from v_ref_trans_types tt,
             f_users u,
             sent_knd sk
       where tt.id_usr = u.user_id         
         and tt.sent_knd = sk.sent_knd
         and (date_ is null or tt.begin_date = (select max(t.begin_date)
                                                 from v_ref_trans_types t
                                                where t.rec_id = tt.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (tt.end_date is null or tt.end_date > date_))
       order by tt.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_trans_types_list;  
  
  procedure ref_read_t_t_list_by_params(
    id_               in  ref_trans_types.id % type,
    date_             in  Date,
    name_ru_          in  ref_trans_types.name_ru % type,
    kind_of_activity_ in  ref_trans_types.kind_of_activity % type,
    rec_id_           in  ref_trans_types.rec_id % type,
    search_all_ver_   in  Integer default 0,
    Cur               out sys_refcursor,
    err_code          out number,
    err_msg           out varchar2    
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_T_T_LIST_BY_PARAMS';
    v_name varchar2(524);
    v_kind_of_activity varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    v_kind_of_activity := '%' || kind_of_activity_ || '%';
    
    Open Cur for
      select tt.id,
             tt.rec_id,
             tt.code,
             tt.name_kz,
             tt.name_ru,
             tt.name_en,
             tt.kind_of_activity,
             tt.short_name,
             tt.begin_date,
             tt.end_date,
             tt.datlast,
             tt.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             tt.user_location,
             sk.name as sent_knd
        from v_ref_trans_types tt,
             f_users u,
             sent_knd sk
       where tt.id_usr = u.user_id         
         and tt.sent_knd = sk.sent_knd         
         and tt.rec_id = (select max(t1.rec_id)
                            from v_ref_trans_types t1
                           where ((
                                    (search_all_ver_ = 1) and 
                                    (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name))) and
                                    (trim(kind_of_activity_) is null or upper(t1.kind_of_activity) like upper(trim(v_kind_of_activity)))
                                   ) or 
                                   (
                                    (search_all_ver_ is null or search_all_ver_ = 0) and 
                                    (trim(name_ru_) is null or upper(tt.name_ru) like upper(trim(v_name))) and
                                    (trim(kind_of_activity_) is null or upper(tt.kind_of_activity) like upper(trim(v_kind_of_activity)))
                                   ) 
                                 )
                             and t1.rec_id = tt.rec_id
                           )
         and (id_ is null or tt.id = id_)
         /*and (trim(name_ru_) is null or upper(tt.name_ru) like upper(trim(v_name)))
         and (trim(kind_of_activity_) is null or upper(tt.kind_of_activity) like upper(trim(v_kind_of_activity)))*/
         and (rec_id_ is null or tt.rec_id = rec_id_)
         and (date_ is null or tt.begin_date = (select max(t.begin_date)
                                                 from v_ref_trans_types t
                                                where t.rec_id = tt.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (tt.end_date is null or tt.end_date > date_))
       order by tt.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_t_t_list_by_params;  
  
  
  procedure ref_read_trans_types_hst_list(
    id_      in  ref_trans_types.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_TRANS_TYPES_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select tth.id_hst,
             tth.id,
             tth.rec_id,
             tth.code,
             tth.name_kz,
             tth.name_ru,
             tth.name_en,
             tth.kind_of_activity,
             tth.short_name,
             tth.begin_date,
             tth.end_date,
             tth.datlast,
             tth.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             tth.type_change,
             tc.name as type_change_name,
             tth.user_location,
             sk.name as sent_knd
        from ref_trans_types_hst tth,
             f_users u,
             type_change tc,
             sent_knd sk
       where tth.id_usr = u.user_id
         and tth.sent_knd = sk.sent_knd
         and tth.type_change = tc.type_change
         and tth.id = id_
       order by tth.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_trans_types_hst_list;
  
  
  procedure ref_insert_trans_types(
    rec_id_           in  ref_trans_types.rec_id           % type,
    code_             in  ref_trans_types.code             % type,    
    name_kz_          in  ref_trans_types.name_kz          % type,
    name_ru_          in  ref_trans_types.name_ru          % type,
    name_en_          in  ref_trans_types.name_en          % type,            
    kind_of_activity_ in  ref_trans_types.kind_of_activity % type,
    short_name_       in  ref_trans_types.short_name       % type,
    begin_date_       in  ref_trans_types.begin_date       % type,
    end_date_         in  ref_trans_types.end_date         % type,
    id_usr_           in  ref_trans_types.id_usr           % type,    
    user_location_    in  ref_trans_types.user_location    % type,
    datlast_          in  ref_trans_types.datlast          % type,
    do_commit_        in  integer default 1,
    id_               out ref_trans_types.id % type,
    err_code          out number,
    err_msg           out varchar2
  )
  is
    ProcName    constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_TRANS_TYPES';
    ref_code    Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_trans_types';
    
    if /*(trim(code_) is null) or */(trim(name_ru_) is null) or (begin_date_ is null) or (trim(kind_of_activity_) is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if; 
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if; */
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    id_ := seq_ref_trans_types_id.nextval;
                  
    insert into ref_trans_types(
      id,
      rec_id,
      code,
      name_kz,
      name_ru,
      name_en,
      kind_of_activity,
      short_name,
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),
      nvl(rec_id_,id_),--code_,
      name_kz_,                 
      name_ru_, 
      name_en_,
      kind_of_activity_,
      short_name_,
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_
    );
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';      
  end ref_insert_trans_types;
  
  
 procedure ref_update_trans_types(
    id_               in  ref_trans_types.id               % type,
    rec_id_           in  ref_trans_types.rec_id           % type,
    code_             in  ref_trans_types.code             % type,    
    name_kz_          in  ref_trans_types.name_kz          % type,
    name_ru_          in  ref_trans_types.name_ru          % type,
    name_en_          in  ref_trans_types.name_en          % type,            
    kind_of_activity_ in  ref_trans_types.kind_of_activity % type,
    short_name_       in  ref_trans_types.short_name       % type,
    begin_date_       in  ref_trans_types.begin_date       % type,
    end_date_         in  ref_trans_types.end_date         % type,
    id_usr_           in  ref_trans_types.id_usr           % type,    
    user_location_    in  ref_trans_types.user_location    % type,    
    datlast_          in  ref_trans_types.datlast          % type,
    do_commit_        in  integer default 1,     
    err_code          out number,
    err_msg           out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_TRANS_TYPES';
    v_have_chg        boolean;
    v_ref_data        ref_trans_types %rowtype;
    ref_code          Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_have_chg := false;
    ref_code := 'ref_trans_types';
    
    if (trim(code_) is null) or (trim(name_ru_) is null) or (begin_date_ is null) or (trim(kind_of_activity_) is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;*/
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    /*report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;*/
    
    begin
      select *
        into v_ref_data
        from ref_trans_types
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;


    if v_ref_data.id <> 0 then
      if nvl(v_ref_data.name_kz,' ') <> nvl(name_kz_,' ') or 
         nvl(v_ref_data.name_ru,' ') <> nvl(name_ru_,' ') or
         nvl(v_ref_data.name_en,' ') <> nvl(name_en_,' ') or 
         nvl(v_ref_data.kind_of_activity,0) <> nvl(kind_of_activity_,0) or
         nvl(v_ref_data.short_name,' ') <> nvl(short_name_,' ') or
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or 
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) then

        v_have_chg := true;

      end if;
    end if;
    
    if v_have_chg = true then
      update ref_trans_types
         set --code             = code_,
             name_kz          = name_kz_,
             name_ru          = name_ru_,
             name_en          = name_en_,
             kind_of_activity = kind_of_activity_,
             short_name       = short_name_,
             begin_date       = begin_date_,
             end_date         = end_date_,
             id_usr           = id_usr_,
             user_location    = user_location_,
             sent_knd         = 0,
             datlast          = datlast_
       where id = id_;
             
      if do_commit_ = 1 then
        Commit;
      end if;
    end if;
    
  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';
  end ref_update_trans_types;
  
  
  procedure ref_delete_trans_types(
    id_        in  ref_trans_types.id  % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_TRANS_TYPES';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_trans_types',
                          /*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
  
    update ref_trans_types 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_trans_types;
  
    /* Балансовые счета для отчетов о сделках */
  procedure ref_read_balance_acc_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_BALANCE_ACC_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select ba.id,
             ba.rec_id,
             level,
             ba.code,
             ba.parent_code,
             ba.level_code,
             ba.name_kz,
             ba.name_ru,
             ba.name_en,
             ba.short_name_kz,
             ba.short_name_ru,
             ba.short_name_en,          
             ba.begin_date,
             ba.end_date,
             ba.datlast,
             ba.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             ba.user_location,
             sk.name as sent_knd
        from v_ref_balance_account ba,
             f_users u,
             sent_knd sk
       where ba.id_usr = u.user_id(+)
         and ba.sent_knd = sk.sent_knd         
         and (date_ is null or ba.begin_date = (select max(t.begin_date)
                                                 from v_ref_balance_account t
                                                where t.rec_id = ba.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (ba.end_date is null or ba.end_date > date_))
       start with ba.parent_code is null
       connect by prior ba.code = ba.parent_code
       order siblings by ba.code;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_balance_acc_list;  
  
   procedure ref_read_b_acc_l_by_params(
    id_             in  ref_balance_account.id % type,
    date_           in  Date,
    name_ru_        in  ref_balance_account.name_ru % type,
    code_           in  ref_balance_account.code % type,
    parent_code_    in  ref_balance_account.parent_code % type,
    rec_id_         in  ref_balance_account.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_B_ACC_L_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    Open Cur for
      select ba.id,
             ba.rec_id,
             ba.code,
             ba.parent_code,
             ba.level_code,
             ba.name_kz,
             ba.name_ru,
             ba.name_en,
             ba.short_name_kz,
             ba.short_name_ru,
             ba.short_name_en,
             ba.begin_date,
             ba.end_date,
             ba.datlast,
             ba.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             ba.user_location,
             sk.name as sent_knd
        from v_ref_balance_account ba,
             f_users u,
             sent_knd sk
       where ba.id_usr = u.user_id(+)
         and ba.sent_knd = sk.sent_knd 
         and ba.rec_id = (select max(ba1.rec_id)
                            from v_ref_balance_account ba1
                           where ((
                                    (search_all_ver_ = 1) and 
                                    (trim(name_ru_) is null or upper(ba1.name_ru) like upper(trim(v_name))) and
                                    (trim(code_) is null or upper(ba1.code) like upper(trim(code_)) || '%') and
                                    (trim(parent_code_) is null or upper(ba1.parent_code) like upper(trim(parent_code_)) || '%')
                                   ) or 
                                   (
                                    (search_all_ver_ is null or search_all_ver_ = 0) and 
                                    (trim(name_ru_) is null or upper(ba.name_ru) like upper(trim(v_name))) and
                                    (trim(code_) is null or upper(ba.code) like upper(trim(code_)) || '%') and
                                    (trim(parent_code_) is null or upper(ba.parent_code) like upper(trim(parent_code_)) || '%')
                                   ) 
                                 )
                             and ba1.rec_id = ba.rec_id
                           )
         and (id_ is null or ba.id = id_)
         and (rec_id_ is null or ba.rec_id = rec_id_)
         and (date_ is null or ba.begin_date = (select max(t.begin_date)
                                                 from v_ref_balance_account t
                                                where t.rec_id = ba.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (ba.end_date is null or ba.end_date > date_))
         and ba.id in (select ba.id
                         from v_ref_balance_account ba                        
                        /*start with ((trim(name_ru_) is null or upper(ba.name_ru) like upper(trim(v_name)))
                                and (trim(code_) is null or upper(ba.code) like upper(trim(code_)) || '%'))*/                                                               
                      connect by prior ba.parent_code = ba.code
                        group by ba.id)
          order by ba.code;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_b_acc_l_by_params;  
  
  
  procedure ref_read_balance_acc_hst_list(
    id_      in  ref_balance_account.id  % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_BALANCE_ACC_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select bah.id_hst,
             bah.id,
             bah.rec_id,
             bah.code,
             bah.parent_code,
             bah.level_code,
             bah.name_kz,
             bah.name_ru,
             bah.name_en,
             bah.short_name_kz,
             bah.short_name_ru,
             bah.short_name_en,
             bah.begin_date,
             bah.end_date,
             bah.datlast,
             bah.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             bah.type_change,
             tc.name as type_change_name,
             bah.user_location,
             sk.name as sent_knd
        from ref_balance_account_hst bah,
             f_users u,
             type_change tc,
             sent_knd sk
       where bah.id_usr = u.user_id
         and bah.sent_knd = sk.sent_knd
         and bah.type_change = tc.type_change
         and bah.id = id_
       order by bah.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_balance_acc_hst_list;
  
  procedure ref_read_bal_acc_last_rec_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_BAL_ACC_LAST_REC_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select ba.id,
             ba.rec_id,
             level,
             ba.code,
             ba.parent_code,
             ba.level_code,
             ba.name_kz,
             ba.name_ru,
             ba.name_en,
             ba.short_name_kz,
             ba.short_name_ru,
             ba.short_name_en,
             ba.begin_date,
             ba.end_date,
             ba.datlast,
             ba.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             ba.user_location,
             sk.name as sent_knd
        from v_ref_balance_account ba,
             f_users u,
             sent_knd sk
       where ba.id_usr = u.user_id(+)
         and ba.sent_knd = sk.sent_knd         
         and not exists (select *
                           from ref_balance_account d
                          where ba.code = d.parent_code)
         and (date_ is null or ba.begin_date = (select max(t.begin_date)
                                                  from v_ref_balance_account t
                                                 where t.rec_id = ba.rec_id
                                                   and t.begin_date <= date_))
         and (date_ is null or (ba.end_date is null or ba.end_date > date_))
       start with ba.parent_code is null
       connect by prior ba.code = ba.parent_code
       order siblings by ba.code;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_bal_acc_last_rec_list;  
  
  
  procedure ref_insert_balance_acc(
    rec_id_           in  ref_balance_account.rec_id           % type,
    code_             in  ref_balance_account.code             % type,    
    parent_code_      in  ref_balance_account.parent_code      % type,
    level_code_       in  ref_balance_account.level_code       % type,
    name_kz_          in  ref_balance_account.name_kz          % type,
    name_ru_          in  ref_balance_account.name_ru          % type,
    name_en_          in  ref_balance_account.name_en          % type,                    
    short_name_kz_    in  ref_balance_account.short_name_kz    % type,
    short_name_ru_    in  ref_balance_account.short_name_ru    % type,
    short_name_en_    in  ref_balance_account.short_name_en    % type,
    begin_date_       in  ref_balance_account.begin_date       % type,    
    end_date_         in  ref_balance_account.end_date         % type,
    id_usr_           in  ref_balance_account.id_usr           % type,    
    user_location_    in  ref_balance_account.user_location    % type,
    datlast_          in  ref_balance_account.datlast          % type,
    do_commit_        in  integer default 1,
    id_               out ref_balance_account.id % type,
    err_code          out number,
    err_msg           out varchar2
  )
  is
    ProcName     constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_BALANCE_ACC';    
    ref_code     Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_balance_account';
    
    if (trim(code_) is null) or 
       (trim(name_ru_) is null) or 
       (begin_date_ is null) or 
       (trim(level_code_) is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if; 
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if; 
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    id_ := seq_ref_balance_acc_id.nextval;
                  
    insert into ref_balance_account(
      id,
      rec_id,
      code,
      parent_code,
      level_code,
      name_kz,
      name_ru,
      name_en, 
      short_name_kz,
      short_name_ru,
      short_name_en,     
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),
      code_,
      parent_code_,
      level_code_,
      name_kz_,
      name_ru_,
      name_en_,
      short_name_kz_,
      short_name_ru_,
      short_name_en_,
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_
    );
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';      
  end ref_insert_balance_acc;
  
  
  procedure ref_update_balance_acc(
    id_               in  ref_balance_account.id               % type,
    rec_id_           in  ref_balance_account.rec_id           % type,
    code_             in  ref_balance_account.code             % type,        
    name_kz_          in  ref_balance_account.name_kz          % type,
    name_ru_          in  ref_balance_account.name_ru          % type,
    name_en_          in  ref_balance_account.name_en          % type,                    
    short_name_kz_    in  ref_balance_account.short_name_kz    % type,
    short_name_ru_    in  ref_balance_account.short_name_ru    % type,
    short_name_en_    in  ref_balance_account.short_name_en    % type,
    begin_date_       in  ref_balance_account.begin_date       % type,
    end_date_         in  ref_balance_account.end_date         % type,
    id_usr_           in  ref_balance_account.id_usr           % type,    
    user_location_    in  ref_balance_account.user_location    % type,    
    datlast_          in  ref_balance_account.datlast          % type,
    do_commit_        in  integer default 1,     
    err_code          out number,
    err_msg           out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_BALANCE_ACC';
    v_have_chg        boolean;
    v_ref_data        ref_balance_account %rowtype;
    ref_code          Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_balance_account';
    v_have_chg := false;
    
    if (trim(code_) is null) or 
       (trim(name_ru_) is null) or 
       (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;*/
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    /*report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;*/
    
    begin
      select *
        into v_ref_data
        from ref_balance_account
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;


    if v_ref_data.id <> 0 then
      if nvl(v_ref_data.code,' ') <> nvl(code_,' ') or
         nvl(v_ref_data.name_kz,' ') <> nvl(name_kz_,' ') or 
         nvl(v_ref_data.name_ru,' ') <> nvl(name_ru_,' ') or
         nvl(v_ref_data.name_en,' ') <> nvl(name_en_,' ') or 
         nvl(v_ref_data.short_name_kz,' ') <> nvl(short_name_kz_,' ') or
         nvl(v_ref_data.short_name_ru,' ') <> nvl(short_name_ru_,' ') or 
         nvl(v_ref_data.short_name_en,' ') <> nvl(short_name_en_,' ') or
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or 
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) then

        v_have_chg := true;

      end if;
    end if;
    
    if v_have_chg = true then    
      update ref_balance_account
         set code             = code_,
             name_kz          = name_kz_,
             name_ru          = name_ru_,
             name_en          = name_en_,
             short_name_kz    = short_name_kz_,
             short_name_ru    = short_name_ru_,
             short_name_en    = short_name_en_,
             begin_date       = begin_date_,
             end_date         = end_date_,
             id_usr           = id_usr_,
             user_location    = user_location_,
             sent_knd         = 0,
             datlast          = datlast_
       where id = id_;
             
      if do_commit_ = 1 then
        Commit;
      end if;
    end if;

  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';
  end ref_update_balance_acc;
  
  
  procedure ref_delete_balance_acc(
    id_         in  ref_balance_account.id  % type,
    do_commit_  in  integer default 1,     
    err_code    out number,
    err_msg     out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_BALANCE_ACC';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_balance_account',
                          /*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
  
    update ref_balance_account 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_balance_acc;
     
  
  /* Признаки связанности с подотчетной организацией особыми отношениями*/
  procedure ref_read_conn_org_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2     
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_CONN_ORG_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select co.id,
             co.rec_id,
             co.code,
             co.name_kz,
             co.name_ru,
             co.name_en,             
             co.short_name,
             co.begin_date,
             co.end_date,
             co.datlast,
             co.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             co.user_location,
             sk.name as sent_knd
        from v_ref_conn_org co,
             f_users u,
             sent_knd sk
       where co.id_usr = u.user_id
         and co.sent_knd = sk.sent_knd
         and (date_ is null or co.begin_date = (select max(t.begin_date)
                                                 from v_ref_conn_org t
                                                where t.rec_id = co.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (co.end_date is null or co.end_date > date_))
       order by co.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_conn_org_list;  
  
  procedure ref_read_conn_org_l_by_params(
    id_             in  ref_conn_org.id % type,
    date_           in  Date,
    code_           in  ref_conn_org.code % type,
    name_ru_        in  ref_conn_org.name_ru % type,
    rec_id_         in  ref_conn_org.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2     
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_CONN_ORG_L_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    Open Cur for
      select co.id,
             co.rec_id,
             co.code,
             co.name_kz,
             co.name_ru,
             co.name_en,             
             co.short_name,
             co.begin_date,
             co.end_date,          
             co.datlast,
             co.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             co.user_location,
             sk.name as sent_knd
        from v_ref_conn_org co,
             f_users u,
             sent_knd sk
       where co.id_usr = u.user_id         
         and co.sent_knd = sk.sent_knd         
         and co.rec_id = (select max(t1.rec_id)
                            from v_ref_conn_org t1
                           where ((
                                    (search_all_ver_ = 1) and 
                                    (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name))) and
                                    (trim(code_) is null or upper(t1.code) like upper(trim(code_)) || '%')
                                   ) or 
                                   (
                                    (search_all_ver_ is null or search_all_ver_ = 0) and 
                                    (trim(name_ru_) is null or upper(co.name_ru) like upper(trim(v_name))) and
                                    (trim(code_) is null or upper(co.code) like upper(trim(code_)) || '%')
                                   ) 
                                 )
                             and t1.rec_id = co.rec_id
                           )
         and (id_ is null or co.id = id_)
         /*and (trim(code_) is null or upper(co.code) like upper(trim(code_)) || '%')
         and (trim(name_ru_) is null or upper(co.name_ru) like upper(trim(v_name)))*/
         and (rec_id_ is null or co.rec_id = rec_id_)
         and (date_ is null or co.begin_date = (select max(t.begin_date)
                                                 from v_ref_conn_org t
                                                where t.rec_id = co.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (co.end_date is null or co.end_date > date_))
       order by co.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_conn_org_l_by_params; 
  
  
  procedure ref_read_conn_org_hst_list(
    id_      in  ref_conn_org.id  % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_CONN_ORG_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select coh.id_hst,
             coh.id,
             coh.rec_id,
             coh.code,
             coh.name_kz,
             coh.name_ru,
             coh.name_en,
             coh.short_name,
             coh.begin_date,
             coh.end_date,
             coh.datlast,
             coh.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             coh.type_change,
             tc.name as type_change_name,
             coh.user_location,
             sk.name as sent_knd
        from ref_conn_org_hst coh,
             f_users u,
             type_change tc,
             sent_knd sk
       where coh.id_usr = u.user_id
         and coh.sent_knd = sk.sent_knd
         and coh.type_change = tc.type_change
         and coh.id = id_
       order by coh.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_conn_org_hst_list;
  
  
  procedure ref_insert_conn_org(
    rec_id_        in  ref_conn_org.rec_id           % type,
    code_          in  ref_conn_org.code             % type,    
    name_kz_       in  ref_conn_org.name_kz          % type,
    name_ru_       in  ref_conn_org.name_ru          % type,
    name_en_       in  ref_conn_org.name_en          % type,            
    short_name_    in  ref_conn_org.short_name       % type,
    begin_date_    in  ref_conn_org.begin_date       % type,
    end_date_      in  ref_conn_org.end_date         % type,
    id_usr_        in  ref_conn_org.id_usr           % type,    
    user_location_ in  ref_conn_org.user_location    % type,
    datlast_       in  ref_conn_org.datlast          % type,
    do_commit_     in  integer default 1,
    id_            out ref_conn_org.id % type,
    err_code       out number,
    err_msg        out varchar2
  )
  is
    ProcName     constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_CONN_ORG';    
    ref_code     Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_conn_org';
    
    if (trim(code_) is null) or 
       (trim(name_ru_) is null) or 
       (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if; 
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if; */
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    id_ := seq_ref_conn_org_id.nextval;
                  
    insert into ref_conn_org(
      id,
      rec_id,
      code,
      name_kz,
      name_ru,
      name_en,
      short_name,
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),
      code_,
      name_kz_,                 
      name_ru_, 
      name_en_,
      short_name_,
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_
    );
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';
  end ref_insert_conn_org;
   

  procedure ref_update_conn_org(
    id_            in  ref_conn_org.id            	 % type,
    rec_id_        in  ref_conn_org.rec_id           % type,
    code_          in  ref_conn_org.code             % type,    
    name_kz_       in  ref_conn_org.name_kz          % type,
    name_ru_       in  ref_conn_org.name_ru          % type,
    name_en_       in  ref_conn_org.name_en          % type,                
    short_name_    in  ref_conn_org.short_name       % type,
    begin_date_    in  ref_conn_org.begin_date       % type,
    end_date_      in  ref_conn_org.end_date         % type,
    id_usr_        in  ref_conn_org.id_usr           % type,    
    user_location_ in  ref_conn_org.user_location    % type,    
    datlast_       in  ref_conn_org.datlast          % type,
    do_commit_     in  integer default 1,     
    err_code       out number,
    err_msg        out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_CONN_ORG';
    v_have_chg        boolean;
    v_ref_data        ref_conn_org %rowtype;
    ref_code          Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_have_chg := false;
    ref_code := 'ref_conn_org';
    
    if (trim(code_) is null) or 
       (trim(name_ru_) is null) or 
       (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;*/
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    /*report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;*/
    
    begin
      select *
        into v_ref_data
        from ref_conn_org
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;


    if v_ref_data.id <> 0 then
      if nvl(v_ref_data.code,' ') <> nvl(code_,' ') or
         nvl(v_ref_data.name_kz,' ') <> nvl(name_kz_,' ') or 
         nvl(v_ref_data.name_ru,' ') <> nvl(name_ru_,' ') or
         nvl(v_ref_data.name_en,' ') <> nvl(name_en_,' ') or 
         nvl(v_ref_data.short_name,' ') <> nvl(short_name_,' ') or         
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or 
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) then

        v_have_chg := true;

      end if;
    end if;
    
    if v_have_chg = true then
      update ref_conn_org
         set code           = code_,
             name_kz        = name_kz_,
             name_ru        = name_ru_,
             name_en        = name_en_,           
             short_name     = short_name_,
             begin_date     = begin_date_,
             end_date       = end_date_,
             id_usr         = id_usr_,
             user_location  = user_location_,
             sent_knd       = 0,
             datlast        = datlast_
       where id = id_;
             
      if do_commit_ = 1 then
        Commit;
      end if;
    end if;

  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';
  end ref_update_conn_org;
  
  
  procedure ref_delete_conn_org(
    id_        in  ref_conn_org.id  % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_CONN_ORG';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_conn_org',
                          /*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
  
    update ref_conn_org 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_conn_org;
  
  
  /* Подразделения НБ РК*/  
  procedure ref_read_department_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2    
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_DEPARTMENT_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select d.id,
             d.rec_id,
             d.code,             
             d.name_kz,
             d.name_ru,
             d.name_en,             
             d.begin_date,
             d.end_date,
             d.ref_department_type,
             dt.name_ru as dep_type_name,
             d.datlast,
             d.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             d.user_location,
             sk.name as sent_knd
        from v_ref_department d,
             v_ref_department_type dt,
             f_users u,
             sent_knd sk
       where d.id_usr = u.user_id         
         and d.sent_knd = sk.sent_knd
         and d.ref_department_type = dt.id
         and (date_ is null or d.begin_date = (select max(t.begin_date)
                                                 from v_ref_department t
                                                where t.rec_id = d.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (d.end_date is null or d.end_date > date_))
       order by d.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_department_list;
  
  procedure ref_read_dep_list_by_params(
    id_             in  ref_department.id % type,
    date_           in  Date,
    name_ru_        in  ref_department.name_ru % type,
    rec_id_         in  ref_department.rec_id % type,
    dept_type_      in  ref_department.ref_department_type % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2    
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_DEP_LIST_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    Open Cur for
      select d.id,
             d.rec_id,
             d.code,             
             d.name_kz,
             d.name_ru,
             d.name_en,             
             d.begin_date,
             d.end_date,
             d.ref_department_type,
             dt.name_ru as dep_type_name,
             d.datlast,
             d.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             d.user_location,
             sk.name as sent_knd
        from v_ref_department d,
             v_ref_department_type dt,
             f_users u,
             sent_knd sk
       where d.id_usr = u.user_id
         and d.sent_knd = sk.sent_knd
         and d.ref_department_type = dt.id
         and (dept_type_ is null or d.ref_department_type = dept_type_)  
         and d.rec_id = (select max(t1.rec_id)
                          from v_ref_department t1
                         where ((
                                  (search_all_ver_ = 1) and 
                                  (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name)))
                                 ) or 
                                 (
                                  (search_all_ver_ is null or search_all_ver_ = 0) and 
                                  (trim(name_ru_) is null or upper(d.name_ru) like upper(trim(v_name)))
                                 ) 
                               )
                           and t1.rec_id = d.rec_id
                         )
         and (id_ is null or d.id = id_)
--         and (trim(name_ru_) is null or upper(d.name_ru) like upper(trim(v_name)))
         and (rec_id_ is null or d.rec_id = rec_id_)
         and (date_ is null or d.begin_date = (select max(t.begin_date)
                                                 from v_ref_department t
                                                where t.rec_id = d.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (d.end_date is null or d.end_date > date_))
       order by d.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_dep_list_by_params;
  
  procedure ref_read_dep_type(
    date_    in Date,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_DEP_TYPE';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select d.id,
             d.rec_id,
             d.code,             
             d.name_kz,
             d.name_ru,
             d.name_en,
             d.is_independent,
             d.begin_date,
             d.end_date,
             d.datlast,
             d.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             d.user_location
        from v_ref_department_type d,
             f_users u
       where d.id_usr = u.user_id           
         and (date_ is null or d.begin_date = (select max(t.begin_date)
                                                 from v_ref_department_type t
                                                where t.rec_id = d.rec_id
                                                  and t.begin_date <= date_))
       order by d.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_dep_type;
  
/* Банки второго уровня */
  
  procedure ref_read_bank_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    Err_Code  out number,
    Err_Msg   out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_BANK_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select b.id,
             b.rec_id,
             b.code,
             b.bic,
             b.bic_head,
             b.bic_nbrk,
             b.name_kz,
             b.name_ru,
             b.name_en,
             b.idn,
             b.post_address,
             b.phone_num,
             b.begin_date,
             b.end_date,
             b.datlast,
             b.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             b.user_location,
             sk.name as sent_knd,
             b.is_load,
             b.is_non_rezident,
             b.ref_country,
             c.rec_id as country_rec_id,
             c.name_ru as country_name
        from v_ref_bank b,
             v_ref_country c,
             f_users u,
             sent_knd sk
       where b.id_usr = u.user_id         
         and b.sent_knd = sk.sent_knd
         and b.ref_country = c.id(+)
         and (date_ is null or b.begin_date = (select max(t.begin_date)
                                                 from v_ref_bank t
                                                where t.rec_id = b.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (b.end_date is null or b.end_date > date_))
       order by b.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_bank_list;
  
  procedure ref_read_bank_list_by_params(
    id_              in  ref_bank.id % type,
    date_            in  Date,
    idn_             in  ref_bank.idn % type,
    name_ru_         in  ref_bank.name_ru % type,
    rec_id_          in  ref_bank.rec_id % type,
    is_load_         in  ref_bank.is_load % type,    
    is_non_rezident_ in  ref_bank.is_non_rezident % type,
    search_all_ver_  in  Integer default 0,
    Cur              out sys_refcursor,
    err_code         out number,
    err_msg          out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_BANK_LIST_BY_PARAMS';
    v_name_ru varchar2(1024);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name_ru := '%' || name_ru_ || '%';
    
    Open Cur for
      select b.id,
             b.rec_id,
             b.code,
             b.bic,
             b.bic_head,
             b.bic_nbrk,
             b.name_kz,
             b.name_ru,
             b.name_en,
             b.idn,
             b.post_address,
             b.phone_num,
             b.begin_date,
             b.end_date,
             b.datlast,
             b.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             b.user_location,
             sk.name as sent_knd,
             b.is_load,
             b.is_non_rezident,
             b.ref_country,
             c.rec_id as country_rec_id,
             c.name_ru as country_name             
        from v_ref_bank b,
             v_ref_country c,
             f_users u,
             sent_knd sk
       where b.id_usr = u.user_id         
         and b.sent_knd = sk.sent_knd
         and b.ref_country = c.id(+)
         and b.rec_id = (select max(t1.rec_id)
                          from v_ref_bank t1
                         where ((
                                  (search_all_ver_ = 1) and 
                                  (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name_ru))) and 
                                  (trim(idn_) is null or upper(t1.idn) like upper(trim(idn_)) || '%') and
                                  (is_non_rezident_ is null or t1.is_non_rezident = is_non_rezident_) and
                                  (is_load_ is null or t1.is_load = is_load_)
                                 ) or 
                                 (
                                  (search_all_ver_ is null or search_all_ver_ = 0) and 
                                  (trim(name_ru_) is null or upper(b.name_ru) like upper(trim(v_name_ru))) and 
                                  (trim(idn_) is null or upper(b.idn) like upper(trim(idn_)) || '%') and 
                                  (is_non_rezident_ is null or b.is_non_rezident = is_non_rezident_) and
                                  (is_load_ is null or b.is_load = is_load_)
                                 ) 
                               )
                           and t1.rec_id = b.rec_id
                         )
         and (id_ is null or b.id = id_)
         and (rec_id_ is null or b.rec_id = rec_id_)
         and (date_ is null or b.begin_date = (select max(t.begin_date)
                                                 from v_ref_bank t
                                                where t.rec_id = b.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (b.end_date is null or b.end_date > date_))
       order by b.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_bank_list_by_params;
  
  procedure ref_read_bank_hst_list(
    id_      in  ref_bank.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_BANK_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select bh.id_hst,
             bh.id,
             bh.rec_id,
             bh.code,
             bh.name_kz,
             bh.name_ru,
             bh.name_en,
             bh.idn,
             bh.post_address,
             bh.phone_num,
             bh.begin_date,
             bh.end_date,
             bh.datlast,
             bh.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             bh.type_change,
             tc.name as type_change_name,
             bh.user_location,
             sk.name as sent_knd,
             bh.is_load,
             bh.is_non_rezident,
             bh.ref_country,
             c.rec_id as country_rec_id,
             c.name_ru as country_name      
        from ref_bank_hst bh,
             ref_country c,
             f_users u,
             type_change tc,
             sent_knd sk
       where bh.id_usr = u.user_id
       	 and bh.ref_country = c.id(+)
         and bh.sent_knd = sk.sent_knd
         and bh.type_change = tc.type_change
         and bh.id = id_
       order by bh.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_bank_hst_list;
  
  
  procedure ref_insert_bank(
    rec_id_          in  ref_bank.rec_id          % type,
    code_            in  ref_bank.code            % type,
    name_kz_         in  ref_bank.name_kz         % type,
    name_ru_         in  ref_bank.name_ru         % type,
    name_en_         in  ref_bank.name_en         % type,
    idn_             in  ref_bank.idn             % type,
    post_address_    in  ref_bank.post_address    % type,
    phone_num_       in  ref_bank.phone_num       % type,
    ref_country_     in  ref_bank.ref_country     % type,
    is_non_rezident_ in  ref_bank.is_non_rezident % type,
    begin_date_      in  ref_bank.begin_date      % type,
    end_date_        in  ref_bank.end_date        % type,
    id_usr_          in  ref_bank.id_usr          % type,    
    user_location_   in  ref_bank.user_location   % type,
    datlast_         in  ref_bank.datlast         % type,
    do_commit_       in  integer default 1,     
    id_              out ref_bank.id % type,
    err_code         out number,
    err_msg          out varchar2
  )
  is
    ProcName    constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_BANK';    
    ref_code    Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_bank';
    
    if /*(trim(code_) is null) or */(trim(name_ru_) is null) or (begin_date_ is null)
      or (trim(ref_country_) is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if; 
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if; */
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
        
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    id_ := seq_ref_bank_id.nextval;
                  
    insert into ref_bank(
      id,
      rec_id,
      code,
      name_kz,
      name_ru,
      name_en,
      idn,
      post_address,
      phone_num,
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast,
      is_load,
      is_non_rezident,
      ref_country
    )
    values (
      id_,
      nvl(rec_id_,id_),
      nvl(rec_id_,id_),--code_,
      name_kz_,                 
      name_ru_, 
      name_en_,
      idn_,
      post_address_,
      phone_num_,
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_,
      0,
      is_non_rezident_,
      ref_country_
    );
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';
  end ref_insert_bank;
  
  
  procedure ref_update_bank(
    id_              in  ref_bank.id               % type,
    rec_id_          in  ref_bank.rec_id           % type,
    code_            in  ref_bank.code             % type,    
    name_kz_         in  ref_bank.name_kz          % type,
    name_ru_         in  ref_bank.name_ru          % type,
    name_en_         in  ref_bank.name_en          % type,            
    idn_             in  ref_bank.idn              % type,
    post_address_    in  ref_bank.post_address     % type,
    phone_num_       in  ref_bank.phone_num        % type,
    ref_country_     in  ref_bank.ref_country      % type,
    is_non_rezident_ in  ref_bank.is_non_rezident  % type,
    begin_date_      in  ref_bank.begin_date       % type,
    end_date_        in  ref_bank.end_date         % type,
    id_usr_          in  ref_bank.id_usr           % type,    
    user_location_   in  ref_bank.user_location    % type,
    datlast_         in  ref_bank.datlast          % type,
    do_commit_       in  integer default 1,     
    err_code         out number,
    err_msg          out varchar2
  )is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_BANK';
    v_have_chg        boolean;
    v_ref_data        ref_bank %rowtype;
    ref_code          Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_bank';
    
    if (trim(code_) is null) or (trim(name_ru_) is null) or (begin_date_ is null) or (trim(ref_country_) is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;*/
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    begin
      select *
        into v_ref_data
        from ref_bank
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;


  if v_ref_data.id <> 0 then
      if nvl(v_ref_data.name_kz,' ') <> nvl(name_kz_,' ') or 
         nvl(v_ref_data.name_ru,' ') <> nvl(name_ru_,' ') or
         nvl(v_ref_data.name_en,' ') <> nvl(name_en_,' ') or
         nvl(v_ref_data.idn,' ') <> nvl(idn_,' ') or
         nvl(v_ref_data.post_address,' ') <> nvl(post_address_,' ') or
         nvl(v_ref_data.phone_num,' ') <> nvl(phone_num_,' ') or
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or 
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) or
         nvl(v_ref_data.ref_country,0) <> nvl(ref_country_,0) or
         nvl(v_ref_data.is_non_rezident,0) <> nvl(is_non_rezident_,0) then

        v_have_chg := true;

      end if;
    end if;
    
    if v_have_chg = true then
      update ref_bank
         set --code          = code_,
             name_kz         = name_kz_,
             name_ru         = name_ru_,
             name_en         = name_en_,
             idn             = idn_,
             post_address    = post_address_,
             phone_num       = phone_num_,
             begin_date      = begin_date_,
             end_date        = end_date_,
             id_usr          = id_usr_,
             user_location   = user_location_,
             sent_knd        = 0,
             datlast         = datlast_,
             is_load         = 0,
             is_non_rezident = is_non_rezident_,
             ref_country     = ref_country_
       where id = id_;
             
      if do_commit_ = 1 then
        Commit;
      end if;
    end if;
    
  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';  
  end ref_update_bank;
  
  
  procedure ref_delete_bank(
    id_        in  ref_bank.id  % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  )is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_BANK';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
  
    update ref_bank 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_bank;
  
  /* Рейтинговые агенства */ 
  procedure ref_read_rate_agency_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    Err_Code  out number,
    Err_Msg   out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_RATE_AGENCY_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select ra.id,
             ra.rec_id,
             ra.code,
             ra.name_kz,
             ra.name_ru,
             ra.name_en,                          
             ra.begin_date,
             ra.end_date,
             ra.datlast,
             ra.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             ra.user_location,
             sk.name as sent_knd
        from v_ref_rate_agency ra,
             f_users u,
             sent_knd sk
       where ra.id_usr = u.user_id         
         and ra.sent_knd = sk.sent_knd
         and (date_ is null or ra.begin_date = (select max(t.begin_date)
                                                 from v_ref_rate_agency t
                                                where t.rec_id = ra.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (ra.end_date is null or ra.end_date > date_))
       order by ra.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_rate_agency_list;
  
  procedure ref_read_rat_agen_l_by_params(
    id_             in  ref_rate_agency.id % type,
    date_           in  Date,
    name_ru_        in  ref_rate_agency.name_ru % type,
    rec_id_         in  ref_rate_agency.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_RAT_AGEN_L_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    Open Cur for
      select ra.id,
             ra.rec_id,
             ra.code,
             ra.name_kz,
             ra.name_ru,
             ra.name_en,                          
             ra.begin_date,
             ra.end_date,
             ra.datlast,
             ra.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             ra.user_location,
             sk.name as sent_knd
        from v_ref_rate_agency ra,
             f_users u,
             sent_knd sk
       where ra.id_usr = u.user_id         
         and ra.sent_knd = sk.sent_knd
         and ra.rec_id = (select max(t1.rec_id)
                            from v_ref_rate_agency t1
                           where ((
                                    (search_all_ver_ = 1) and 
                                    (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name)))
                                   ) or 
                                   (
                                    (search_all_ver_ is null or search_all_ver_ = 0) and 
                                    (trim(name_ru_) is null or upper(ra.name_ru) like upper(trim(v_name)))
                                   ) 
                                 )
                             and t1.rec_id = ra.rec_id
                           )
         and (id_ is null or ra.id = id_)
--         and (trim(name_ru_) is null or upper(ra.name_ru) like upper(trim(v_name)))
         and (rec_id_ is null or ra.rec_id = rec_id_)
         and (date_ is null or ra.begin_date = (select max(t.begin_date)
                                                 from v_ref_rate_agency t
                                                where t.rec_id = ra.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (ra.end_date is null or ra.end_date > date_))
       order by ra.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_rat_agen_l_by_params;
  
  
  /* Валюты */
   procedure ref_read_currency_list(
    date_     in  Date,   
    Cur       out sys_refcursor,
    Err_Code  out number,
    Err_Msg   out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_CURRENCY_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select c.id,
             c.rec_id,
             c.code,
             c.minor_units,
             c.rate,
             c.name_kz,
             c.name_ru,
             c.name_en,
             c.REF_CURRENCY_RATE,
             cr.rec_id as REF_CURRENCY_RATE_REC_ID,
             cr.name_ru as cur_rate_name,
             ra.name_ru as rate_agency,
             c.begin_date,
             c.end_date,
             c.datlast,
             c.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             c.user_location,
             sk.name as sent_knd
        from v_ref_currency c,
             v_ref_currency_rate cr,
             v_ref_rate_agency ra,
             f_users u,
             sent_knd sk
       where c.id_usr = u.user_id
         and c.ref_currency_rate = cr.id(+)
         and cr.ref_rate_agency = ra.id(+)
         and c.sent_knd = sk.sent_knd
         and (date_ is null or c.begin_date = (select max(t.begin_date)
                                                 from v_ref_currency t
                                                where t.rec_id = c.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (c.end_date is null or c.end_date > date_))
       order by c.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_currency_list;
  
  procedure ref_read_curr_list_by_params(
    id_             in  ref_currency.id % type,
    date_           in  Date,
    code_           in  ref_currency.code % type,
    cur_rate_name_  in  ref_currency_rate.name_ru % type,
    rate_agency_    in  ref_rate_agency.name_ru % type,
    name_ru_        in  ref_currency.name_ru % type,          
    rec_id_         in  ref_currency.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_CURR_LIST_BY_PARAMS';
    v_name varchar2(524);
    v_cur_rate_name varchar2(524);
    v_rate_agency varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    v_cur_rate_name := '%' || cur_rate_name_ || '%';
    v_rate_agency := '%' || rate_agency_ || '%';
    
    Open Cur for
      select c.id,
             c.rec_id,
             c.code,
             c.minor_units,
             c.rate,
             c.name_kz,
             c.name_ru,
             c.name_en,
             c.REF_CURRENCY_RATE,
             cr.rec_id as REF_CURRENCY_RATE_REC_ID,
             cr.name_ru as cur_rate_name,
             ra.name_ru as rate_agency,
             c.begin_date,
             c.end_date,
             c.datlast,
             c.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             c.user_location,
             sk.name as sent_knd
        from v_ref_currency c,
             v_ref_currency_rate cr,
             v_ref_rate_agency ra,
             f_users u,
             sent_knd sk
       where c.id_usr = u.user_id
         and c.ref_currency_rate = cr.id(+)
         and cr.ref_rate_agency = ra.id(+)
         and c.sent_knd = sk.sent_knd
         and c.rec_id = (select max(t1.rec_id)
                          from v_ref_currency t1,
                               v_ref_currency_rate cr1,
                               v_ref_rate_agency ra1
                         where ((
                                  (search_all_ver_ = 1) and 
                                  (trim(code_) is null or upper(t1.code) like upper(trim(code_)) || '%') and
                                  (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name))) and
                                  (trim(cur_rate_name_) is null or upper(cr1.name_ru) like upper(trim(v_cur_rate_name))) and
                                  (trim(rate_agency_) is null or upper(ra1.name_ru) like upper(trim(v_rate_agency)))
                                 ) or
                                 (
                                  (search_all_ver_ is null or search_all_ver_ = 0) and 
                                  (trim(code_) is null or upper(c.code) like upper(trim(code_)) || '%') and
                                  (trim(name_ru_) is null or upper(c.name_ru) like upper(trim(v_name))) and
                                  (trim(cur_rate_name_) is null or upper(cr.name_ru) like upper(trim(v_cur_rate_name))) and
                                  (trim(rate_agency_) is null or upper(ra.name_ru) like upper(trim(v_rate_agency)))                                                                            
                                 ) 
                               )
                           and t1.rec_id = c.rec_id
                           and t1.ref_currency_rate = cr1.id(+)
                           and cr1.ref_rate_agency = ra1.id(+)
                         )
         and (id_ is null or c.id = id_)
         /*and (trim(code_) is null or upper(c.code) like upper(trim(code_)) || '%')
         and (trim(cur_rate_name_) is null or upper(cr.name_ru) like upper(trim(v_cur_rate_name)))
         and (trim(rate_agency_) is null or upper(ra.name_ru) like upper(trim(v_rate_agency)))
         and (trim(name_ru_) is null or upper(c.name_ru) like upper(trim(v_name)))*/
         and (rec_id_ is null or c.rec_id = rec_id_)
         and (date_ is null or c.begin_date = (select max(t.begin_date)
                                                 from v_ref_currency t
                                                where t.rec_id = c.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (c.end_date is null or c.end_date > date_))
       order by c.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_curr_list_by_params;
  
  
  /* Рейтинг валют */  
   procedure ref_read_currency_rate_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    Err_Code  out number,
    Err_Msg   out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_CURRENCY_RATE_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select cr.id,
             cr.rec_id,
             cr.code,             
             cr.name_kz,
             cr.name_ru,
             cr.name_en,
             cr.REF_RATE_AGENCY,
             ra.name_ru as rate_agency,
             ra.rec_id as REF_RATE_AGENCY_REC_ID,
             cr.begin_date,
             cr.end_date,
             cr.datlast,
             cr.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             cr.user_location,
             sk.name as sent_knd
        from v_ref_currency_rate cr,
             v_ref_rate_agency ra,
             f_users u,
             sent_knd sk
       where cr.id_usr = u.user_id
         and cr.ref_rate_agency = ra.id(+)                  
         and cr.sent_knd = sk.sent_knd
         and (date_ is null or cr.begin_date = (select max(t.begin_date)
                                                 from v_ref_currency_rate t
                                                where t.rec_id = cr.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (cr.end_date is null or cr.end_date > date_))
       order by cr.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_currency_rate_list;
  
  procedure ref_read_curr_rat_l_by_params(
    id_             in  ref_currency_rate.id % type,
    date_           in  Date,
    rate_agency_    in  ref_rate_agency.name_ru % type,
    name_ru_        in  ref_currency_rate.name_ru % type,
    rec_id_         in  ref_currency_rate.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_CURR_RAT_L_BY_PARAMS';
    v_name varchar2(524);
    v_rate_agency varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    v_rate_agency := '%' || rate_agency_ || '%';
    
    Open Cur for
      select cr.id,
             cr.rec_id,
             cr.code,             
             cr.name_kz,
             cr.name_ru,
             cr.name_en,
             cr.REF_RATE_AGENCY,
             ra.name_ru as rate_agency,
             ra.rec_id as REF_RATE_AGENCY_REC_ID,
             cr.begin_date,
             cr.end_date,
             cr.datlast,
             cr.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             cr.user_location,
             sk.name as sent_knd
        from v_ref_currency_rate cr,
             v_ref_rate_agency ra,
             f_users u,
             sent_knd sk
       where cr.id_usr = u.user_id
         and cr.ref_rate_agency = ra.id(+)                  
         and cr.sent_knd = sk.sent_knd
         and cr.rec_id = (select max(t1.rec_id)
                            from v_ref_currency_rate t1,
                                 v_ref_rate_agency ra1
                           where ((
                                    (search_all_ver_ = 1) and 
                                    (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name))) and
                                    (trim(rate_agency_) is null or upper(ra1.name_ru) like upper(trim(v_rate_agency)))
                                   ) or 
                                   (
                                    (search_all_ver_ is null or search_all_ver_ = 0) and 
                                    (trim(name_ru_) is null or upper(cr.name_ru) like upper(trim(v_name))) and
                                    (trim(rate_agency_) is null or upper(ra.name_ru) like upper(trim(v_rate_agency)))
                                   ) 
                                 )
                             and t1.rec_id = cr.rec_id
                             and t1.ref_rate_agency = ra1.id(+)
                           )
         and (id_ is null or cr.id = id_)
         /*and (trim(rate_agency_) is null or upper(ra.name_ru) like upper(trim(v_rate_agency)))
         and (trim(name_ru_) is null or upper(cr.name_ru) like upper(trim(v_name)))*/
         and (rec_id_ is null or cr.rec_id = rec_id_)
         and (date_ is null or cr.begin_date = (select max(t.begin_date)
                                                 from v_ref_currency_rate t
                                                where t.rec_id = cr.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (cr.end_date is null or cr.end_date > date_))
       order by cr.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_curr_rat_l_by_params;
  
  
  /* Тип организации */
  procedure ref_read_subject_type_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2      
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_SUBJECT_TYPE_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select st.id,
             st.rec_id,
             st.code,
             st.name_kz,
             st.name_ru,
             st.name_en,             
             st.short_name_kz,
             st.short_name_ru,
             st.short_name_en,
             st.kind_id,
             st.rep_per_dur_months,
             st.is_advance,
             dm.name as du_name,
             st.begin_date,
             st.end_date,
             st.datlast,
             st.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             st.user_location,
             sk.name as sent_knd
        from v_ref_subject_type st,
             rep_per_dur_months dm,
             f_users u,
             sent_knd sk
       where st.id_usr = u.user_id
         and st.rep_per_dur_months = dm.id         
         and st.sent_knd = sk.sent_knd
         and (date_ is null or st.begin_date = (select max(t.begin_date)
                                                 from v_ref_subject_type t
                                                where t.rec_id = st.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (st.end_date is null or st.end_date > date_))
       order by st.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_subject_type_list;  
  
  procedure ref_read_subj_type_l_by_params(
    id_             in  ref_subject_type.id % type,
    date_           in  Date,
    name_ru_        in  ref_subject_type.name_ru % type,
    rec_id_         in  ref_subject_type.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2      
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_SUBJ_TYPE_L_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    Open Cur for
      select st.id,
             st.rec_id,
             st.code,
             st.name_kz,
             st.name_ru,
             st.name_en,
             st.short_name_kz,
             st.short_name_ru,
             st.short_name_en,    
             st.kind_id,
             st.rep_per_dur_months,
             st.is_advance,
             dm.name as du_name,
             st.begin_date,
             st.end_date,
             st.datlast,
             st.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             st.user_location,
             sk.name as sent_knd
        from v_ref_subject_type st,
             rep_per_dur_months dm,
             f_users u,
             sent_knd sk
       where st.id_usr = u.user_id
         and st.rep_per_dur_months = dm.id         
         and st.sent_knd = sk.sent_knd
         and st.rec_id = (select max(t1.rec_id)
                            from v_ref_subject_type t1
                           where ((
                                    (search_all_ver_ = 1) and 
                                    (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name)))
                                   ) or 
                                   (
                                    (search_all_ver_ is null or search_all_ver_ = 0) and 
                                    (trim(name_ru_) is null or upper(st.name_ru) like upper(trim(v_name)))
                                   ) 
                                 )
                             and t1.rec_id = st.rec_id
                           )
         and (id_ is null or st.id = id_)
--         and (trim(name_ru_) is null or upper(st.name_ru) like upper(trim(v_name)))
         and (rec_id_ is null or st.rec_id = rec_id_)
         and (date_ is null or st.begin_date = (select max(t.begin_date)
                                                 from v_ref_subject_type t
                                                where t.rec_id = st.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (st.end_date is null or st.end_date > date_))
       order by st.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_subj_type_l_by_params;  
  
  
  procedure ref_read_subject_type_hst_list(
    id_      in  ref_subject_type.id  % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_SUBJECT_TYPE_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select sth.id_hst,
             sth.id,
             sth.rec_id,
             sth.code,
             sth.name_kz,
             sth.name_ru,
             sth.name_en,
             sth.short_name_kz,
             sth.short_name_ru,
             sth.short_name_en,
             sth.kind_id,
             sth.rep_per_dur_months,
             sth.is_advance,
             du.name as du_name,
             sth.begin_date,
             sth.end_date,
             sth.datlast,
             sth.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             sth.type_change,
             tc.name as type_change_name,
             sth.user_location,
             sk.name as sent_knd
        from ref_subject_type_hst sth,
             rep_per_dur_months du,
             f_users u,
             type_change tc,
             sent_knd sk
       where sth.id_usr = u.user_id
         and sth.sent_knd = sk.sent_knd
         and sth.rep_per_dur_months = du.id
         and sth.type_change = tc.type_change
         and sth.id = id_
       order by sth.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_subject_type_hst_list;
  
  
  procedure ref_insert_subject_type(
    rec_id_             in  ref_subject_type.rec_id             % type,
    code_               in  ref_subject_type.code               % type,    
    name_kz_            in  ref_subject_type.name_kz            % type,
    name_ru_            in  ref_subject_type.name_ru            % type,
    name_en_            in  ref_subject_type.name_en            % type,                
    short_name_kz_      in  ref_subject_type.short_name_kz      % type,
    short_name_ru_      in  ref_subject_type.short_name_ru      % type,
    short_name_en_      in  ref_subject_type.short_name_en      % type,
--    rep_per_dur_months_ in  ref_subject_type.rep_per_dur_months % type,
    is_advance_         in  ref_subject_type.is_advance         % type,
    begin_date_         in  ref_subject_type.begin_date         % type,
    end_date_           in  ref_subject_type.end_date           % type,
    id_usr_             in  ref_subject_type.id_usr             % type,    
    user_location_      in  ref_subject_type.user_location      % type,
    datlast_            in  ref_subject_type.datlast            % type,
    do_commit_          in  integer default 1,
    id_                 out ref_subject_type.id % type,
    err_code            out number,
    err_msg             out varchar2
  )
  is
    ProcName     constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_SUBJECT_TYPE';    
    ref_code     Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_subject_type';
    
    if /*(trim(code_) is null) or */
       (trim(name_ru_) is null) or 
       (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if; 
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if; */
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    id_ := seq_ref_subject_type_id.nextval;    
                  
    insert into ref_subject_type(
      id,
      rec_id,
      code,
      name_kz,
      name_ru,
      name_en,
      short_name_kz,
      short_name_ru,
      short_name_en,
--      rep_per_dur_months,
      is_advance,
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),
      nvl(rec_id_,id_),--code_,
      name_kz_,                 
      name_ru_, 
      name_en_,
      short_name_kz_,
      short_name_ru_, 
      short_name_en_,
--      rep_per_dur_months_,
      is_advance_,
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_
    );
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';
  end ref_insert_subject_type;
   

  procedure ref_update_subject_type(
    id_                 in  ref_subject_type.id                 % type,
    rec_id_             in  ref_subject_type.rec_id             % type,
    code_               in  ref_subject_type.code               % type,    
    name_kz_            in  ref_subject_type.name_kz            % type,
    name_ru_            in  ref_subject_type.name_ru            % type,
    name_en_            in  ref_subject_type.name_en            % type,                
    short_name_kz_      in  ref_subject_type.short_name_kz      % type,
    short_name_ru_      in  ref_subject_type.short_name_ru      % type,
    short_name_en_      in  ref_subject_type.short_name_en      % type,
--    rep_per_dur_months_ in  ref_subject_type.rep_per_dur_months % type,
    is_advance_         in  ref_subject_type.is_advance         % type,
    begin_date_         in  ref_subject_type.begin_date         % type,
    end_date_           in  ref_subject_type.end_date           % type,
    id_usr_             in  ref_subject_type.id_usr             % type,    
    user_location_      in  ref_subject_type.user_location      % type,
    datlast_            in  ref_subject_type.datlast            % type,
    do_commit_          in  integer default 1,     
    err_code            out number,
    err_msg             out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_SUBJECT_TYPE';
    v_have_chg        boolean;
    v_ref_data        ref_subject_type %rowtype;
    ref_code          Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_subject_type';
    v_have_chg := false;
    
    if (trim(code_) is null) or 
       (trim(name_ru_) is null) or 
       (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;*/
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    /*report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;*/
    
    begin
      select *
        into v_ref_data
        from ref_subject_type
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;

    if v_ref_data.id <> 0 then
      if nvl(v_ref_data.name_kz,' ') <> nvl(name_kz_,' ') or 
         nvl(v_ref_data.name_ru,' ') <> nvl(name_ru_,' ') or
         nvl(v_ref_data.name_en,' ') <> nvl(name_en_,' ') or 
         nvl(v_ref_data.short_name_kz,' ') <> nvl(short_name_kz_,' ') or 
         nvl(v_ref_data.short_name_ru,' ') <> nvl(short_name_ru_,' ') or
         nvl(v_ref_data.short_name_en,' ') <> nvl(short_name_en_,' ') or
--         nvl(v_ref_data.rep_per_dur_months,0) <> nvl(rep_per_dur_months_,0) or
         nvl(v_ref_data.is_advance,0) <> nvl(is_advance_,0) or         
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or 
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) then

        v_have_chg := true;

      end if;
    end if;
    
    if v_have_chg = true then
      update ref_subject_type
         set --code               = code_,
             name_kz            = name_kz_,
             name_ru            = name_ru_,
             name_en            = name_en_,
             short_name_kz      = short_name_kz_,
             short_name_ru      = short_name_ru_,
             short_name_en      = short_name_en_,
--             rep_per_dur_months = rep_per_dur_months_,
             is_advance         = is_advance_,
             begin_date         = begin_date_,
             end_date           = end_date_,
             id_usr             = id_usr_,
             user_location      = user_location_,
             sent_knd           = 0,
             datlast            = datlast_
       where id = id_;
             
      if do_commit_ = 1 then
        Commit;
      end if;
   end if;

  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';
  end ref_update_subject_type;
  
  
  procedure ref_delete_subject_type(
    id_        in  ref_subject_type.id   % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_SUBJECT_TYPE';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    if ref_check_record('ref_respondent','ref_subject_type', id_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || ' Внимание! Имеется дочерняя запись!';
      raise E_Force_Exit;
    end if;
        
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_subject_type',
                          /*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
  
    update ref_subject_type 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_subject_type;
  
  
  /* Респонденты */  
  procedure ref_read_respondent_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2     
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_RESPONDENT_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select r.id,
             r.rec_id,
             r.code,
             r.ref_legal_person,
             lp.rec_id as REF_LEGAL_PERSON_REC_ID,
             lp.name_ru as LEGAL_PERSON_NAME,
             lp.idn,
             r.ref_subject_type,             
             st.rec_id as ref_subject_type_rec_id,
             st.NAME_RU as ref_subject_type_name,
             r.nokbdb_code,
             r.main_buh,
             r.date_begin_lic,
             r.date_end_lic,
             r.stop_lic,
             r.vid_activity,
             r.ref_department,
             d.rec_id as ref_department_rec_id,
             d.name_ru as ref_department_name,
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
             sent_knd sk
       where r.id_usr = u.user_id
         and r.ref_legal_person = lp.id
         and r.ref_subject_type = st.id
         and r.ref_department = d.id(+)
         and r.sent_knd = sk.sent_knd         
         and (date_ is null or r.begin_date = (select max(t.begin_date)
                                                 from v_ref_respondent t
                                                where t.rec_id = r.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (r.end_date is null or r.end_date > date_))
       order by r.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_respondent_list;  
  
  procedure ref_read_resp_list_by_params(
    id_               in  ref_respondent.id % type,
    date_             in  Date,
    name_ru_          in  ref_legal_person.name_ru % type,
    rec_id_           in  ref_legal_person.rec_id % type,
    idn_              in  ref_legal_person.idn % type,
    ref_department_   in  ref_respondent.ref_department % type,
    ref_subject_type_ in  ref_respondent.ref_subject_type % type,
    search_all_ver_   in  Integer default 0,
    Cur               out sys_refcursor,
    err_code          out number,
    err_msg           out varchar2     
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_RESP_LIST_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    
    Open Cur for
      select r.id,
             r.rec_id,
             r.code,
             r.ref_legal_person,
             lp.rec_id as REF_LEGAL_PERSON_REC_ID,
             lp.name_ru as LEGAL_PERSON_NAME,
             lp.idn,
             r.ref_subject_type,             
             st.rec_id as ref_subject_type_rec_id,
             st.NAME_RU as ref_subject_type_name,
             r.nokbdb_code,
             r.main_buh,
             r.date_begin_lic,
             r.date_end_lic,
             r.stop_lic,
             r.vid_activity,
             r.ref_department,
             d.rec_id as ref_department_rec_id,
             d.name_ru as ref_department_name,
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
             sent_knd sk
       where r.id_usr = u.user_id
         and r.ref_legal_person = lp.id
         and r.ref_subject_type = st.id
         and r.ref_department = d.id(+)
         and r.sent_knd = sk.sent_knd   
         and r.rec_id = (select max(t1.rec_id)
                          from v_ref_respondent t1,
                               v_ref_legal_person lp1
                         where ((
                                  (search_all_ver_ = 1) and 
                                  (trim(name_ru_) is null or upper(lp1.name_ru) like upper(trim(v_name))) and
                                  (idn_ is null or lp1.idn = idn_) and
                                  (ref_department_ is null or t1.REF_DEPARTMENT = ref_department_) and
                                  (ref_subject_type_ is null or t1.REF_SUBJECT_TYPE = ref_subject_type_)
                                 ) or 
                                 (
                                  (search_all_ver_ is null or search_all_ver_ = 0) and 
                                  (trim(name_ru_) is null or upper(lp.name_ru) like upper(trim(v_name))) and
                                  (idn_ is null or lp.idn = idn_) and
                                  (ref_department_ is null or r.REF_DEPARTMENT = ref_department_) and
                                  (ref_subject_type_ is null or r.REF_SUBJECT_TYPE = ref_subject_type_)
                                 ) 
                               )
                           and t1.rec_id = r.rec_id
                           and t1.ref_legal_person = lp1.id
                         )            
         and (id_ is null or r.id = id_)
--         and (trim(name_ru_) is null or upper(lp.name_ru) like upper(trim(v_name)))
         and (rec_id_ is null or r.rec_id = rec_id_)
--         and (idn_ is null or lp.idn = idn_)
         and (date_ is null or r.begin_date = (select max(t.begin_date)
                                                 from v_ref_respondent t
                                                where t.rec_id = r.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (r.end_date is null or r.end_date > date_))                                                  
       order by r.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_resp_list_by_params;  
  
  
  procedure ref_read_respondent_hst_list(
    id_      in  ref_respondent.id  % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_RESPONDENT_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select rh.id_hst,
             rh.id,
             rh.rec_id,
             rh.code,
             rh.ref_legal_person,
             lp.name_ru as LEGAL_PERSON_NAME,
             rh.nokbdb_code,
             rh.main_buh,
             rh.date_begin_lic,
             rh.date_end_lic,
             rh.stop_lic,
             rh.vid_activity,
             rh.ref_subject_type,             
             st.rec_id as ref_subject_type_rec_id,
             st.NAME_RU as ref_subject_type_name,
             rh.ref_department,
             d.rec_id as ref_department_rec_id,
             d.name_ru as ref_department_name,
             rh.begin_date,
             rh.end_date,
             rh.datlast,
             rh.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             rh.type_change,
             tc.name as type_change_name,
             rh.user_location,
             sk.name as sent_knd
        from ref_respondent_hst rh,
             ref_legal_person lp,
             ref_department d,
             ref_subject_type st,
             f_users u,
             type_change tc,
             sent_knd sk
       where rh.id_usr = u.user_id
         and rh.ref_legal_person = lp.id
         and rh.ref_subject_type = st.id(+)
         and rh.ref_department = d.id(+)
         and rh.sent_knd = sk.sent_knd
         and rh.type_change = tc.type_change
         and rh.id = id_
       order by rh.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_respondent_hst_list;
  
  
  procedure ref_insert_respondent(
    rec_id_             in  ref_respondent.rec_id             % type,
    code_               in  ref_respondent.code               % type,    
    ref_legal_person_   in  ref_respondent.ref_legal_person   % type,
    nokbdb_code_        in  ref_respondent.nokbdb_code        % type,    
    main_buh_           in  ref_respondent.main_buh           % type,
    date_begin_lic_     in  ref_respondent.date_begin_lic     % type,
    date_end_lic_       in  ref_respondent.date_end_lic       % type,
    stop_lic_           in  ref_respondent.stop_lic           % type,
    vid_activity_       in  ref_respondent.vid_activity       % type,
    ref_department_     in  ref_respondent.ref_department     % type,
    ref_subject_type_   in  ref_respondent.ref_subject_type   % type,
    begin_date_         in  ref_respondent.begin_date         % type,
    end_date_           in  ref_respondent.end_date           % type,
    id_usr_             in  ref_respondent.id_usr             % type,    
    user_location_      in  ref_respondent.user_location      % type,
    datlast_            in  ref_respondent.datlast            % type,
    do_commit_          in  integer default 1,
    id_                 out ref_respondent.id % type,
    err_code            out number,
    err_msg             out varchar2
  )
  is
    ProcName    constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_RESPONDENT';    
    ref_code    Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_respondent';
    
    if /*(trim(code_) is null) or*/ 
       (ref_legal_person_ is null)or
       (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if; 
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
   /* if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if; */
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_respondent(ref_legal_person_,rec_id_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется такой респондент!';
      raise E_Force_Exit;
    end if;
    
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
            
    if ref_check_respondent_idn(ref_legal_person_) > 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 07 ' || 'Внимание! Пустой БИН не допустим!';
      raise E_Force_Exit;
    end if;
    
    
    id_ := seq_ref_respondent_id.nextval;
                  
    insert into ref_respondent(
      id,
      rec_id,
      code,
      ref_legal_person,
      nokbdb_code,
      main_buh,
      date_begin_lic,
      date_end_lic,
      stop_lic,
      vid_activity,
      ref_department,
      ref_subject_type,      
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),
      nvl(rec_id_,id_),--code_,
      ref_legal_person_,
      nokbdb_code_,
      main_buh_,
      date_begin_lic_,
      date_end_lic_,
      stop_lic_,
      vid_activity_,
      ref_department_,
      ref_subject_type_,
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_
    );
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';
  end ref_insert_respondent;
   

  procedure ref_update_respondent(
    id_               in  ref_respondent.id               % type,
    rec_id_           in  ref_respondent.rec_id           % type,
    code_             in  ref_respondent.code             % type,    
    ref_legal_person_ in  ref_respondent.ref_legal_person % type,
    nokbdb_code_      in  ref_respondent.nokbdb_code      % type,    
    main_buh_         in  ref_respondent.main_buh         % type,
    date_begin_lic_   in  ref_respondent.date_begin_lic   % type,
    date_end_lic_     in  ref_respondent.date_end_lic     % type,
    stop_lic_         in  ref_respondent.stop_lic         % type,
    vid_activity_     in  ref_respondent.vid_activity     % type,    
    ref_department_   in  ref_respondent.ref_department   % type,
    ref_subject_type_ in  ref_respondent.ref_subject_type % type,
    begin_date_       in  ref_respondent.begin_date       % type,
    end_date_         in  ref_respondent.end_date         % type,
    id_usr_           in  ref_respondent.id_usr           % type,    
    user_location_    in  ref_respondent.user_location    % type,
    datlast_          in  ref_respondent.datlast          % type,
    do_commit_        in  integer default 1,     
    err_code          out number,
    err_msg           out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_RESPONDENT';
    v_have_chg        boolean;
    v_ref_data        ref_respondent %rowtype;
    ref_code          Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_respondent';
    v_have_chg := false;
    
    if (trim(code_) is null) or 
       (ref_legal_person_ is null) or
       (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
   /* if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;
    */
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_respondent(ref_legal_person_,rec_id_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется такой респондент!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_respondent_idn(ref_legal_person_) > 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 07 ' || 'Внимание! Пустой БИН не допустим!';
      raise E_Force_Exit;
    end if;    
    
    /*report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;*/
    
    begin
      select *
        into v_ref_data
        from ref_respondent
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;


    if v_ref_data.id <> 0 then
      if nvl(v_ref_data.ref_legal_person,0) <> nvl(ref_legal_person_,0) or 
         nvl(v_ref_data.nokbdb_code,' ') <> nvl(nokbdb_code_,' ') or
         nvl(v_ref_data.main_buh,' ') <> nvl(main_buh_,' ') or 
         nvl(v_ref_data.date_begin_lic,sysdate) <> nvl(date_begin_lic_,sysdate) or
         nvl(v_ref_data.date_end_lic,sysdate) <> nvl(date_end_lic_,sysdate) or
         nvl(v_ref_data.stop_lic,' ') <> nvl(stop_lic_,' ') or 
         nvl(v_ref_data.vid_activity,' ') <> nvl(vid_activity_,' ') or
         nvl(v_ref_data.ref_department,0) <> nvl(ref_department_,0) or
         nvl(v_ref_data.ref_subject_type,0) <> nvl(ref_subject_type_,0) or
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or 
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) then

        v_have_chg := true;

      end if;
    end if;
    
    if v_have_chg = true then
      update ref_respondent
         set --code               = code_,
             ref_legal_person   = ref_legal_person_,
             nokbdb_code        = nokbdb_code_,
             main_buh           = main_buh_,
             date_begin_lic     = date_begin_lic_,
             date_end_lic       = date_end_lic_,
             stop_lic           = stop_lic_,
             vid_activity       = vid_activity_,
             ref_department     = ref_department_,
             ref_subject_type   = ref_subject_type_,
             begin_date         = begin_date_,
             end_date           = end_date_,
             id_usr             = id_usr_,
             user_location      = user_location_,
             sent_knd           = 0,
             datlast            = datlast_
       where id = id_;
             
      if do_commit_ = 1 then
        Commit;
      end if;
    end if;

  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';
  end ref_update_respondent;
  
  
  procedure ref_delete_respondent(
    id_         in  ref_respondent.id   % type,
    do_commit_  in  integer default 1,     
    err_code    out number,
    err_msg     out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_RESPONDENT';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_respondent',
                          /*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
  
    update ref_respondent 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_respondent;
  

  procedure ref_read_respondent_by_rec_id(
    rec_id_   in ref_respondent.rec_id % type,
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2     
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_RESPONDENT_BY_REC_ID';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select r.id,
             r.rec_id,
             r.code,
             r.ref_legal_person,
             lp.name_ru as LEGAL_PERSON_NAME,
             lp.idn,
             lp.rec_id as ref_legal_person_rec_id,
             r.ref_subject_type,
             r.rec_id as ref_subject_type_rec_id,
             r.nokbdb_code,
             r.main_buh,
             r.date_begin_lic,
             r.date_end_lic,
             r.stop_lic,
             r.vid_activity,
             r.ref_department,
             d.name_ru as dept_name,              
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
             sent_knd sk
       where r.rec_id = rec_id_ 
         and r.id_usr = u.user_id
         and r.ref_legal_person = lp.id
         and r.ref_subject_type = st.id
         and r.ref_department = d.id(+)
         and r.sent_knd = sk.sent_knd         
         and (date_ is null or r.begin_date = (select max(t.begin_date)
                                                 from v_ref_respondent t
                                                where t.rec_id = rec_id_
                                                  and t.begin_date <= date_))
         and (date_ is null or (r.end_date is null or r.end_date > date_))
       order by r.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_respondent_by_rec_id;    
  
  
  
  /* Типы документов */    
  procedure ref_read_doc_type_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2     
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_DOC_TYPE_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select dt.id,
             dt.rec_id,
             dt.code,
             dt.name_kz,
             dt.name_ru,
             dt.name_en,             
             dt.is_identification,
             dt.is_organization_doc,
             dt.is_person_doc,
             dt.sign_count,
             dt.weight,
             dt.begin_date,
             dt.end_date,
             dt.datlast,
             dt.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             dt.user_location,
             sk.name as sent_knd
        from v_ref_doc_type dt,
             f_users u,
             sent_knd sk
       where dt.id_usr = u.user_id         
         and dt.sent_knd = sk.sent_knd
         and (date_ is null or dt.begin_date = (select max(t.begin_date)
                                                 from v_ref_doc_type t
                                                where t.rec_id = dt.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (dt.end_date is null or dt.end_date > date_))
       order by dt.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_doc_type_list;  
  
  procedure ref_read_doc_type_l_by_params(
    id_             in  ref_doc_type.id % type,
    date_           in  Date,
    name_ru_        in  ref_doc_type.name_ru % type,
    rec_id_         in  ref_doc_type.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2     
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_DOC_TYPE_L_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    Open Cur for
      select dt.id,
             dt.rec_id,
             dt.code,
             dt.name_kz,
             dt.name_ru,
             dt.name_en,             
             dt.is_identification,
             dt.is_organization_doc,
             dt.is_person_doc,
             dt.sign_count,
             dt.weight,
             dt.begin_date,
             dt.end_date,
             dt.datlast,
             dt.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             dt.user_location,
             sk.name as sent_knd
        from v_ref_doc_type dt,
             f_users u,
             sent_knd sk
       where dt.id_usr = u.user_id         
         and dt.sent_knd = sk.sent_knd
         and dt.rec_id = (select max(t1.rec_id)
                            from v_ref_doc_type t1
                           where ((
                                    (search_all_ver_ = 1) and 
                                    (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name)))
                                   ) or 
                                   (
                                    (search_all_ver_ is null or search_all_ver_ = 0) and 
                                    (trim(name_ru_) is null or upper(dt.name_ru) like upper(trim(v_name)))
                                   ) 
                                 )
                             and t1.rec_id = dt.rec_id
                           )
         and (id_ is null or dt.id = id_)
--         and (trim(name_ru_) is null or upper(dt.name_ru) like upper(trim(v_name)))
         and (rec_id_ is null or dt.rec_id = rec_id_)
         and (date_ is null or dt.begin_date = (select max(t.begin_date)
                                                 from v_ref_doc_type t
                                                where t.rec_id = dt.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (dt.end_date is null or dt.end_date > date_))
       order by dt.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_doc_type_l_by_params;  
  
  
  procedure ref_read_doc_type_hst_list(
    id_      in  ref_doc_type.id  % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_DOC_TYPE_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select dth.id_hst,
             dth.id,
             dth.rec_id,
             dth.code,
             dth.name_kz,
             dth.name_ru,
             dth.name_en,   
             dth.is_identification,
             dth.is_organization_doc,
             dth.is_person_doc,
             dth.sign_count,
             dth.weight,
             dth.begin_date,
             dth.end_date,
             dth.datlast,
             dth.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             dth.type_change,
             tc.name as type_change_name,
             dth.user_location,
             sk.name as sent_knd
        from ref_doc_type_hst dth,
             f_users u,
             type_change tc,
             sent_knd sk
       where dth.id_usr = u.user_id
         and dth.sent_knd = sk.sent_knd
         and dth.type_change = tc.type_change
         and dth.id = id_
       order by dth.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_doc_type_hst_list;
  
  
  procedure ref_insert_doc_type(
    rec_id_              in  ref_doc_type.rec_id              % type,
    code_                in  ref_doc_type.code                % type,    
    name_kz_             in  ref_doc_type.name_kz             % type,
    name_ru_             in  ref_doc_type.name_ru             % type,
    name_en_             in  ref_doc_type.name_en             % type,                
    is_identification_   in  ref_doc_type.is_identification   % type,
    is_organization_doc_ in  ref_doc_type.is_organization_doc % type,
    is_person_doc_       in  ref_doc_type.is_person_doc       % type,    
    begin_date_          in  ref_doc_type.begin_date          % type,
    end_date_            in  ref_doc_type.end_date            % type,
    id_usr_              in  ref_doc_type.id_usr              % type,    
    user_location_       in  ref_doc_type.user_location       % type,
    datlast_             in  ref_doc_type.datlast             % type,
    do_commit_           in  integer default 1,
    id_                  out ref_doc_type.id % type,
    err_code             out number,
    err_msg              out varchar2
  )
  is
    ProcName     constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_DOC_TYPE';    
    ref_code     Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_doc_type';
        
    if /*(trim(code_) is null) or */
       (trim(name_ru_) is null) or
       (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if; 
    
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
   /* if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if; */
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    id_ := seq_ref_doc_type_id.nextval;
                  
    insert into ref_doc_type(
      id,
      rec_id,
      code,
      name_kz,
      name_ru,
      name_en,
      is_identification,
      is_organization_doc,
      is_person_doc,      
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),
      nvl(rec_id_,id_),--code_,
      name_kz_,                 
      name_ru_, 
      name_en_,      
      nvl(is_identification_,0),
      nvl(is_organization_doc_,0),
      nvl(is_person_doc_,0),
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_
    );
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';
  end ref_insert_doc_type;
   

  procedure ref_update_doc_type(
    id_                  in  ref_doc_type.id                  % type,
    rec_id_              in  ref_doc_type.rec_id              % type,
    code_                in  ref_doc_type.code                % type,    
    name_kz_             in  ref_doc_type.name_kz             % type,
    name_ru_             in  ref_doc_type.name_ru             % type,
    name_en_             in  ref_doc_type.name_en             % type,                
    is_identification_   in  ref_doc_type.is_identification   % type,
    is_organization_doc_ in  ref_doc_type.is_organization_doc % type,
    is_person_doc_       in  ref_doc_type.is_person_doc       % type,    
    begin_date_          in  ref_doc_type.begin_date          % type,
    end_date_            in  ref_doc_type.end_date            % type,
    id_usr_              in  ref_doc_type.id_usr              % type,    
    user_location_       in  ref_doc_type.user_location       % type,
    datlast_             in  ref_doc_type.datlast             % type,
    do_commit_           in  integer default 1,     
    err_code             out number,
    err_msg              out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_DOC_TYPE';
    v_have_chg        boolean;
    v_ref_data        ref_doc_type %rowtype;
    ref_code          Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_doc_type';
    v_have_chg := false;
    
    if (trim(code_) is null) or 
       (trim(name_ru_) is null) or
       (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;*/
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    /*report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;*/
    
    begin
      select *
        into v_ref_data
        from ref_doc_type
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;


    if v_ref_data.id <> 0 then
      if nvl(v_ref_data.name_kz,' ') <> nvl(name_kz_,' ') or 
         nvl(v_ref_data.name_ru,' ') <> nvl(name_ru_,' ') or
         nvl(v_ref_data.name_en,' ') <> nvl(name_en_,' ') or 
         nvl(v_ref_data.is_identification,0) <> nvl(is_identification_,0) or
         nvl(v_ref_data.is_organization_doc,0) <> nvl(is_organization_doc_,0) or
         nvl(v_ref_data.is_person_doc,0) <> nvl(is_person_doc_,0) or         
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or 
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) then

        v_have_chg := true;

      end if;
    end if;
    
    if v_have_chg = true then
      update ref_doc_type
         set --code                = code_,
             name_kz             = name_kz_,
             name_ru             = name_ru_,
             name_en             = name_en_,           
             is_identification   = is_identification_,
             is_organization_doc = is_organization_doc_,
             is_person_doc       = is_person_doc_,
             begin_date          = begin_date_,
             end_date            = end_date_,
             id_usr              = id_usr_,
             user_location       = user_location_,
             sent_knd            = 0,
             datlast             = datlast_
       where id = id_;
             
      if do_commit_ = 1 then
        Commit;
      end if;
    end if;

  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';  
  end ref_update_doc_type;
  
  
  procedure ref_delete_doc_type(
    id_         in  ref_doc_type.id   % type,
    do_commit_  in  integer default 1,     
    err_code    out number,
    err_msg     out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_DOC_TYPE';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_doc_type',
                          /*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
  
    update ref_doc_type 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_doc_type;
  
  
  /* Документы */  
  procedure ref_read_document_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2     
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_DOCUMENT_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select d.id,
             d.rec_id,
             d.code,
             d.name_kz,
             d.name_ru,
             d.name_en,             
             d.ref_doc_type,
             dt.rec_id as REF_DOC_TYPE_REC_ID,
             dt.name_ru as DOC_TYPE_NAME,
             d.ref_respondent,
             lp.rec_id as REF_RESPONDENT_REC_ID,
             lp.name_ru as RESPONDENT_NAME,
             d.begin_date,
             d.end_date,             
             d.datlast,
             d.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             dt.user_location,
             sk.name as sent_knd
        from v_ref_document d,
             v_ref_doc_type dt,
             v_ref_respondent r,
             v_ref_legal_person lp,
             f_users u,
             sent_knd sk
       where d.id_usr = u.user_id
         and d.ref_doc_type = dt.id
         and d.ref_respondent = r.id
         and r.ref_legal_person = lp.id         
         and d.sent_knd = sk.sent_knd
         and (date_ is null or d.begin_date = (select max(t.begin_date)
                                                 from v_ref_document t
                                                where t.rec_id = d.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (d.end_date is null or d.end_date > date_))
       order by d.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_document_list;  
  
  procedure ref_read_doc_list_by_params(
    id_             in  ref_document.id % type,
    date_           in  Date,
    name_ru_        in  ref_document.name_ru % type,
    rec_id_         in  ref_document.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2     
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_DOC_LIST_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    Open Cur for
      select d.id,
             d.rec_id,
             d.code,
             d.name_kz,
             d.name_ru,
             d.name_en,             
             d.ref_doc_type,
             dt.rec_id as REF_DOC_TYPE_REC_ID,
             dt.name_ru as DOC_TYPE_NAME,
             d.ref_respondent,
             lp.rec_id as REF_RESPONDENT_REC_ID,
             lp.name_ru as RESPONDENT_NAME,
             d.begin_date,
             d.end_date,
             d.datlast,
             d.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             dt.user_location,
             sk.name as sent_knd
        from v_ref_document d,
             v_ref_doc_type dt,
             v_ref_respondent r,
             v_ref_legal_person lp,
             f_users u,
             sent_knd sk
       where d.id_usr = u.user_id
         and d.ref_doc_type = dt.id
         and d.ref_respondent = r.id
         and r.ref_legal_person = lp.id         
         and d.sent_knd = sk.sent_knd
         and d.rec_id = (select max(t1.rec_id)
                          from v_ref_document t1
                         where ((
                                  (search_all_ver_ = 1) and 
                                  (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name)))
                                 ) or 
                                 (
                                  (search_all_ver_ is null or search_all_ver_ = 0) and 
                                  (trim(name_ru_) is null or upper(d.name_ru) like upper(trim(v_name)))
                                 ) 
                               )
                           and t1.rec_id = d.rec_id
                         )
         and (id_ is null or d.id = id_)
--         and (trim(name_ru_) is null or upper(d.name_ru) like upper(trim(v_name)))
         and (rec_id_ is null or d.rec_id = rec_id_)
         and (date_ is null or d.begin_date = (select max(t.begin_date)
                                                 from v_ref_document t
                                                where t.rec_id = d.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (d.end_date is null or d.end_date > date_))
       order by d.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_doc_list_by_params;  
  
  
  procedure ref_read_document_hst_list(
    id_      in  ref_document.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_DOCUMENT_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select dh.id_hst,
             dh.id,
             dh.rec_id,
             dh.code,
             dh.name_kz,
             dh.name_ru,
             dh.name_en,   
             dh.ref_doc_type,
             dt.name_ru as DOC_TYPE_NAME,
             dh.ref_respondent,
             lp.name_ru as RESPONDENT_NAME,             
             dh.begin_date,
             dh.end_date,
             dh.datlast,
             dh.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             dh.type_change,
             tc.name as type_change_name,
             dh.user_location,
             sk.name as sent_knd
        from ref_document_hst dh,
             ref_doc_type dt,
             ref_respondent r,
             ref_legal_person lp,
             f_users u,
             type_change tc,
             sent_knd sk
       where dh.id_usr = u.user_id
         and dh.sent_knd = sk.sent_knd
         and dh.type_change = tc.type_change
         and dh.ref_doc_type = dt.id
         and dh.ref_respondent = r.id
         and r.ref_legal_person = lp.id
         and dh.id = id_
       order by dh.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_document_hst_list;
  
  
  procedure ref_insert_document(
    rec_id_              in  ref_document.rec_id         % type,
    code_                in  ref_document.code           % type,    
    name_kz_             in  ref_document.name_kz        % type,
    name_ru_             in  ref_document.name_ru        % type,
    name_en_             in  ref_document.name_en        % type,                
    ref_doc_type_        in  ref_document.ref_doc_type   % type,
    ref_respondent_      in  ref_document.ref_respondent % type,
    begin_date_          in  ref_document.begin_date     % type,
    end_date_            in  ref_document.end_Date       % type,
    id_usr_              in  ref_document.id_usr         % type,    
    user_location_       in  ref_document.user_location  % type,
    datlast_             in  ref_document.datlast        % type,
    do_commit_           in  integer default 1,
    id_                  out ref_document.id % type,
    err_code             out number,
    err_msg              out varchar2
  )
  is
    ProcName     constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_DOCUMENT';    
    ref_code     Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_document';
    
    if (trim(code_) is null) or 
       (trim(name_ru_) is null) or
       (begin_date_ is null) or 
       (ref_doc_type_ is null) or 
       (ref_respondent_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if; 
    
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if; */
    
    /*if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;*/
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    id_ := seq_ref_document_id.nextval;    
                  
    insert into ref_document(
      id,
      rec_id,
      code,
      name_kz,
      name_ru,
      name_en,
      ref_doc_type,
      ref_respondent,
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),
      code_,
      name_kz_,
      name_ru_,
      name_en_,
      ref_doc_type_,
      ref_respondent_,
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_
    );
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';         
  end ref_insert_document;
   

  procedure ref_update_document(
    id_                  in  ref_document.id             % type,
    rec_id_              in  ref_document.rec_id         % type,
    code_                in  ref_document.code           % type,    
    name_kz_             in  ref_document.name_kz        % type,
    name_ru_             in  ref_document.name_ru        % type,
    name_en_             in  ref_document.name_en        % type,                
    ref_doc_type_        in  ref_document.ref_doc_type   % type,
    ref_respondent_      in  ref_document.ref_respondent % type,
    begin_date_          in  ref_document.begin_date     % type,
    end_date_            in  ref_document.end_Date       % type,
    id_usr_              in  ref_document.id_usr         % type,    
    user_location_       in  ref_document.user_location  % type,
    datlast_             in  ref_document.datlast        % type,
    do_commit_           in  integer default 1,     
    err_code             out number,
    err_msg              out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_DOCUMENT';
    v_have_chg        boolean;
    v_ref_data        ref_document %rowtype;
    ref_code          Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_document';
    v_have_chg := false;
    
    if (trim(code_) is null) or 
       (trim(name_ru_) is null) or
       (begin_date_ is null) or 
       (ref_doc_type_ is null) or 
       (ref_respondent_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;*/
    
    /*if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;*/
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    /*report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;*/
    
    begin
      select *
        into v_ref_data
        from ref_document
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;

    if v_ref_data.id <> 0 then
      if nvl(v_ref_data.code,' ') <> nvl(code_,' ') or
         nvl(v_ref_data.name_kz,' ') <> nvl(name_kz_,' ') or 
         nvl(v_ref_data.name_ru,' ') <> nvl(name_ru_,' ') or
         nvl(v_ref_data.name_en,' ') <> nvl(name_en_,' ') or 
         nvl(v_ref_data.ref_doc_type,0) <> nvl(ref_doc_type_,0) or
         nvl(v_ref_data.ref_respondent,0) <> nvl(ref_respondent_,0) or         
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or 
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) then

        v_have_chg := true;

      end if;
    end if;
    
    if v_have_chg = true then
      update ref_document
         set code                = code_,
             name_kz             = name_kz_,
             name_ru             = name_ru_,
             name_en             = name_en_,           
             ref_doc_type        = ref_doc_type_,
             ref_respondent      = ref_respondent_,
             begin_date          = begin_date_,
             end_date            = end_date_,
             id_usr              = id_usr_,
             user_location       = user_location_,
             sent_knd            = 0,
             datlast             = datlast_
       where id = id_;
             
      if do_commit_ = 1 then
        Commit;
      end if;
    end if;
    
  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';
  end ref_update_document;
  
  
  procedure ref_delete_document(
    id_           in  ref_document.id % type,
    do_commit_    in  integer default 1,     
    err_code      out number,
    err_msg       out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_DOCUMENT';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_document',
                          /*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
  
    update ref_document 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_document;
  
  
  /* Эмитенты */
  procedure ref_read_issuers_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2   
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_ISSUERS_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select i.id,
             i.rec_id,
             i.code,
             i.name_kz,
             i.name_ru,
             i.name_en,
             i.begin_date,
             i.end_date,
             i.sign_name,
             i.is_state,
             i.is_resident,
             i.listing_estimation,
             i.rating_estimation,
             i.is_from_kase,
             i.datlast,
             i.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             i.user_location,
             sk.name as sent_knd
        from v_ref_issuers i,
             f_users u,
             sent_knd sk
       where i.id_usr = u.user_id         
         and i.sent_knd = sk.sent_knd
         and (date_ is null or i.begin_date = (select max(t.begin_date)
                                                 from v_ref_issuers t
                                                where t.rec_id = i.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (i.end_date is null or i.end_date > date_))
       order by i.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_issuers_list;
  
  procedure ref_read_issuers_l_by_params(
    id_             in  ref_issuers.id % type,
    date_           in  Date,
    name_ru_        in  ref_issuers.name_ru % type,
    rec_id_         in  ref_issuers.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2   
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_ISSUERS_L_BY_PARAMS';
    v_name            varchar2(524);    
    v_search_all_ver  integer;
    Sql_Text          VarChar2(12000);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    if (trim(name_ru_) is null) then
      v_search_all_ver := 0;
    else 
      v_search_all_ver := search_all_ver_;
    end if;        
    
    Sql_Text := 'select i.id,
                       i.rec_id,
                       i.code,
                       i.name_kz,
                       i.name_ru,
                       i.name_en,
                       i.begin_date,
                       i.end_date,
                       i.sign_name,
                       i.is_state,
                       i.is_resident,
                       i.listing_estimation,
                       i.rating_estimation,
                       i.is_from_kase,
                       i.datlast,
                       i.id_usr,
                       u.last_name || '' '' || u.first_name || '' '' || u.middle_name as USER_NAME,                          
                       i.user_location,
                       sk.name as sent_knd
                  from v_ref_issuers i,
                       f_users u,
                       sent_knd sk
                 where i.id_usr = u.user_id
                   and i.sent_knd = sk.sent_knd';
                     
      if (v_search_all_ver is null or v_search_all_ver = 0) then
        if trim(name_ru_) is not null then
          Sql_Text := Sql_Text || ' and upper(i.name_ru) like upper(trim(''' || v_name || '''))';
        end if;        
      end if;
      
      if (v_search_all_ver = 1) then
        Sql_Text := Sql_Text || ' and i.rec_id = (select max(i1.rec_id) ' ||
                                                   'from v_ref_issuers i1 ' ||
                                                  'where i1.rec_id = i.rec_id ';
        if trim(name_ru_) is not null then
          Sql_Text := Sql_Text || ' and upper(i1.name_ru) like upper(trim(''' || v_name || '''))';
        end if;
        Sql_Text := Sql_Text || ')';
      end if;
      
      if (id_ is not null) then
        Sql_Text := Sql_Text || ' and i.id = ' || id_ ;
      end if;
      
      if (rec_id_ is not null) then
        Sql_Text := Sql_Text || ' and i.rec_id = ' || rec_id_ ;
      end if;
      
      if (date_ is not null) then
        Sql_Text := Sql_Text || ' and i.begin_date = (select max(i2.begin_date) ' ||
                                                       'from v_ref_issuers i2 ' ||
                                                      'where i2.rec_id = i.rec_id ' ||
                                                        'and i2.begin_date <= ''' || date_ || ''')';
      end if;

    open Cur for Sql_Text;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_issuers_l_by_params;
  
    
  /* Ценные бумаги */    
  procedure ref_read_securities_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_SECURITIES_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select s.id,
             s.rec_id,
             s.s_issuer,
             s.issuer_name,
             s.s_g_issuer_sign,
             s.sign_name,
             s.sign_code,
             s.is_resident,
             s.is_state,
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
             s.begin_date,
             s.end_date,
             s.issue_volume,
             s.circul_period,
             s.listing_estimation,
             s.rating_estimation,
             s.is_bond_program,
             s.bond_program_volume,
             s.bond_prg_cnt,
             s.is_garant,
             s.garant,
             s.is_permit,
             s.is_from_kase,
             s.datlast,
             s.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             s.user_location,
             sk.name as sent_knd,
             s.sec_var_rec_id,
             s.currency_rec_id
        from v_ref_securities s,
             f_users u,
             sent_knd sk
       where s.id_usr = u.user_id         
         and s.sent_knd = sk.sent_knd
         and (date_ is null or s.begin_date = (select max(t.begin_date)
                                                 from v_ref_securities t
                                                where t.rec_id = s.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (s.end_date is null or s.end_date > date_))
 order by s.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_securities_list;
  
  procedure ref_read_sec_list_by_params(
    id_             in  ref_securities.id % type,
    date_           in  Date,
    issuer_name_    in  ref_securities.issuer_name % type,
    nin_            in  ref_securities.nin % type,
    rec_id_         in  ref_securities.rec_id % type,    
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_SEC_LIST_BY_PARAMS';
    v_name            varchar2(524);
    v_nin             varchar2(100);
    v_search_all_ver  integer;
    Sql_Text          VarChar2(12000);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    if (trim(issuer_name_) is null) and (trim(nin_) is null) then
      v_search_all_ver := 0;
    else 
      v_search_all_ver := search_all_ver_;
    end if;
    
    v_name := '%' || issuer_name_ || '%';
    v_nin := nin_ || '%';
    
    Sql_Text := 'select s.id,
                         s.rec_id,
                         s.s_issuer,
                         s.issuer_name,
                         s.s_g_issuer_sign,
                         s.sign_name,
                         s.sign_code,
                         s.is_resident,
                         s.is_state,
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
                         s.begin_date,
                         s.end_date,
                         s.issue_volume,
                         s.circul_period,
                         s.listing_estimation,
                         s.rating_estimation,
                         s.is_bond_program,
                         s.bond_program_volume,
                         s.bond_prg_cnt,
                         s.is_garant,
                         s.garant,
                         s.is_permit,
                         s.is_from_kase,
                         s.datlast,
                         s.id_usr,
                         u.last_name || '' '' || u.first_name || '' '' || u.middle_name as USER_NAME,                          
                         s.user_location,
                         sk.name as sent_knd,
                         s.sec_var_rec_id,
                         s.currency_rec_id 
                    from v_ref_securities s,
                         f_users u,
                         sent_knd sk
                   where s.id_usr = u.user_id
                     and s.sent_knd = sk.sent_knd';
                     
      if (v_search_all_ver is null or v_search_all_ver = 0) then
        if trim(issuer_name_) is not null then
          Sql_Text := Sql_Text || ' and upper(s.issuer_name) like upper(trim(''' || v_name || '''))';
        end if;
        if trim(nin_) is not null then
          Sql_Text := Sql_Text || ' and upper(s.nin) like upper(trim(''' || v_nin || '''))';
        end if;
      end if;
      
      if (v_search_all_ver = 1) then
        Sql_Text := Sql_Text || ' and s.rec_id = (select max(s1.rec_id) ' ||
                                                   'from v_ref_securities s1 ' ||
                                                  'where s1.rec_id = s.rec_id ';
        if trim(issuer_name_) is not null then
          Sql_Text := Sql_Text || ' and upper(s1.issuer_name) like upper(trim(''' || v_name || '''))';
        end if;
        if trim(nin_) is not null then
          Sql_Text := Sql_Text || ' and upper(s1.nin) like upper(trim(''' || v_nin || '''))';
        end if;
        Sql_Text := Sql_Text || ')';
      end if;
      
      if (id_ is not null) then
        Sql_Text := Sql_Text || ' and s.id = ' || id_ ;
      end if;
      
      if (rec_id_ is not null) then
        Sql_Text := Sql_Text || ' and s.rec_id = ' || rec_id_ ;
      end if;
      
      if (date_ is not null) then
        Sql_Text := Sql_Text || ' and s.begin_date = (select max(s2.begin_date) ' ||
                                                       'from v_ref_securities s2 ' ||
                                                      'where s2.rec_id = s.rec_id ' ||
                                                        'and s2.begin_date <= ''' || date_ || ''')';
      end if;

    open Cur for Sql_Text;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_sec_list_by_params;
  
  
  /* Вид операций */ 
  procedure ref_read_vid_oper_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2      
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_VID_OPER_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select vo.id,
             vo.rec_id,
             vo.code,
             vo.name_kz,
             vo.name_ru,
             vo.name_en,                          
             vo.begin_date,
             vo.end_date,
             vo.datlast,
             vo.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             vo.user_location,
             sk.name as sent_knd
        from v_ref_vid_oper vo,             
             f_users u,
             sent_knd sk
       where vo.id_usr = u.user_id         
         and vo.sent_knd = sk.sent_knd
         and (date_ is null or vo.begin_date = (select max(t.begin_date)
                                                 from v_ref_vid_oper t
                                                where t.rec_id = vo.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (vo.end_date is null or vo.end_date > date_))
       order by vo.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_vid_oper_list;  
  
  procedure ref_read_vid_oper_l_by_params(
    id_             in  ref_vid_oper.id % type,
    date_           in  Date,
    name_ru_        in  ref_vid_oper.name_ru % type,
    rec_id_         in  ref_vid_oper.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2      
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_VID_OPER_L_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    Open Cur for
      select vo.id,
             vo.rec_id,
             vo.code,
             vo.name_kz,
             vo.name_ru,
             vo.name_en,                          
             vo.begin_date,
             vo.end_date,            
             vo.datlast,
             vo.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             vo.user_location,
             sk.name as sent_knd
        from v_ref_vid_oper vo,             
             f_users u,
             sent_knd sk
       where vo.id_usr = u.user_id         
         and vo.sent_knd = sk.sent_knd
         and vo.rec_id = (select max(t1.rec_id)
                            from v_ref_vid_oper t1
                           where ((
                                    (search_all_ver_ = 1) and 
                                    (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name)))
                                   ) or 
                                   (
                                    (search_all_ver_ is null or search_all_ver_ = 0) and 
                                    (trim(name_ru_) is null or upper(vo.name_ru) like upper(trim(v_name)))
                                   ) 
                                 )
                             and t1.rec_id = vo.rec_id
                           )
         and (id_ is null or vo.id = id_)
--         and (trim(name_ru_) is null or upper(vo.name_ru) like upper(trim(v_name)))
         and (rec_id_ is null or vo.rec_id = rec_id_)
         and (date_ is null or vo.begin_date = (select max(t.begin_date)
                                                 from v_ref_vid_oper t
                                                where t.rec_id = vo.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (vo.end_date is null or vo.end_date > date_))
       order by vo.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_vid_oper_l_by_params;  
  
  
  procedure ref_read_vid_oper_hst_list(
    id_           in  ref_vid_oper.id % type,
    Cur           out sys_refcursor,
    err_code      out number,
    err_msg       out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_VID_OPER_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select voh.id_hst,
             voh.id,
             voh.rec_id,
             voh.code,
             voh.name_kz,
             voh.name_ru,
             voh.name_en,                             
             voh.begin_date,
             voh.end_date,
             voh.datlast,
             voh.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             voh.type_change,
             tc.name as type_change_name,
             voh.user_location,
             sk.name as sent_knd
        from ref_vid_oper_hst voh,
             f_users u,
             type_change tc,
             sent_knd sk
       where voh.id_usr = u.user_id
         and voh.sent_knd = sk.sent_knd
         and voh.type_change = tc.type_change
         and voh.id = id_
       order by voh.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_vid_oper_hst_list;
  
  
   procedure ref_insert_vid_oper(
    rec_id_        in  ref_vid_oper.rec_id         % type,
    code_          in  ref_vid_oper.code           % type,    
    name_kz_       in  ref_vid_oper.name_kz        % type,
    name_ru_       in  ref_vid_oper.name_ru        % type,
    name_en_       in  ref_vid_oper.name_en        % type,                    
    begin_date_    in  ref_vid_oper.begin_date     % type,
    end_date_      in  ref_vid_oper.end_date       % type,
    id_usr_        in  ref_vid_oper.id_usr         % type,    
    user_location_ in  ref_vid_oper.user_location  % type,
    datlast_       in  ref_vid_oper.datlast        % type,
    do_commit_     in  integer default 1,
    id_            out ref_vid_oper.id % type,
    err_code       out number,
    err_msg        out varchar2
  )
  is
    ProcName     constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_VID_OPER';    
    ref_code     Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_vid_oper';
    
    if (trim(code_) is null) or 
       (trim(name_ru_) is null) or
       (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if; 
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if; */
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_, begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    id_ := seq_ref_vid_oper_id.nextval;
                  
    insert into ref_vid_oper(
      id,
      rec_id,
      code,
      name_kz,
      name_ru,
      name_en,      
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),
      code_,
      name_kz_,                 
      name_ru_, 
      name_en_,            
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_
    );
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';
  end ref_insert_vid_oper;
   

  procedure ref_update_vid_oper(
    id_                  in  ref_vid_oper.id   % type,
    rec_id_              in  ref_vid_oper.rec_id         % type,
    code_                in  ref_vid_oper.code           % type,    
    name_kz_             in  ref_vid_oper.name_kz        % type,
    name_ru_             in  ref_vid_oper.name_ru        % type,
    name_en_             in  ref_vid_oper.name_en        % type,                    
    begin_date_          in  ref_vid_oper.begin_date     % type,
    end_date_            in  ref_vid_oper.end_date       % type,
    id_usr_              in  ref_vid_oper.id_usr         % type,    
    user_location_       in  ref_vid_oper.user_location  % type,
    datlast_             in  ref_vid_oper.datlast        % type,
    do_commit_           in  integer default 1,     
    err_code             out number,
    err_msg              out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_VID_OPER';
    v_have_chg        boolean;
    v_ref_data        ref_vid_oper %rowtype;
    ref_code          Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_vid_oper';
    v_have_chg := false;
    
    if (trim(code_) is null) or
       (trim(name_ru_) is null) or
       (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;*/
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    /*report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;*/
    
    begin
      select *
        into v_ref_data
        from ref_vid_oper
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;

    if v_ref_data.id <> 0 then
      if nvl(v_ref_data.code,' ') <> nvl(code_,' ') or
         nvl(v_ref_data.name_kz,' ') <> nvl(name_kz_,' ') or 
         nvl(v_ref_data.name_ru,' ') <> nvl(name_ru_,' ') or
         nvl(v_ref_data.name_en,' ') <> nvl(name_en_,' ') or          
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or 
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) then

        v_have_chg := true;

      end if;
    end if;
    
    if v_have_chg = true then
      update ref_vid_oper
         set code          = code_,
             name_kz       = name_kz_,
             name_ru       = name_ru_,
             name_en       = name_en_,                      
             begin_date    = begin_date_,
             end_date      = end_date_,
             id_usr        = id_usr_,
             user_location = user_location_,
             sent_knd      = 0,
             datlast       = datlast_
       where id = id_;
             
      if do_commit_ = 1 then
        Commit;
      end if;
    end if;
    
  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';
  end ref_update_vid_oper;
  
  
  procedure ref_delete_vid_oper(
    id_           in  ref_vid_oper.id % type,
    do_commit_    in  integer default 1,     
    err_code      out number,
    err_msg       out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_VID_OPER';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_vid_oper',
                          /*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
  
    update ref_vid_oper 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_vid_oper;
  
  
  /* Вид ЦБ */
  /*procedure ref_read_vid_cb_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2      
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_VID_CB_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select vc.id,
             vc.rec_id,
             vc.code,
             vc.name_kz,
             vc.name_ru,
             vc.name_en,                          
             vc.begin_date,
             vc.end_date,
             vc.datlast,
             vc.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             vc.user_location,
             sk.name as sent_knd
        from v_ref_vid_cb vc,
             f_users u,
             sent_knd sk
       where vc.id_usr = u.user_id                  
         and vc.sent_knd = sk.sent_knd
         and (date_ is null or vc.begin_date = (select max(t.begin_date)
                                                 from v_ref_vid_cb t
                                                where t.rec_id = vc.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (vc.end_date is null or vc.end_date > date_))
       order by vc.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_vid_cb_list;  
  
  procedure ref_read_vid_cb_l_by_params(
    id_             in  ref_vid_cb.id % type,
    date_           in  Date,
    name_ru_        in  ref_vid_cb.name_ru % type,
    rec_id_         in  ref_vid_cb.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2      
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_VID_CB_L_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    Open Cur for
      select vc.id,
             vc.rec_id,
             vc.code,
             vc.name_kz,
             vc.name_ru,
             vc.name_en,                          
             vc.begin_date,
             vc.end_date,
             vc.datlast,
             vc.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             vc.user_location,
             sk.name as sent_knd
        from v_ref_vid_cb vc,
             f_users u,
             sent_knd sk
       where vc.id_usr = u.user_id
         and vc.sent_knd = sk.sent_knd
         and vc.rec_id = (select max(t1.rec_id)
                            from v_ref_vid_cb t1
                           where ((
                                    (search_all_ver_ = 1) and 
                                    (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name)))
                                   ) or 
                                   (
                                    (search_all_ver_ is null or search_all_ver_ = 0) and 
                                    (trim(name_ru_) is null or upper(vc.name_ru) like upper(trim(v_name)))
                                   ) 
                                 )
                             and t1.rec_id = vc.rec_id
                           )
         and (id_ is null or vc.id = id_)
--         and (trim(name_ru_) is null or upper(vc.name_ru) like upper(trim(v_name)))
         and (rec_id_ is null or vc.rec_id = rec_id_)
         and (date_ is null or vc.begin_date = (select max(t.begin_date)
                                                 from v_ref_vid_cb t
                                                where t.rec_id = vc.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (vc.end_date is null or vc.end_date > date_))
       order by vc.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_vid_cb_l_by_params; 
  
  
  procedure ref_read_vid_cb_hst_list(
    id_         in  ref_vid_cb.id % type,
    Cur         out sys_refcursor,
    err_code    out number,
    err_msg     out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_VID_CB_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select vch.id_hst,
             vch.id,
             vch.rec_id,
             vch.code,
             vch.name_kz,
             vch.name_ru,
             vch.name_en,                             
             vch.begin_date,
             vch.end_date,
             vch.datlast,
             vch.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             vch.type_change,
             tc.name as type_change_name,
             vch.user_location,
             sk.name as sent_knd
        from ref_vid_cb_hst vch,
             f_users u,
             type_change tc,
             sent_knd sk
       where vch.id_usr = u.user_id
         and vch.sent_knd = sk.sent_knd
         and vch.type_change = tc.type_change
         and vch.id = id_
       order by vch.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_vid_cb_hst_list;
  
  
  procedure ref_insert_vid_cb(
    rec_id_        in  ref_vid_cb.rec_id         % type,
    code_          in  ref_vid_cb.code           % type,    
    name_kz_       in  ref_vid_cb.name_kz        % type,
    name_ru_       in  ref_vid_cb.name_ru        % type,
    name_en_       in  ref_vid_cb.name_en        % type,                    
    begin_date_    in  ref_vid_cb.begin_date     % type,
    end_date_      in  ref_vid_cb.end_date       % type,
    id_usr_        in  ref_vid_cb.id_usr         % type,    
    user_location_ in  ref_vid_cb.user_location  % type,
    datlast_       in  ref_vid_cb.datlast        % type,
    do_commit_     in  integer default 1,
    id_            out ref_vid_cb.id % type,
    err_code       out number,
    err_msg        out varchar2
  )
  is
    ProcName     constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_VID_CB';    
    ref_code     Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_vid_cb';
    
    if (trim(code_) is null) or 
       (trim(name_ru_) is null) or
       (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if; 
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    \*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if; *\
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    id_ := seq_ref_vid_cb_id.nextval;    
                  
    insert into ref_vid_cb(
      id,
      rec_id,
      code,
      name_kz,
      name_ru,
      name_en,      
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),
      code_,
      name_kz_,                 
      name_ru_, 
      name_en_,            
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_
    );
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';      
  end ref_insert_vid_cb;
   

  procedure ref_update_vid_cb(
    id_            in  ref_vid_cb.id             % type,
    rec_id_        in  ref_vid_cb.rec_id         % type,
    code_          in  ref_vid_cb.code           % type,    
    name_kz_       in  ref_vid_cb.name_kz        % type,
    name_ru_       in  ref_vid_cb.name_ru        % type,
    name_en_       in  ref_vid_cb.name_en        % type,                    
    begin_date_    in  ref_vid_cb.begin_date     % type,
    end_date_      in  ref_vid_cb.end_date       % type,
    id_usr_        in  ref_vid_cb.id_usr         % type,    
    user_location_ in  ref_vid_cb.user_location  % type,
    datlast_       in  ref_vid_cb.datlast        % type,
    do_commit_     in  integer default 1,     
    err_code       out number,
    err_msg        out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_VID_CB';
    v_have_chg        boolean;
    v_ref_data        ref_vid_cb %rowtype;
    ref_code          Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_have_chg := false;
    ref_code := 'ref_vid_cb';
    
    if (trim(code_) is null) or 
       (trim(name_ru_) is null) or
       (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    \*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;*\
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    \*report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;*\
    
    begin
      select *
        into v_ref_data
        from ref_vid_cb
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;


    if v_ref_data.id <> 0 then
      if nvl(v_ref_data.code,' ') <> nvl(code_,' ') or
         nvl(v_ref_data.name_kz,' ') <> nvl(name_kz_,' ') or 
         nvl(v_ref_data.name_ru,' ') <> nvl(name_ru_,' ') or
         nvl(v_ref_data.name_en,' ') <> nvl(name_en_,' ') or          
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or 
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) then

        v_have_chg := true;

      end if;
    end if;
    
    if v_have_chg = true then
      update ref_vid_cb
         set code          = code_,
             name_kz       = name_kz_,
             name_ru       = name_ru_,
             name_en       = name_en_,                      
             begin_date    = begin_date_,
             end_date      = end_date_,
             id_usr        = id_usr_,
             user_location = user_location_,
             sent_knd      = 0,
             datlast       = datlast_
       where id = id_;
             
      if do_commit_ = 1 then
        Commit;
      end if;
    end if;

  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';  
  end ref_update_vid_cb;
  
  
  procedure ref_delete_vid_cb(
    id_         in  ref_vid_cb.id % type,
    do_commit_  in  integer default 1,     
    err_code    out number,
    err_msg     out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_VID_CB';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_vid_cb',
                          \*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*\
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
  
    update ref_vid_cb 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_vid_cb;
  */
 
  /* Отрасли */
  procedure ref_read_branch_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2       
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_BRANCH_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select b.id,
             b.rec_id,
             b.code,
             b.name_kz,
             b.name_ru,
             b.name_en,                          
             b.begin_date,
             b.end_date,
             b.datlast,
             b.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             b.user_location,
             sk.name as sent_knd
        from v_ref_branch b,
             f_users u,
             sent_knd sk
       where b.id_usr = u.user_id
         and b.sent_knd = sk.sent_knd
         and (date_ is null or b.begin_date = (select max(t.begin_date)
                                                 from v_ref_branch t
                                                where t.rec_id = b.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (b.end_date is null or b.end_date > date_))
       order by b.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_branch_list;  
  
  procedure ref_read_branch_list_by_params(
    id_             in  ref_branch.id % type,
    date_           in  Date,
    code_           in  ref_branch.code % type,
    name_ru_        in  ref_branch.name_ru % type,
    rec_id_         in  ref_branch.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2       
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_BRANCH_LIST_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    Open Cur for
      select b.id,
             b.rec_id,
             b.code,
             b.name_kz,
             b.name_ru,
             b.name_en,                          
             b.begin_date,
             b.end_date,
             b.datlast,
             b.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             b.user_location,
             sk.name as sent_knd
        from v_ref_branch b,
             f_users u,
             sent_knd sk
       where b.id_usr = u.user_id
         and b.sent_knd = sk.sent_knd
         and b.rec_id = (select max(t1.rec_id)
                          from v_ref_branch t1
                         where ((
                                  (search_all_ver_ = 1) and 
                                  (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name))) and
                                  (trim(code_) is null or upper(t1.code) like upper(trim(code_)) || '%')
                                 ) or 
                                 (
                                  (search_all_ver_ is null or search_all_ver_ = 0) and 
                                  (trim(name_ru_) is null or upper(b.name_ru) like upper(trim(v_name))) and
                                  (trim(code_) is null or upper(b.code) like upper(trim(code_)) || '%')
                                 ) 
                               )
                           and t1.rec_id = b.rec_id
                         )
         and (id_ is null or b.id = id_)
         /*and (trim(code_) is null or upper(b.code) like upper(trim(code_)) || '%')
         and (trim(name_ru_) is null or upper(b.name_ru) like upper(trim(v_name)))*/
         and (rec_id_ is null or b.rec_id = rec_id_)
         and (date_ is null or b.begin_date = (select max(t.begin_date)
                                                 from v_ref_branch t
                                                where t.rec_id = b.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (b.end_date is null or b.end_date > date_))
       order by b.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_branch_list_by_params;  
  
  
  /*procedure ref_read_branch_hst_list(
    id_         in  ref_branch.id % type,
    Cur         out sys_refcursor,
    err_code    out number,
    err_msg     out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_BRANCH_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select bh.id_hst,
             bh.id,
             bh.rec_id,
             bh.code,
             bh.name_kz,
             bh.name_ru,
             bh.name_en,                             
             bh.begin_date,
             bh.end_date,
             bh.datlast,
             bh.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             bh.type_change,
             tc.name as type_change_name,
             bh.user_location,
             sk.name as sent_knd
        from ref_branch_hst bh,             
             f_users u,
             type_change tc,
             sent_knd sk
       where bh.id_usr = u.user_id
         and bh.sent_knd = sk.sent_knd
         and bh.type_change = tc.type_change
         and bh.id = id_
       order by bh.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_branch_hst_list;
  
  
  procedure ref_insert_branch(
    rec_id_        in  ref_branch.rec_id         % type,
    code_          in  ref_branch.code           % type,    
    name_kz_       in  ref_branch.name_kz        % type,
    name_ru_       in  ref_branch.name_ru        % type,
    name_en_       in  ref_branch.name_en        % type,                    
    begin_date_    in  ref_branch.begin_date     % type,
    end_date_      in  ref_branch.end_date       % type,
    id_usr_        in  ref_branch.id_usr         % type,    
    user_location_ in  ref_branch.user_location  % type,
    datlast_       in  ref_branch.datlast        % type,
    do_commit_     in  integer default 1,
    id_            out ref_branch.id % type,
    err_code       out number,
    err_msg        out varchar2
  )
  is
    ProcName     constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_BRANCH';    
    ref_code     Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_branch';
    
    if (trim(code_) is null) or 
       (trim(name_ru_) is null) or
       (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if; 
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    \*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if; *\
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    id_ := seq_ref_branch_id.nextval;
                  
    insert into ref_branch(
      id,
      rec_id,
      code,
      name_kz,
      name_ru,
      name_en,      
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),
      code_,
      name_kz_,                 
      name_ru_, 
      name_en_,            
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_
    );
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';
  end ref_insert_branch;
   

  procedure ref_update_branch(
    id_            in  ref_branch.id             % type,
    rec_id_        in  ref_branch.rec_id         % type,
    code_          in  ref_branch.code           % type,    
    name_kz_       in  ref_branch.name_kz        % type,
    name_ru_       in  ref_branch.name_ru        % type,
    name_en_       in  ref_branch.name_en        % type,                    
    begin_date_    in  ref_branch.begin_date     % type,
    end_date_      in  ref_branch.end_date       % type,
    id_usr_        in  ref_branch.id_usr         % type,    
    user_location_ in  ref_branch.user_location  % type,
    datlast_       in  ref_branch.datlast        % type,
    do_commit_     in  integer default 1,     
    err_code       out number,
    err_msg        out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_BRANCH';
    v_have_chg        boolean;
    v_ref_data        ref_branch %rowtype;
    ref_code          Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_have_chg := false;
    ref_code := 'ref_branch';
    
    if (trim(code_) is null) or 
       (trim(name_ru_) is null) or
       (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    \*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;*\
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
    
    begin
      select *
        into v_ref_data
        from ref_branch
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;


    if v_ref_data.id <> 0 then
      if nvl(v_ref_data.code,' ') <> nvl(code_,' ') or
         nvl(v_ref_data.name_kz,' ') <> nvl(name_kz_,' ') or 
         nvl(v_ref_data.name_ru,' ') <> nvl(name_ru_,' ') or
         nvl(v_ref_data.name_en,' ') <> nvl(name_en_,' ') or          
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or 
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) then

        v_have_chg := true;

      end if;
    end if;
    
    if v_have_chg = true then
      update ref_branch
         set code          = code_,
             name_kz       = name_kz_,
             name_ru       = name_ru_,
             name_en       = name_en_,                      
             begin_date    = begin_date_,
             end_date      = end_date_,
             id_usr        = id_usr_,
             user_location = user_location_,
             sent_knd      = 0,
             datlast       = datlast_
       where id = id_;
             
      if do_commit_ = 1 then
        Commit;
      end if;
   end if;

  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';
  end ref_update_branch;
  
  
  procedure ref_delete_branch(
    id_         in  ref_branch.id % type,
    do_commit_  in  integer default 1,     
    err_code    out number,
    err_msg     out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_BRANCH';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_branch',
                          begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
      
    update ref_branch 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_branch;*/
    
  
  /* Межформенный контроль */ 
  procedure ref_read_crosscheck_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_CROSSCHECK_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select c.id,
             c.rec_id,             
             c.l_src_formula,
             c.r_src_formula,
             c.l_rel_formula,
             c.r_rel_formula, 
             c.l_desc, 
             c.r_desc, 
             c.l_src_cond, 
             c.r_src_cond, 
             c.l_rel_cond, 
             c.r_rel_cond,
             c.formula_symbol,
             c.cond_symbol,
             c.crosscheck_type,
             t.name cross_type_name,                          
             c.num,
             c.is_available,
             c.begin_date,
             c.end_date,
             c.datlast,
             c.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             c.user_location
        from v_ref_crosscheck c,
             crosscheck_type t,
             f_users u
       where c.id_usr = u.user_id
         and c.crosscheck_type = t.id
         and (date_ is null or c.begin_date = (select max(t.begin_date)
                                                 from v_ref_crosscheck t
                                                where t.rec_id = c.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (c.end_date is null or c.end_date > date_))
       order by c.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_crosscheck_list;  
  
  procedure ref_read_cross_list_by_params(
    id_             in  ref_crosscheck.id % type,
    date_           in  Date,
    l_src_formula_  in  ref_crosscheck.l_src_formula % type,
    r_src_formula_  in  ref_crosscheck.r_src_formula % type,
    l_desc_         in  ref_crosscheck.l_desc % type,
    r_desc_         in  ref_crosscheck.r_desc % type,
    cf_type_        in  ref_crosscheck.crosscheck_type % type,
    l_src_cond_     in  ref_crosscheck.l_src_cond % type,
    r_src_cond_     in  ref_crosscheck.r_src_cond % type,
    is_available_   in  ref_crosscheck.is_available % type,
    rec_id_         in  ref_crosscheck.rec_id % type,
    form_codes_     in  form_code_array,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_CROSS_LIST_BY_PARAMS';
    v_l_src_formula ref_crosscheck.l_src_formula % type;
    v_r_src_formula ref_crosscheck.r_src_formula % type;
    v_l_desc        ref_crosscheck.l_desc % type;
    v_r_desc        ref_crosscheck.r_desc % type;    
    v_l_src_cond    ref_crosscheck.l_src_cond % type;
    v_r_src_cond    ref_crosscheck.r_src_cond % type;
    v_is_available ref_crosscheck.is_available % type;
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_l_src_formula := '%' || l_src_formula_ || '%';
    v_r_src_formula := '%' || r_src_formula_ || '%';
    v_l_desc := '%' || l_desc_ || '%';
    v_r_desc := '%' || r_desc_ || '%';
    v_l_src_cond := '%' || l_src_cond_ || '%';
    v_r_src_cond := '%' || r_src_cond_ || '%';    
    
    if is_available_ is null then
      v_is_available := 0;
    else
      v_is_available := is_available_;
    end if;
    
    Open Cur for
      select c.id,
             c.rec_id,
             c.l_src_formula || ' ' || get_condition(c.formula_symbol) || ' ' || c.r_src_formula as src_formula,
             c.l_src_formula,
             c.r_src_formula,
             c.l_rel_formula,
             c.r_rel_formula,
             c.l_desc ||  ' ' || get_condition(c.formula_symbol) || ' ' || c.r_desc as descrus,
             c.l_desc, 
             c.r_desc,
             c.l_src_cond || ' ' || get_condition(c.cond_symbol) || ' ' || c.r_src_cond as src_cond,
             c.l_src_cond, 
             c.r_src_cond, 
             c.l_rel_cond, 
             c.r_rel_cond,
             c.formula_symbol,
             c.cond_symbol,
             c.crosscheck_type,
             t.name cross_type_name,                          
             c.num,
             c.is_available,
             c.begin_date,
             c.end_date,
             c.datlast,
             c.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             c.user_location
        from v_ref_crosscheck c,
             ref_crosscheck_forms cf,
             crosscheck_type t,
             f_users u
       where c.id_usr = u.user_id
         and c.crosscheck_type = t.id
         and c.id = cf.ref_crosscheck_id
         and cf.form_code in (select * from TABLE(form_codes_))
         and c.rec_id = (select max(t1.rec_id)
                          from v_ref_crosscheck t1
                         where ((
                                  (search_all_ver_ = 1) and 
                                  ((trim(l_src_formula_) is null or upper(t1.l_src_formula) like upper(trim(v_l_src_formula))) or 
                                  (trim(r_src_formula_) is null or upper(t1.r_src_formula) like upper(trim(v_r_src_formula)))) and
                                  ((trim(l_desc_) is null or upper(t1.l_desc) like upper(trim(v_l_desc))) or
                                  (trim(r_desc_) is null or upper(t1.r_desc) like upper(trim(v_r_desc)))) and
                                  (is_available_ is null or t1.is_available = is_available_) and
                                  --t1.is_available = v_is_available and 
                                  (cf_type_ is null or t1.crosscheck_type = cf_type_) and 
                                  ((trim(l_src_cond_) is null or upper(t1.l_src_cond) like upper(trim(v_l_src_cond))) or
                                  (trim(r_src_cond_) is null or upper(t1.r_src_cond) like upper(trim(v_r_src_cond))))
                                 ) or 
                                 (
                                  (search_all_ver_ is null or search_all_ver_ = 0) and 
                                  ((trim(l_src_formula_) is null or upper(c.l_src_formula) like upper(trim(v_l_src_formula))) or
                                  (trim(r_src_formula_) is null or upper(c.r_src_formula) like upper(trim(v_r_src_formula)))) and
                                  ((trim(l_desc_) is null or upper(c.l_desc) like upper(trim(v_l_desc))) or
                                  (trim(r_desc_) is null or upper(c.r_desc) like upper(trim(v_r_desc)))) and
                                  (is_available_ is null or c.is_available = is_available_) and
                                  --c.is_available = v_is_available and
                                  (cf_type_ is null or c.crosscheck_type = cf_type_) and
                                  ((trim(l_src_cond_) is null or upper(c.l_src_cond) like upper(trim(v_l_src_cond))) or
                                  (trim(r_src_cond_) is null or upper(c.r_src_cond) like upper(trim(v_r_src_cond))))
                                 ) 
                               )
                           and t1.rec_id = c.rec_id
                         )
         and (id_ is null or c.id = id_)
         /*and (trim(formula_) is null or upper(c.formula) like upper(trim(v_formula)))
         and (trim(descr_rus_) is null or upper(c.description_ru) like upper(trim(v_descr_rus)))*/
         and (rec_id_ is null or c.rec_id = rec_id_)
         and (date_ is null or c.begin_date = (select max(t.begin_date)
                                                 from v_ref_crosscheck t
                                                where t.rec_id = c.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (c.end_date is null or c.end_date > date_))
       order by c.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_cross_list_by_params; 
  
  
  procedure ref_read_crosscheck_hst_list(
    id_             in  ref_crosscheck.id % type,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_CROSSCHECK_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select ch.id_hst,
             ch.id,
             ch.rec_id,                
             ch.l_src_formula,
             ch.r_src_formula,
             ch.l_rel_formula,
             ch.r_rel_formula, 
             ch.l_desc, 
             ch.r_desc, 
             ch.l_src_cond, 
             ch.r_src_cond, 
             ch.l_rel_cond, 
             ch.r_rel_cond,
             ch.formula_symbol,
             ch.cond_symbol,             
             ch.crosscheck_type,
             t.name cross_type_name,             
             ch.num,
             ch.is_available,
             ch.begin_date,
             ch.end_date,
             ch.datlast,
             ch.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             ch.type_change,
             tc.name as type_change_name,
             ch.user_location
        from ref_crosscheck_hst ch,
             crosscheck_type t,
             f_users u,
             type_change tc
       where ch.id_usr = u.user_id         
         and ch.crosscheck_type = t.id
         and ch.type_change = tc.type_change
         and ch.id = id_
       order by ch.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_crosscheck_hst_list;
  
  
  procedure ref_insert_crosscheck(
    rec_id_           in  ref_crosscheck.rec_id           % type,                   
    l_src_formula_    in  ref_crosscheck.l_src_formula    % type,
    r_src_formula_    in  ref_crosscheck.r_src_formula    % type,
    l_rel_formula_    in  ref_crosscheck.l_rel_formula    % type,
    r_rel_formula_    in  ref_crosscheck.r_rel_formula    % type,
    l_desc_           in  ref_crosscheck.l_desc           % type,
    r_desc_           in  ref_crosscheck.r_desc           % type,
    l_src_cond_       in  ref_crosscheck.l_src_cond       % type,
    r_src_cond_       in  ref_crosscheck.r_src_cond       % type,
    l_rel_cond_       in  ref_crosscheck.l_rel_cond       % type,
    r_rel_cond_       in  ref_crosscheck.r_rel_cond       % type,
    formula_symbol_   in  ref_crosscheck.formula_symbol   % type,
    cond_symbol_      in  ref_crosscheck.cond_symbol      % type,
    crosscheck_type_  in  ref_crosscheck.crosscheck_type  % type,
    num_              in  ref_crosscheck.num              % type,
    is_available_     in  ref_crosscheck.is_available     % type,
    begin_date_       in  ref_crosscheck.begin_date       % type,
    end_date_         in  ref_crosscheck.end_date         % type,
    id_usr_           in  ref_crosscheck.id_usr           % type,    
    user_location_    in  ref_crosscheck.user_location    % type,
    datlast_          in  ref_crosscheck.datlast          % type,
    form_codes_       in  form_code_array,    
    do_commit_        in  integer default 1,
    id_               out ref_crosscheck.id % type,
    err_code          out number,
    err_msg           out varchar2
  )
  is
    ProcName     constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_CROSSCHECK';
    ref_code     Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_crosscheck';
            
    if begin_date_ is null then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;     
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    id_ := seq_ref_crosscheck_id.nextval;    
                  
    insert into ref_crosscheck(
      id,
      rec_id,      
      l_src_formula,
      r_src_formula,
      l_rel_formula, 
      r_rel_formula, 
      l_desc, 
      r_desc, 
      l_src_cond, 
      r_src_cond, 
      l_rel_cond, 
      r_rel_cond,
      formula_symbol,
      cond_symbol,
      crosscheck_type,
      num,
      is_available,
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),      
      l_src_formula_,
      r_src_formula_,
      l_rel_formula_,
      r_rel_formula_,
      l_desc_,
      r_desc_,
      l_src_cond_,
      r_src_cond_,
      l_rel_cond_, 
      r_rel_cond_,
      formula_symbol_,
      cond_symbol_,
      crosscheck_type_,
      num_,
      is_available_,
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_
    );
    
    pkg_frsi_ref.ref_update_crosscheck_forms(id_ => id_,
                                             form_codes_ => form_codes_,
                                             do_commit_ => do_commit_,
                                             err_code => Err_Code,
                                             err_msg => Err_Msg);

    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';
  end ref_insert_crosscheck;
   

  procedure ref_update_crosscheck(
    id_                  in  ref_crosscheck.id               % type,
    rec_id_              in  ref_crosscheck.rec_id           % type,                    
    l_src_formula_       in  ref_crosscheck.l_src_formula    % type,
    r_src_formula_       in  ref_crosscheck.r_src_formula    % type,
    l_rel_formula_       in  ref_crosscheck.l_rel_formula    % type,
    r_rel_formula_       in  ref_crosscheck.r_rel_formula    % type,
    l_desc_              in  ref_crosscheck.l_desc           % type,
    r_desc_              in  ref_crosscheck.r_desc           % type,
    l_src_cond_          in  ref_crosscheck.l_src_cond       % type,
    r_src_cond_          in  ref_crosscheck.r_src_cond       % type,
    l_rel_cond_          in  ref_crosscheck.l_rel_cond       % type,
    r_rel_cond_          in  ref_crosscheck.r_rel_cond       % type,
    formula_symbol_      in  ref_crosscheck.formula_symbol   % type,
    cond_symbol_         in  ref_crosscheck.cond_symbol      % type,
    crosscheck_type_     in  ref_crosscheck.crosscheck_type  % type,
    num_                 in  ref_crosscheck.num              % type,
    is_available_        in  ref_crosscheck.is_available     % type,
    begin_date_          in  ref_crosscheck.begin_date       % type,
    end_date_            in  ref_crosscheck.end_date         % type,
    id_usr_              in  ref_crosscheck.id_usr           % type,    
    user_location_       in  ref_crosscheck.user_location    % type,
    datlast_             in  ref_crosscheck.datlast          % type,
    form_codes_          in  form_code_array,    
    do_commit_           in  integer default 1,     
    err_code             out number,
    err_msg              out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_CROSSCHECK';
    v_have_chg        boolean;
    v_ref_data        ref_crosscheck %rowtype;
    ref_code          Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_have_chg := false;
    ref_code := 'ref_crosscheck';
    
    if begin_date_ is null then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    /*report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;*/
    
    begin
      select *
        into v_ref_data
        from ref_crosscheck
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;

    if v_ref_data.id <> 0 then
      if nvl(v_ref_data.l_src_formula,' ') <> nvl(l_src_formula_,' ') or 
         nvl(v_ref_data.r_src_formula,' ') <> nvl(r_src_formula_,' ') or
         nvl(v_ref_data.l_rel_formula,' ') <> nvl(l_rel_formula_,' ') or 
         nvl(v_ref_data.r_rel_formula,' ') <> nvl(r_rel_formula_,' ') or
         nvl(v_ref_data.l_desc,' ') <> nvl(l_desc_,' ') or 
         nvl(v_ref_data.r_desc,' ') <> nvl(r_desc_,' ') or
         nvl(v_ref_data.l_src_cond,' ') <> nvl(l_src_cond_,' ') or 
         nvl(v_ref_data.r_src_cond,' ') <> nvl(r_src_cond_,' ') or
         nvl(v_ref_data.l_rel_cond,' ') <> nvl(l_rel_cond_,' ') or 
         nvl(v_ref_data.r_rel_cond,' ') <> nvl(r_rel_cond_,' ') or
         nvl(v_ref_data.formula_symbol,' ') <> nvl(formula_symbol_,' ') or 
         nvl(v_ref_data.cond_symbol,' ') <> nvl(cond_symbol_,' ') or         
         nvl(v_ref_data.crosscheck_type,0) <> nvl(crosscheck_type_,0) or 
         nvl(v_ref_data.num,0) <> nvl(num_,0) or
         nvl(v_ref_data.is_available,0) <> nvl(is_available_,0) or          
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or 
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) then

        v_have_chg := true;

      end if;
    end if;
        
    if v_have_chg = true then
      update ref_crosscheck
         set l_src_formula    = l_src_formula_,
             r_src_formula    = r_src_formula_,
             l_rel_formula    = l_rel_formula_,
             r_rel_formula    = r_rel_formula_,
             l_desc           = l_desc_,
             r_desc           = r_desc_,
             l_src_cond       = l_src_cond_,
             r_src_cond       = r_src_cond_,             
             l_rel_cond       = l_rel_cond_,
             r_rel_cond       = r_rel_cond_,
             formula_symbol   = formula_symbol_,
             cond_symbol      = cond_symbol_,
             crosscheck_type  = crosscheck_type_,             
             num              = num_,
             is_available     = is_available_,             
             begin_date       = begin_date_,
             end_date         = end_date_,
             id_usr           = id_usr_,
             user_location    = user_location_,
             sent_knd         = 0,
             datlast          = datlast_
       where id = id_;
       
       pkg_frsi_ref.ref_update_crosscheck_forms(id_ => id_,
                                             form_codes_ => form_codes_,
                                             do_commit_ => do_commit_,
                                             err_code => Err_Code,
                                             err_msg => Err_Msg);
             
      if do_commit_ = 1 then
        Commit;
      end if;
    end if;
    
  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';
  end ref_update_crosscheck;
  
  
  procedure ref_delete_crosscheck(
    id_             in  ref_crosscheck.id   % type,
    do_commit_      in  integer default 1,     
    err_code        out number,
    err_msg         out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_CROSSCHECK';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_crosscheck',
                          /*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
  
    update ref_crosscheck 
       set delfl = 1
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_crosscheck;
  
  procedure ref_update_crosscheck_forms(
    id_                  in  ref_crosscheck.id % type,
    form_codes_          in  form_code_array,
    do_commit_           in  integer default 1,     
    err_code             out number,
    err_msg              out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_CROSSCHECK_FORMS';
    form_code_ varchar2(250);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';

    delete from ref_crosscheck_forms f where f.ref_crosscheck_id=id_;

    for i in form_codes_.FIRST .. form_codes_.LAST
    loop
        form_code_ := form_codes_(i);                
        insert into ref_crosscheck_forms (id, ref_crosscheck_id, form_code) values (SEQ_REF_CROSSCHECK_FORMS_ID.NEXTVAL, id_, form_code_);
    end loop;

    if do_commit_ = 1 then
      Commit;
    end if;           
    
  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка добавления записи !';  
  end ref_update_crosscheck_forms;
  
  
  /* Правила выходных форм */  
  procedure ref_read_reports_rules_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    err_code  out number,
    err_msg   out varchar2     
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_REPORTS_RULES_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select rr.id,
             rr.rec_id,
             rr.code,
             rr.name_kz,
             rr.name_ru,
             rr.name_en,    
             rr.formname,
             rr.fieldname,
             rr.formula,
             rr.coeff,
             rr.condition,
             rr.priority,
             rr.begin_date,
             rr.end_date,
             rr.datlast,
             rr.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             rr.user_location,
             sk.name as sent_knd,
             rr.report_type,
             rt.name as report_type_name,
             rr.report_kind,
             rk.name as report_kind_name,
             rr.rep_per_dur_months,
             dur.name as dur_name,
             dur.code as dur_code,
             rr.keyvalue,
             rr.table_name
        from v_ref_reports_rules rr,
             report_type rt,
             report_kind rk,
             rep_per_dur_months dur,
             f_users u,
             sent_knd sk
       where rr.id_usr = u.user_id         
         and rr.sent_knd = sk.sent_knd
         and rr.report_type = rt.id
         and rr.report_kind = rk.id
         and rr.rep_per_dur_months = dur.id(+)
         and (date_ is null or rr.begin_date = (select max(t.begin_date)
                                                 from v_ref_reports_rules t
                                                where t.rec_id = rr.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (rr.end_date is null or rr.end_date > date_))
       order by rr.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_reports_rules_list;  
  
  procedure ref_read_rep_rules_l_by_params(    
    id_             in  ref_reports_rules.id % type,
    date_           in  Date,
    form_name_      in  ref_reports_rules.formname % type,
    formula_        in  ref_reports_rules.formula % type,
    rec_id_         in  ref_reports_rules.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2     
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_REP_RULES_L_BY_PARAMS';
    v_form_name varchar2(524);
    v_formula varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_form_name := '%' || form_name_ || '%';
    v_formula := '%' || formula_ || '%';
    
    Open Cur for
      select rr.id,
             rr.rec_id,
             rr.code,
             rr.name_kz,
             rr.name_ru,
             rr.name_en,    
             rr.formname,
             rr.fieldname,
             rr.formula,
             rr.coeff,
             rr.condition,
             rr.priority,             
             rr.begin_date,
             rr.end_date,
             rr.datlast,
             rr.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             rr.user_location,
             sk.name as sent_knd,
             rr.report_type,
             rt.name as report_type_name,
             rr.report_kind,
             rk.name as report_kind_name,
             rr.rep_per_dur_months,
             dur.name as dur_name,
             dur.code as dur_code,
             rr.keyvalue,
             rr.table_name
        from v_ref_reports_rules rr,
             report_type rt,
             report_kind rk,
             rep_per_dur_months dur,
             f_users u,
             sent_knd sk
       where rr.id_usr = u.user_id         
         and rr.sent_knd = sk.sent_knd
         and rr.report_type = rt.id
         and rr.report_kind = rk.id
         and rr.rec_id = (select max(t1.rec_id)
                            from v_ref_reports_rules t1
                           where ((
                                    (search_all_ver_ = 1) and 
                                    (trim(form_name_) is null or upper(t1.formname) like upper(trim(v_form_name))) and
                                    (trim(formula_) is null or upper(t1.formula) like upper(trim(v_formula)))
                                   ) or 
                                   (
                                    (search_all_ver_ is null or search_all_ver_ = 0) and 
                                    (trim(form_name_) is null or upper(rr.formname) like upper(trim(v_form_name))) and
                                    (trim(formula_) is null or upper(rr.formula) like upper(trim(v_formula)))
                                   ) 
                                 )
                             and t1.rec_id = rr.rec_id
                           )
         and rr.rep_per_dur_months = dur.id(+)
         and (id_ is null or rr.id = id_)
         /*and (trim(form_name_) is null or upper(rr.formname) like upper(trim(v_form_name)))
         and (trim(formula_) is null or upper(rr.formula) like upper(trim(v_formula)))*/
         and (rec_id_ is null or rr.rec_id = rec_id_)
         and (date_ is null or rr.begin_date = (select max(t.begin_date)
                                                 from v_ref_reports_rules t
                                                where t.rec_id = rr.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (rr.end_date is null or rr.end_date > date_))
       order by rr.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_rep_rules_l_by_params;  
  
  
  procedure ref_read_reps_rul_hst_list(
    id_      in  ref_reports_rules.id % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_REPS_RUL_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select rrh.id_hst,
             rrh.id,
             rrh.rec_id,
             rrh.code,
             rrh.name_kz,
             rrh.name_ru,
             rrh.name_en,   
             rrh.formname,
             rrh.fieldname,
             rrh.formula,
             rrh.coeff,
             rrh.condition,  
             rrh.priority,                        
             rrh.begin_date,
             rrh.end_date,
             rrh.datlast,
             rrh.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             rrh.type_change,
             tc.name as type_change_name,
             rrh.user_location,
             sk.name as sent_knd,
             rrh.report_type,
             rt.name as report_type_name,
             rrh.report_kind,
             rk.name as report_kind_name,
             rrh.rep_per_dur_months,
             dur.name as dur_name,
             dur.code as dur_code,
             rrh.keyvalue,
             rrh.table_name
        from ref_reports_rules_hst rrh,
             report_type rt,
             report_kind rk,
             rep_per_dur_months dur,
             f_users u,
             type_change tc,
             sent_knd sk
       where rrh.id_usr = u.user_id
         and rrh.sent_knd = sk.sent_knd
         and rrh.report_type = rt.id
         and rrh.report_kind = rk.id
         and rrh.rep_per_dur_months = dur.id(+)
         and rrh.type_change = tc.type_change
         and rrh.id = id_
       order by rrh.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;     
  end ref_read_reps_rul_hst_list;
  
  
  procedure ref_insert_reports_rules(
    rec_id_              in  ref_reports_rules.rec_id              % type,
    code_                in  ref_reports_rules.code                % type,    
    name_kz_             in  ref_reports_rules.name_kz             % type,
    name_ru_             in  ref_reports_rules.name_ru             % type,
    name_en_             in  ref_reports_rules.name_en             % type,                    
    formname_            in  ref_reports_rules.formname            % type,
    fieldname_           in  ref_reports_rules.fieldname           % type,
    formula_             in  ref_reports_rules.formula             % type,
    coeff_               in  ref_reports_rules.coeff               % type,
    condition_           in  ref_reports_rules.condition           % type,
    priority_            in  ref_reports_rules.priority            % type,
    report_type_         in  ref_reports_rules.report_type         % type,
    keyvalue_            in  ref_reports_rules.keyvalue            % type,
    report_kind_         in  ref_reports_rules.report_kind         % type,
    rep_per_dur_months_  in  ref_reports_rules.rep_per_dur_months  % type,
    table_name_          in  ref_reports_rules.table_name          % type,
    begin_date_          in  ref_reports_rules.begin_date          % type,
    end_date_            in  ref_reports_rules.end_date            % type,
    id_usr_              in  ref_reports_rules.id_usr              % type,    
    user_location_       in  ref_reports_rules.user_location       % type,
    datlast_             in  ref_reports_rules.datlast             % type,
    do_commit_           in  integer default 1,
    id_                  out ref_reports_rules.id % type,
    err_code             out number,
    err_msg              out varchar2
  )
  is
    ProcName     constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_REPORTS_RULES';    
    ref_code     Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_reports_rules';
    
    if /*(trim(code_) is null) or*/ 
       (trim(name_ru_) is null) or (begin_date_ is null) or
       (trim(formname_) is null) or (trim(fieldname_) is null) or
       (trim(formula_) is null) or (trim(priority_) is null) or
       (coeff_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if; 
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if; */
    
    if ref_check_rr_param(rec_id_,formname_,fieldname_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    id_ := seq_ref_reports_rules_id.nextval;
                  
    insert into ref_reports_rules(
      id,
      rec_id,
      code,
      name_kz,
      name_ru,
      name_en,
      formname,
      fieldname,
      formula,
      coeff,
      priority,
      condition,
      report_type,
      keyvalue,
      report_kind,
      rep_per_dur_months,
      table_name,
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),
      nvl(rec_id_,id_),--code_,
      name_kz_,                 
      name_ru_, 
      name_en_,      
      formname_,
      fieldname_,
      formula_,
      coeff_,
      priority_,
      condition_,
      report_type_,
      keyvalue_,
      report_kind_,
      rep_per_dur_months_,
      table_name_,
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_
    );
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';      
  end ref_insert_reports_rules;
   

  procedure ref_update_reports_rules(
    id_                  in  ref_reports_rules.id                  % type,
    rec_id_              in  ref_reports_rules.rec_id              % type,
    code_                in  ref_reports_rules.code                % type,    
    name_kz_             in  ref_reports_rules.name_kz             % type,
    name_ru_             in  ref_reports_rules.name_ru             % type,
    name_en_             in  ref_reports_rules.name_en             % type,                
    formname_            in  ref_reports_rules.formname            % type,
    fieldname_           in  ref_reports_rules.fieldname           % type,
    formula_             in  ref_reports_rules.formula             % type,
    coeff_               in  ref_reports_rules.coeff               % type,
    condition_           in  ref_reports_rules.condition           % type,
    priority_            in  ref_reports_rules.priority            % type,
    report_type_         in  ref_reports_rules.report_type         % type,
    keyvalue_            in  ref_reports_rules.keyvalue            % type,
    report_kind_         in  ref_reports_rules.report_kind         % type,
    rep_per_dur_months_  in  ref_reports_rules.rep_per_dur_months  % type,
    table_name_          in  ref_reports_rules.table_name          % type,
    begin_date_          in  ref_reports_rules.begin_date          % type,
    end_date_            in  ref_reports_rules.end_date            % type,
    id_usr_              in  ref_reports_rules.id_usr              % type,    
    user_location_       in  ref_reports_rules.user_location       % type,
    datlast_             in  ref_reports_rules.datlast             % type,
    do_commit_           in  integer default 1,     
    err_code             out number,
    err_msg              out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_REPORTS_RULES';
    v_have_chg        boolean;
    v_ref_data        ref_reports_rules %rowtype;
    ref_code          Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_have_chg := false;
    ref_code := 'ref_reports_rules';
    
    if (trim(code_) is null) or 
       (trim(name_ru_) is null) or
       (begin_date_ is null) or 
       (trim(formname_) is null) or
       (trim(fieldname_) is null) or 
       (trim(formula_) is null) or
       (trim(priority_) is null) or 
       (coeff_ is null)  then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
   /* if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;*/
    
    /*if ref_check_rr_param(rec_id_,formname_,fieldname_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;*/
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    /*report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;*/
    
    begin
      select *
        into v_ref_data
        from ref_reports_rules
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;


    if v_ref_data.id <> 0 then
      if nvl(v_ref_data.name_kz,' ') <> nvl(name_kz_,' ') or 
         nvl(v_ref_data.name_ru,' ') <> nvl(name_ru_,' ') or
         nvl(v_ref_data.name_en,' ') <> nvl(name_en_,' ') or          
         nvl(v_ref_data.formname,' ') <> nvl(formname_,' ') or
         nvl(v_ref_data.fieldname,' ') <> nvl(fieldname_,' ') or 
         nvl(v_ref_data.formula,' ') <> nvl(formula_,' ') or
         nvl(v_ref_data.coeff,0) <> nvl(coeff_,0) or
         nvl(v_ref_data.condition,' ') <> nvl(condition_,' ') or
         nvl(v_ref_data.priority,0) <> nvl(priority_,0) or         
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or 
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) or
         nvl(v_ref_data.report_type,0) <> nvl(report_type_,0) or 
         nvl(v_ref_data.report_kind,0) <> nvl(report_kind_,0) or
         nvl(v_ref_data.rep_per_dur_months, 0) <> nvl(rep_per_dur_months_,0) or
         nvl(v_ref_data.keyvalue, ' ') <> nvl(keyvalue_, ' ') or
         nvl(v_ref_data.table_name, ' ') <> nvl(table_name_, ' ') then
        v_have_chg := true;

      end if;
    end if;
    
    if v_have_chg = true then
      update ref_reports_rules
         set --code                = code_,
             name_kz             = name_kz_,
             name_ru             = name_ru_,
             name_en             = name_en_,
             formname            = formname_,
             fieldname           = fieldname_,
             formula             = formula_,
             coeff               = coeff_,
             condition           = condition_,
             priority            = priority_,
             report_type         = report_type_,
             keyvalue            = keyvalue_,
             report_kind         = report_kind_,
             rep_per_dur_months  = rep_per_dur_months_,
             table_name          = table_name_,
             begin_date          = begin_date_,
             end_date            = end_date_,
             id_usr              = id_usr_,
             user_location       = user_location_,
             sent_knd            = 0,
             datlast             = datlast_
       where id = id_;
             
      if do_commit_ = 1 then
        Commit;
      end if;
    end if;

  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';
  end ref_update_reports_rules;
  
  
  procedure ref_delete_reports_rules(
    id_        in  ref_reports_rules.id % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_REPORTS_RULES';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_reports_rules',
                          /*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
  
    update ref_reports_rules 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_reports_rules;    
  
  /* Листинговые оценки */
  procedure ref_read_listing_est_list(    
    date_    in ref_listing_estimation.begin_date % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2  
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_LISTING_EST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select le.id,
             le.rec_id,
             le.name_kz,
             le.name_ru,
             le.name_en,    
             le.priority,             
             le.begin_date,
             le.end_date,
             le.datlast,
             le.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             le.user_location,
             sk.name as sent_knd
        from v_ref_listing_estimation le,             
             f_users u,
             sent_knd sk
       where le.id_usr = u.user_id         
         and le.sent_knd = sk.sent_knd
         and (date_ is null or le.begin_date = (select max(t.begin_date)
                                                 from v_ref_listing_estimation t
                                                where t.rec_id = le.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (le.end_date is null or le.end_date > date_))
       order by le.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_listing_est_list;  
  
  procedure ref_read_lis_est_l_by_params(    
    id_             in ref_listing_estimation.id % type,
    date_           in ref_listing_estimation.begin_date % type,
    name_ru_        in ref_listing_estimation.name_ru % type,
    rec_id_         in ref_listing_estimation.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2     
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_LIS_EST_L_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    Open Cur for
      select le.id,
             le.rec_id,
             le.name_kz,
             le.name_ru,
             le.name_en,    
             le.priority,             
             le.begin_date,
             le.end_date,
             le.datlast,
             le.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             le.user_location,
             sk.name as sent_knd
        from v_ref_listing_estimation le,
             f_users u,
             sent_knd sk
       where le.id_usr = u.user_id
         and le.sent_knd = sk.sent_knd
         and le.rec_id = (select max(t1.rec_id)
                            from v_ref_listing_estimation t1
                           where ((
                                    (search_all_ver_ = 1) and 
                                    (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name)))
                                   ) or 
                                   (
                                    (search_all_ver_ is null or search_all_ver_ = 0) and 
                                    (trim(name_ru_) is null or upper(le.name_ru) like upper(trim(v_name)))
                                   ) 
                                 )
                             and t1.rec_id = le.rec_id
                           )
         and (id_ is null or le.id = id_)
--         and (trim(name_ru_) is null or upper(le.name_ru) like upper(trim(v_name)))
         and (rec_id_ is null or le.rec_id = rec_id_)
         and (date_ is null or le.begin_date = (select max(t.begin_date)
                                                 from v_ref_listing_estimation t
                                                where t.rec_id = le.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (le.end_date is null or le.end_date > date_))
       order by le.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_lis_est_l_by_params; 
  
  /* Рейтинговые оценки */
  procedure ref_read_rating_est_list(    
    date_    in ref_rating_estimation.begin_date % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2    
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_RATING_EST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select re.id,
             re.rec_id,
             re.name_kz,
             re.name_ru,
             re.name_en,    
             re.priority,
             re.ref_rating_category,
             rc.name_ru as RATING_CATEGORY_NAME,
             rc.rec_id as REF_RATING_CATEGORY_REC_ID,
             re.begin_date,
             re.end_date,
             re.datlast,
             re.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             re.user_location,
             sk.name as sent_knd
        from v_ref_rating_estimation re,
             v_ref_rating_category rc,            
             f_users u,
             sent_knd sk
       where re.id_usr = u.user_id         
         and re.ref_rating_category = rc.rec_id
         and re.sent_knd = sk.sent_knd
         and (date_ is null or re.begin_date = (select max(t.begin_date)
                                                 from v_ref_rating_estimation t
                                                where t.rec_id = re.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (re.end_date is null or re.end_date > date_))
         and rc.begin_date = (select max(rc1.begin_date)
                                from v_ref_rating_category rc1
                               where rc1.rec_id = rc.rec_id
                                 and rc1.begin_date <= nvl(date_,date_))
       order by re.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_rating_est_list;  
  
  procedure ref_read_rat_est_l_by_params(    
    id_             in  ref_rating_estimation.id % type,
    date_           in  ref_rating_estimation.begin_date % type,
    name_ru_        in  ref_rating_estimation.name_ru % type,
    rat_cat_name_   in  ref_rating_category.name_ru % type,
    rec_id_         in  ref_rating_category.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2     
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_RAT_EST_L_BY_PARAMS';
    v_name varchar2(524);
    v_rat_cat_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    v_rat_cat_name := '%' || rat_cat_name_ || '%';
    
    Open Cur for
      select re.id,
             re.rec_id,
             re.name_kz,
             re.name_ru,
             re.name_en,    
             re.priority,
             re.ref_rating_category,
             rc.name_ru as RATING_CATEGORY_NAME,
             re.begin_date,
             re.end_date,
             re.datlast,
             re.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             re.user_location,
             sk.name as sent_knd
        from v_ref_rating_estimation re,
             v_ref_rating_category rc,            
             f_users u,
             sent_knd sk
       where re.id_usr = u.user_id         
         and re.ref_rating_category = rc.rec_id
         and re.sent_knd = sk.sent_knd
         and re.rec_id = (select max(t1.rec_id)
                            from v_ref_rating_estimation t1
                           where ((
                                    (search_all_ver_ = 1) and 
                                    (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name)))
                                   ) or 
                                   (
                                    (search_all_ver_ is null or search_all_ver_ = 0) and 
                                    (trim(name_ru_) is null or upper(re.name_ru) like upper(trim(v_name)))
                                   ) 
                                 )
                             and t1.rec_id = re.rec_id
                           )
         and rc.rec_id = (select max(t2.rec_id)
                            from v_ref_rating_category t2
                           where ((
                                    (search_all_ver_ = 1) and 
                                    (trim(rat_cat_name_) is null or upper(t2.name_ru) like upper(trim(v_rat_cat_name)))
                                   ) or 
                                   (
                                    (search_all_ver_ is null or search_all_ver_ = 0) and 
                                    (trim(rat_cat_name_) is null or upper(rc.name_ru) like upper(trim(v_rat_cat_name)))
                                   ) 
                                 )
                             and t2.rec_id = rc.rec_id
                           )                  
         and (id_ is null or re.id = id_)
--         and (trim(name_ru_) is null or upper(re.name_ru) like upper(trim(v_name)))
--         and (trim(rat_cat_name_) is null or upper(rc.name_ru) like upper(trim(v_rat_cat_name)))
         and (rec_id_ is null or re.rec_id = rec_id_)
         and (date_ is null or re.begin_date = (select max(t.begin_date)
                                                 from v_ref_rating_estimation t
                                                where t.rec_id = re.rec_id
                                                  and t.begin_date <= date_))
         and rc.begin_date = (select max(rc1.begin_date)
                                from v_ref_rating_category rc1
                               where rc1.rec_id = rc.rec_id
                                 and rc1.begin_date <= nvl(date_,sysdate))
         --and (date_ is null or (re.end_date is null or re.end_date > date_))         
       order by re.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_rat_est_l_by_params;  
  
  /* Категории рейтинговых оценок */
  procedure ref_read_rating_category_list(    
    date_    in ref_rating_category.begin_date % type,
    Cur      out sys_refcursor,
    err_code out number,
    err_msg  out varchar2      
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_RATING_CATEGORY_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select rc.id,
             rc.rec_id,
             rc.code,
             rc.name_kz,
             rc.name_ru,
             rc.name_en,                              
             rc.begin_date,
             rc.end_date,
             rc.datlast,
             rc.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             rc.user_location,
             sk.name as sent_knd
        from v_ref_rating_category rc,       
             f_users u,
             sent_knd sk
       where rc.id_usr = u.user_id         
         and rc.sent_knd = sk.sent_knd
         and (date_ is null or rc.begin_date = (select max(t.begin_date)
                                                 from v_ref_rating_category t
                                                where t.rec_id = rc.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (rc.end_date is null or rc.end_date > date_))
       order by rc.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_rating_category_list;  
  
  procedure ref_read_rat_cat_l_by_params(
    id_             in  ref_rating_category.id % type,
    date_           in  ref_rating_category.begin_date % type,
    name_ru_        in  ref_rating_category.name_ru % type,
    code_           in  ref_rating_category.code % type,
    rec_id_         in  ref_rating_category.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    err_code        out number,
    err_msg         out varchar2       
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_RAT_CAT_L_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    Open Cur for
      select rc.id,
             rc.rec_id,
             rc.code,
             rc.name_kz,
             rc.name_ru,
             rc.name_en,                              
             rc.begin_date,
             rc.end_date,
             rc.datlast,
             rc.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             rc.user_location,
             sk.name as sent_knd
        from v_ref_rating_category rc,       
             f_users u,
             sent_knd sk
       where rc.id_usr = u.user_id         
         and rc.sent_knd = sk.sent_knd
         and rc.rec_id = (select max(t1.rec_id)
                            from v_ref_rating_category t1
                           where ((
                                    (search_all_ver_ = 1) and 
                                    (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name))) and
                                    (trim(code_) is null or upper(t1.code) like upper(trim(code_)) || '%')
                                   ) or 
                                   (
                                    (search_all_ver_ is null or search_all_ver_ = 0) and 
                                    (trim(name_ru_) is null or upper(rc.name_ru) like upper(trim(v_name))) and
                                    (trim(code_) is null or upper(rc.code) like upper(trim(code_)) || '%')
                                   ) 
                                 )
                             and t1.rec_id = rc.rec_id
                           )
         and (id_ is null or rc.id = id_)
         /*and (trim(name_ru_) is null or upper(rc.name_ru) like upper(trim(v_name)))
         and (trim(code_) is null or upper(rc.code) like upper(trim(code_)) || '%')*/
         and (rec_id_ is null or rc.rec_id = rec_id_)
         and (date_ is null or rc.begin_date = (select max(t.begin_date)
                                                  from v_ref_rating_category t
                                                 where t.rec_id = rc.rec_id
                                                   and t.begin_date <= date_))
         --and (date_ is null or (rc.end_date is null or rc.end_date > date_))
       order by rc.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_rat_cat_l_by_params;  
  
  /* Справочник МРП */
  procedure ref_read_mrp_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    Err_Code  out number,
    Err_Msg   out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_MRP_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select m.id,
             m.rec_id,
             m.code,
             m.name_kz,
             m.name_ru,
             m.name_en,
             m.value,
             m.begin_date,
             m.end_date,
             m.datlast,
             m.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             m.user_location,
             sk.name as sent_knd
        from v_ref_mrp m,
             f_users u,
             sent_knd sk
       where m.id_usr = u.user_id
         and m.sent_knd = sk.sent_knd         
         and (date_ is null or m.begin_date = (select max(t.begin_date)
                                                 from v_ref_mrp t
                                                where t.rec_id = m.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (m.end_date is null or m.end_date > date_))
       order by m.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_mrp_list;
  
  procedure ref_read_mrp_list_by_params(
    id_             in  ref_mrp.id % type,
    date_           in  Date,
    name_ru_        in  ref_mrp.name_ru % type,
    rec_id_         in  ref_mrp.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    Err_Code        out number,
    Err_Msg         out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_MRP_LIST_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    Open Cur for
      select m.id,
             m.rec_id,
             m.code,
             m.name_kz,
             m.name_ru,
             m.name_en,
             m.value,
             m.begin_date,
             m.end_date,
             m.datlast,
             m.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             m.user_location,
             sk.name as sent_knd
        from v_ref_mrp m,
             f_users u,
             sent_knd sk
       where m.id_usr = u.user_id
         and m.sent_knd = sk.sent_knd
         and m.rec_id = (select max(t1.rec_id)
                          from v_ref_mrp t1
                         where ((
                                  (search_all_ver_ = 1) and 
                                  (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name)))
                                 ) or 
                                 (
                                  (search_all_ver_ is null or search_all_ver_ = 0) and 
                                  (trim(name_ru_) is null or upper(m.name_ru) like upper(trim(v_name)))
                                 ) 
                               )
                           and t1.rec_id = m.rec_id
                         )                  
         and (id_ is null or m.id = id_)
--         and (trim(name_ru_) is null or upper(m.name_ru) like upper(trim(v_name)))
         and (rec_id_ is null or m.rec_id = rec_id_)          
         and (date_ is null or m.begin_date = (select max(t.begin_date)
                                                 from v_ref_mrp t
                                                where t.rec_id = m.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (p.end_date is null or p.end_date > date_))
       order by m.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_mrp_list_by_params;
  
  
  procedure ref_read_mrp_hst_list(
    id_       in  ref_mrp.id % type,
    Cur       out sys_refcursor,
    Err_Code  out number,
    Err_Msg   out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_MRP_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select mh.id_hst,
             mh.id,
             mh.rec_id,
             mh.code,
             mh.name_kz,
             mh.name_ru,
             mh.name_en,
             mh.value,
             mh.begin_date,
             mh.end_date,
             mh.datlast,
             mh.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             mh.type_change,
             tc.name as type_change_name,
             mh.user_location,
             sk.name as sent_knd
        from ref_mrp_hst mh,
             f_users u,
             type_change tc,
             sent_knd sk
       where mh.id_usr = u.user_id         
         and mh.sent_knd = sk.sent_knd
         and mh.type_change = tc.type_change
         and mh.id = id_
       order by mh.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_mrp_hst_list;
  
  
  procedure ref_insert_mrp(
    rec_id_        in  ref_mrp.rec_id        % type,
    code_          in  ref_mrp.code          % type,
    name_kz_       in  ref_mrp.name_kz       % type,
    name_ru_       in  ref_mrp.name_ru       % type,
    name_en_       in  ref_mrp.name_en       % type,
    value_         in  ref_mrp.value         % type,        
    begin_date_    in  ref_mrp.begin_date    % type,
    end_date_      in  ref_mrp.end_date      % type,
    id_usr_        in  ref_mrp.id_usr        % type,
    user_location_ in  ref_mrp.user_location % type,
    datlast_       in  ref_mrp.datlast       % type,
    do_commit_     in  Integer default 1,
    id_            out ref_mrp.id            % type,
    Err_Code       out number,
    Err_Msg        out varchar2
  )
  is
    ProcName     constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_MRP';
    ref_code     Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_mrp';
    
    if /*(trim(code_) is null) or*/ (trim(name_ru_) is null) or (begin_date_ is null)
      or (value_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
                
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if; */
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;    
    
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,null,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_year(ref_code, begin_date_, rec_id_) > 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 07 ' || 'Внимание! Имеется запись за этот год!';
      raise E_Force_Exit;
    end if;
       
    id_ := seq_ref_mrp_id.nextval;
    
    insert into ref_mrp(
      id,
      rec_id,
      code,
      name_kz,
      name_ru,
      name_en,
      value,      
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),
      nvl(rec_id_,id_),--code_,
      name_kz_,
      name_ru_,
      name_en_,
      value_,
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_
    );
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';   
  end ref_insert_mrp;
  
  
  procedure ref_update_mrp(
    id_            in  ref_mrp.id            % type,
    rec_id_        in  ref_mrp.rec_id        % type,
    code_          in  ref_mrp.code          % type,
    name_kz_       in  ref_mrp.name_kz       % type,
    name_ru_       in  ref_mrp.name_ru       % type,
    name_en_       in  ref_mrp.name_en       % type,  
    value_         in  ref_mrp.value         % type,        
    begin_date_    in  ref_mrp.begin_date    % type,
    end_date_      in  ref_mrp.end_date      % type,
    id_usr_        in  ref_mrp.id_usr        % type,
    user_location_ in  ref_mrp.user_location % type,
    datlast_       in  ref_mrp.datlast       % type,
    do_commit_     in  integer default 1,     
    err_code       out number,
    err_msg        out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_MRP';
    v_have_chg           boolean;
    v_ref_data           ref_mrp %rowtype;
    ref_code             Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_mrp';
    v_have_chg := false;
    
    
    if (trim(name_ru_) is null) or (begin_date_ is null) or 
       (trim(code_) is null) or (value_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;*/
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_, end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_year(ref_code, begin_date_, rec_id_) > 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 07 ' || 'Внимание! Имеется запись за этот год!';
      raise E_Force_Exit;
    end if;
    
    /*report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;*/
    
    begin
      select *
        into v_ref_data
        from ref_mrp
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;
    
    if v_ref_data.id <> 0 then
      if nvl(v_ref_data.name_kz,' ') <> nvl(name_kz_,' ') or 
         nvl(v_ref_data.name_ru,' ') <> nvl(name_ru_,' ') or
         nvl(v_ref_data.name_en,' ') <> nvl(name_en_,' ') or
         nvl(v_ref_data.value,0) <> nvl(value_,0) or
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) then
         
        v_have_chg := true;
      end if;
    end if;
    
    if v_have_chg = true then
      update ref_mrp 
         set --code          = code_,
             name_kz       = name_kz_,
             name_ru       = name_ru_,
             name_en       = name_en_,
             value         = value_,           
             user_location = user_location_,
             begin_date    = begin_date_,
             end_date      = end_date_,
             id_usr        = id_usr_,
             sent_knd      = 0,
             datlast       = datlast_
       where id = id_;
             
      if do_commit_ = 1 then
        Commit;
      end if;
    end if;
    
  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';  
  end ref_update_mrp;
  
  
  procedure ref_delete_mrp(
    id_        in  ref_mrp.id % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_MRP';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_mrp',
                          /*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
  
    update ref_mrp 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_mrp;
  
  /* Справочник Реестр МФО */
  procedure ref_read_mfo_reg_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    Err_Code  out number,
    Err_Msg   out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_MFO_REG_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select m.id,
             m.rec_id,
             m.code,
             m.name_kz,
             m.name_ru,
             m.name_en,
             m.ref_department,
             d.NAME_RU as dep_name_ru,             
             m.ref_legal_person,
             lp.name_ru as lp_name_ru,             
             m.base,
             m.num_reg,
             m.fio_manager,
             m.address,
             m.contact_details,
             m.begin_date,
             m.end_date,
             m.datlast,
             m.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             m.user_location,
             sk.name as sent_knd
        from v_ref_mfo_reg m,
             v_ref_department d,
             v_ref_legal_person lp,
             f_users u,
             sent_knd sk
       where m.id_usr = u.user_id
         and m.sent_knd = sk.sent_knd
         and m.ref_department = d.ID
         and m.ref_legal_person = lp.id
         and (date_ is null or m.begin_date = (select max(t.begin_date)
                                                 from v_ref_mfo_reg t
                                                where t.rec_id = m.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (m.end_date is null or m.end_date > date_))
       order by m.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_mfo_reg_list;
  
  procedure ref_read_mfo_reg_list_by_p(
    id_             in  ref_mfo_reg.id % type,
    date_           in  Date,
    name_ru_        in  ref_mfo_reg.name_ru % type,
    rec_id_         in  ref_mfo_reg.rec_id % type,
    ref_department_ in  ref_mfo_reg.ref_department % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    Err_Code        out number,
    Err_Msg         out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_MFO_REG_LIST_BY_P';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    Open Cur for
      select m.id,
             m.rec_id,
             m.code,
             m.name_kz,
             m.name_ru,
             m.name_en,
             m.ref_department,
             d.NAME_RU as dep_name_ru,             
             m.ref_legal_person,
             lp.name_ru as lp_name_ru,             
             m.base,
             m.num_reg,
             m.fio_manager,
             m.address,
             m.contact_details,
             m.begin_date,
             m.end_date,
             m.datlast,
             m.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             m.user_location,
             sk.name as sent_knd
        from v_ref_mfo_reg m,
             v_ref_department d,
             v_ref_legal_person lp,
             f_users u,
             sent_knd sk
       where m.id_usr = u.user_id
         and m.sent_knd = sk.sent_knd
         and m.ref_department = d.ID
         and m.ref_legal_person = lp.id
         and m.rec_id = (select max(t1.rec_id)
                          from v_ref_mfo_reg t1
                         where ((
                                  (search_all_ver_ = 1) and 
                                  (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name))) and
                                  (ref_department_ is null or t1.ref_department = ref_department_)
                                 ) or 
                                 (
                                  (search_all_ver_ is null or search_all_ver_ = 0) and 
                                  (trim(name_ru_) is null or upper(m.name_ru) like upper(trim(v_name))) and
                                  (ref_department_ is null or m.ref_department = ref_department_)
                                 )
                               )
                           and t1.rec_id = m.rec_id
                         )                  
         and (id_ is null or m.id = id_)
--         and (trim(name_ru_) is null or upper(m.name_ru) like upper(trim(v_name)))
         and (rec_id_ is null or m.rec_id = rec_id_)          
         and (date_ is null or m.begin_date = (select max(t.begin_date)
                                                 from v_ref_mfo_reg t
                                                where t.rec_id = m.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (p.end_date is null or p.end_date > date_))
       order by m.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_mfo_reg_list_by_p;
  
  
  procedure ref_read_mfo_reg_hst_list(
    id_       in  ref_mfo_reg.id % type,
    Cur       out sys_refcursor,
    Err_Code  out number,
    Err_Msg   out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_MFO_REG_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select mh.id_hst,
             mh.id,
             mh.rec_id,
             mh.code,
             mh.name_kz,
             mh.name_ru,
             mh.name_en,
             mh.ref_department,
             d.NAME_RU as dep_name_ru,             
             mh.ref_legal_person,
             lp.name_ru as lp_name_ru,
             mh.base,
             mh.num_reg,
             mh.fio_manager,
             mh.address,
             mh.contact_details,
             mh.begin_date,
             mh.end_date,
             mh.datlast,
             mh.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             mh.type_change,
             tc.name as type_change_name,
             mh.user_location,
             sk.name as sent_knd
        from ref_mfo_reg_hst mh,
             v_ref_department d,
             v_ref_legal_person lp,
             f_users u,
             type_change tc,
             sent_knd sk
       where mh.id_usr = u.user_id
         and mh.ref_department = d.ID
         and mh.ref_legal_person = lp.id
         and mh.sent_knd = sk.sent_knd
         and mh.type_change = tc.type_change
         and mh.id = id_
       order by mh.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_mfo_reg_hst_list;
  
  
  procedure ref_insert_mfo_reg(
    rec_id_           in  ref_mfo_reg.rec_id           % type,
    code_             in  ref_mfo_reg.code             % type,
    name_kz_          in  ref_mfo_reg.name_kz          % type,
    name_ru_          in  ref_mfo_reg.name_ru          % type,
    name_en_          in  ref_mfo_reg.name_en          % type,
    ref_department_   in  ref_mfo_reg.ref_department   % type,
    ref_legal_person_ in  ref_mfo_reg.ref_legal_person % type,
    base_             in  ref_mfo_reg.base             % type,
    num_reg_          in  ref_mfo_reg.num_reg          % type,
    fio_manager_      in  ref_mfo_reg.fio_manager      % type,
    address_          in  ref_mfo_reg.address          % type,
    contact_details_  in  ref_mfo_reg.contact_details  % type,    
    begin_date_       in  ref_mfo_reg.begin_date       % type,
    end_date_         in  ref_mfo_reg.end_date         % type,
    id_usr_           in  ref_mfo_reg.id_usr           % type,
    user_location_    in  ref_mfo_reg.user_location    % type,
    datlast_          in  ref_mfo_reg.datlast          % type,
    do_commit_        in  Integer default 1,
    id_               out ref_mfo_reg.id               % type,
    Err_Code          out number,
    Err_Msg           out varchar2
  )
  is
    ProcName     constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_MFO_REG';
    ref_code     Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_mfo_reg';
    
    if /*(trim(code_) is null) or*/ (trim(name_ru_) is null) or (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
                
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if; */
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;    
    
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,null,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
       
    id_ := seq_ref_mfo_reg_id.nextval;
    
    insert into ref_mfo_reg(
      id,
      rec_id,
      code,
      name_kz,
      name_ru,
      name_en,
      ref_department,
      ref_legal_person,
      base,
      num_reg,
      fio_manager,
      address,
      contact_details,      
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),
      nvl(rec_id_,id_),--code_,
      name_kz_,
      name_ru_,
      name_en_,
      ref_department_,
      ref_legal_person_,
      base_,
      num_reg_,
      fio_manager_,
      address_,
      contact_details_,
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_
    );
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';   
  end ref_insert_mfo_reg;
  
  
  procedure ref_update_mfo_reg(
    id_               in  ref_mfo_reg.id               % type,
    rec_id_           in  ref_mfo_reg.rec_id           % type,
    code_             in  ref_mfo_reg.code             % type,
    name_kz_          in  ref_mfo_reg.name_kz          % type,
    name_ru_          in  ref_mfo_reg.name_ru          % type,
    name_en_          in  ref_mfo_reg.name_en          % type,
    ref_department_   in  ref_mfo_reg.ref_department   % type,
    ref_legal_person_ in  ref_mfo_reg.ref_legal_person % type,
    base_             in  ref_mfo_reg.base             % type,
    num_reg_          in  ref_mfo_reg.num_reg          % type,
    fio_manager_      in  ref_mfo_reg.fio_manager      % type,
    address_          in  ref_mfo_reg.address          % type,
    contact_details_  in  ref_mfo_reg.contact_details  % type,    
    begin_date_       in  ref_mfo_reg.begin_date       % type,
    end_date_         in  ref_mfo_reg.end_date         % type,
    id_usr_           in  ref_mfo_reg.id_usr           % type,
    user_location_    in  ref_mfo_reg.user_location    % type,
    datlast_          in  ref_mfo_reg.datlast          % type,
    do_commit_        in  integer default 1,     
    err_code          out number,
    err_msg           out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_MFO_REG';
    v_have_chg           boolean;
    v_ref_data           ref_mfo_reg %rowtype;
    ref_code             Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_mfo_reg';
    v_have_chg := false;
    
    
    if (trim(name_ru_) is null) or (begin_date_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;*/
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_, end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    /*report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;*/
    
    begin
      select *
        into v_ref_data
        from ref_mfo_reg
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;
    
    if v_ref_data.id <> 0 then
      if nvl(v_ref_data.name_kz,' ') <> nvl(name_kz_,' ') or 
         nvl(v_ref_data.name_ru,' ') <> nvl(name_ru_,' ') or
         nvl(v_ref_data.name_en,' ') <> nvl(name_en_,' ') or
         nvl(v_ref_data.ref_department,0) <> nvl(ref_department_,0) or
         nvl(v_ref_data.ref_legal_person,0) <> nvl(ref_legal_person_,0) or 
         nvl(v_ref_data.base,' ') <> nvl(base_,' ') or
         nvl(v_ref_data.num_reg,' ') <> nvl(num_reg_,' ') or
         nvl(v_ref_data.fio_manager,' ') <> nvl(fio_manager_,' ') or
         nvl(v_ref_data.address,' ') <> nvl(address_,' ') or
         nvl(v_ref_data.contact_details,' ') <> nvl(contact_details_,' ') or        
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) then
         
        v_have_chg := true;
      end if;
    end if;
    
    if v_have_chg = true then
      update ref_mfo_reg 
         set --code           = code_,
             name_kz          = name_kz_,
             name_ru          = name_ru_,
             name_en          = name_en_,
             ref_department   = ref_department_,
             ref_legal_person = ref_legal_person_,
             base             = base_,
             num_reg          = num_reg_,
             fio_manager      = fio_manager_,
             address          = address_,
             contact_details  = contact_details_,           
             user_location    = user_location_,
             begin_date       = begin_date_,
             end_date         = end_date_,
             id_usr           = id_usr_,
             sent_knd         = 0,
             datlast          = datlast_
       where id = id_;
             
      if do_commit_ = 1 then
        Commit;
      end if;
    end if;
    
  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';  
  end ref_update_mfo_reg;
  
  
  procedure ref_delete_mfo_reg(
    id_        in  ref_mfo_reg.id % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_MFO_REG';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_mfo_reg',
                          /*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
  
    update ref_mfo_reg 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_mfo_reg;
  
  /* Справочник балансовых счетов для отчетов о сделках */
  procedure ref_read_deal_balance_acc_list(
    date_     in  Date,
    Cur       out sys_refcursor,
    Err_Code  out number,
    Err_Msg   out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_DEAL_BALANCE_ACC_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select m.id,
             m.rec_id,
             m.code,
             m.name_kz,
             m.name_ru,
             m.name_en,
             m.short_name_kz,
             m.short_name_ru,
             m.short_name_en,
             m.num_acc,
             m.begin_date,
             m.end_date,
             m.datlast,
             m.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             m.user_location,
             sk.name as sent_knd
        from v_ref_deal_balance_acc m,
             f_users u,
             sent_knd sk
       where m.id_usr = u.user_id
         and m.sent_knd = sk.sent_knd         
         and (date_ is null or m.begin_date = (select max(t.begin_date)
                                                 from v_ref_deal_balance_acc t
                                                where t.rec_id = m.rec_id
                                                  and t.begin_date <= date_))
         and (date_ is null or (m.end_date is null or m.end_date > date_))
       order by m.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end ref_read_deal_balance_acc_list;
  
  procedure ref_read_deal_ba_l_by_params(
    id_             in  ref_deal_balance_acc.id % type,
    date_           in  Date,
    name_ru_        in  ref_deal_balance_acc.name_ru % type,
    rec_id_         in  ref_deal_balance_acc.rec_id % type,
    search_all_ver_ in  Integer default 0,
    Cur             out sys_refcursor,
    Err_Code        out number,
    Err_Msg         out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_DEAL_BA_L_BY_PARAMS';
    v_name varchar2(524);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    v_name := '%' || name_ru_ || '%';
    
    Open Cur for
      select m.id,
             m.rec_id,
             m.code,
             m.name_kz,
             m.name_ru,
             m.name_en,
             m.short_name_kz,
             m.short_name_ru,
             m.short_name_en,
             m.num_acc,
             m.begin_date,
             m.end_date,
             m.datlast,
             m.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,                          
             m.user_location,
             sk.name as sent_knd
        from v_ref_deal_balance_acc m,
             f_users u,
             sent_knd sk
       where m.id_usr = u.user_id
         and m.sent_knd = sk.sent_knd
         and m.rec_id = (select max(t1.rec_id)
                          from v_ref_deal_balance_acc t1
                         where ((
                                  (search_all_ver_ = 1) and 
                                  (trim(name_ru_) is null or upper(t1.name_ru) like upper(trim(v_name)))
                                 ) or 
                                 (
                                  (search_all_ver_ is null or search_all_ver_ = 0) and 
                                  (trim(name_ru_) is null or upper(m.name_ru) like upper(trim(v_name)))
                                 ) 
                               )
                           and t1.rec_id = m.rec_id
                         )                  
         and (id_ is null or m.id = id_)
--         and (trim(name_ru_) is null or upper(m.name_ru) like upper(trim(v_name)))
         and (rec_id_ is null or m.rec_id = rec_id_)          
         and (date_ is null or m.begin_date = (select max(t.begin_date)
                                                 from v_ref_deal_balance_acc t
                                                where t.rec_id = m.rec_id
                                                  and t.begin_date <= date_))
         --and (date_ is null or (p.end_date is null or p.end_date > date_))
       order by m.id;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end;
  
  
  procedure ref_read_deal_ba_hst_list(
    id_       in  ref_deal_balance_acc.id % type,
    Cur       out sys_refcursor,
    Err_Code  out number,
    Err_Msg   out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_READ_DEAL_BA_HST_LIST';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    Open Cur for
      select mh.id_hst,
             mh.id,
             mh.rec_id,
             mh.code,
             mh.name_kz,
             mh.name_ru,
             mh.name_en,
             mh.short_name_kz,
             mh.short_name_ru,
             mh.short_name_en,
             mh.num_acc,
             mh.begin_date,
             mh.end_date,
             mh.datlast,
             mh.id_usr,
             u.last_name || ' ' || u.first_name || ' ' || u.middle_name as USER_NAME,
             mh.type_change,
             tc.name as type_change_name,
             mh.user_location,
             sk.name as sent_knd
        from ref_deal_balance_acc_hst mh,
             f_users u,
             type_change tc,
             sent_knd sk
       where mh.id_usr = u.user_id         
         and mh.sent_knd = sk.sent_knd
         and mh.type_change = tc.type_change
         and mh.id = id_
       order by mh.id_hst;
      
  exception
    when others then
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка получения курсора !';
      
      open Cur for
        select null from dual;
      rollback;
  end;
  
  
  procedure ref_insert_deal_balance_acc(
    rec_id_        in  ref_deal_balance_acc.rec_id        % type,
    code_          in  ref_deal_balance_acc.code          % type,
    name_kz_       in  ref_deal_balance_acc.name_kz       % type,
    name_ru_       in  ref_deal_balance_acc.name_ru       % type,
    name_en_       in  ref_deal_balance_acc.name_en       % type,
    short_name_kz_ in  ref_deal_balance_acc.name_kz       % type,
    short_name_ru_ in  ref_deal_balance_acc.name_ru       % type,
    short_name_en_ in  ref_deal_balance_acc.name_en       % type,
    num_acc_	     in  ref_deal_balance_acc.num_acc       % type,        
    begin_date_    in  ref_deal_balance_acc.begin_date    % type,
    end_date_      in  ref_deal_balance_acc.end_date      % type,
    id_usr_        in  ref_deal_balance_acc.id_usr        % type,
    user_location_ in  ref_deal_balance_acc.user_location % type,
    datlast_       in  ref_deal_balance_acc.datlast       % type,
    do_commit_     in  Integer default 1,
    id_            out ref_deal_balance_acc.id            % type,
    Err_Code       out number,
    Err_Msg        out varchar2
  )
  is
    ProcName     constant Varchar2(50) := 'PKG_FRSI_REF.REF_INSERT_DEAL_BALANCE_ACC';
    ref_code     Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_deal_balance_acc';
    
    if (trim(code_) is null) or 
       (trim(name_ru_) is null) or 
       (begin_date_ is null) or 
       (num_acc_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
                
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if; */
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,null,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;    
    
    if rec_id_ is not null then
      if ref_update_end_date(ref_code,rec_id_,begin_date_) <> 0 then
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 06 ' || 'Внимание! Ошибка обновления даты окончания!';
        raise E_Force_Exit;
      end if;
    end if;
    
    if ref_check_end_date(ref_code,null,rec_id_,begin_date_,end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
       
    id_ := seq_ref_deal_balance_acc_id.nextval;
    
    insert into ref_deal_balance_acc(
      id,
      rec_id,
      code,
      name_kz,
      name_ru,
      name_en,
      short_name_kz,
      short_name_ru,
      short_name_en,
      num_acc,      
      begin_date,
      end_date,
      id_usr,
      user_location,
      datlast
    )
    values (
      id_,
      nvl(rec_id_,id_),
      code_,
      name_kz_,
      name_ru_,
      name_en_,
      short_name_kz_,
      short_name_ru_,
      short_name_en_,
      num_acc_,
      begin_date_,
      end_date_,
      id_usr_,
      user_location_,
      datlast_
    );
                 
    if Do_Commit_ = 1 then
      Commit;
    end if;    
                 
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка добавления записи !';   
  end ref_insert_deal_balance_acc;
  
  
  procedure ref_update_deal_balance_acc(
    id_            in  ref_deal_balance_acc.id            % type,
    rec_id_        in  ref_deal_balance_acc.rec_id        % type,
    code_          in  ref_deal_balance_acc.code          % type,
    name_kz_       in  ref_deal_balance_acc.name_kz       % type,
    name_ru_       in  ref_deal_balance_acc.name_ru       % type,
    name_en_       in  ref_deal_balance_acc.name_en       % type,  
    short_name_kz_ in  ref_deal_balance_acc.name_kz       % type,
    short_name_ru_ in  ref_deal_balance_acc.name_ru       % type,
    short_name_en_ in  ref_deal_balance_acc.name_en       % type,
    num_acc_	     in  ref_deal_balance_acc.num_acc       % type,        
    begin_date_    in  ref_deal_balance_acc.begin_date    % type,
    end_date_      in  ref_deal_balance_acc.end_date      % type,
    id_usr_        in  ref_deal_balance_acc.id_usr        % type,
    user_location_ in  ref_deal_balance_acc.user_location % type,
    datlast_       in  ref_deal_balance_acc.datlast       % type,
    do_commit_     in  integer default 1,     
    err_code       out number,
    err_msg        out varchar2
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_UPDATE_DEAL_BALANCE_ACC';
    v_have_chg           boolean;
    v_ref_data           ref_deal_balance_acc %rowtype;
    ref_code             Varchar2(64);
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    ref_code := 'ref_deal_balance_acc';
    v_have_chg := false;
    
    
    if (trim(name_ru_) is null) or 
       (begin_date_ is null) or 
       (trim(code_) is null) or 
       (num_acc_ is null) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01 ' || 'Не допустимы пустые данные !';
      raise E_Force_Exit;
    end if;
    
    if(end_date_ is not null and end_date_ < begin_date_) then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 01/1 ' || 'Дата окончания не может быть меньша даты начала!';
      raise E_Force_Exit;
    end if;
    
    /*if ref_check_code(ref_code,rec_id_,code_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 02 ' || 'Внимание! Уже имеется запись с данным кодом!';
      raise E_Force_Exit;
    end if;*/
    
    if ref_check_name(ref_code,rec_id_,name_ru_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 03 ' || 'Внимание! Уже имеется запись с таким наименованием!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date(ref_code,id_,rec_id_,begin_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 04 ' || 'Внимание! Уже имеется запись на эту дату!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_end_date(ref_code,id_,rec_id_,begin_date_, end_date_) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 05 ' || 'Внимание! Имеется запись с не закрытой датой начала!';
      raise E_Force_Exit;
    end if;
    
    if ref_check_date_period(ref_code,id_,rec_id_, begin_date_, end_date_ ) <> 0 then
      Err_Code := -20500;
      Err_Msg  := ProcName || ' 06 ' || 'Внимание! Имеется запись за этот период!';
      raise E_Force_Exit;
    end if;
    
    /*report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => ref_code,
                          begin_date_ => begin_date_,
                          end_date_ => end_date_,
                          kind_event_ => 'update',
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;*/
    
    begin
      select *
        into v_ref_data
        from ref_deal_balance_acc
       where id = id_;
    exception
      when others then
        v_ref_data.id := 0;
        Err_Code := -20500;
        Err_Msg  := ProcName || ' 08 ' || 'Внимание! Ошибка нахождения реквизитов!';
        raise E_Force_Exit;        
    end;
    
    if v_ref_data.id <> 0 then
      if nvl(v_ref_data.name_kz,' ') <> nvl(name_kz_,' ') or 
         nvl(v_ref_data.name_ru,' ') <> nvl(name_ru_,' ') or
         nvl(v_ref_data.name_en,' ') <> nvl(name_en_,' ') or
         nvl(v_ref_data.code,' ') <> nvl(code_,' ') or
         nvl(v_ref_data.short_name_kz,' ') <> nvl(short_name_kz_,' ') or 
         nvl(v_ref_data.short_name_ru,' ') <> nvl(short_name_ru_,' ') or
         nvl(v_ref_data.short_name_en,' ') <> nvl(short_name_en_,' ') or
         nvl(v_ref_data.num_acc,' ') <> nvl(num_acc_,' ') or
         nvl(v_ref_data.begin_date,sysdate) <> nvl(begin_date_,sysdate) or
         nvl(v_ref_data.end_date,sysdate) <> nvl(end_date_,sysdate) then
         
        v_have_chg := true;
      end if;
    end if;
    
    if v_have_chg = true then
      update ref_deal_balance_acc 
         set code          = code_,
             name_kz       = name_kz_,
             name_ru       = name_ru_,
             name_en       = name_en_,
             short_name_kz = short_name_kz_,
             short_name_ru = short_name_ru_,
             short_name_en = short_name_en_,
             num_acc       = num_acc_,
             user_location = user_location_,
             begin_date    = begin_date_,
             end_date      = end_date_,
             id_usr        = id_usr_,
             sent_knd      = 0,
             datlast       = datlast_
       where id = id_;
             
      if do_commit_ = 1 then
        Commit;
      end if;
    end if;
    
  exception
    when E_Force_Exit then
      rollback;    
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || ' ' || Err_Msg || ' Ошибка обновления записи !';  
  end ref_update_deal_balance_acc;
  
  
  procedure ref_delete_deal_balance_acc(
    id_        in  ref_deal_balance_acc.id % type,
    do_commit_ in  integer default 1,     
    err_code   out number,
    err_msg    out varchar2 
  )
  is
    ProcName constant Varchar2(50) := 'PKG_FRSI_REF.REF_DELETE_DEAL_BALANCE_ACC';
  begin
    Err_Code := 0;
    Err_Msg  := ' ';
    
    report_ref_link_check(rec_id_ => null,
                          id_ => id_,
                          ref_code_ => 'ref_deal_balance_acc',
                          /*begin_date_ => null,
                          end_date_ => null,
                          kind_event_ => 'delete',*/
                          err_code => Err_code,
                          err_msg => Err_msg);
    if Err_Code <> 0 then
      raise E_Force_Exit;
    end if;
  
    update ref_deal_balance_acc 
       set delfl = 1,
           sent_knd = 0
     where id = id_;
       
  if do_commit_ = 1 then
    Commit;
  end if;
    
  exception
    when E_Force_Exit then
      rollback;
    when others then
      rollback;
      Err_Code := SQLCODE;
      Err_Msg  := ProcName || 'Ошибка удаления записи !';
  end ref_delete_deal_balance_acc;
  
end PKG_FRSI_REF;
/
