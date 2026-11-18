package sh.adelessfox.odradek.app.ui.tools.graph.filter;

import sh.adelessfox.odradek.parsing.AbstractLexer;
import sh.adelessfox.odradek.parsing.Location;
import sh.adelessfox.odradek.parsing.Source;
import sh.adelessfox.odradek.parsing.SyntaxException;

final class FilterLexer extends AbstractLexer<FilterToken> {
    FilterLexer(Source source) {
        super(source);
    }

    @Override
    protected FilterToken nextImpl() {
        source.consumeWhile(Character::isWhitespace);

        var start = location();
        int ch = source.next();

        if (ch == Source.EOF) {
            return new FilterToken.End(start);
        } else if (ch == '(') {
            return new FilterToken.Open(start);
        } else if (ch == ')') {
            return new FilterToken.Close(start);
        } else if (ch == ':') {
            return new FilterToken.Colon(start);
        } else if (isNameStart(ch)) {
            return readString(start, ch);
        } else if (isNumberStart(ch)) {
            return readNumber(start, ch);
        } else {
            throw new SyntaxException("Unexpected character '" + (char) ch + "'", start);
        }
    }

    private FilterToken readNumber(Location start, int ch) {
        var buffer = new StringBuilder().appendCodePoint(ch);
        while (!source.isEof() && isNumberPart(source.peek())) {
            buffer.appendCodePoint(source.next());
        }
        try {
            int value = Integer.parseInt(buffer.toString());
            return new FilterToken.Number(value, start);
        } catch (NumberFormatException e) {
            throw new SyntaxException(e.getMessage(), e, start);
        }
    }

    private FilterToken readString(Location start, int ch) {
        var buffer = new StringBuilder().appendCodePoint(ch);
        while (!source.isEof() && isNamePart(source.peek())) {
            buffer.appendCodePoint(source.next());
        }
        var value = buffer.toString();
        return switch (value) {
            case "and" -> new FilterToken.And(start);
            case "or" -> new FilterToken.Or(start);
            case "not" -> new FilterToken.Not(start);
            default -> new FilterToken.Name(value, start);
        };
    }

    private static boolean isNameStart(int ch) {
        return Character.isJavaIdentifierStart(ch);
    }

    private static boolean isNamePart(int ch) {
        return Character.isJavaIdentifierPart(ch);
    }

    private static boolean isNumberStart(int ch) {
        return ch >= '0' && ch <= '9';
    }

    private static boolean isNumberPart(int ch) {
        return ch >= '0' && ch <= '9';
    }
}
