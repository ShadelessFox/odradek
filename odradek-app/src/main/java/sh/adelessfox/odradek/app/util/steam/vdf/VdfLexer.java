package sh.adelessfox.odradek.app.util.steam.vdf;

import sh.adelessfox.odradek.parsing.AbstractLexer;
import sh.adelessfox.odradek.parsing.Location;
import sh.adelessfox.odradek.parsing.Source;
import sh.adelessfox.odradek.parsing.SyntaxException;

final class VdfLexer extends AbstractLexer<VdfToken> {
    VdfLexer(Source source) {
        super(source);
    }

    @Override
    protected VdfToken nextImpl() {
        source.consumeWhile(Character::isWhitespace);

        var start = location();
        int ch = source.next();

        if (ch == Source.EOF) {
            return new VdfToken.End(start);
        } else if (ch == '{') {
            return new VdfToken.Open(start);
        } else if (ch == '}') {
            return new VdfToken.Close(start);
        } else if (ch == '"') {
            return readQuotedString();
        } else {
            return readRawString(start, ch);
        }
    }

    private VdfToken readQuotedString() {
        var start = location();
        var buffer = new StringBuilder();
        while (!source.isEof()) {
            int ch = source.next();
            if (ch == '\\') {
                int escape = source.next();
                switch (escape) {
                    case 'n' -> buffer.append('\n');
                    case 't' -> buffer.append('\t');
                    case '\\' -> buffer.append('\\');
                    case '\"' -> buffer.append('\"');
                    default -> throw new SyntaxException("Invalid escape sequence \\" + (char) escape, location());
                }
            } else if (ch == '"') {
                return new VdfToken.Text(buffer.toString(), start);
            } else {
                buffer.append((char) ch);
            }
        }
        throw new SyntaxException("Unterminated string", location());
    }

    private VdfToken readRawString(Location start, int ch) {
        var buffer = new StringBuilder().appendCodePoint(ch);
        while (!source.isEof()) {
            int next = source.peek();
            if (Character.isWhitespace(next) || next == '"' || next == '{' || next == '}') {
                break;
            }
            buffer.appendCodePoint(source.next());
        }
        return new VdfToken.Text(buffer.toString(), start);
    }
}
