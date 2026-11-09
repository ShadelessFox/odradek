package sh.adelessfox.odradek.export.dds;

import wtf.reversed.toolbox.collect.Bytes;

record DdsPixelFormat(
    int flags,
    int fourCC,
    int rgbBitCount,
    int rBitMask,
    int gBitMask,
    int bBitMask,
    int aBitMask
) {
    public static final int BYTES = 32;

    // flags
    static final int DDPF_FOURCC = 0x4;

    public Bytes toBytes() {
        return Bytes.allocate(BYTES)
            .setInt(0x00, BYTES)
            .setInt(0x04, flags)
            .setInt(0x08, fourCC)
            .setInt(0x0C, rgbBitCount)
            .setInt(0x10, rBitMask)
            .setInt(0x14, gBitMask)
            .setInt(0x18, bBitMask)
            .setInt(0x1C, aBitMask);
    }
}
