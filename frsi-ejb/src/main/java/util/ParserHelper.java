package util;

import entities.Indicator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nuriddin on 10/13/16.
 */
public class ParserHelper {
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-z]+\\w*", Pattern.CASE_INSENSITIVE);
    private static final Pattern NON_DYNAMIC_ROW_PATTERN = Pattern.compile("[^:;\\*]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern DYNAMIC_ROW_PATTERN = Pattern.compile("^\\$D\\.(group\\.|([0-9]+.)*)(n|[0-9]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_PART_PATTERN = Pattern.compile("(.(y|h|q|m)\\(-{0,1}[0-9]+\\)|.f\\((Y|H|Q|M|D)\\))+", Pattern.CASE_INSENSITIVE);

    public static Indicator parseIndicator(String str) throws IndicatorParseException {
        return parseIndicator(str, false, false);
    }
    public static Indicator parseIndicator(String str, boolean allowEmptyKeyField, boolean allowEmptyRowId) throws IndicatorParseException {
        if (str == null)
            throw new IllegalStateException("Показатель не можеть быть null");
        Indicator ind = new Indicator();
        ind.setOrigin(str);
        String indPart = null;
        String formPart = null;
        String datePart = null;
        String[] arr = str.split(";");
        switch (arr.length) {
            case 3:
                formPart = arr[0];
                indPart = arr[1];
                datePart = arr[2];
                break;
            case 2:
                if (!arr[0].contains("*")) {
                    formPart = arr[0];
                    indPart = arr[1];
                } else {
                    indPart = arr[0];
                    datePart = arr[1];
                }
                break;
            case 1:
                indPart = arr[0];
                break;
            default:
                throw new IndicatorParseException(str);
        }

        int asterisk = indPart.indexOf("*");
        if (asterisk == -1)
            throw new IndicatorParseException(str);
        ind.setContainer(indPart.substring(0, asterisk));
        Matcher m = IDENTIFIER_PATTERN.matcher(ind.getContainer());
        if (!m.matches())
            throw new IndicatorParseException(str);

        int firstColon = indPart.indexOf(":", asterisk);
        if (firstColon == -1)
            throw new IndicatorParseException(str);
        ind.setFieldName(indPart.substring(asterisk + 1, firstColon));
        m = IDENTIFIER_PATTERN.matcher(ind.getFieldName());
        if (!m.matches())
            throw new IndicatorParseException(str);

        int secondColon = indPart.indexOf(":", firstColon + 1);
        if (secondColon == -1)
            throw new IndicatorParseException(str);
        ind.setKeyFieldName(indPart.substring(firstColon + 1, secondColon));
        if (!allowEmptyKeyField || !ind.getKeyFieldName().isEmpty()) {
            m = IDENTIFIER_PATTERN.matcher(ind.getKeyFieldName());
            if (!m.matches())
                throw new IndicatorParseException(str);
        }

        ind.setRowId(indPart.substring(secondColon + 1));
        if (!allowEmptyRowId || !ind.getKeyFieldName().isEmpty()) {
            if (ind.getRowId().toLowerCase().startsWith("$d.")) {
                m = DYNAMIC_ROW_PATTERN.matcher(ind.getRowId());
                if (!m.matches())
                    throw new IndicatorParseException(str);
            } else {
                m = NON_DYNAMIC_ROW_PATTERN.matcher(ind.getRowId());
                if (!m.matches())
                    throw new IndicatorParseException(str);
            }
        }

        if (formPart != null && !formPart.trim().isEmpty()) {
            ind.setFormCode(formPart.trim());
            if (!ind.getContainer().toLowerCase().startsWith(ind.getFormCode().toLowerCase()))
                throw new IndicatorParseException(str);
        }
        if (datePart != null && !datePart.trim().isEmpty()) {
            ind.setDateOffset(datePart.trim());
            m = DATE_PART_PATTERN.matcher(ind.getDateOffset());
            if (!m.matches())
                throw new IndicatorParseException(str);
        }

        ind.setIndicator(indPart);

        return ind;
    }
}
