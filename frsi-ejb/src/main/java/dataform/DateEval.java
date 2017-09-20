package dataform;

import ejb.PeriodType;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import util.PeriodUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by nuriddin on 3/29/16.
 */
public class DateEval {

    private static final String ILLEGAL_FORMULA_EXCEPTION = "Illegal formula";
    private final char eof = (char) -1;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public Date eval(String formula, Date contextDate) throws FormulaSyntaxError {
        DateTimeZone dtZone = DateTimeZone.forTimeZone(TimeZone.getDefault());
        LocalDate localDate = new LocalDate(contextDate, dtZone);

        if (hasDate(formula)) {
            try {
                int pos = formula.indexOf("'", 1);
                localDate = new LocalDate(dateFormat.parse(formula.substring(1, pos)));
                formula = formula.substring(pos + 1);
            } catch (ParseException e) {
                throw new FormulaSyntaxError(ILLEGAL_FORMULA_EXCEPTION);
            }
        }

        int index = -1;
        boolean waitingFunctionName = false;
        int f1 = -1;
        int f2 = -1;
        int b1 = -1;
        int b2 = -1;
        while (true) {
            index++;
            char last = index == formula.length() ? eof : formula.charAt(index);

            if (f1 > -1 && f2 > -1 && b1 > -1 && b2 > -1) {
                String formulaName = formula.substring(f1, f2 + 1);
                String paramStr = formula.substring(b1 + 1, b2);
                localDate = doEval(new Function(formulaName, paramStr, f1), localDate);

                f1 = f2 = b1 = b2 = -1;
                waitingFunctionName = false;

                if (last != eof && last != '.') {
                    throw new FormulaSyntaxError(ILLEGAL_FORMULA_EXCEPTION);
                }
            }

            if (last == eof) {
                if (waitingFunctionName
                        || f1 > -1 || f2 > -1
                        || b1 > -1 || b2 > -1) {
                    throw new FormulaSyntaxError(ILLEGAL_FORMULA_EXCEPTION);
                } else {
                    break;
                }
            }
            if (index == 0 && last != '.') {
                throw new FormulaSyntaxError(ILLEGAL_FORMULA_EXCEPTION);
            }
            if (last == '.') {
                waitingFunctionName = true;
                continue;
            }
            if (waitingFunctionName) {
                f1 = index;
                waitingFunctionName = false;
            }
            if (f1 > -1 && b1 == -1) {
                if (last == '(') {
                    b1 = index;
                } else {
                    f2 = index;
                }
            }
            if (b1 > -1 && b2 == -1 && last == ')') {
                b2 = index;
            }

        }

        return localDate.toDate();
    }

    private LocalDate doEval(Function f, LocalDate localDate) throws FormulaSyntaxError {
        if (f.getName().equalsIgnoreCase("y")) {
            int param = parseParam(f.getParamStr());
            localDate = PeriodUtil.plusPeriod(localDate, PeriodType.Y, param);
        } else if (f.getName().equalsIgnoreCase("h")) {
            int param = parseParam(f.getParamStr());
            localDate = PeriodUtil.plusPeriod(localDate, PeriodType.H, param);
        } else if (f.getName().equalsIgnoreCase("q")) {
            int param = parseParam(f.getParamStr());
            localDate = PeriodUtil.plusPeriod(localDate, PeriodType.Q, param);
        } else if (f.getName().equalsIgnoreCase("m")) {
            int param = parseParam(f.getParamStr());
            localDate = PeriodUtil.plusPeriod(localDate, PeriodType.M, param);
        } else if (f.getName().equalsIgnoreCase("f")) {
            if (f.getParamStr() == null)
                throw new FormulaSyntaxError(ILLEGAL_FORMULA_EXCEPTION);
            localDate = PeriodUtil.floor(localDate, PeriodUtil.getPeriodTypeByName(f.getParamStr().toUpperCase()));
        } else {
            throw new FormulaSyntaxError(ILLEGAL_FORMULA_EXCEPTION);
        }
        return localDate;
    }

    private int parseParam(String paramsStr) throws FormulaSyntaxError {
        int param;
        if (paramsStr == null) {
            throw new FormulaSyntaxError(ILLEGAL_FORMULA_EXCEPTION);
        }
        paramsStr = paramsStr.trim();
        try {
            param = Integer.parseInt(paramsStr);
        } catch (NumberFormatException e) {
            throw new FormulaSyntaxError(ILLEGAL_FORMULA_EXCEPTION);
        }
        return param;
    }

    // TODO: 3/30/16 need to optimize
    private boolean hasDate(String str) {
        return !str.isEmpty() && str.startsWith("'");
    }

    class Function {
        private String name;
        private String paramStr;
        private int index;

        public Function(String name, String paramStr, int index) {
            this.name = name;
            this.paramStr = paramStr;
            this.index = index;
        }

        public String getName() {
            return name;
        }

        public String getParamStr() {
            return paramStr;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Function function = (Function) o;

            return index == function.index;

        }

        @Override
        public int hashCode() {
            return index;
        }

        @Override
        public String toString() {
            return "Function{" +
                    "name='" + name + '\'' +
                    ", paramStr='" + paramStr + '\'' +
                    ", index=" + index +
                    '}';
        }
    }

    public static void main(String[] args) {
        DateEval dateEval = new DateEval();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        try {
            Date date = dateEval.eval("'01.03.2016'.m(-1)", new Date());
            System.out.println(format.format(date));
        } catch (FormulaSyntaxError e) {
            e.printStackTrace();
        }

    }

}
