package parser.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nuriddin on 9/6/16.
 */
public class ParsedAggFunction extends ParsedItem {
    private int id;
    private String name;
    private List<ParsedKey> children = new ArrayList<ParsedKey>();

    public ParsedAggFunction(int id, String name, String originText, int startIndex, int endIndex) {
        super(originText, startIndex, endIndex);
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public List<ParsedKey> getChildren() {
        return children;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "ParsedAggFunction{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", children=" + children +
                '}';
    }
}
