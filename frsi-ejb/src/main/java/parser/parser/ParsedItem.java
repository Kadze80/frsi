package parser.parser;

/**
 * Created by nuriddin on 9/6/16.
 */
public class ParsedItem {
    private String originText;
    private int startIndex;
    private int endIndex;

    public ParsedItem(String originText, int startIndex, int endIndex) {
        this.originText = originText;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public String getOriginText() {
        return originText;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    @Override
    public String toString() {
        return "ParsedItem{" +
                "originText='" + originText + '\'' +
                ", startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParsedItem that = (ParsedItem) o;

        return startIndex == that.startIndex;

    }

    @Override
    public int hashCode() {
        return startIndex;
    }
}
