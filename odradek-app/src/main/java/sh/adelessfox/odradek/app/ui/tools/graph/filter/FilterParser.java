package sh.adelessfox.odradek.app.ui.tools.graph.filter;

import sh.adelessfox.odradek.parsing.AbstractParser;
import sh.adelessfox.odradek.parsing.Location;
import sh.adelessfox.odradek.parsing.SyntaxException;

final class FilterParser extends AbstractParser<FilterToken, FilterLexer> {
    FilterParser(FilterLexer lexer) {
        super(lexer);
    }

    Filter parse() {
        var result = parseInfix();
        return switch (next()) {
            case FilterToken.End _ -> result;
            case FilterToken t -> throw new SyntaxException("Unexpected token " + t.toDisplayString(), t.location());
        };
    }

    private Filter parseInfix() {
        return parseInfix(0);
    }

    private Filter parseInfix(int rbp) {
        var result = parsePrefix();
        while (true) {
            var op = peek();
            if (!(op instanceof FilterToken.Infix infix)) {
                break;
            }

            int lbp = infix.precedence();
            if (lbp < rbp) {
                break;
            }

            next();
            var right = parseInfix(lbp);

            result = switch (infix) {
                case FilterToken.And _ -> new Filter.And(result, right);
                case FilterToken.Or _ -> new Filter.Or(result, right);
            };
        }

        return result;
    }

    private Filter parsePrefix() {
        if (peek() instanceof FilterToken.Prefix prefix) {
            next();
            var right = parseInfix(prefix.precedence());

            return switch (prefix) {
                case FilterToken.Not _ -> new Filter.Not(right);
            };
        }

        return parsePrimary();
    }

    private Filter parsePrimary() {
        return switch (next()) {
            // [key] ':' [value]
            case FilterToken.Name(var key, var location) -> {
                expect(
                    FilterToken.Colon.class::isInstance,
                    t -> new SyntaxException("Expected ':' after name but found " + t.toDisplayString(), t.location()));
                yield parseKey(key, location, next());
            }
            // '(' [expr] ')'
            case FilterToken.Open _ -> {
                var inner = parseInfix();
                expect(
                    FilterToken.Close.class::isInstance,
                    t -> new SyntaxException("Expected closing parenthesis but found " + t.toDisplayString(), t.location()));
                yield inner;
            }
            case FilterToken.End t -> throw new SyntaxException("Unexpected end of input", t.location());
            case FilterToken t -> throw new SyntaxException("Unexpected token " + t.toDisplayString(), t.location());
        };
    }

    private Filter parseKey(String key, Location location, FilterToken value) {
        return switch (key) {
            case "group" -> {
                if (!(value instanceof FilterToken.Number(var id, _))) {
                    throw new SyntaxException(
                        "Expected group ID after ':' but found " + value.toDisplayString(),
                        value.location());
                }
                yield new Filter.GroupId(id);
            }
            case "type" -> {
                if (!(value instanceof FilterToken.Name(var name, _))) {
                    throw new SyntaxException(
                        "Expected type name after ':' but found " + value.toDisplayString(),
                        value.location());
                }
                yield new Filter.GroupType(name);
            }
            case "has" -> {
                if (!(value instanceof FilterToken.Name(var what, _))) {
                    throw new SyntaxException(
                        "Expected criteria name after ':' but found " + value.toDisplayString(),
                        value.location());
                }
                yield switch (what) {
                    case "subgroups" -> new Filter.GroupHasSubgroups();
                    case "supergroups" -> new Filter.GroupHasSupergroups();
                    case "roots" -> new Filter.GroupHasRoots();
                    default -> throw new SyntaxException("Unknown 'has' criteria '" + what + "'", value.location());
                };
            }
            default -> throw new SyntaxException("Unknown criteria key '" + key + "'", location);
        };
    }
}
