package sh.adelessfox.atrac9j;

/**
 * Describes the channel mapping for an ATRAC9 stream
 */
public final class ChannelConfig {
    ChannelConfig(BlockType... blockTypes) {
        BlockCount = blockTypes.length;
        BlockTypes = blockTypes;
        for (BlockType type : blockTypes) {
            ChannelCount += Block.BlockTypeToChannelCount(type);
        }
    }

    /**
     * The number of blocks or substreams in the ATRAC9 stream
     */
    public int BlockCount;

    /**
     * The type of each block or substream in the ATRAC9 stream
     */
    public BlockType[] BlockTypes;

    /**
     * The number of channels in the ATRAC9 stream
     */
    public int ChannelCount;
}

