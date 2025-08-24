package sh.adelessfox.odradek.util;

import java.util.stream.Gatherer;

/**
 * A collection of Stream API gatherers as an addition to {@link java.util.stream.Gatherers}.
 */
public final class Gatherers {
    private Gatherers() {
    }

    /**
     * Creates a gatherer that filters elements based on their type.
     *
     * @param type the class type to filter elements by
     * @param <T>  type of elements being processed
     * @param <R>  type of elements to be gathered
     * @return a gatherer that processes elements and only pushes those that are instances of the specified type
     */
    public static <T, R> Gatherer<T, ?, R> instanceOf(Class<R> type) {
        return Gatherer.of(
            (_, element, downstream) -> {
                if (type.isInstance(element)) {
                    downstream.push(type.cast(element));
                }
                return true;
            }
        );
    }
}
