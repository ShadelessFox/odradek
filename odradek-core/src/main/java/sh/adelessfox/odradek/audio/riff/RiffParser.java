package sh.adelessfox.odradek.audio.riff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.io.BinaryReader;

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

    public RiffFile parse(BinaryReader reader) throws IOException {
        var id = reader.readString(4);
        if (!id.equals("RIFF")) {
            throw new IOException("Not a RIFF container");
        }

        var size = reader.readInt();
        var type = reader.readString(4);
        if (this.type != null && !this.type.equals(type)) {
            throw new IOException("Unexpected RIFF type: '" + type + "' (was expecting '" + this.type + "')");
        }

        var chunks = new HashMap<String, RiffChunk>();
        var end = reader.position() + size - 4;

        while (reader.position() < end) {
            var chunkId = reader.readString(4);
            var chunkSize = reader.readInt();

            var chunkReader = readers.get(chunkId);
            if (chunkReader == null) {
                log.debug("Skipping unsupported chunk '{}' ({} bytes)", chunkId, chunkSize);
                reader.skip(chunkSize);
                continue;
            }

            var chunkStart = reader.position();
            var chunk = chunkReader.read(reader, chunkSize);
            var chunkEnd = reader.position();

            if (chunkEnd - chunkStart != chunkSize) {
                throw new IOException("Amount of data read by chunk reader doesn't match chunk's actual size: " + (chunkEnd - chunkStart) + " != " + chunkSize);
            }

            reader.position(reader.position() + 1 & ~1);
            chunks.put(chunkId, chunk);
        }

        return new RiffFile(type, chunks);
    }
}
