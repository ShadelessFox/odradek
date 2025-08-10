package sh.adelessfox.odradek.game;

import sh.adelessfox.odradek.util.Reflections;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Converts game-specific resource objects to a more generic type.
 *
 * @param <G> the type of the game
 * @param <R> the result type of the conversion
 */
public interface Converter<G extends Game, R> {
    static Stream<Converter<?, ?>> converters() {
        return ServiceLoader.load(Converter.class).stream().map(x -> (Converter<?, ?>) x.get());
    }

    @SuppressWarnings("unchecked")
    static <G extends Game> Stream<Converter<G, ?>> converters(Class<?> clazz) {
        return Converter.converters()
            .filter(c -> c.convertibleTypes().stream().anyMatch(type -> type.isAssignableFrom(clazz)))
            .map(c -> (Converter<G, ?>) c);
    }

    static <G extends Game> Stream<Converter<G, ?>> converters(Object object) {
        return converters(object.getClass());
    }

    static <G extends Game> Optional<Converter<G, ?>> converter(Object object, Class<?> clazz) {
        var converters = Converter.<G>converters(object)
            .filter(c -> clazz.isAssignableFrom(c.resultType()))
            .toList();
        return switch (converters.size()) {
            case 0 -> Optional.empty();
            case 1 -> Optional.of(converters.getFirst());
            default -> throw new IllegalStateException("Multiple converters found for " + clazz.getName() + ": " + converters);
        };
    }

    static <G extends Game, R> Optional<R> convert(Object object, G game, Class<R> clazz) {
        return Converter.<G>converter(object, clazz)
            .flatMap(c -> c.convert(object, game).map(clazz::cast));
    }

    Optional<R> convert(Object object, G game);

    Set<Class<?>> convertibleTypes();

    @SuppressWarnings("unchecked")
    default Class<R> resultType() {
        return Reflections.getGenericInterface(getClass(), Converter.class)
            .map(iface -> (Class<R>) Reflections.getRawType(iface.getActualTypeArguments()[1]))
            .orElseThrow();
    }
}
