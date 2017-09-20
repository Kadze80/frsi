package util;

/**
 * Created by nuriddin on 10/10/16.
 */
public class IndicatorParseException extends Exception {
    private String indicator;

    public IndicatorParseException(String indicator) {
        this.indicator = indicator;
    }

    public IndicatorParseException(String message, String indicator) {
        super(message);
        this.indicator = indicator;
    }

    public String getIndicator() {
        return indicator;
    }
}
