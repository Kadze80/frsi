package parser;

import parser.parser.ParsedRule;

import java.util.Comparator;

/**
 * Created by nuriddin on 9/14/16.
 */
public class ParsedRuleComparator implements Comparator<ParsedRule> {
    @Override
    public int compare(ParsedRule r1, ParsedRule r2) {
        if (r1.getPriority() > r2.getPriority())
            return 1;
        else if (r1.getPriority() < r2.getPriority())
            return -1;
        else
            return 0;
    }
}
