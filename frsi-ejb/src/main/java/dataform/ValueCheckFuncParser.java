package dataform;

import entities.Error;

import java.util.*;

/**
 * Created by nuriddin on 8/8/16.
 */
public class ValueCheckFuncParser {
    private static final String ILLEGAL_FORMULA_EXCEPTION = "Illegal formula";
    private final char eof = (char) -1;
    private Map<Character, Error.Level> errorLevels = new HashMap<Character, Error.Level>();

    {
        for (Error.Level el : Error.Level.values()) {
            errorLevels.put(Character.toLowerCase(el.name().charAt(0)), el);
        }
    }

    private ParamParser paramParser = new ParamParser();

    public List<Function> parse(String formula) throws FormulaSyntaxError {
        List<Function> functions = new ArrayList<Function>();
        Function currentFunction = null;
        int index = -1;
        boolean waitingFunctionName = true;
        boolean waitingErrorLevel = false;
        int f1 = -1;
        int f2 = -1;
        int b1 = -1;
        int b2 = -1;
        while (true) {
            index++;
            char last = index == formula.length() ? eof : formula.charAt(index);

            if (f1 > -1 && f2 > -1 && b1 > -1 && b2 > -1) {
                String funcName = formula.substring(f1, f2 + 1);
                String paramStr = formula.substring(b1 + 1, b2);
                currentFunction = new ValueCheckFuncParser.Function(funcName.trim(), paramStr, f1);
                paramParser.parse(paramStr);
                currentFunction.params = paramParser.params.toArray(new String[paramParser.params.size()]);
                functions.add(currentFunction);

                if (last != ':') {
                    currentFunction = null;
                }

                f1 = f2 = b1 = b2 = -1;
                waitingFunctionName = false;

                if (last == ':') {
                    waitingErrorLevel = true;
                    continue;
                }

                if (last != eof && last != ',') {
                    throw new FormulaSyntaxError(ILLEGAL_FORMULA_EXCEPTION);
                }
            }

            if (last == eof) {
                if (waitingFunctionName
                        || waitingErrorLevel
                        || f1 > -1 || f2 > -1
                        || b1 > -1 || b2 > -1) {
                    throw new FormulaSyntaxError(ILLEGAL_FORMULA_EXCEPTION);
                } else {
                    break;
                }
            }
            if (b1 == -1 && last == ',') {
                waitingFunctionName = true;
                continue;
            }
            if (waitingFunctionName) {
                f1 = index;
                waitingFunctionName = false;
                currentFunction = null;
            }
            if (waitingErrorLevel) {
                char ch = Character.toLowerCase(last);
                if (!errorLevels.containsKey(ch)) {
                    throw new FormulaSyntaxError(ILLEGAL_FORMULA_EXCEPTION);
                } else {
                    currentFunction.setErrorLevel(errorLevels.get(ch));
                }
                waitingErrorLevel = false;
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

        return functions;
    }

    private class ParamParser {
        private char last = eof;
        Map<Character, Character> s = new HashMap<Character, Character>();

        {
            s.put('(', ')');
            s.put('[', ']');
            s.put('\'', '\'');
        }

        private List<String> params = new ArrayList<String>();
        private int index = -1;
        private int start = 0;

        void parse(String paramsStr) throws FormulaSyntaxError {
            params = new ArrayList<String>();
            index = -1;
            start = 0;

            if (paramsStr == null || paramsStr.trim().isEmpty()) {
                return;
            }
            paramsStr = paramsStr.trim();
            while (true) {
                index++;
                if (index >= paramsStr.length()) {
                    if (index > start) {
                        params.add(paramsStr.substring(start, index).trim());
                    }
                    break;
                }
                last = paramsStr.charAt(index);

                if (last == ',') {
                    params.add(paramsStr.substring(start, index).trim());
                    start = index + 1;
                    if (paramsStr.length() == (index + 1)) {
                        throw new FormulaSyntaxError(ILLEGAL_FORMULA_EXCEPTION);
                    }
                    continue;
                }
                if (s.keySet().contains(last)) {
                    if (paramsStr.length() == (index + 1)) {
                        throw new FormulaSyntaxError(ILLEGAL_FORMULA_EXCEPTION);
                    }
                    String f = grabUntil(paramsStr.substring(index + 1), s.get(last));
                    if (f == null) {
                        throw new FormulaSyntaxError(ILLEGAL_FORMULA_EXCEPTION);
                    }
                    index += f.length();
                }
            }
        }

        private String grabUntil(String str1, char ch) {
            int i = str1.indexOf(ch);
            if (i == -1) {
                return null;
            } else {
                return str1.substring(0, i + 1);
            }
        }
    }

    public class Function {
        private String name;
        private String paramStr;
        private String[] params;
        private int index;
        private Error.Level errorLevel;

        private Function(String name, String paramStr, int index) {
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

        public int getIndex() {
            return index;
        }

        public Error.Level getErrorLevel() {
            return errorLevel;
        }

        public void setErrorLevel(Error.Level errorLevel) {
            this.errorLevel = errorLevel;
        }

        public String[] getParams() {
            return params;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ValueCheckFuncParser.Function function = (ValueCheckFuncParser.Function) o;

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
                    ", params=" + Arrays.toString(params) +
                    ", index=" + index +
                    ", errorLevel=" + errorLevel +
                    '}';
        }
    }

    public static void main(String[] args) {
        ValueCheckFuncParser dateEval = new ValueCheckFuncParser();
        try {
//            String str = "contains([fs_sr_1_array*name,fs_sr_1_array*bin_iin],[fs_sr_2_array*name,fs_sr_2_array*bin],'в таблице 2'):w, validateIDN()";
            String str = "contains([fs_sr_1_array*name,fs_sr_1_array*bin_iin],[fs_sr_2_array*name,fs_sr_2_array*bin],'в таблице 2'):w, validateIDN()";
            List<Function> functions = dateEval.parse(str);
            for (Function function : functions) {
                System.out.println(function.toString());
            }
        } catch (FormulaSyntaxError e) {
            e.printStackTrace();
        }

    }
}
