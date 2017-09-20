CREATE OR REPLACE FUNCTION get_permission_forms_ae_kind(
  p_permission_name IN VARCHAR2,
  p_active          IN NUMBER,
  p_owner_type         VARCHAR2
)
  RETURN NUMBER
AS
  c_show_grant        NUMBER;
  c_show_revoke       NUMBER;

  c_edit_grant        NUMBER;
  c_edit_revoke       NUMBER;

  c_delete_grant      NUMBER;
  c_delete_revoke     NUMBER;

  c_approve_grant     NUMBER;
  c_approve_revoke    NUMBER;

  c_disapprove_grant  NUMBER;
  c_disapprove_revoke NUMBER;

  Result              NUMBER;
  BEGIN
    IF lower(p_owner_type) = 'group'
    THEN
      c_show_grant := 85;
      c_show_revoke := 86;

      c_edit_grant := 87;
      c_edit_revoke := 88;

      c_delete_grant := 89;
      c_delete_revoke := 90;

      c_approve_grant := 91;
      c_approve_revoke := 92;

      c_disapprove_grant := 93;
      c_disapprove_revoke := 94;
    ELSE
      c_show_grant := 98;
      c_show_revoke := 99;

      c_edit_grant := 100;
      c_edit_revoke := 101;

      c_delete_grant := 102;
      c_delete_revoke := 103;

      c_approve_grant := 104;
      c_approve_revoke := 105;

      c_disapprove_grant := 106;
      c_disapprove_revoke := 107;
    END IF;
    Result := CASE
              WHEN p_permission_name = 'F:SHOW' AND coalesce(p_active, 0) = 1
                THEN c_show_grant
              WHEN p_permission_name = 'F:SHOW' AND coalesce(p_active, 0) = 0
                THEN c_show_revoke
              WHEN p_permission_name = 'F:EDIT' AND coalesce(p_active, 0) = 1
                THEN c_edit_grant
              WHEN p_permission_name = 'F:EDIT' AND coalesce(p_active, 0) = 0
                THEN c_edit_revoke
              WHEN p_permission_name = 'F:DELETE' AND coalesce(p_active, 0) = 1
                THEN c_delete_grant
              WHEN p_permission_name = 'F:DELETE' AND coalesce(p_active, 0) = 0
                THEN c_delete_revoke
              WHEN p_permission_name = 'F:APPROVE' AND coalesce(p_active, 0) = 1
                THEN c_approve_grant
              WHEN p_permission_name = 'F:APPROVE' AND coalesce(p_active, 0) = 0
                THEN c_approve_revoke
              WHEN p_permission_name = 'F:DISAPPROVE' AND coalesce(p_active, 0) = 1
                THEN c_disapprove_grant
              WHEN p_permission_name = 'F:DISAPPROVE' AND coalesce(p_active, 0) = 0
                THEN c_disapprove_revoke
              END;
    RETURN Result;
  END;