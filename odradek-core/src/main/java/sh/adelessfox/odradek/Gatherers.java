package sh.adelessfox.odradek;

import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Gatherer;

/**
 * A collection of Stream API gatherers as an addition to {@link java.util.stream.Gatherers}.
 */
public final class Gatherers {
    private Gatherers() {
    }

    /**
     * Creates a gatherer that ensures distinct elements based on a key extracted from each element.
     *
     * @param keyExtractor      a function that extracts a key from an element
     * @param exceptionSupplier a function that provides an exception to throw if a duplicate key is found
     * @param <T>               type of elements being processed
     * @param <K>               type of keys extracted from elements
     * @return a gatherer that processes elements sequentially, ensuring distinct keys
     */
    public static <T, K> Gatherer<T, ?, T> ensureDistinct(
        Function<? super T, K> keyExtractor,
        Function<? super T, ? extends RuntimeException> exceptionSupplier
    ) {
        return Gatherer.ofSequential(
            () -> new HashSet<K>(),
            (state, element, downstream) -> {
                if (state.add(keyExtractor.apply(element))) {
                    downstream.push(element);
                    return true;
                } else {
                    throw exceptionSupplier.apply(element);
                }
            }
        );
    }
}
