package sh.adelessfox.odradek.audio.container.riff;

import java.util.Map;
import java.util.Optional;

public record RiffFile(String type, Map<String, RiffChunk> chunks) {
    public RiffFile {
        chunks = Map.copyOf(chunks);
    }

    public <T extends RiffChunk> Optional<T> get(RiffChunk.Id<T> id) {
        return Optional.ofNullable(id.type().cast(chunks.get(id.id())));
    }
}
