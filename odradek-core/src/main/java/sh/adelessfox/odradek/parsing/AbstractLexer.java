package sh.adelessfox.odradek.parsing;

import sh.adelessfox.odradek.util.Result;

public abstract class AbstractLexer<T extends Token, E> {
    protected Source source;
    protected T peeked;

    public AbstractLexer(Source source) {
        this.source = source;
    }

    public final Result<T, E> peek() {
        if (peeked == null) {
            var result = nextImpl();
            if (result.isError()) {
                return result;
            }
            peeked = result.unwrap();
        }
        return Result.ok(peeked);
    }

    public final Result<T, E> next() {
        if (peeked == null) {
            return nextImpl();
        }
        var result = peeked;
        peeked = null;
        return Result.ok(result);
    }

    public final Location location() {
        return new Location(source.line(), source.column());
    }

    protected abstract Result<T, E> nextImpl();
}
