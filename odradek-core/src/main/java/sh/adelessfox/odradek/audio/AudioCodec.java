package sh.adelessfox.odradek.audio;

public sealed interface AudioCodec {
    enum Pcm implements AudioCodec {
        /** PCM 16-bit signed little-endian */
        S16LE,
        /** PCM 24-bit signed little-endian */
        S24LE,
        /** PCM 32-bit signed little-endian */
        S32LE,
        /** PCM 32-bit float little-endian */
        F32LE;

        public int sizeBytes() {
            return switch (this) {
                case S16LE -> 2;
                case S24LE -> 3;
                case S32LE, F32LE -> 4;
            };
        }
    }

    record Atrac9() implements AudioCodec {
    }

    record Wwise() implements AudioCodec {
    }
}
