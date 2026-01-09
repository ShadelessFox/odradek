package sh.adelessfox.atrac9j;

final class Block {
    final Atrac9Config config;
    final BlockType blockType;
    final int blockIndex;

    final Channel[] channels;
    final int channelCount;

    boolean firstInSuperframe;
    boolean reuseBandParams;

    int bandCount;
    int stereoBand;
    int extensionBand;
    int quantizationUnitCount;
    int stereoQuantizationUnit;
    int extensionUnit;
    int quantizationUnitsPrev;

    final int[] gradient = new int[31];
    int gradientMode;
    int gradientStartUnit;
    int gradientStartValue;
    int gradientEndUnit;
    int gradientEndValue;
    int gradientBoundary;

    int primaryChannelIndex;
    final int[] jointStereoSigns = new int[30];
    boolean hasJointStereoSigns;

    boolean bandExtensionEnabled;
    boolean hasExtensionData;
    int bexDataLength;
    int bexMode;

    Block(Atrac9Config config, int blockIndex) {
        this.blockIndex = blockIndex;
        this.config = config;
        blockType = this.config.channelConfig().blockTypes()[blockIndex];
        channelCount = blockType.channelCount();
        channels = new Channel[channelCount];
        for (int i = 0; i < channelCount; i++) {
            channels[i] = new Channel(this, i);
        }
    }

    Channel primaryChannel() {
        return channels[primaryChannelIndex == 0 ? 0 : 1];
    }

    Channel secondaryChannel() {
        return channels[primaryChannelIndex == 0 ? 1 : 0];
    }
}
