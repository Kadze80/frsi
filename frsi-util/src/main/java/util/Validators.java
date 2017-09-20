package util;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Nuriddin.Baideuov on 21.04.2015.
 */
public class Validators implements Serializable{
    public static boolean validateIDN(String idn) {
        if (idn.length() != 12)
            return false;

        if (idn.equals("000000000000"))
            return false;

        int[] a = new int[11];
        int idnDg = 0;

        for (int i = 0; i < 11; i++) {
            try {
                a[i] = Integer.parseInt(idn.substring(i, 1 + i));
            } catch (NumberFormatException ex){
                return false;
            }
            idnDg += (i + 1) * a[i];
        }

        idnDg = idnDg % 11;

        if (idnDg == 10) {
            idnDg = (3 * a[0] + 4 * a[1] + 5 * a[2] + 6 * a[3] + 7 * a[4] + 8 * a[5] + 9 * a[6] + 10 * a[7] + 11 * a[8] + 1 * a[9] + 2 * a[10]) % 11;
        }

        int lastDigit;
        try {
            lastDigit = Integer.parseInt(idn.substring(11, 12));
        } catch (NumberFormatException ex){
            return false;
        }

        if (idnDg == lastDigit)
            return true;
        else
            return false;
    }

    public static void main(String[] args) {
    }

    public static boolean IsValidLong(String value) {
        try
        {
            long n = Long.parseLong(value.replace(" ", "").replace(",", "."));
        }
        catch ( NumberFormatException e )
        {
            return false;
        }
        return true;
    }

    public static boolean IsValidDouble(String value) {
        try
        {
            Double n = Double.parseDouble(value.replace(" ", "").replace(",", "."));
        }
        catch ( NumberFormatException e )
        {
            return false;
        }
        return true;
    }

    public static boolean IsValidDate(String value) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        formatter.setLenient (false);
        try
        {
            Date date = formatter.parse(value);
            if (!formatter.format(date).equals(value))
                return false;
        }
        catch (ParseException e)
        {
            return false;
        }
        return true;
    }

    public static boolean IsValidTime(String value) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setLenient (false);
        try
        {
            Date date = formatter.parse(value);
            if (!formatter.format(date).equals(value))
                return false;
        }
        catch (ParseException e)
        {
            return false;
        }
        return true;
    }

    public static boolean isValidMask(String value, String mask) throws Exception{
        if (value == null) {
            return false;
        } else {
            mask = mask == null ? "0" : mask;
            value = value.replace("-","");
            value = value.replace(",",".");
            String integer;
            Integer index;
            index = value.indexOf(".");
//                String fraction;
            if (Util.getIntFromStr(mask) > 0 && index > 0) {
                integer = value.substring(0, index);
//                    fraction = value.substring(value.indexOf(".") + 1, value.length());
            } else {
                integer = value.substring(0, value.length());
//                    fraction = "";
            }
            if (integer.length() > 13 /*|| fraction.length() > 6 || fraction.length() > iMask*/)
                return false;
        }
        return true;
    }

    public static boolean isValidXlsVersion(Integer verFromExcel, Integer verFromBD, Date dateFromExcel, Date dateFromBD){
        if((verFromExcel == null || verFromExcel == 0) && verFromBD > 1) {
            return false;
        }else if (dateFromExcel == null || dateFromBD == null) {
            return false;
        }else if(!verFromExcel.equals(verFromBD) || !Convert.getDateStringFromDate(dateFromExcel).equals(Convert.getDateStringFromDate(dateFromBD))) {
            return false;
        }else {
            return true;
        }
    }


    public static boolean IsPositiveNumber(int value) {
        return (value >= 0) ? true : false;
    }

    public static boolean IsPositiveNumber(float value) {
        return (value >= 0) ? true : false;
    }

    public static boolean equalDates(Date date, Date curDate){
        if(date == null || curDate == null || !Convert.getDateStringFromDate(date).equals(Convert.getDateStringFromDate(curDate)))
            return false;
        return true;
    }

    public static boolean IsNegativeNumber(int value) {
        return (value <= 0) ? true : false;
    }

    public static boolean IsNegativeNumber(float value) {
        return (value <= 0) ? true : false;
    }

    public static boolean IsNotEqualToZero(int value) {
        return (value != 0) ? true : false;
    }

    public static boolean IsNotEqualToZero(float value) {
        return (value != 0) ? true : false;
    }

    public static boolean IsPercent(int value) {
        return ((value >= 0) && (value <= 100)) ? true : false;
    }

    public static boolean IsPercent(float value) {
        return ((value >= 0) && (value <= 100)) ? true : false;
    }
}
