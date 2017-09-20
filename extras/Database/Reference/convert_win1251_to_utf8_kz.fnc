create or replace function convert_win1251_to_utf8_kz(
  str_in in varchar2
)return varchar2
as
  i number;
  j number;
  str varchar2(1 char);
  chr_ number;
  str_out varchar2(2000);
begin
  select length(str_in)
    into i
    from dual;

  j := 1;
  str_out := '';
  
  while j <= i loop
    str := substr(str_in, j, 1);
      
    select ascii(str)
      into chr_
      from dual;
      
    if chr_ = 53656 then
      str_out := str_out || chr(54169);
    elsif chr_ = 53384 then
      str_out := str_out || chr(54168);
    elsif chr_ = 53653 then
      str_out := str_out || chr(53923);
    elsif chr_ = 53381 then
      str_out := str_out || chr(53922);
    elsif chr_ = 53652 then
      str_out := str_out || chr(53907);
    elsif chr_ = 53380 then
      str_out := str_out || chr(53906);
    elsif chr_ = 53655 then
      str_out := str_out || chr(53935);
    elsif chr_ = 53383 then
      str_out := str_out || chr(53934);
    elsif chr_ = 53662 then
      str_out := str_out || chr(53937);
    elsif chr_ = 53390 then
      str_out := str_out || chr(53936);
    elsif chr_ = 53660 then
      str_out := str_out || chr(53915);
    elsif chr_ = 53388 then
      str_out := str_out || chr(53914);
    elsif chr_ = 53905 then
      str_out := str_out || chr(54185);
    elsif chr_ = 53904 then
      str_out := str_out || chr(54184);
    /*elsif chr_ =  then
      str_out := str_out || chr(53947);
    elsif chr_ =  then
      str_out := str_out || chr(53946);*/
    else
      str_out := str_out || str;
    end if;
        
    j := j + 1;
  end loop;
  return str_out;
end;
/
