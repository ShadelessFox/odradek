package sh.adelessfox.odradek.parsing;

import sh.adelessfox.odradek.util.Result;

import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractParser<T extends Token, E, L extends AbstractLexer<T, E>> {
    protected final L lexer;

    public AbstractParser(L lexer) {
        this.lexer = lexer;
    }

    protected final Result<T, E> next() {
        return lexer.next();
    }

    protected final Result<T, E> peek() {
        return lexer.peek();
    }

    protected final Result<T, E> expect(Predicate<T> predicate, Function<T, E> errorMapper) {
        var peeked = peek();
        if (peeked.isError()) {
            return peeked;
        }
        var token = peeked.unwrap();
        if (!predicate.test(token)) {
            return Result.error(errorMapper.apply(token));
        }
        return next();
    }
}
