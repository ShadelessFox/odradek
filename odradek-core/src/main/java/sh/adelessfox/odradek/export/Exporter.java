package sh.adelessfox.odradek.export;

import sh.adelessfox.odradek.Reflections;

import java.io.IOException;
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
        return Reflections.getGenericInterface(getClass(), Exporter.class)
            .map(iface -> (Class<T>) Reflections.getRawType(iface.getActualTypeArguments()[0]))
            .orElseThrow();
    }
}
