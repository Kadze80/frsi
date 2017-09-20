create or replace view v_form_kl_6 as
select r.id, r.idn, r.report_date, rh.id id_his, substr(rhl.key, instr(rhl.key, ':', 1, 2) + 1) as code, to_number(rhl.value) as value
                from report_history_list rhl,
                     report_history rh,
                     reports r,
                     report_history_statuses rhs
               where rhl.value_type = 'n0'
                 and rhl.report_history_id = rh.id
                 and rh.report_id = r.id
                 and r.form_code = 'kl_6'
                 and rh.id = rhs.report_history_id
                 and upper(rhs.status_code) = 'APPROVED'
                 and rhs.id = (select max(rhs1.id)
                                 from report_history_statuses rhs1
                                where rhs1.report_history_id = rh.id);
