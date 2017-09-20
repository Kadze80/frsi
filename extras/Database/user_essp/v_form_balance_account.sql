create or replace view v_form_balance_account as
  select rh1.id, rh1.idn, rh1.report_date, rh1.code, s.name_ru, rh1.value
      from (select r.id, r.idn, r.report_date, rh.id id_his, substr(rhl.key, instr(rhl.key, ':', 1, 2) + 1) as code, to_number(rhl.value) as value
              from report_history_list rhl,
                   report_history rh,
                   reports r,
                   report_history_statuses rhs
             where rhl.value_type = 'n0'
               and rhl.report_history_id = rh.id
               and rh.report_id = r.id
               and r.form_code = 'balance_accounts'
               and rh.id = rhs.report_history_id
               and upper(rhs.status_code) = 'APPROVED'
               and rhs.id = (select max(rhs1.id)
                               from report_history_statuses rhs1
                              where rhs1.report_history_id = rh.id)) rh1,
           ref_balance_account s
     where rh1.code = s.code;
