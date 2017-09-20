package util;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DataUtils {
    public static final long MILLISECONDS_PER_DAY = 86400000L;

    public DataUtils() {
    }

    public static Date plus(Date date, int field, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        calendar.add(field, amount);
        return calendar.getTime();
    }

    public static Date nowPlus(int field, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(field, amount);
        return calendar.getTime();
    }

    public static int compareBeginningOfTheDay(Date comparingDate, Date anotherDate) {
        Date newComparingDate = new Date(comparingDate.getTime());
        Date newAnotherDate = new Date(anotherDate.getTime());
        toBeginningOfTheDay(newComparingDate);
        toBeginningOfTheDay(newAnotherDate);
        return newComparingDate.compareTo(newAnotherDate);
    }

    public static void toBeginningOfTheDay(Date date) {
        long oldTime = date.getTime();
        long timeZoneOffset = (long)TimeZone.getDefault().getOffset(oldTime);
        date.setTime((oldTime + timeZoneOffset) / 86400000L * 86400000L - timeZoneOffset);
    }

    public static void toBeginningOfTheSecond(Date date) {
        long oldTime = date.getTime();
        date.setTime(oldTime - oldTime % 1000L);
    }

    public static long cutOffTime(java.sql.Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(11, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        cal.set(14, 0);
        return cal.getTimeInMillis();
    }

    public static Date convert(java.sql.Date date) {
        return date == null?null:new Date(date.getTime());
    }

    public static java.sql.Date convert(Date date) {
        return date == null?null:new java.sql.Date(date.getTime());
    }

    public static Date convert(Timestamp timestamp) {
        return timestamp == null?null:new Date(timestamp.getTime());
    }

    public static java.sql.Date convertToSQLDate(Timestamp timestamp) {
        return timestamp == null?null:new java.sql.Date(timestamp.getTime());
    }

    public static Timestamp convertToTimestamp(Date date) {
        return date == null?null:new Timestamp(date.getTime());
    }

    public static Byte convert(boolean b) {
        return Byte.valueOf(b?Byte.valueOf("1").byteValue():0);
    }

    public static boolean convert(Byte value) {
        return value.equals(Byte.valueOf("1"));
    }
}