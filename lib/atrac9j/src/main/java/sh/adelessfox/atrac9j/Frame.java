package sh.adelessfox.atrac9j;

final class Frame {
    final Atrac9Config Config;
    final Block[] Blocks;
    int FrameIndex;

    Frame(Atrac9Config config) {
        Config = config;
        Blocks = new Block[config.ChannelConfig.BlockCount];

        for (int i = 0; i < config.ChannelConfig.BlockCount; i++) {
            Blocks[i] = new Block(this, i);
        }
    }
}
