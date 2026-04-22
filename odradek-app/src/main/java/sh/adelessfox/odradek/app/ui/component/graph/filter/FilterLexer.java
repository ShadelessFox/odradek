package sh.adelessfox.odradek.app.ui.component.graph.filter;

import sh.adelessfox.odradek.parsing.AbstractLexer;
import sh.adelessfox.odradek.parsing.Location;
import sh.adelessfox.odradek.parsing.Source;
import sh.adelessfox.odradek.util.Result;

final class FilterLexer extends AbstractLexer<FilterToken, FilterError> {
    FilterLexer(Source source) {
        super(source);
    }

    @Override
    protected Result<FilterToken, FilterError> nextImpl() {
        source.consumeWhile(Character::isWhitespace);

        var start = location();
        int ch = source.next();

        if (ch == Source.EOF) {
            return Result.ok(new FilterToken.End(start));
        } else if (ch == '(') {
            return Result.ok(new FilterToken.Open(start));
        } else if (ch == ')') {
            return Result.ok(new FilterToken.Close(start));
        } else if (ch == ':') {
            return Result.ok(new FilterToken.Colon(start));
        } else if (isNameStart(ch)) {
            return readString(start, ch);
        } else if (isNumberStart(ch)) {
            return readNumber(start, ch);
        } else {
            return Result.error(new FilterError("Unexpected character '" + (char) ch + "'", start));
        }
    }

    private Result<FilterToken, FilterError> readNumber(Location start, int ch) {
        var buffer = new StringBuilder().appendCodePoint(ch);
        while (!source.isEof() && isNumberPart(source.peek())) {
            buffer.appendCodePoint(source.next());
        }
        try {
            int value = Integer.parseInt(buffer.toString());
            return Result.ok(new FilterToken.Number(value, start));
        } catch (NumberFormatException e) {
            return Result.error(new FilterError(e.getMessage(), start));
        }
    }

    private Result<FilterToken, FilterError> readString(Location start, int ch) {
        var buffer = new StringBuilder().appendCodePoint(ch);
        while (!source.isEof() && isNamePart(source.peek())) {
            buffer.appendCodePoint(source.next());
        }
        var value = buffer.toString();
        return switch (value) {
            case "and" -> Result.ok(new FilterToken.And(start));
            case "or" -> Result.ok(new FilterToken.Or(start));
            case "not" -> Result.ok(new FilterToken.Not(start));
            default -> Result.ok(new FilterToken.Name(value, start));
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
