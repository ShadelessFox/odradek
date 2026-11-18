package sh.adelessfox.odradek.parsing;

import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractParser<T extends Token, L extends AbstractLexer<T>> {
    protected final L lexer;

    public AbstractParser(L lexer) {
        this.lexer = lexer;
    }

    protected final T next() {
        return lexer.next();
    }

    protected final T peek() {
        return lexer.peek();
    }

    protected final T expect(Predicate<T> predicate, Function<T, SyntaxException> errorMapper) {
        var token = peek();
        if (!predicate.test(token)) {
            throw errorMapper.apply(token);
        }
        return next();
    }
}
