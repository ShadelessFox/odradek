package sh.adelessfox.libatrac9;

/// <summary>
/// Describes the channel mapping for an ATRAC9 stream
/// </summary>
public final class ChannelConfig {
    ChannelConfig(BlockType... blockTypes) {
        BlockCount = blockTypes.length;
        BlockTypes = blockTypes;
        for (BlockType type : blockTypes) {
            ChannelCount += Block.BlockTypeToChannelCount(type);
        }
    }

    /// <summary>
    /// The number of blocks or substreams in the ATRAC9 stream
    /// </summary>
    public int BlockCount;

    /// <summary>
    /// The type of each block or substream in the ATRAC9 stream
    /// </summary>
    public BlockType[] BlockTypes;

    /// <summary>
    /// The number of channels in the ATRAC9 stream
    /// </summary>
    public int ChannelCount;
}

