package sh.adelessfox.odradek.audio.container.riff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class RiffParser {
    private static final Logger log = LoggerFactory.getLogger(RiffParser.class);

    private final Map<String, RiffChunkReader<?>> readers = new HashMap<>();
    private String type;

    public <T extends RiffChunk> RiffParser reader(RiffChunk.Id<T> id, RiffChunkReader<? extends T> reader) {
        readers.put(id.id(), reader);
        return this;
    }

    public RiffParser type(String type) {
        this.type = type;
        return this;
    }

    public RiffFile parse(BinarySource source) throws IOException {
        var id = source.readString(4);
        if (!id.equals("RIFF")) {
            throw new IOException("Not a RIFF container");
        }

        var size = source.readInt();
        var type = source.readString(4);
        if (this.type != null && !this.type.equals(type)) {
            throw new IOException("Unexpected RIFF type: '" + type + "' (was expecting '" + this.type + "')");
        }

        var chunks = new HashMap<String, RiffChunk>();
        var end = source.position() + size - 4;

        while (source.position() < end) {
            source.position(source.position() + 1 & ~1);

            var chunkId = source.readString(4);
            var chunkSize = source.readInt();

            var chunkReader = readers.get(chunkId);
            if (chunkReader == null) {
                log.debug("Skipping unsupported chunk '{}' ({} bytes)", chunkId, chunkSize);
                source.skip(chunkSize);
                continue;
            }

            var chunkStart = source.position();
            var chunk = chunkReader.read(source, chunkSize);
            var chunkEnd = source.position();

            if (chunkEnd - chunkStart != chunkSize) {
                throw new IOException("Amount of data read by chunk reader doesn't match chunk's actual size: " + (chunkEnd - chunkStart) + " != " + chunkSize);
            }

            chunks.put(chunkId, chunk);
        }

        return new RiffFile(type, chunks);
    }
}
