insert into role_right_items
select gi2.role_id,
       gi1.right_item
  from tmp_right_items gi1,
       tmp_role_right_items gi2
 where gi1.id = gi2.right_item_id
