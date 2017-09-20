package parser.functions;

import dataform.FormulaSyntaxError;
import dataform.NoReportDataError;
import entities.RefRespondentItem;
import parser.DoubleCallback;
import parser.Helper;
import parser.parser.ParsedKey;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by nuriddin on 9/7/16.
 */
public class SumFunction extends AbstractFunction {

    private int iterationCount;
    private int noDataErrorCount;

    public SumFunction(ContextProvider context) {
        super(context);
    }

    public double exec(DoubleCallback expr, int[] keyIds) throws Exception {
        if (keyIds.length == 1) {
            ParsedKey key = context.getParsedKey(keyIds[0]);
            if (key.isRowRange()) {
                return sumRowRange(expr, key);
            }
            if (key.isFieldRange()) {
                return sumFieldRange(expr, key);
            }
        }
        context.validateParsedKeysForAggFunction(keyIds);
        if (context.getFilterRows() != null) {
            return sumAll(context.getFilterRows(), expr);
        } else {
            for (int id : keyIds) {
                ParsedKey pk = context.getParsedKey(id);
                if (pk.isDynamicRow()) {
                    return sumAll(context.getDynamicRowIdsByTemplate(pk), expr);
                }
            }
        }
        return sumByResp(expr);
    }

    private double sumAll(Map<String, Set<String>> filterRows, DoubleCallback expr) throws Exception {
        init();
        double sum = 0;
        for (Map.Entry<String, Set<String>> e : filterRows.entrySet()) {
            context.setContextBin(e.getKey());
            for (String rowId : e.getValue()) {
                context.setDynamicRowId(rowId);
                try {
                    sum += execNext(expr);
                } catch (Exception ex) {
                    if (context.getNoReportDataError() == null)
                        throw ex;
                }
            }
        }
        context.setDynamicRowId(null);
        validate();
        return sum;
    }

    private double sumByResp(DoubleCallback expr) throws Exception {
        init();
        double sum = 0;
        for (RefRespondentItem r : context.getRespondents()) {
            context.setContextBin(r.getIdn());
            try {
                sum += execNext(expr);
            } catch (Exception ex) {
                if (context.getNoReportDataError() == null)
                    throw ex;
            }
        }
        validate();
        return sum;
    }

    private double sumRowRange(DoubleCallback expr, ParsedKey key) throws Exception {
        init();
        double sum = 0;
        for (RefRespondentItem r : context.getRespondents()) {
            context.setContextBin(r.getIdn());
            List<String> rowIds = Helper.getRangeRowIds(key.getStartRange(), key.getEndRange());
            for (String rowId : rowIds) {
                context.setRangeRowId(rowId);
                try {
                    sum += execNext(expr);
                } catch (Exception ex) {
                    if (context.getNoReportDataError() == null)
                        throw ex;
                }
            }
        }
        validate();
        return sum;
    }

    private double sumFieldRange(DoubleCallback expr, ParsedKey key) throws Exception {
        init();
        double sum = 0;
        for (RefRespondentItem r : context.getRespondents()) {
            context.setContextBin(r.getIdn());
            List<String> fields = Helper.getRangeFields(context.getFields().get(key.getContainer()), key.getStartRange(), key.getEndRange());
            for (String field : fields) {
                context.setRangeField(field);
                try {
                    sum += execNext(expr);
                } catch (Exception ex) {
                    if (context.getNoReportDataError() == null)
                        throw ex;
                }
            }
        }
        validate();
        return sum;
    }

    private double execNext(DoubleCallback expr) throws Exception {
        iterationCount++;
        try {
            return expr.run(context);
        } catch (Exception ex) {
            if (context.getNoReportDataError() == null)
                throw ex;
            noDataErrorCount++;
            return 0;
        }
    }

    private void init() {
        iterationCount = 0;
        noDataErrorCount = 0;
    }

    private void validate() throws NoReportDataError {
        if (noDataErrorCount > 0 && iterationCount == noDataErrorCount)
            throw new NoReportDataError();
    }

    @Override
    public String getName() {
        return "sum";
    }
}
