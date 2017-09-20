update ref_balance_account t
   set t.level_code = 'A'
 where t.code = '1610000.';
commit;
