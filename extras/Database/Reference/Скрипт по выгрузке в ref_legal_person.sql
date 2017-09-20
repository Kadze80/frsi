insert into ref_legal_person
  (id, rec_id, code, idn, name_kz, name_ru, ref_subject_type, ref_type_bus_entity, ref_country, legal_address, fact_address, begin_date, id_usr, user_location, short_name_ru)
  (select t.k_g_jur_person as id,
          t.k_g_jur_person as rec_id,
          t.jur_unique_code as code,
          t.rnn as idn,
          t.kaz_name as name_kz,
          t.name as name_ru,
          decode(t.k_g_jur_person_kind,0,8,1,9,2,1,3,10,4,11,5,4,6,12,7,13,8,14,9,6,10,15,11,16) as ref_subject_type,
          5 as ref_type_bus_entity,
          1347 as ref_country,
          t.legal_address,
          t.fact_address,
          to_date('01.01.2015','dd.mm.yyyy'),
          10196 as id_usr,
          '127.0.0.1' as user_location,
          t.short_name as short_name_ru          
     from k_g_jur_person t,
          k_g_jur_person_kind t1
    where t.k_g_jur_person_kind = t1.k_g_jur_person_kind
  );
Commit;
