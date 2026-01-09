package sh.adelessfox.atrac9j;

import java.util.stream.Stream;

/**
 * Describes the channel mapping for an ATRAC9 stream
 *
 * @param blockTypes   The type of each block or substream in the ATRAC9 stream
 * @param blockCount   The number of blocks or substreams in the ATRAC9 stream
 * @param channelCount The number of channels in the ATRAC9 stream
 */
public record ChannelConfig(
    BlockType[] blockTypes,
    int blockCount,
    int channelCount
) {
    static ChannelConfig of(BlockType... blockTypes) {
        return new ChannelConfig(
            blockTypes,
            blockTypes.length,
            Stream.of(blockTypes).mapToInt(BlockType::channelCount).sum()
        );
    }
}

