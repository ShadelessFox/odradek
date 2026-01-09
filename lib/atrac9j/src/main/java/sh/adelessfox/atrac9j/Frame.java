package sh.adelessfox.atrac9j;

final class Frame {
    Atrac9Config Config;
    int FrameIndex;
    Block[] Blocks;

    Frame(Atrac9Config config) {
        Config = config;
        Blocks = new Block[config.ChannelConfig.BlockCount];

        for (int i = 0; i < config.ChannelConfig.BlockCount; i++) {
            Blocks[i] = new Block(this, i);
        }
    }
}
