CREATE OR REPLACE PROCEDURE FILL_USER_PERMISSIONS(
  p_User_Id       IN  NUMBER,
  p_right_items   IN  NUMBER,
  p_departments   IN  NUMBER,
  p_subject_types IN  NUMBER,
  p_creditors     IN  NUMBER,
  p_forms         IN  NUMBER,
  Err_Code        OUT NUMBER,
  Err_Msg         OUT VARCHAR2,
  Do_Commit       IN  NUMBER DEFAULT 1) IS
  TYPE FORMSTYPE IS TABLE OF VARCHAR2(250);
  TYPE STRINGARRAY IS TABLE OF VARCHAR2(500) INDEX BY BINARY_INTEGER;
  admin_department_role CONSTANT   NUMBER := 2;
  admin_subject_type_role CONSTANT NUMBER := 6;
  respondent_role CONSTANT         NUMBER := 5;
  ref_respondent_rec_id_           f_users.ref_respondent_rec_id%TYPE;
  screen_name_                     f_users.screen_name%TYPE;
  role_id_                         groups.role_id%TYPE;
  ProcName CONSTANT                VARCHAR2(50) := 'FILL_USER_PERMISSIONS';
    E_Force_Exit EXCEPTION;
  form_permissions                 STRINGARRAY;
  v_count                          NUMBER;
  v_step                           VARCHAR2(50);
  v_group_count                    NUMBER;
  BEGIN
    Err_Code := 0;
    Err_Msg := '';

    IF p_user_id IS NULL
    THEN
      Err_Code := 001;
      Err_Msg := ProcName || ' 01 ' || ' user id is null!';
      RAISE E_Force_Exit;
    END IF;

    v_step := 'define_ref_respondent_rec_id';
    SELECT
      u.ref_respondent_rec_id,
      lower(u.screen_name)
    INTO ref_respondent_rec_id_,
      screen_name_
    FROM f_users u
    WHERE u.user_id = p_user_id;

    v_step := 'define_role_id';

    SELECT count(*)
    INTO v_group_count
    FROM GROUP_USERS
    WHERE USER_ID = p_user_Id;

    IF screen_name_ = 'frsiadmin'
    THEN
      role_id_ := 1;
    ELSE
      IF v_group_count = 1
      THEN
        SELECT g.role_id
        INTO role_id_
        FROM group_users gu,
          groups g
        WHERE gu.user_id = p_user_id
              AND gu.group_id = g.group_id
        GROUP BY g.role_id;
      ELSE
        role_id_ := 0;
      END IF;
    END IF;


    v_step := 'fill_right_items';
    /* fill right items */
    IF p_right_items = 1
    THEN

      DELETE FROM F_SESSION_RIGHT_ITEMS
      WHERE user_id = p_user_id;

      INSERT INTO f_session_right_items
      (right_item_id, user_id)
        SELECT
          ur.right_item_id,
          p_user_id
        FROM user_right_items ur,
          role_right_items urri
        WHERE ur.user_id = p_user_id
              AND ur.is_active = 1
              AND ur.right_item_id = urri.right_item_id
              AND urri.role_id = role_id_
        UNION
        SELECT
          gr.right_item_id,
          p_user_id
        FROM group_right_items gr
          INNER JOIN group_users gu
            ON gr.group_id = gu.group_id
          LEFT JOIN user_right_items ur
            ON ur.user_id = gu.user_id
               AND ur.right_item_id = gr.right_item_id
          ,
          role_right_items grri
        WHERE gu.user_id = p_user_id
              AND gr.is_active = 1
              AND nvl(ur.is_active, 1) = 1
              AND gr.right_item_id = grri.right_item_id
              AND grri.role_id = role_id_;
    END IF;

    v_step := 'fill_departments';
    IF (p_departments = 1)
    THEN
      DELETE FROM F_SESSION_DEPARTMENTS
      WHERE user_id = p_user_id;

      IF role_id_ = admin_department_role
      THEN
        INSERT INTO f_session_departments
        (department_id, user_id)
          SELECT
            uc.ref_department_rec_id,
            p_user_id
          FROM user_departments uc
          WHERE uc.user_id = p_user_id
                AND uc.is_active = 1
          UNION
          SELECT
            gc.ref_department_rec_id,
            p_user_id
          FROM group_departments gc
            INNER JOIN group_users g
              ON gc.group_id = g.group_id
            LEFT JOIN user_departments uc
              ON g.user_id = uc.user_id
                 AND gc.ref_department_rec_id = uc.ref_department_rec_id
          WHERE g.user_id = p_user_id
                AND gc.is_active = 1
                AND nvl(uc.is_active, 1) = 1;
      END IF;
    END IF;

    v_step := 'fill_subject_types';
    IF (p_subject_types = 1)
    THEN
      DELETE FROM F_SESSION_SUBJECT_TYPES
      WHERE user_id = p_user_id;

      IF role_id_ = admin_subject_type_role
      THEN
        INSERT INTO F_SESSION_SUBJECT_TYPES
        (SUBJECT_TYPE_ID, user_id)
          SELECT
            uc.REF_SUBJECT_TYPE_REC_ID,
            p_user_id
          FROM USER_SUBJECT_TYPES uc
          WHERE uc.user_id = p_user_id
                AND uc.is_active = 1
          UNION
          SELECT
            gc.REF_SUBJECT_TYPE_REC_ID,
            p_user_id
          FROM GROUP_SUBJECT_TYPES gc
            INNER JOIN group_users g
              ON gc.group_id = g.group_id
            LEFT JOIN USER_SUBJECT_TYPES uc
              ON g.user_id = uc.user_id
                 AND gc.REF_SUBJECT_TYPE_REC_ID = uc.REF_SUBJECT_TYPE_REC_ID
          WHERE g.user_id = p_user_id
                AND gc.is_active = 1
                AND nvl(uc.is_active, 1) = 1;
      END IF;
    END IF;

    v_step := 'fill_creditors';
    IF p_creditors = 1
    THEN
      DELETE FROM F_SESSION_CREDITORS
      WHERE user_id = p_user_id;

      IF role_id_ <> respondent_role
      THEN
        INSERT INTO f_session_creditors
        (creditor_id, user_id)
          SELECT
            uc.ref_respondent_rec_id,
            p_user_id
          FROM user_respondents uc
          WHERE uc.user_id = p_user_id
                AND uc.is_active = 1
          UNION
          SELECT
            gc.ref_respondent_rec_id,
            p_user_id
          FROM group_respondents gc
            INNER JOIN group_users g
              ON gc.group_id = g.group_id
            LEFT JOIN user_respondents uc
              ON g.user_id = uc.user_id
                 AND gc.ref_respondent_rec_id = uc.ref_respondent_rec_id
          WHERE g.user_id = p_user_id
                AND gc.is_active = 1
                AND nvl(uc.is_active, 1) = 1;
      END IF;

      IF ref_respondent_rec_id_ IS NOT NULL
      THEN
        SELECT count(*)
        INTO v_count
        FROM f_session_creditors f
        WHERE f.user_id = p_user_id
              AND f.creditor_id = ref_respondent_rec_id_;
        IF v_count = 0
        THEN
          INSERT INTO f_session_creditors
          (creditor_id, user_id)
          VALUES
            (ref_respondent_rec_id_, p_user_id);
        END IF;
      END IF;
    END IF;

    v_step := 'fill_forms';
    IF p_forms = 1 OR p_creditors = 1
    THEN
      DELETE FROM F_SESSION_RESP_FORMS
      WHERE user_id = p_user_id;

      /* fill form names by user and usergroups */
      form_permissions(1) := 'F:SHOW';
      form_permissions(2) := 'F:EDIT';
      form_permissions(3) := 'F:APPROVE';
      form_permissions(4) := 'F:DISAPPROVE';
      form_permissions(5) := 'F:DELETE';

      FOR i IN form_permissions.FIRST..form_permissions.LAST
      LOOP
        DECLARE
          form_codes      FORMSTYPE;
          permission_name VARCHAR2(250) := form_permissions(i);
        BEGIN
          SELECT DISTINCT sf.form_code
          BULK COLLECT INTO form_codes
          FROM subjecttype_forms sf
          WHERE exists
          (SELECT 'x'
           FROM f_session_creditors t
             INNER JOIN v_ref_respondent rr
               ON t.creditor_id = rr.rec_id
                  AND rr.begin_date =
                      (SELECT max(t.begin_date)
                       FROM v_ref_respondent t
                       WHERE t.rec_id = rr.rec_id
                             AND t.begin_date <= sysdate)
             INNER JOIN v_ref_subject_type st
               ON rr.ref_subject_type = st.id
             INNER JOIN v_ref_legal_person lp
               ON rr.ref_legal_person = lp.id
           WHERE t.user_id = p_user_id
                 AND st.rec_id = sf.ref_subject_type_rec_id);

          FOR line IN (SELECT
                         uf.form_code,
                         uf.REF_RESPONDENT_REC_ID
                       FROM USER_RESP_FORMS uf
                         INNER JOIN right_items ri
                           ON uf.right_item_id = ri.id
                              AND ri.name = permission_name
                       WHERE uf.user_id = p_user_id
                             AND uf.is_active = 1 AND uf.REF_RESPONDENT_REC_ID IN (SELECT cr.CREDITOR_ID
                                                                                   FROM F_SESSION_CREDITORS cr
                                                                                   WHERE cr.USER_ID = p_user_id)
                       UNION
                       SELECT
                         gf.form_code,
                         gf.REF_RESPONDENT_REC_ID
                       FROM GROUP_RESP_FORMS gf
                         INNER JOIN right_items ri1
                           ON gf.right_item_id = ri1.id
                              AND ri1.name = permission_name
                         INNER JOIN group_users gu
                           ON gf.group_id = gu.group_id
                         LEFT JOIN (user_forms uf1
                           INNER JOIN right_items ri
                             ON uf1.right_item_id = ri.id
                                AND ri.name = permission_name)
                           ON gu.user_id = uf1.user_id AND gf.form_code = uf1.form_code AND uf1.is_active = 0
                       WHERE gu.user_id = p_user_id
                             AND gf.is_active = 1
                             AND nvl(uf1.is_active, 1) = 1
                             AND gf.REF_RESPONDENT_REC_ID IN (SELECT cr.CREDITOR_ID
                                                              FROM F_SESSION_CREDITORS cr
                                                              WHERE cr.USER_ID = p_user_id)
          ) LOOP
            IF line.form_code MEMBER OF form_codes
            THEN
              EXECUTE IMMEDIATE
              'insert into f_session_resp_forms (right_item_name, form_code, ref_respondent_rec_id, user_id) values (''' ||
              permission_name || ''',''' || line.form_code || ''',' || line.REF_RESPONDENT_REC_ID || ',' || p_user_id
              || ')';
            END IF;
          END LOOP;

        END;
      END LOOP;
    END IF;

    IF Do_Commit = 1
    THEN
      COMMIT;
    END IF;

    EXCEPTION
    WHEN E_Force_Exit THEN
    ROLLBACK;
    WHEN OTHERS THEN
    ROLLBACK;
    Err_Code := SQLCODE;
    Err_Msg := ProcName || 'Some exception at ' || v_step || ' ' || sqlerrm;
  END;