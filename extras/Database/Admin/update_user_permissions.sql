CREATE OR REPLACE PROCEDURE update_user_permissions(
  p_permission_form_table IN  PERMISSION_FORM_TABLE,
  p_selected_user_id      IN  NUMBER,
  p_user_id               IN  NUMBER,
  p_date                  IN  DATE,
  p_user_location         IN  ae_main.user_location%TYPE,
  p_parent_id             IN  ae_main.parent_id%TYPE,
  p_err_code              OUT NUMBER,
  p_err_msg               OUT VARCHAR2)
IS
    E_Force_Exit exception;
  v_parent_ae_main_id  ae_main.id%TYPE;
  v_user_name          ae_main.user_name%TYPE;
  v_screen_name        ae_main.screen_name%TYPE;
  c_proc_name CONSTANT VARCHAR2(50) := 'update_user_permissions';
  BEGIN

    PKG_FRSI_AE.AE_INSERT_MAIN('PERMISSIONS_USER', 'Доступ пользователя на формы', 97, p_date, NULL, p_date, NULL,
                               p_user_id, p_user_location, NULL, p_parent_id, 0, v_parent_ae_main_id, p_err_code,
                               p_err_msg);

    BEGIN
      SELECT
        last_name || ' ' || first_name || ' ' || middle_name AS user_name,
        screen_name
      INTO v_user_name,
        v_screen_name
      FROM f_users t
      WHERE user_id = p_user_id;
      EXCEPTION
      WHEN OTHERS THEN
      p_err_code := -20500;
      p_err_msg := c_proc_name || ' 01 ' || 'Внимание! Ошибка нахождения реквизитов пользователя!';
      RAISE E_Force_Exit;
    END;

    INSERT INTO USER_RESP_FORMS (id, user_id, form_code, right_item_id, REF_RESPONDENT_REC_ID, is_active)
      SELECT
        SEQ_USER_RESP_FORMS_ID.nextval,
        p_selected_user_id,
        t.form_code,
        (SELECT id
         FROM right_items
         WHERE name = t.permission_name),
        t.ref_respondent_rec_id,
        t.is_active
      FROM table(p_permission_form_table) t
      WHERE COALESCE(t.id, 0) = 0 OR (COALESCE(t.id, 0) != 0 AND coalesce(t.is_for_group, 0) = 1);

    UPDATE USER_RESP_FORMS f
    SET is_active = (SELECT t.is_active
                     FROM table(p_permission_form_table) t
                     WHERE t.id = f.ID AND coalesce(t.is_for_group, 0) = 0 AND
                           coalesce(t.is_active, 0) != coalesce(t.is_inh_active, 0))
    WHERE id IN (SELECT t.id
                 FROM table(p_permission_form_table) t
                 WHERE COALESCE(t.id, 0) != 0 AND coalesce(t.is_for_group, 0) = 0 AND
                       coalesce(t.is_active, 0) != coalesce(t.is_inh_active, 0));

    DELETE FROM USER_RESP_FORMS
    WHERE id IN (SELECT t.id
                 FROM table(p_permission_form_table) t
                 WHERE COALESCE(t.id, 0) != 0 AND coalesce(t.is_for_group, 0) = 0 AND
                       coalesce(t.is_active, 0) = coalesce(t.is_inh_active, 0));

    /*inserting audit records*/
    INSERT INTO ae_main
    (id,
     parent_id,
     code_object,
     name_object,
     ae_kind_event,
     date_event,
     ref_respondent,
     date_in,
     rec_id,
     user_id,
     user_location,
     user_name,
     screen_name,
     notice_sts)
      SELECT
        seq_ae_main_id.nextval,
        v_parent_ae_main_id,
        t.form_code,
        t.form_name,
        get_permission_forms_ae_kind(t.permission_name, t.is_active,'user'),
        p_date,
        t.ref_respondent_rec_id,
        p_date,
        NULL,
        p_user_id,
        p_user_location,
        v_user_name,
        v_screen_name,
        NULL
      FROM table(p_permission_form_table) t;

    EXCEPTION
    WHEN E_Force_Exit THEN
    ROLLBACK;
    WHEN OTHERS THEN
    ROLLBACK;
    p_err_code := SQLCODE;
    p_err_msg := c_proc_name || ' Ошибка добавления записи в таблицу прав пользователя!';

  END update_user_permissions;