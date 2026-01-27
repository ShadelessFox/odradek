package sh.adelessfox.odradek.game;

import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;
import sh.adelessfox.odradek.util.Reflections;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts game-specific resource objects to a more generic type.
 *
 * @param <T> the input object type of the conversion
 * @param <R> the result type of the conversion
 * @param <G> the type of the game
 */
public interface Converter<T, R, G extends Game> {
    static Stream<Converter<?, ?, ?>> converters() {
        class Holder {
            static final List<Converter<?, ?, ?>> converters = ServiceLoader.load(Converter.class).stream()
                .map(x -> (Converter<?, ?, ?>) x.get())
                .collect(Collectors.toUnmodifiableList());
        }
        return Holder.converters.stream();
    }

    @SuppressWarnings("unchecked")
    static <T, R, G extends Game> Stream<Converter<T, R, G>> converters(TypeInfo info) {
        var registered = Converter.converters();
        var noop = Stream.of(noop(info.type()));

        return Stream.concat(registered, noop)
            .filter(c -> c.supports(info))
            .map(c -> (Converter<T, R, G>) c);
    }

    @SuppressWarnings("unchecked")
    static <T, R, G extends Game> Optional<Converter<T, R, G>> converter(TypeInfo info, Class<R> result) {
        if (result.isAssignableFrom(info.type())) {
            return Optional.of((Converter<T, R, G>) Converter.noop(result));
        }
        var converters = Converter.converters(info)
            .filter(c -> result.isAssignableFrom(c.outputType()))
            .toList();
        return switch (converters.size()) {
            case 0 -> Optional.empty();
            case 1 -> Optional.of((Converter<T, R, G>) converters.getFirst());
            default ->
                throw new IllegalStateException("Multiple converters found for " + result.getName() + ": " + converters);
        };
    }

    static <T, R, G extends Game> Optional<R> convert(TypeInfo info, T object, Class<R> result, G game) {
        return Converter.converter(info, result)
            .flatMap(c -> c.convert(object, game));
    }

    static <T extends TypedObject, R, G extends Game> Optional<R> convert(T object, Class<R> result, G game) {
        return Converter.converter(object.getType(), result)
            .flatMap(c -> c.convert(object, game));
    }

    static <T, G extends Game> Converter<T, T, G> noop(Class<T> type) {
        return new Converter<>() {
            @Override
            public Optional<T> convert(T object, G game) {
                return Optional.of(type.cast(object));
            }

            @Override
            public Class<T> inputType() {
                return type;
            }

            @Override
            public Class<T> outputType() {
                return type;
            }
        };
    }

    Optional<R> convert(T object, G game);

    default boolean supports(TypeInfo info) {
        return inputType().isAssignableFrom(info.type());
    }

    @SuppressWarnings("unchecked")
    default Class<T> inputType() {
        return Reflections.getGenericInterface(getClass(), Converter.class)
            .map(iface -> (Class<T>) Reflections.getRawType(iface.getActualTypeArguments()[0]))
            .orElseThrow();
    }

    @SuppressWarnings("unchecked")
    default Class<R> outputType() {
        return Reflections.getGenericInterface(getClass(), Converter.class)
            .map(iface -> (Class<R>) Reflections.getRawType(iface.getActualTypeArguments()[1]))
            .orElseThrow();
    }
}
