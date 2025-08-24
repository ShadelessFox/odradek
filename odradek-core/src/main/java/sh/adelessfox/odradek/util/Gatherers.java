package sh.adelessfox.odradek.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
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

    public static <T, K> Gatherer<T, ?, Map.Entry<K, List<T>>> groupingBy(
        Function<? super T, ? extends K> classifier
    ) {
        return groupingBy(classifier, Collectors.toList());
    }

    public static <T, K, A, D> Gatherer<T, ?, Map.Entry<K, D>> groupingBy(
        Function<? super T, ? extends K> classifier,
        Collector<? super T, A, D> downstreamCollector
    ) {
        return groupingBy(classifier, HashMap::new, downstreamCollector);
    }

    public static <T, K, A, D> Gatherer<T, ?, Map.Entry<K, D>> groupingBy(
        Function<? super T, ? extends K> classifier,
        Supplier<Map<K, A>> mapFactory,
        Collector<? super T, A, D> downstreamCollector
    ) {
        return Gatherer.ofSequential(
            mapFactory,
            (state, element, _) -> {
                var supplier = downstreamCollector.supplier();
                var accumulator = downstreamCollector.accumulator();
                var container = state.computeIfAbsent(classifier.apply(element), _ -> supplier.get());
                accumulator.accept(container, element);
                return true;
            },
            (state, downstream) -> {
                var finisher = downstreamCollector.finisher();
                state.entrySet().stream()
                    .map(entry -> Map.entry(entry.getKey(), finisher.apply(entry.getValue())))
                    .forEach(downstream::push);
            }
        );
    }
}
