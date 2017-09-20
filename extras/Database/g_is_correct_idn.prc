create or replace procedure G_Is_Correct_IDN
/*
 a12=(a1*b1+...+a11*b11) mod 11, ���: ai-�������� i-�� �������, bi-��� i-�� �������, --
 ������ ��:   |1|2|3|4|5|6|7|8|9|10|11|,
 ��� �������: |1|2|3|4|5|6|7|8|9|10|11|,
 ���� ���������� ����� ����� 10, �� ������ ������������ ������� ������������
 � ������ ������������������� �����
 ������ ���:  |1|2|3|4|5|6|7| 8| 9|10|11|,
 ��� �������: |3|4|5|6|7|8|9|10|11| 1| 2|,
 ���� ���������� ����� ����� ����� 10, �� ������ ��� �� ������������
 ���� ��������� ����� ����� �������� �� 0 �� 9, �� ������ ����� ������� � �������� ������������ �������
*/
  (
  Idn_     In  VarChar2,
  Result   Out SmallInt,
  Err_Code Out Integer,
  Err_Msg  Out VarChar2
  )
is
  ProcName     constant VarChar2(50) := 'G_Is_Correct_IDN';
  E_Force_Exit exception;
  i            SmallInt;
  type TIntArray is table of Integer index by binary_integer;
  a            TIntArray;
  Idn_Dg_      Integer;
begin

  Result   := 0;
  Err_Code := 0;
  Err_Msg  := '';

  if Length(Idn_) != 12 then
    Err_Code := -20500;
    Err_Msg  := ProcName || ' 01 ����� �� ������ ���� ����� 12, ��� ���������� ' || To_CHar(Length(Idn_));
    raise E_Force_Exit;
  end if;
  
  if Idn_ = '000000000000' then
    Err_Code := -20500;
    Err_Msg  := ProcName || ' 02 ������ �� ������� ��';
    raise E_Force_Exit;
  end if;
  
  Idn_Dg_ := 0;
  for i in 1..11 loop
    a(i) := To_Number(SubStr(Idn_, i, 1));
    Idn_Dg_ := Idn_Dg_+i*a(i);
  end loop;
  Idn_Dg_ := mod(Idn_Dg_, 11);
  
  if Idn_Dg_ = 10 then
    Idn_Dg_ := mod(3*a(1)+4*a(2)+5*a(3)+6*a(4)+7*a(5)+8*a(6)+9*a(7)+10*a(8)+11*a(9)+1*a(10)+2*a(11), 11);
  end if;
  
  if Idn_Dg_ = To_Number(SubStr(Idn_, 12, 1)) then
    Result := 1;
  end if;
exception
  when others then
    Result := 0;   
end;
/
