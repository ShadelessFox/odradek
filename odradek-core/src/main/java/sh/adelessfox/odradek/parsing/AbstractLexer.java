package sh.adelessfox.odradek.parsing;

public abstract class AbstractLexer<T extends Token> {
    protected Source source;
    protected T peeked;

    public AbstractLexer(Source source) {
        this.source = source;
    }

    public final T peek() {
        if (peeked == null) {
            peeked = nextImpl();
        }
        return peeked;
    }

    public final T next() {
        if (peeked == null) {
            return nextImpl();
        }
        var result = peeked;
        peeked = null;
        return result;
    }

    public final Location location() {
        return new Location(source.line(), source.column());
    }

    protected abstract T nextImpl();
}
