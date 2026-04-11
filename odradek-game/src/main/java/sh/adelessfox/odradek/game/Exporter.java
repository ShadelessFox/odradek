package sh.adelessfox.odradek.game;

import sh.adelessfox.odradek.util.Reflections;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public sealed interface Exporter<T> {
    non-sealed interface OfSingleOutput<T> extends Exporter<T> {
        void export(T object, WritableByteChannel channel) throws IOException;

        String extension();

        @Override
        default Class<T> supportedType() {
            return Reflections.getGenericInterfaceArgument(getClass(), OfSingleOutput.class, 0);
        }
    }

    non-sealed interface OfMultipleOutputs<T> extends Exporter<T> {
        void export(T object, OutputProvider provider) throws IOException;

        interface OutputProvider {
            /**
             * Opens a channel for writing an output with the given name.
             * <p>
             * Calling this method multiple times with the same name is guaranteed to return the same channel.
             *
             * @param name the name of the output, e.g. "diffuse" or "normal"
             * @return a channel for writing the output data
             * @throws IOException              if an I/O error occurs while opening the channel
             * @throws IllegalArgumentException if the name represents an invalid path or the path
             *                                  resolves to a location outside the export directory
             */
            WritableByteChannel channel(String name) throws IOException;
        }

        @Override
        default Class<T> supportedType() {
            return Reflections.getGenericInterfaceArgument(getClass(), OfMultipleOutputs.class, 0);
        }
    }

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

    static Optional<Exporter<?>> exporter(String id) {
        return exporters()
            .filter(e -> e.id().equals(id))
            .findFirst();
    }

    void export(T object, WritableByteChannel channel) throws IOException;

    /**
     * A unique identifier of this exporter.
     * <p>
     * Can contain an optional namespace, e.g. {@code audio.wwise} or {@code image.dds};
     * currently only used for grouping exporters in the UI.
     */
    String id();

    default Optional<String> namespace() {
        var id = id();
        var dot = id.indexOf('.');
        if (dot < 0) {
            return Optional.empty();
        }
        return Optional.of(id.substring(0, dot));
    }

    String name();

    String extension();

    default Optional<String> icon() {
        return Optional.empty();
    }

    Class<T> supportedType();
}
