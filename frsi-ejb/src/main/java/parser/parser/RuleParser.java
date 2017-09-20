package parser.parser;

import dataform.*;
import entities.OutReportRuleItem;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.IRFactory;
import org.mozilla.javascript.ast.*;
import parser.DataType;
import parser.functions.AbstractFunction;
import parser.functions.SumFunction;
import util.PeriodUtil;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by nuriddin on 9/7/16.
 */
public class RuleParser {
    private DateEval dateEval = new DateEval();
    KeysHandler keysHandler = new KeysHandler();
    private IdGenerator idGenerator;
    private Set<String> functionNames;

    public void init(Date reportDate, Date date1, Date date2, String periodType, Set<String> f) {
        keysHandler.date1 = date1;
        keysHandler.date2 = date2;
        keysHandler.periodType = periodType;
        keysHandler.reportDate = reportDate;
        idGenerator = new IdGenerator();
        functionNames = f;
    }

    private void printPositions(String f1, String f2, List<Map<String, Object>> positions) {
        StringBuilder sb = new StringBuilder();
        sb.append(f1).append("\n").append(f2).append("\n");
        for (Map<String, Object> m : positions) {
            sb.append("startIndex: ").append(m.get("startIndex")).append(",\noffset: ").append(m.get("offset")).append(",\nkey: ").append(m.get("key"));
        }
        System.out.println(sb.toString());
    }

    public ParsedRule parse(OutReportRuleItem item) throws FormulaSyntaxError {
        validateRuleItem(item);

        idGenerator.init();
        String formula = item.getFormula();
        keysHandler.dynamicCell = item.getFieldName().toLowerCase().endsWith("@dynamiccellid");

        final List<ParsedKey> keys = new ArrayList<ParsedKey>();
        keysHandler.parsedKeys = keys;
        keysHandler.offset = 0;
        FormulaParser p = new FormulaParser(formula, keysHandler);
        formula = p.getResult();
        if (item.getId() == 5470)
            printPositions(item.getFormula(), formula, keysHandler.positions);

        CompilerEnvirons env = new CompilerEnvirons();
        env.setRecoverFromErrors(true);
        env.setGenerateDebugInfo(true);
        env.setRecordingComments(true);

        StringReader strReader = new StringReader(formula);
        IRFactory factory = new IRFactory(env);
        AstRoot astRoot;
        try {
            astRoot = factory.parse(strReader, null, 0);
        } catch (IOException e) {
            throw new FormulaSyntaxError(e.getMessage());
        }

        final List<ParsedAggFunction> parsedAggFunctions = new ArrayList<ParsedAggFunction>();
        final List<FormulaSyntaxError> errors = new ArrayList<FormulaSyntaxError>();
        final SortedSet<ReplaceText> replaceTexts = new TreeSet<ReplaceText>();
        final String ff = formula;
        astRoot.visit(new NodeVisitor() {
            @Override
            public boolean visit(AstNode astNode) {
                if (astNode instanceof FunctionCall) {
                    if (((FunctionCall) astNode).getTarget() instanceof Name) {
                        Name name = (Name) ((FunctionCall) astNode).getTarget();
                        if (functionNames.contains(name.getIdentifier().toLowerCase())) {
                            int startIndex = astNode.getAbsolutePosition();
                            int endIndex = astNode.getAbsolutePosition() + astNode.getLength() - 1;
                            String originText = ff.substring(startIndex, endIndex + 1);
                            String argsText = originText.substring(originText.indexOf("(") + 1, originText.lastIndexOf(")"));
                            ParsedAggFunction parsedFunction = new ParsedAggFunction(idGenerator.next(), name.getIdentifier(), originText, startIndex, endIndex);

                            String s = "";
                            Iterator<ParsedKey> it = keys.iterator();
                            while (it.hasNext()) {
                                ParsedKey k = it.next();
                                if (k.getStartIndex() > startIndex && k.getStartIndex() < endIndex) {
                                    s += "," + String.valueOf(k.getId());
                                    parsedFunction.getChildren().add(k);
                                    it.remove();
                                }
                            }

                            parsedAggFunctions.add(parsedFunction);

                            String f = parsedFunction.getName() + "(function(p){return " + argsText + ";}" + s + ")";

                            replaceTexts.add(new ReplaceText(startIndex, endIndex, f));
                        }
                    }
                }
                return errors.size() == 0;
            }
        });

        if (errors.size() > 0) {
            throw errors.get(0);
        }

        int offset = 0;
        for (ReplaceText r : replaceTexts) {
            formula = formula.substring(0, offset + r.start) + r.text + formula.substring(offset + r.end + 1);
            int l = r.end - r.start + 1;
            offset += r.text.length() - l;
        }

        ParsedRule parsedRule = new ParsedRule();
        parsedRule.setRuleId(item.getId());
        parsedRule.setParsedFormula(formula);
        parsedRule.getItems().addAll(keys);
        parsedRule.getItems().addAll(parsedAggFunctions);
        parsedRule.setDynamicCell(keysHandler.dynamicCell);
        parsedRule.setPriority(item.getPriority());
        return parsedRule;
    }

    private class IdGenerator {
        private int id;

        public IdGenerator() {
            init();
        }

        private int next() {
            return ++id;
        }

        private void init() {
            id = 0;
        }
    }

    private class ReplaceText implements Comparable {
        private int start;
        private int end;
        private String text;

        public ReplaceText(int start, int end, String text) {
            this.start = start;
            this.end = end;
            this.text = text;
        }

        @Override
        public int compareTo(Object o) {
            ReplaceText r = (ReplaceText) o;
            if (this.start < r.start)
                return -1;
            else
                return 1;
        }
    }

    private void validateRuleItem(OutReportRuleItem ruleItem) throws FormulaSyntaxError {
        if (ruleItem.isGrouping() && ruleItem.getFieldName().toLowerCase().endsWith("@dynamiccellid")) { //если группирующий и динамическое поле
            throw new FormulaSyntaxError(MessageFormat.format("Группирующее правило не может быть динамическим полем: Поле {0}", ruleItem.getFieldName()));
        }
    }

    private String grabFormCode(String key) throws FormulaSyntaxError {
        if (!key.contains(";"))
            throw new FormulaSyntaxError();

        String formCode_ = key.substring(0, key.indexOf(";"));
        if (formCode_.length() == 0)
            throw new FormulaSyntaxError();

        return formCode_;
    }

    private class KeysHandler implements IKeyHandler {

        private boolean dynamicCell;
        private Date reportDate;
        private Date date1;
        private Date date2;
        private String periodType;
        private DateTimeZone dtZone = DateTimeZone.forTimeZone(TimeZone.getDefault());
        private List<ParsedKey> parsedKeys;
        private int offset;
        private List<Map<String, Object>> positions = new ArrayList<Map<String, Object>>();

        @Override
        public String onKey(String key, int startIndex, int endIndex) throws FormulaSyntaxError, NoReportDataError {
            ParsedKey parsedKey = new ParsedKey(idGenerator.next(), key, startIndex + offset, endIndex);
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("startIndex", startIndex);
            m.put("offset", offset);
            m.put("key", key);
            positions.add(m);

            if (key.startsWith("^")) {
                parsedKey.setRequired(true);
                key = key.substring(1);
            }
            parsedKey.setFormCode(grabFormCode(key));

            String key_;
            if (key.contains(";"))
                key_ = key.substring(key.indexOf(";") + 1);
            else
                key_ = key;

            int asteriskIndex = key_.indexOf("*");
            int semicolonIndex = key_.indexOf(";");
            if (semicolonIndex > asteriskIndex || semicolonIndex == -1)
                throw new FormulaSyntaxError("В формуле не указан тип данных показателя");
            String dt = key_.substring(0, semicolonIndex);
            parsedKey.setDataType(DataType.shortNameToDataType(dt));

            key_ = key_.substring(semicolonIndex + 1);

            Set<LocalDate> dates = new HashSet<LocalDate>();

            if (dynamicCell) {
                dates.addAll(PeriodUtil.generateDates(new LocalDate(date1, dtZone), new LocalDate(date2, dtZone), PeriodUtil.getPeriodTypeByName(periodType)));
            } else {
                dates.add(new LocalDate(reportDate, dtZone));
            }

            if (key_.contains(";")) {
                parsedKey.setDatePart(key_.substring(key_.indexOf(";") + 1));
                key_ = key_.substring(0, key_.indexOf(";"));
            } else
                parsedKey.setDatePart("");

            parsedKey.setFieldName(key_.substring(key_.indexOf("*") + 1, key_.indexOf(":")));
            if (parsedKey.getFieldName().toLowerCase().startsWith("$dc(")) {
                String fieldName = parsedKey.getFieldName();
                parsedKey.setFieldRange(true);
                parsedKey.setStartRange(fieldName.substring(fieldName.indexOf("(") + 1, fieldName.indexOf(",")));
                parsedKey.setEndRange(fieldName.substring(fieldName.indexOf(",") + 1, fieldName.indexOf(")")));
            }

            parsedKey.setRowId(key_.substring(key_.lastIndexOf(":") + 1));
            if (parsedKey.getRowId().toLowerCase().startsWith("$d.")) {
                parsedKey.setDynamicRow(true);
            }
            if (parsedKey.getRowId().toLowerCase().startsWith("$dr(")) {
                String rowId = parsedKey.getRowId();
                parsedKey.setRowRange(true);
                parsedKey.setStartRange(rowId.substring(rowId.indexOf("(") + 1, rowId.indexOf(",")));
                parsedKey.setEndRange(rowId.substring(rowId.indexOf(",") + 1, rowId.indexOf(")")));
            }

            for (LocalDate ld : dates) {
                Date d = ld.toDate();
                Date td = ld.toDate();
                if (!parsedKey.getDatePart().isEmpty()) {
                    td = dateEval.eval(parsedKey.getDatePart(), d);
                }
                parsedKey.getDates().put(d, td);
            }
            parsedKey.setKey(key_);
            parsedKey.setContainer(key_.substring(0, key_.indexOf("*")));
            parsedKeys.add(parsedKey);

            String result = "p." + dt + "(" + parsedKey.getId() + ")";
            int diff = result.length() - (key.length() + 2);
            offset += diff;
            return result;
        }
    }
}
