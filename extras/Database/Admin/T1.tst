PL/SQL Developer Test script 3.0
23
declare 
  i integer;
  id_ right_items.id % type;
begin
  
  for Rec in (select gi1.*
                from tmp_right_items gi1)
  loop
    begin
      select gi2.id
        into id_
        from right_items gi2
       where gi2.name = Rec.Name;
      
      update tmp_right_items gi3
         set gi3.right_item = id_
       where gi3.id = Rec.Id;
    exception
      when no_data_found then
        id_ := null;
    end;
  end loop;
end;
0
0
