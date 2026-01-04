package sh.adelessfox.odradek.audio.container.riff;

public interface RiffChunk {
    record Id<T extends RiffChunk>(String id, Class<T> type) {
        public Id {
            if (id.length() != 4) {
                throw new IllegalArgumentException("Identifier must be 4 characters long");
            }
        }
    }
}
