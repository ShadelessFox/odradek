package sh.adelessfox.atrac9j;

final class Frame {
    public Atrac9Config Config;
    public int FrameIndex;
    public Block[] Blocks;

    public Frame(Atrac9Config config) {
        Config = config;
        Blocks = new Block[config.ChannelConfig.BlockCount];

        for (int i = 0; i < config.ChannelConfig.BlockCount; i++) {
            Blocks[i] = new Block(this, i);
        }
    }
}
