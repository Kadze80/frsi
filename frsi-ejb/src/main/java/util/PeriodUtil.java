package util;

import dataform.FormulaSyntaxError;
import ejb.PeriodType;
import org.joda.time.LocalDate;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nuriddin on 4/6/16.
 */
public class PeriodUtil {

    public static final Map<String, PeriodType> PERIOD_TYPE_NAMES;

    static {
        PERIOD_TYPE_NAMES = new HashMap<String, PeriodType>();
        for (PeriodType pt : PeriodType.values()) {
            PERIOD_TYPE_NAMES.put(pt.name(), pt);
        }
    }

    public static PeriodType getPeriodTypeByName(String periodTypeName) {
        if (periodTypeName == null)
            throw new IllegalStateException("Тип периода не может быть пустым");
        if (!PERIOD_TYPE_NAMES.keySet().contains(periodTypeName.toUpperCase()))
            throw new IllegalStateException(MessageFormat.format("Не найден тип периода с идентификатором {0}", periodTypeName));
        return PERIOD_TYPE_NAMES.get(periodTypeName.toUpperCase());
    }

    public static LocalDate floor(LocalDate localDate, PeriodType pt) {
        switch (pt) {
            case Y:
                localDate = localDate.monthOfYear().withMinimumValue().dayOfMonth().withMinimumValue();
                break;
            case H:
                int h = (localDate.getMonthOfYear() / 6);
                if ((localDate.getMonthOfYear() % 2) != 0)
                    h++;
                int hm = h * 6 - 5;
                localDate = localDate.minusMonths(localDate.getMonthOfYear() - hm).dayOfMonth().withMinimumValue();
                break;
            case Q:
                int q = (localDate.getMonthOfYear() / 3);
                if ((localDate.getMonthOfYear() % 3) != 0)
                    q++;
                int qm = q * 3 - 2;
                localDate = localDate.minusMonths(localDate.getMonthOfYear() - qm).dayOfMonth().withMinimumValue();
                break;
            case M:
                localDate = localDate.dayOfMonth().withMinimumValue();
                break;
            case W:
                localDate = localDate.dayOfWeek().withMinimumValue();

        }
        return localDate;
    }

    public static LocalDate plusPeriod(LocalDate localDate, PeriodType pt, int periodCount) {
        switch (pt) {
            case Y:
                localDate = localDate.plusYears(periodCount);
                break;
            case H:
                localDate = localDate.plusMonths(periodCount * 6);
                break;
            case Q:
                localDate = localDate.plusMonths(periodCount * 3);
                break;
            case M:
                localDate = localDate.plusMonths(periodCount);
                break;
            case W:
                localDate = localDate.plusWeeks(periodCount);
                break;
            case D:
                localDate = localDate.plusDays(periodCount);
                break;
            default:
                localDate = localDate.plusDays(periodCount);
        }
        return localDate;
    }

    public static List<LocalDate> generateDates(LocalDate date1, LocalDate date2, PeriodType periodType) {
        List<LocalDate> dates = new ArrayList<LocalDate>();
        LocalDate startDate = floor(date1, periodType);
        LocalDate endDate = floor(date2, periodType);
        LocalDate interDate = startDate;
        while (interDate.compareTo(endDate) <= 0) {
            dates.add(interDate);
            interDate = plusPeriod(interDate, periodType, 1);
        }
        return dates;
    }

    public static void main(String[] args) {
        LocalDate now = new LocalDate();
        System.out.println(now);
        System.out.println(PeriodUtil.floor(now, PeriodType.W));

        System.out.println(PeriodUtil.plusPeriod(PeriodUtil.floor(now, PeriodType.W), PeriodType.W,1));

    }
}
