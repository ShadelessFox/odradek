package sh.adelessfox.libatrac9;

/// <summary>
/// An ATRAC9 block (substream) type
/// </summary>
public enum BlockType {
    /// <summary>
    /// Mono ATRAC9 block
    /// </summary>
    Mono(0),
    /// <summary>
    /// Stereo ATRAC9 block
    /// </summary>
    Stereo(1),
    /// <summary>
    /// Low-frequency effects ATRAC9 block
    /// </summary>
    LFE(2);

    private int value;

    BlockType(int value) {
        this.value = value;
    }
}
