package sh.adelessfox.odradek.game.ds2.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.decima.DecimaGame;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.io.DirectStorageReader;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class StreamingGraphStorage implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(StreamingGraphStorage.class);
    private static final Pattern PACKAGE_NAME = Pattern.compile("^package\\.(?<channel>\\d+)\\.(?<index>\\d+)\\.core");

    private final Map<String, BinaryReader> files = new HashMap<>();
    private final DecimaGame game;

    public StreamingGraphStorage(DecimaGame game) {
        this.game = game;
    }

    public void mount(String file) throws IOException {
        if (files.containsKey(file)) {
            log.warn("File already mounted: {}", file);
            return;
        }

        Path path = game.resolvePath(file);
        if (Files.notExists(path)) {
            log.warn("File not found: {}", file);
            return;
        }

        BinaryReader reader;

        try {
            reader = DirectStorageReader.open(path);
        } catch (IOException e) {
            reader = BinaryReader.open(path);
        }

        files.put(file, reader);

        var filename = path.getFileName().toString();
        var matcher = PACKAGE_NAME.matcher(filename);
        if (matcher.find()) {
            var channel = DS2.EStreamingDataChannel.valueOf(Byte.parseByte(matcher.group("channel")));
            log.info("Mounted file: {} ({})", file, channel);
        } else {
            log.info("Mounted file: {}", file);
        }
    }

    public byte[] read(String file, long offset, long length) throws IOException {
        var reader = resolve(file);
        var buffer = new byte[Math.toIntExact(length)];

        if (length == 0) {
            return buffer;
        }

        synchronized (reader) {
            reader.position(offset);
            reader.readBytes(buffer, 0, buffer.length);
        }

        return buffer;
    }

    public BinaryReader resolve(String file) {
        BinaryReader reader = files.get(file);
        if (reader == null) {
            throw new IllegalArgumentException("Can't resolve file: " + file);
        }
        return reader;
    }

    @Override
    public void close() throws IOException {
        for (BinaryReader value : files.values()) {
            value.close();
        }
        files.clear();
    }
}
