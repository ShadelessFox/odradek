package sh.adelessfox.odradek.game.ds2.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.decima.DecimaGame;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.io.DirectStorageSource;
import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.StructuredTaskScope;
import java.util.regex.Pattern;

public final class StreamingGraphStorage implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(StreamingGraphStorage.class);
    private static final Pattern PACKAGE_NAME = Pattern.compile("^package\\.(?<channel>\\d+)\\.(?<index>\\d+)\\.core");

    private final Map<String, BinarySource> files = new HashMap<>();
    private final DecimaGame game;

    public StreamingGraphStorage(DecimaGame game) {
        this.game = game;
    }

    public void mountAll(Iterable<? extends String> files) throws IOException {
        try (var scope = StructuredTaskScope.open()) {
            for (String file : files) {
                scope.fork(() -> {
                    mount(file);
                    return null;
                });
            }

            scope.join();
        } catch (StructuredTaskScope.FailedException e) {
            throw new IOException("Failed to load storage files", e.getCause());
        } catch (InterruptedException e) {
            throw new IOException("Failed to load storage files", e);
        }
    }

    public void mount(String file) throws IOException {
        synchronized (files) {
            if (files.containsKey(file)) {
                log.warn("File already mounted: {}", file);
                return;
            }
        }

        Path path = game.resolvePath(file);
        if (Files.notExists(path)) {
            log.warn("File not found: {}", file);
            return;
        }

        BinarySource source;

        try {
            source = DirectStorageSource.open(path);
        } catch (IOException e) {
            source = BinarySource.open(path);
        }

        var matcher = PACKAGE_NAME.matcher(file);
        if (matcher.find()) {
            var channel = DS2.EStreamingDataChannel.valueOf(Byte.parseByte(matcher.group("channel")));
            log.info("Mounted file: {} ({})", file, channel);
        } else {
            log.info("Mounted file: {}", file);
        }

        synchronized (files) {
            files.put(file, source);
        }
    }

    public Bytes read(String file, long offset, long length) throws IOException {
        var source = resolve(file);
        synchronized (source) {
            return source
                .position(offset)
                .readBytes(Math.toIntExact(length));
        }
    }

    private BinarySource resolve(String file) {
        BinarySource source = files.get(file);
        if (source == null) {
            throw new IllegalArgumentException("Can't resolve file: " + file);
        }
        return source;
    }

    @Override
    public void close() throws IOException {
        for (BinarySource value : files.values()) {
            value.close();
        }
        files.clear();
    }
}
