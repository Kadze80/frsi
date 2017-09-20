package parser;

import parser.parser.ParsedKey;

/**
 * Created by nuriddin on 9/16/16.
 */
public class RequiredKeyError extends Exception {
    public RequiredKeyError(String message) {
        super(message);
    }
}
