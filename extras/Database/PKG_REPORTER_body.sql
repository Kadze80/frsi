create or replace package body PKG_REPORTER is

  procedure get_data(p_creditor_id     in eav_a_creditor_user.creditor_id%TYPE,
                     p_report_date     in eav_batches.rep_date%TYPE,
                     p_input_form_name in eav_m_classes.name%TYPE,
                     p_code_fld        in eav_m_simple_attributes.name%TYPE,
                     p_cursor_         OUT SYS_REFCURSOR)
  is
  begin
    OPEN p_cursor_ FOR
      -- ¿¿¿¿¿¿¿ ¿¿¿¿¿¿¿ ¿¿¿¿¿ entities
      select m_cs.name || '*' || attr_val.name || ':' || attr.name || ':' || sv.value as PATH,
             i_val.value
        from eav_be_entities e,
             eav_m_classes c,
             eav_m_classes c_arr,
             eav_m_complex_set m_cs,
             eav_be_entity_complex_sets cs,
             eav_be_complex_set_values cv,
             eav_be_string_values      sv,
             eav_m_simple_attributes   attr,
             eav_be_integer_values     i_val,
             eav_m_simple_attributes   attr_val,
             eav_batches               b,
             eav_a_creditor_user       cu
       where e.class_id = c.id
         and c.name = p_input_form_name
         and cs.entity_id = e.id
         and cv.set_id = cs.set_id
         and b.rep_date = p_report_date
         and b.user_id = cu.user_id
         and cu.creditor_id = p_creditor_id
         and cv.batch_id = b.id
         and cv.report_date = p_report_date
         and c.id = m_cs.containing_id
         and m_cs.class_id = c_arr.id
         and sv.entity_id = cv.entity_value_id
         and attr.id = sv.attribute_id
         and attr.name  = p_code_fld
         and i_val.entity_id = cv.entity_value_id
         and attr_val.id = i_val.attribute_id
       UNION ALL
      select m_cs.name || '*' || attr_val.name || ':' || attr.name || ':' || sv.value as PATH,
             d_val.value
        from eav_be_entities e,
             eav_m_classes c,
             eav_m_classes c_arr,
             eav_m_complex_set m_cs,
             eav_be_entity_complex_sets cs,
             eav_be_complex_set_values cv,
             eav_be_string_values      sv,
             eav_m_simple_attributes   attr,
             eav_be_double_values      d_val,
             eav_m_simple_attributes   attr_val,
             eav_batches               b,
             eav_a_creditor_user       cu
       where e.class_id = c.id
         and c.name = p_input_form_name
         and cs.entity_id = e.id
         and cv.set_id = cs.set_id
         and b.rep_date = p_report_date
         and b.user_id = cu.user_id
         and cu.creditor_id = p_creditor_id
         and cv.batch_id = b.id
         and cv.report_date = p_report_date
         and c.id = m_cs.containing_id
         and m_cs.class_id = c_arr.id
         and sv.entity_id = cv.entity_value_id
         and attr.id = sv.attribute_id
         and attr.name  = p_code_fld
         and d_val.entity_id = cv.entity_value_id
         and attr_val.id = d_val.attribute_id;
  end;

end PKG_REPORTER;
