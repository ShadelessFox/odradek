package sh.adelessfox.odradek.export;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.nio.channels.WritableByteChannel;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public interface Exporter<T> {
    static Stream<Exporter<?>> exporters() {
        return ServiceLoader.load(Exporter.class).stream().map(x -> (Exporter<?>) x.get());
    }

    @SuppressWarnings("unchecked")
    static <T> Stream<Exporter<T>> exporters(Class<T> clazz) {
        return exporters()
            .filter(v -> v.supportedType().isAssignableFrom(clazz))
            .map(v -> (Exporter<T>) v);
    }

    void export(T object, WritableByteChannel channel) throws IOException;

    String name();

    String extension();

    @SuppressWarnings("unchecked")
    default Class<T> supportedType() {
        return Stream.of(getClass().getGenericInterfaces())
            .map(ParameterizedType.class::cast)
            .filter(type -> type.getRawType() == Exporter.class)
            .map(type -> (Class<T>) type.getActualTypeArguments()[0])
            .findFirst().orElseThrow();
    }
}
