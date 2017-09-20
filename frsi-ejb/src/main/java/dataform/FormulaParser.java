package dataform;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nuriddin.Baideuov on 05.06.2015.
 */
public class FormulaParser implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    private String formula;
    private IKeyHandler keyValueMapper;

    private final char eof = (char) -1;
    private char last;
    private int currPos = -1;
    private char[] pushArr = new char[10];
    private int pushPos;
    private StringBuilder result = new StringBuilder();
    private boolean noReportDataError;
    private List<NoReportDataError> errors = new ArrayList<NoReportDataError>();

    public FormulaParser(String formula, IKeyHandler keyValueMapper) throws FormulaSyntaxError {
        this.formula = formula;
        this.keyValueMapper = keyValueMapper;
        parse();
    }

    public String getResult() {
        return result.toString();
    }

    public boolean hasNoReportDataError() {
        return noReportDataError;
    }

    public List<NoReportDataError> getErrors() {
        return errors;
    }

    private String parse() throws FormulaSyntaxError {
        last = eof;
        currPos = -1;
        pushPos = -1;

        while (true) {
            char c = next();
            if (c == eof) {
                break;
            }
            if (c == '\\') {
                char c2 = next();
                if (c2 == '[') {
                    result.append(c2);
                } else {
                    result.append(c );
                    push(c2);
                }
            } else if (c == '[') {
                int startIndex = currPos;
                String key = grabUntil(']');
                int endIndex = currPos;
                if (keyValueMapper != null) {
                    try {
                        result.append(keyValueMapper.onKey(key, startIndex, endIndex));
                    }catch (NoReportDataError e){
                        result.append("null");
                        noReportDataError = true;
                        errors.add(e);
                    }
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private char next() {
        if (pushPos >= 0) {
            last = pushArr[pushPos];
            pushPos--;
        } else {
            currPos++;
            if ((formula.length() - 1) < currPos)
                last = eof;
            else
                last = formula.charAt(currPos);

        }
        return last;
    }

    private void push(char ch) {
        pushPos++;
        pushArr[pushPos] = ch;
    }

    private String grabUntil(char terminate) {
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = next();
            if (c != terminate && c != eof) {
                sb.append(c);
            } else {
                break;
            }
        }
        return sb.toString();
    }


}
