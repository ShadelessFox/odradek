package sh.adelessfox.odradek.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.util.Reflections;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.*;

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

        /**
         * A default implementation of the {@link OutputProvider} that writes outputs to files in a specified directory.
         */
        final class DefaultOutputProvider implements OutputProvider, AutoCloseable {
            private final Logger log = LoggerFactory.getLogger(Exporter.class);

            private final Path root;
            private final Map<Path, WritableByteChannel> channels = new HashMap<>();

            public DefaultOutputProvider(Path root) {
                this.root = root;
            }

            @Override
            public WritableByteChannel channel(String name) throws IOException {
                var path = root.resolve(name).toAbsolutePath();
                if (!path.startsWith(root.toAbsolutePath())) {
                    throw new IllegalArgumentException("Output path must be within the export directory");
                }
                var channel = channels.get(path);
                if (channel == null) {
                    Files.createDirectories(path.getParent());
                    channel = Files.newByteChannel(path, WRITE, CREATE, TRUNCATE_EXISTING);
                    channels.put(path, channel);
                    log.debug("Opened channel for output '{}': {}", name, path);
                }
                return channel;
            }

            @Override
            public void close() {
                channels.forEach((path, channel) -> {
                    try {
                        channel.close();
                    } catch (IOException ex) {
                        log.warn("Failed to close channel for path {}", path, ex);
                    }
                });
            }
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

    default Optional<String> icon() {
        return Optional.empty();
    }

    Class<T> supportedType();
}
