package sh.adelessfox.atrac9j;

class Block {
    public Atrac9Config Config;
    public BlockType BlockType;
    public int BlockIndex;
    public Frame Frame;

    public Channel[] Channels;
    public int ChannelCount;

    public boolean FirstInSuperframe;
    public boolean ReuseBandParams;

    public int BandCount;
    public int StereoBand;
    public int ExtensionBand;
    public int QuantizationUnitCount;
    public int StereoQuantizationUnit;
    public int ExtensionUnit;
    public int QuantizationUnitsPrev;

    public int[] Gradient = new int[31];
    public int GradientMode;
    public int GradientStartUnit;
    public int GradientStartValue;
    public int GradientEndUnit;
    public int GradientEndValue;
    public int GradientBoundary;

    public int PrimaryChannelIndex;
    public int[] JointStereoSigns = new int[30];
    public boolean HasJointStereoSigns;

    public boolean BandExtensionEnabled;
    public boolean HasExtensionData;
    public int BexDataLength;
    public int BexMode;

    public Block(Frame parentFrame, int blockIndex) {
        Frame = parentFrame;
        BlockIndex = blockIndex;
        Config = parentFrame.Config;
        BlockType = Config.ChannelConfig.BlockTypes[blockIndex];
        ChannelCount = BlockTypeToChannelCount(BlockType);
        Channels = new Channel[ChannelCount];
        for (int i = 0; i < ChannelCount; i++) {
            Channels[i] = new Channel(this, i);
        }
    }

    public static int BlockTypeToChannelCount(BlockType blockType) {
        return switch (blockType) {
            case Mono, LFE -> 1;
            case Stereo -> 2;
        };
    }

    public Channel PrimaryChannel() {
        return Channels[PrimaryChannelIndex == 0 ? 0 : 1];
    }

    public Channel SecondaryChannel() {
        return Channels[PrimaryChannelIndex == 0 ? 1 : 0];
    }
}
