package sh.adelessfox.odradek.game;

import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.util.Reflections;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts game-specific resource objects to a more generic type.
 *
 * @param <G> the type of the game
 * @param <R> the result type of the conversion
 */
public interface Converter<G extends Game, R> {
    static Stream<Converter<?, ?>> converters() {
        class Holder {
            static final List<Converter<?, ?>> converters = ServiceLoader.load(Converter.class).stream()
                .map(x -> (Converter<?, ?>) x.get())
                .collect(Collectors.toUnmodifiableList());
        }
        return Holder.converters.stream();
    }

    @SuppressWarnings("unchecked")
    static <G extends Game> Stream<Converter<G, ?>> converters(TypeInfo info) {
        return Converter.converters()
            .filter(c -> c.supports(info))
            .map(c -> (Converter<G, ?>) c);
    }

    @SuppressWarnings("unchecked")
    static <G extends Game, R> Optional<Converter<G, R>> converter(TypeInfo info, Class<R> result) {
        if (result.isAssignableFrom(info.type())) {
            return Converter.noop(result);
        }
        var converters = Converter.<G>converters(info)
            .filter(c -> result.isAssignableFrom(c.resultType()))
            .toList();
        return switch (converters.size()) {
            case 0 -> Optional.empty();
            case 1 -> Optional.of((Converter<G, R>) converters.getFirst());
            default ->
                throw new IllegalStateException("Multiple converters found for " + result.getName() + ": " + converters);
        };
    }

    static <G extends Game, R> Optional<R> convert(TypeInfo info, Object object, G game, Class<R> result) {
        return Converter.converter(info, result)
            .flatMap(c -> c.convert(object, game));
    }

    static <G extends Game, T> Optional<Converter<G, T>> noop(Class<T> type) {
        return Optional.of(new Converter<>() {
            @Override
            public Optional<T> convert(Object object, G game) {
                return Optional.of(type.cast(object));
            }

            @Override
            public boolean supports(TypeInfo info) {
                return type.isAssignableFrom(info.type());
            }
        });
    }

    Optional<R> convert(Object object, G game);

    default boolean supports(TypeInfo info) {
        Class<?> type = info.type();
        return supportedTypes().stream().anyMatch(c -> c.isAssignableFrom(type));
    }

    default Set<Class<?>> supportedTypes() {
        return Set.of();
    }

    @SuppressWarnings("unchecked")
    default Class<R> resultType() {
        return Reflections.getGenericInterface(getClass(), Converter.class)
            .map(iface -> (Class<R>) Reflections.getRawType(iface.getActualTypeArguments()[1]))
            .orElseThrow();
    }
}
