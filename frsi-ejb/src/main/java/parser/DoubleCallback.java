package parser;

import dataform.FormulaSyntaxError;
import dataform.NoReportDataError;
import parser.functions.ContextProvider;

import javax.script.ScriptException;

/**
 * Created by nuriddin on 9/7/16.
 */
public interface DoubleCallback {
    double run(ContextProvider p) throws Exception;
}
