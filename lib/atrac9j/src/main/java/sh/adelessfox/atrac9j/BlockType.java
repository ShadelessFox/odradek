package sh.adelessfox.atrac9j;

/**
 * An ATRAC9 block (substream) type
 */
public enum BlockType {
    /**
     * Mono ATRAC9 block
     */
    MONO,
    /**
     * Stereo ATRAC9 block
     */
    STEREO,
    /**
     * Low-frequency effects ATRAC9 block
     */
    LFE
}
