package sh.adelessfox.odradek.app.util.steam.vdf;

import com.google.gson.JsonObject;
import sh.adelessfox.odradek.util.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;

public final class VdfParser {
    private VdfParser() {
    }

    public static Result<JsonObject, VdfError> parse(Path path) throws IOException {
        return parse(Files.readString(path));
    }

    public static Result<JsonObject, VdfError> parse(String input) {
        return tokenize(input).flatMap(VdfParser::parse);
    }

    static Result<JsonObject, VdfError> parse(List<VdfToken> tokens) {
        var pending = new ArrayDeque<>(tokens);
        return parseObject(pending, VdfToken.End.class::isInstance);
    }

    private static Result<?, VdfError> parseValue(Deque<VdfToken> tokens) {
        return switch (tokens.remove()) {
            case VdfToken.Text text -> Result.ok(text.value());
            case VdfToken.Open _ -> parseObject(tokens, VdfToken.Close.class::isInstance);
            default -> Result.error(new VdfError("Expected string or '{'", tokens.element().offset()));
        };
    }

    private static Result<JsonObject, VdfError> parseObject(Deque<VdfToken> tokens, Predicate<VdfToken> end) {
        var object = new JsonObject();
        while (true) {
            var next = tokens.remove();
            if (end.test(next)) {
                return Result.ok(object);
            } else if (next instanceof VdfToken.Text key) {
                var value = parseValue(tokens);
                if (value.isError()) {
                    return value.map(_ -> null);
                }
                switch (value.unwrap()) {
                    case String v -> object.addProperty(key.value(), v);
                    case JsonObject v -> object.add(key.value(), v);
                    default -> throw new IllegalStateException();
                }
            } else {
                return Result.error(new VdfError("Expected key or '}'", next.offset()));
            }
        }
    }

    static Result<List<VdfToken>, VdfError> tokenize(String input) {
        List<VdfToken> tokens = new ArrayList<>();
        for (int offset = 0; offset < input.length(); offset++) {
            char ch = input.charAt(offset);
            if (Character.isWhitespace(ch)) {
                continue;
            }
            if (ch == '{') {
                tokens.add(new VdfToken.Open(offset));
            } else if (ch == '}') {
                tokens.add(new VdfToken.Close(offset));
            } else if (ch == '"') {
                int start = offset;
                var terminated = false;
                var buf = new StringBuilder();
                while (offset++ < input.length() - 1) {
                    char next = input.charAt(offset);
                    if (next == '\\') {
                        char escape = input.charAt(++offset);
                        switch (escape) {
                            case '"' -> buf.append('"');
                            case '?' -> buf.append('?');
                            case 'b' -> buf.append('\b');
                            case 'f' -> buf.append('\f');
                            case 'n' -> buf.append('\n');
                            case 'r' -> buf.append('\r');
                            case 't' -> buf.append('\t');
                            case '\'' -> buf.append('\'');
                            case '\\' -> buf.append('\\');
                            default -> {
                                return Result.error(new VdfError("Invalid escape sequence \\" + escape, offset));
                            }
                        }
                    } else if (next == '"') {
                        terminated = true;
                        break;
                    } else {
                        buf.append(next);
                    }
                }
                if (!terminated) {
                    return Result.error(new VdfError("Unterminated string", offset));
                }
                tokens.add(new VdfToken.Text(buf.toString(), start));
            } else {
                int start = offset;
                while (offset < input.length() - 1) {
                    char next = input.charAt(offset + 1);
                    if (Character.isWhitespace(next) || next == '"') {
                        break;
                    }
                    offset++;
                }
                var value = input.substring(start, offset + 1);
                tokens.add(new VdfToken.Text(value, start));
            }
        }
        tokens.add(new VdfToken.End(input.length()));
        return Result.ok(List.copyOf(tokens));
    }
}
