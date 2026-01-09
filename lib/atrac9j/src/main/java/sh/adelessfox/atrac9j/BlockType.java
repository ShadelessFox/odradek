package sh.adelessfox.atrac9j;

/// <summary>
/// An ATRAC9 block (substream) type
/// </summary>
enum BlockType {
    /// <summary>
    /// Mono ATRAC9 block
    /// </summary>
    MONO,
    /// <summary>
    /// Stereo ATRAC9 block
    /// </summary>
    STEREO,
    /// <summary>
    /// Low-frequency effects ATRAC9 block
    /// </summary>
    LFE;
}
