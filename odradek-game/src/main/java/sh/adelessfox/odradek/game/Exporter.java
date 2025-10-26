package sh.adelessfox.odradek.game;

import sh.adelessfox.odradek.util.Reflections;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Exporter<T> {
    static Stream<Exporter<?>> exporters() {
        class Holder {
            static final List<Exporter<?>> exporters = ServiceLoader.load(Exporter.class).stream()
                .map(x -> (Exporter<?>) x.get())
                .collect(Collectors.toUnmodifiableList());
        }
        return Holder.exporters.stream();
    }

    @SuppressWarnings("unchecked")
    static <T> Stream<Exporter<T>> exporters(Class<T> clazz) {
        return exporters()
            .filter(e -> e.supportedType().isAssignableFrom(clazz))
            .map(e -> (Exporter<T>) e);
    }

    void export(T object, WritableByteChannel channel) throws IOException;

    String name();

    String extension();

    default Optional<String> icon() {
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    default Class<T> supportedType() {
        return Reflections.getGenericInterface(getClass(), Exporter.class)
            .map(iface -> (Class<T>) Reflections.getRawType(iface.getActualTypeArguments()[0]))
            .orElseThrow();
    }
}
