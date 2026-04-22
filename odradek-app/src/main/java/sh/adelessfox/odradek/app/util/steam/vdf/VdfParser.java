package sh.adelessfox.odradek.app.util.steam.vdf;

import com.google.gson.JsonObject;
import sh.adelessfox.odradek.parsing.AbstractParser;
import sh.adelessfox.odradek.parsing.util.ReaderSource;
import sh.adelessfox.odradek.util.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

public final class VdfParser extends AbstractParser<VdfToken, VdfError, VdfLexer> {
    private VdfParser(VdfLexer lexer) {
        super(lexer);
    }

    public static Result<JsonObject, VdfError> parse(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return new VdfParser(new VdfLexer(new ReaderSource(reader))).parse();
        }
    }

    Result<JsonObject, VdfError> parse() {
        return parseObject(VdfToken.End.class::isInstance);
    }

    private Result<?, VdfError> parseValue() {
        return switch (next()) {
            case Result.Error<?, VdfError> error -> error;
            case Result.Ok<VdfToken, VdfError>(var token) -> switch (token) {
                case VdfToken.Text text -> Result.ok(text.value());
                case VdfToken.Open _ -> parseObject(VdfToken.Close.class::isInstance);
                default -> Result.error(new VdfError("Expected string or '{'", token.location()));
            };
        };
    }

    private Result<JsonObject, VdfError> parseObject(Predicate<VdfToken> end) {
        var object = new JsonObject();
        while (true) {
            switch (next()) {
                case Result.Error<?, VdfError> error -> {
                    return error.map(_ -> null);
                }
                case Result.Ok<VdfToken, VdfError>(var token) when end.test(token) -> {
                    return Result.ok(object);
                }
                case Result.Ok<VdfToken, VdfError>(var token) when token instanceof VdfToken.Text key -> {
                    var value = parseValue();
                    if (value.isError()) {
                        return value.map(_ -> null);
                    }
                    switch (value.unwrap()) {
                        case String v -> object.addProperty(key.value(), v);
                        case JsonObject v -> object.add(key.value(), v);
                        default -> throw new IllegalStateException();
                    }
                }
                case Result.Ok<VdfToken, VdfError>(var token) -> {
                    return Result.error(new VdfError("Expected key or '}'", token.location()));
                }
            }
        }
    }
}
