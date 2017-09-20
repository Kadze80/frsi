package parser.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nuriddin on 9/4/16.
 */
public class ParsedRule {
    private long ruleId;
    private String parsedFormula;
    private boolean dynamicCell;
    private int priority;
    private RuleRow ruleRow;

    private List<ParsedItem> items = new ArrayList<ParsedItem>();

    public long getRuleId() {
        return ruleId;
    }

    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
    }

    public List<ParsedItem> getItems() {
        return items;
    }

    public String getParsedFormula() {
        return parsedFormula;
    }

    public void setParsedFormula(String parsedFormula) {
        this.parsedFormula = parsedFormula;
    }

    public boolean isDynamicCell() {
        return dynamicCell;
    }

    public void setDynamicCell(boolean dynamicCell) {
        this.dynamicCell = dynamicCell;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public List<ParsedKey> getAllParsedKeys() {
        List<ParsedKey> keys = new ArrayList<ParsedKey>();
        for (ParsedItem i : items) {
            if (i instanceof ParsedKey)
                keys.add((ParsedKey) i);
            else if(i instanceof ParsedAggFunction)
                keys.addAll(((ParsedAggFunction) i).getChildren());
        }
        return keys;
    }

    public RuleRow getRuleRow() {
        return ruleRow;
    }

    public void setRuleRow(RuleRow ruleRow) {
        this.ruleRow = ruleRow;
    }

    @Override
    public String toString() {
        return "ParsedRule{" +
                "ruleId=" + ruleId +
                ", parsedFormula='" + parsedFormula + '\'' +
                ", dynamicCell=" + dynamicCell +
                ", priority=" + priority +
                ", items=" + items +
                '}';
    }
}
