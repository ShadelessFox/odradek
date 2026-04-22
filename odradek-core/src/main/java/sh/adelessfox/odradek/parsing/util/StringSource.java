package sh.adelessfox.odradek.parsing.util;

import sh.adelessfox.odradek.parsing.Source;

public final class StringSource implements Source {
    private final String string;
    private int position;
    private int line;
    private int column;

    public StringSource(String string) {
        this.string = string;
    }

    @Override
    public int peek() {
        if (position >= string.length()) {
            return EOF;
        }
        return string.codePointAt(position);
    }

    @Override
    public int next() {
        if (position >= string.length()) {
            return EOF;
        }
        int result = string.codePointAt(position);
        if (result == '\n') {
            line++;
            column = 0;
        } else {
            column++;
        }
        position += Character.charCount(result);
        return result;
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public int column() {
        return column;
    }
}
