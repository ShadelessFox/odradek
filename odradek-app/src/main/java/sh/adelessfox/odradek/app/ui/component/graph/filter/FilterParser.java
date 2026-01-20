package sh.adelessfox.odradek.app.ui.component.graph.filter;

import sh.adelessfox.odradek.util.Result;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public final class FilterParser {
    private FilterParser() {
    }

    static Result<Filter, FilterError> parse(String input) {
        return tokenize(input).flatMap(FilterParser::parse);
    }

    private static Result<Filter, FilterError> parse(List<FilterToken> tokens) {
        var pending = new ArrayDeque<>(tokens);
        var result = parseInfix(pending);
        if (result.isError()) {
            return result;
        }
        FilterToken end = pending.remove();
        if (!(end instanceof FilterToken.End)) {
            return Result.error(new FilterError("Unexpected token " + end.toDisplayString(), end.offset()));
        }
        return result;
    }

    private static Result<Filter, FilterError> parseInfix(Queue<FilterToken> tokens) {
        return parsePrefix(tokens).flatMap(left -> parseInfix(left, tokens));
    }

    private static Result<Filter, FilterError> parseInfix(Filter left, Queue<FilterToken> tokens) {
        while (tokens.element() instanceof FilterToken.And || tokens.element() instanceof FilterToken.Or) {
            var op = tokens.remove();
            var right = parsePrefix(tokens);
            if (right.isError()) {
                return right;
            }
            if (op instanceof FilterToken.And) {
                left = new Filter.And(left, right.ok().orElseThrow());
            } else if (op instanceof FilterToken.Or) {
                left = new Filter.Or(left, right.ok().orElseThrow());
            } else {
                throw new IllegalStateException();
            }
        }
        return Result.ok(left);
    }

    private static Result<Filter, FilterError> parsePrefix(Queue<FilterToken> tokens) {
        if (tokens.element() instanceof FilterToken.Not) {
            tokens.remove();
            return parsePrimary(tokens).map(Filter.Not::new);
        }
        return parsePrimary(tokens);
    }

    private static Result<Filter, FilterError> parsePrimary(Queue<FilterToken> tokens) {
        var token = tokens.remove();
        return switch (token) {
            case FilterToken.Name(var key, int offset) -> {
                var colon = tokens.remove();
                if (!(colon instanceof FilterToken.Colon)) {
                    yield Result.error(new FilterError("Expected ':' after name but found " + colon.toDisplayString(), colon.offset()));
                }
                var value = tokens.remove();
                yield parseKey(key, offset, value);
            }
            case FilterToken.Open _ -> {
                var inner = parseInfix(tokens);
                if (inner.isError()) {
                    yield inner;
                }
                var close = tokens.remove();
                if (!(close instanceof FilterToken.Close)) {
                    yield Result.error(new FilterError("Expected closing parenthesis but found " + close.toDisplayString(), close.offset()));
                }
                yield inner;
            }
            case FilterToken.End _ -> Result.error(new FilterError("Unexpected end of input", token.offset()));
            default -> Result.error(new FilterError("Unexpected token " + token.toDisplayString(), token.offset()));
        };
    }

    private static Result<Filter, FilterError> parseKey(String key, int offset, FilterToken value) {
        return switch (key) {
            case "group" -> {
                if (!(value instanceof FilterToken.Number(var id, _))) {
                    yield Result.error(new FilterError("Expected group ID after ':' but found " + value.toDisplayString(), value.offset()));
                }
                yield Result.ok(new Filter.GroupId(id));
            }
            case "type" -> {
                if (!(value instanceof FilterToken.Name(var name, _))) {
                    yield Result.error(new FilterError("Expected type name after ':' but found " + value.toDisplayString(), value.offset()));
                }
                yield Result.ok(new Filter.GroupType(name));
            }
            case "has" -> {
                if (!(value instanceof FilterToken.Name(var what, _))) {
                    yield Result.error(new FilterError("Expected criteria name after ':' but found " + value.toDisplayString(), value.offset()));
                }
                yield switch (what) {
                    case "subgroups" -> Result.ok(new Filter.GroupHasSubgroups());
                    case "roots" -> Result.ok(new Filter.GroupHasRoots());
                    default -> Result.error(new FilterError("Unknown 'has' criteria '" + what + "'", value.offset()));
                };
            }
            default -> Result.error(new FilterError("Unknown criteria key '" + key + "'", offset));
        };
    }

    static Result<List<FilterToken>, FilterError> tokenize(String input) {
        var tokens = new ArrayList<FilterToken>();
        for (int offset = 0; offset < input.length(); offset++) {
            char ch = input.charAt(offset);
            if (Character.isWhitespace(ch)) {
                continue;
            }
            if (ch == '(') {
                tokens.add(new FilterToken.Open(offset));
            } else if (ch == ')') {
                tokens.add(new FilterToken.Close(offset));
            } else if (ch == '!') {
                tokens.add(new FilterToken.Not(offset));
            } else if (ch == ':') {
                tokens.add(new FilterToken.Colon(offset));
            } else if (isNameStart(ch)) {
                int start = offset;
                while (offset < input.length() - 1) {
                    if (!isNamePart(input.charAt(offset + 1))) {
                        break;
                    }
                    offset++;
                }
                var value = input.substring(start, offset + 1);
                switch (value) {
                    case "AND" -> tokens.add(new FilterToken.And(start));
                    case "OR" -> tokens.add(new FilterToken.Or(start));
                    default -> tokens.add(new FilterToken.Name(value, start));
                }
            } else if (isNumberStart(ch)) {
                int start = offset;
                while (offset < input.length() - 1) {
                    if (!isNumberPart(input.charAt(offset + 1))) {
                        break;
                    }
                    offset++;
                }
                int value;
                try {
                    value = Integer.parseInt(input.substring(start, offset + 1));
                } catch (NumberFormatException e) {
                    return Result.error(new FilterError(e.getMessage(), start));
                }
                tokens.add(new FilterToken.Number(value, start));
            } else {
                return Result.error(new FilterError("Unexpected character '" + ch + "'", offset));
            }
        }
        tokens.add(new FilterToken.End(input.length()));
        return Result.ok(List.copyOf(tokens));
    }

    private static boolean isNameStart(char ch) {
        return Character.isJavaIdentifierStart(ch);
    }

    private static boolean isNamePart(char ch) {
        return Character.isJavaIdentifierPart(ch);
    }

    private static boolean isNumberStart(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private static boolean isNumberPart(char ch) {
        return ch >= '0' && ch <= '9';
    }
}
