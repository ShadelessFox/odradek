package sh.adelessfox.odradek.app.util.steam.vdf;

import sh.adelessfox.odradek.parsing.AbstractLexer;
import sh.adelessfox.odradek.parsing.Location;
import sh.adelessfox.odradek.parsing.Source;
import sh.adelessfox.odradek.util.Result;

final class VdfLexer extends AbstractLexer<VdfToken, VdfError> {
    VdfLexer(Source source) {
        super(source);
    }

    @Override
    protected Result<VdfToken, VdfError> nextImpl() {
        source.consumeWhile(Character::isWhitespace);

        var start = location();
        int ch = source.next();

        if (ch == Source.EOF) {
            return Result.ok(new VdfToken.End(start));
        } else if (ch == '{') {
            return Result.ok(new VdfToken.Open(start));
        } else if (ch == '}') {
            return Result.ok(new VdfToken.Close(start));
        } else if (ch == '"') {
            return readQuotedString();
        } else {
            return readRawString(start, ch);
        }
    }

    private Result<VdfToken, VdfError> readQuotedString() {
        var start = location();
        var buffer = new StringBuilder();
        while (!source.isEof()) {
            int ch = source.next();
            if (ch == '\\') {
                int escape = source.next();
                switch (escape) {
                    case '"' -> buffer.append('"');
                    case '?' -> buffer.append('?');
                    case 'b' -> buffer.append('\b');
                    case 'f' -> buffer.append('\f');
                    case 'n' -> buffer.append('\n');
                    case 'r' -> buffer.append('\r');
                    case 't' -> buffer.append('\t');
                    case '\'' -> buffer.append('\'');
                    case '\\' -> buffer.append('\\');
                    default -> {
                        return Result.error(new VdfError("Invalid escape sequence \\" + (char) escape, location()));
                    }
                }
            } else if (ch == '"') {
                return Result.ok(new VdfToken.Text(buffer.toString(), start));
            } else {
                buffer.append((char) ch);
            }
        }
        return Result.error(new VdfError("Unterminated string", location()));
    }

    private Result<VdfToken, VdfError> readRawString(Location start, int ch) {
        var buffer = new StringBuilder().appendCodePoint(ch);
        while (!source.isEof()) {
            int next = source.peek();
            if (Character.isWhitespace(next) || next == '"') {
                break;
            }
            buffer.appendCodePoint(source.next());
        }
        return Result.ok(new VdfToken.Text(buffer.toString(), start));
    }
}
