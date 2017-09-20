package util;

import java.io.IOException;
import java.io.Reader;

public abstract class TextParser {

    public static char EOF = (char) -1;

    protected char last;

    private char[] _push = new char[10];
    private int _pushPos;
    private int _lastcol;
    private int _col;
    private int _row;
    private int _pos;
    private Reader _reader;

    class FastCharReader extends Reader {

        private CharSequence source;
        private int sourceLen;
        private int sourceIdx;

        FastCharReader(CharSequence source) {
            this.source = source;
            this.sourceIdx = -1;
            this.sourceLen = source.length();
        }

        public int read() throws IOException {
            sourceIdx++;
            if (sourceIdx >= sourceLen) {
                return EOF;
            } else {
                return source.charAt(sourceIdx);
            }
        }

        public int read(char[] cbuf, int off, int len) throws IOException {
            return EOF;
        }

        public void close() throws IOException {
        }
    }

    /**
     * Ошибка разбора
     */
    public class ErrorParse extends RuntimeException {
        private int row;
        private int col;

        public ErrorParse(Throwable cause) {
            super(cause);
            this.row = getRow();
            this.col = getCol();
        }

        public String getMessage() {
            return "row:" + row + ", col:" + col;
        }
    }

    public void loadFrom(Reader reader) throws Exception {
        _reader = reader;
        last = EOF;
        _pushPos = -1;
        _col = 0;
        _row = 1;
        _pos = -1;
        _lastcol = 0;
        try {
            next();
            push(last);
            onParse();
        } catch (Exception e) {
            throw new ErrorParse(e);
        }
    }

    public void loadFrom(CharSequence source) {
        try {
            loadFrom(new FastCharReader(source));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void onParse() throws Exception;

    public char next() throws Exception {
        if (_pushPos >= 0) {
            last = _push[_pushPos];
            _pushPos--;
        } else {
            last = (char) _reader.read();
            if (last != EOF) {
                _pos++;
                if (last == '\n') {
                    _lastcol = _col + 1;
                    _col = 0;
                    _row++;
                } else {
                    _col++;
                }
            }
        }
        return last;
    }

    public void push(char c) {
        _pushPos++;
        _push[_pushPos] = c;
    }

    public int getPos() {
        return _pos;
    }

    public int getRow() {
        if (_col == 0) {
            return _row - 1;
        } else {
            return _row;
        }
    }

    public int getCol() {
        if (_col == 0) {
            return _lastcol;
        } else {
            return _col;
        }
    }
}
