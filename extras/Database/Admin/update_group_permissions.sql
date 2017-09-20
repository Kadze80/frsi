CREATE OR REPLACE PROCEDURE update_group_permissions(
  p_permission_form_table IN  PERMISSION_FORM_TABLE,
  p_selected_group_id     IN  NUMBER,
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
  c_proc_name CONSTANT VARCHAR2(50) := 'update_group_permissions';

  BEGIN

    PKG_FRSI_AE.AE_INSERT_MAIN('FORMS_PERMISSIONS_GROUP', 'Доступ группы на Формы', 84, p_date, NULL, p_date, NULL,
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

    /*inserting new permissions*/
    INSERT INTO GROUP_RESP_FORMS (id, group_id, form_code, right_item_id, REF_RESPONDENT_REC_ID, is_active)
      SELECT
        SEQ_GROUP_RESP_FORMS_ID.nextval,
        p_selected_group_id,
        t.form_code,
        (SELECT id
         FROM right_items
         WHERE name = t.permission_name),
        t.ref_respondent_rec_id,
        1
      FROM table(p_permission_form_table) t
      WHERE COALESCE(t.id, 0) = 0 AND coalesce(t.is_active, 0) = 1;

    /*deleting the same permissions from user*/
    DELETE FROM USER_RESP_FORMS
    WHERE ID IN (
      SELECT ri.id
      FROM USER_RESP_FORMS ri
        INNER JOIN GROUP_USERS gu ON ri.USER_ID = gu.USER_ID AND gu.GROUP_ID = p_selected_group_id
        INNER JOIN (SELECT *
                    FROM table(p_permission_form_table) t
                    WHERE COALESCE(t.id, 0) = 0 AND coalesce(t.is_active, 0) = 1) t2
          ON ri.FORM_CODE = t2.form_code AND ri.REF_RESPONDENT_REC_ID = t2.ref_respondent_rec_id AND
             ri.IS_ACTIVE = coalesce(t2.is_active, 0)
    );

    /*deleting permissions*/
    DELETE FROM GROUP_RESP_FORMS
    WHERE id IN (SELECT t.id
                 FROM table(p_permission_form_table) t
                 WHERE COALESCE(t.id, 0) != 0 AND coalesce(t.is_active, 0) = 0);

    /*deleting inactive permissions from group users*/
    DELETE FROM USER_RESP_FORMS
    WHERE ID IN (
      SELECT ri.id
      FROM USER_RESP_FORMS ri
        INNER JOIN GROUP_USERS gu ON ri.USER_ID = gu.USER_ID AND gu.GROUP_ID = p_selected_group_id
      WHERE ri.IS_ACTIVE = 0
            AND (ri.FORM_CODE, ri.REF_RESPONDENT_REC_ID, ri.RIGHT_ITEM_ID) NOT IN (
        SELECT
          gri.FORM_CODE,
          gri.REF_RESPONDENT_REC_ID,
          gri.RIGHT_ITEM_ID
        FROM GROUP_RESP_FORMS gri
        WHERE gri.GROUP_ID = p_selected_group_id
      )
    );

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
        get_permission_forms_ae_kind(t.permission_name, t.is_active,'group'),
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
    p_err_msg := c_proc_name || ' Ошибка добавления записи в таблицу прав группы !';

  END update_group_permissions;