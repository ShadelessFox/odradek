package sh.adelessfox.odradek.app.util.steam.vdf;

import sh.adelessfox.odradek.parsing.AbstractParser;
import sh.adelessfox.odradek.parsing.SyntaxException;
import sh.adelessfox.odradek.parsing.util.ReaderSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

public final class VdfParser extends AbstractParser<VdfToken, VdfLexer> {
    private VdfParser(VdfLexer lexer) {
        super(lexer);
    }

    public static VdfObject parse(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return new VdfParser(new VdfLexer(new ReaderSource(reader))).parse();
        }
    }

    VdfObject parse() {
        return parseObject(VdfToken.End.class::isInstance);
    }

    private Object parseValue() {
        return switch (next()) {
            case VdfToken.Text text -> text.value();
            case VdfToken.Open _ -> parseObject(VdfToken.Close.class::isInstance);
            case VdfToken token -> throw new SyntaxException("Expected string or '{'", token.location());
        };
    }

    private VdfObject parseObject(Predicate<VdfToken> end) {
        var object = new VdfObject();
        while (true) {
            var token = next();
            if (end.test(token)) {
                return object;
            }
            if (token instanceof VdfToken.Text key) {
                switch (parseValue()) {
                    case String v -> object.add(key.value(), new VdfString(v));
                    case VdfObject v -> object.add(key.value(), v);
                    default -> throw new IllegalStateException();
                }
            } else {
                throw new SyntaxException("Expected key or '}'", token.location());
            }
        }
    }
}
