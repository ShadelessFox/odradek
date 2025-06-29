package sh.adelessfox.odradek.game;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Converts game-specific resource objects to a more generic type.
 *
 * @param <T> the type of the game
 * @param <R> the result type of the conversion
 */
public interface Converter<T extends Game, R> {
    @SuppressWarnings("unchecked")
    static <T extends Game> Stream<Converter<T, ?>> converters() {
        return ServiceLoader.load(Converter.class).stream().map(x -> (Converter<T, ?>) x.get());
    }

    static <T extends Game> Stream<Converter<T, ?>> converters(Class<?> clazz) {
        return Converter.<T>converters()
            .filter(c -> c.convertibleTypes().stream().anyMatch(type -> type.isAssignableFrom(clazz)));
    }

    static <T extends Game> Stream<Converter<T, ?>> converters(Object object) {
        return converters(object.getClass());
    }

    static <T extends Game> Optional<Converter<T, ?>> converter(Object object, Class<?> clazz) {
        var converters = Converter.<T>converters(object)
            .filter(c -> clazz.isAssignableFrom(c.resultType()))
            .toList();
        return switch (converters.size()) {
            case 0 -> Optional.empty();
            case 1 -> Optional.of(converters.getFirst());
            default -> throw new IllegalStateException("Multiple converters found for " + clazz.getName() + ": " + converters);
        };
    }

    static <T extends Game, R> Optional<R> convert(Object object, T game, Class<R> clazz) {
        return Converter.<T>converter(object, clazz)
            .flatMap(c -> c.convert(object, game).map(clazz::cast));
    }

    Optional<R> convert(Object object, T game);

    Set<Class<?>> convertibleTypes();

    @SuppressWarnings("unchecked")
    default Class<R> resultType() {
        return Stream.of(getClass().getGenericInterfaces())
            .map(ParameterizedType.class::cast)
            .filter(type -> type.getRawType() == Converter.class)
            .map(type -> (Class<R>) type.getActualTypeArguments()[1])
            .findFirst().orElseThrow();
    }
}
