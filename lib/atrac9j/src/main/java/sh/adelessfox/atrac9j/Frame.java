package sh.adelessfox.atrac9j;

record Frame(
    Atrac9Config config,
    Block[] blocks
) {
    static Frame of(Atrac9Config config) {
        var blocks = new Block[config.channelConfig().blockCount()];
        for (int i = 0; i < config.channelConfig().blockCount(); i++) {
            blocks[i] = new Block(config, i);
        }
        return new Frame(config, blocks);
    }
}
