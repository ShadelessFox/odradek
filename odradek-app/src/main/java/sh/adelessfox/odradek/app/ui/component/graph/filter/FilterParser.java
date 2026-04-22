package sh.adelessfox.odradek.app.ui.component.graph.filter;

import sh.adelessfox.odradek.parsing.AbstractParser;
import sh.adelessfox.odradek.parsing.Location;
import sh.adelessfox.odradek.util.Result;

final class FilterParser extends AbstractParser<FilterToken, FilterError, FilterLexer> {
    FilterParser(FilterLexer lexer) {
        super(lexer);
    }

    Result<Filter, FilterError> parse() {
        var result = parseInfix();
        if (result.isError()) {
            return result;
        }
        return switch (next()) {
            case Result.Ok(var tok) when tok instanceof FilterToken.End -> result;
            case Result.Ok(var tok) ->
                Result.error(new FilterError("Unexpected token " + tok.toDisplayString(), tok.location()));
            case Result.Error<?, FilterError> error -> error.map(_ -> null);
        };
    }

    private Result<Filter, FilterError> parseInfix() {
        return parseInfix(0);
    }

    private Result<Filter, FilterError> parseInfix(int rbp) {
        var left = parsePrefix();
        if (left.isError()) {
            return left;
        }

        var result = left.unwrap();
        while (true) {
            var op = peek();
            if (op.isError()) {
                return op.map(_ -> null);
            }
            if (!(op.unwrap() instanceof FilterToken.Infix infix)) {
                break;
            }

            int lbp = infix.precedence();
            if (lbp < rbp) {
                break;
            }

            next();

            var right = parseInfix(lbp);
            if (right.isError()) {
                return right;
            }

            result = switch (infix) {
                case FilterToken.And _ -> new Filter.And(result, right.unwrap());
                case FilterToken.Or _ -> new Filter.Or(result, right.unwrap());
            };
        }

        return Result.ok(result);
    }

    private Result<Filter, FilterError> parsePrefix() {
        var peek = peek();
        if (peek.isError()) {
            return peek.map(_ -> null);
        }

        if (peek.unwrap() instanceof FilterToken.Prefix prefix) {
            next();

            var right = parseInfix(prefix.precedence());
            if (right.isError()) {
                return right;
            }

            return switch (prefix) {
                case FilterToken.Not _ -> right.map(Filter.Not::new);
            };
        }

        return parsePrimary();
    }

    private Result<Filter, FilterError> parsePrimary() {
        var token = next();
        if (token.isError()) {
            return token.map(_ -> null);
        }
        return switch (token.unwrap()) {
            // [key] ':' [value]
            case FilterToken.Name(var key, var location) -> {
                var colon = expect(
                    FilterToken.Colon.class::isInstance,
                    t -> new FilterError("Expected ':' after name but found " + t.toDisplayString(), t.location()));
                if (colon.isError()) {
                    yield colon.map(_ -> null);
                }
                var value = next();
                if (value.isError()) {
                    yield value.map(_ -> null);
                }
                yield parseKey(key, location, value.unwrap());
            }
            // '(' [expr] ')'
            case FilterToken.Open _ -> {
                var inner = parseInfix();
                if (inner.isError()) {
                    yield inner;
                }
                var close = expect(
                    FilterToken.Close.class::isInstance,
                    t -> new FilterError("Expected closing parenthesis but found " + t.toDisplayString(), t.location()));
                if (close.isError()) {
                    yield close.map(_ -> null);
                }
                yield inner;
            }
            case FilterToken.End t -> Result.error(new FilterError("Unexpected end of input", t.location()));
            case FilterToken t ->
                Result.error(new FilterError("Unexpected token " + t.toDisplayString(), t.location()));
        };
    }

    private Result<Filter, FilterError> parseKey(String key, Location location, FilterToken value) {
        return switch (key) {
            case "group" -> {
                if (!(value instanceof FilterToken.Number(var id, _))) {
                    yield Result.error(new FilterError(
                        "Expected group ID after ':' but found " + value.toDisplayString(),
                        value.location()));
                }
                yield Result.ok(new Filter.GroupId(id));
            }
            case "type" -> {
                if (!(value instanceof FilterToken.Name(var name, _))) {
                    yield Result.error(new FilterError(
                        "Expected type name after ':' but found " + value.toDisplayString(),
                        value.location()));
                }
                yield Result.ok(new Filter.GroupType(name));
            }
            case "has" -> {
                if (!(value instanceof FilterToken.Name(var what, _))) {
                    yield Result.error(new FilterError(
                        "Expected criteria name after ':' but found " + value.toDisplayString(),
                        value.location()));
                }
                yield switch (what) {
                    case "subgroups" -> Result.ok(new Filter.GroupHasSubgroups());
                    case "supergroups" -> Result.ok(new Filter.GroupHasSupergroups());
                    case "roots" -> Result.ok(new Filter.GroupHasRoots());
                    default -> Result.error(new FilterError("Unknown 'has' criteria '" + what + "'", value.location()));
                };
            }
            default -> Result.error(new FilterError("Unknown criteria key '" + key + "'", location));
        };
    }
}
