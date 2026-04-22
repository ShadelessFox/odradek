package sh.adelessfox.odradek.parsing;

import java.util.function.IntPredicate;

public interface Source {
    int EOF = -1;

    int peek();

    int next();

    default boolean isEof() {
        return peek() == EOF;
    }

    default void consumeWhile(IntPredicate predicate) {
        while (!isEof() && predicate.test(peek())) {
            next();
        }
    }

    int line();

    int column();
}
