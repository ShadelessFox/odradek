package sh.adelessfox.odradek.export.dds;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

    public ByteBuffer toBuffer() {
        return ByteBuffer.allocate(BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(BYTES)
            .putInt(flags)
            .putInt(fourCC)
            .putInt(rgbBitCount)
            .putInt(rBitMask)
            .putInt(gBitMask)
            .putInt(bBitMask)
            .putInt(aBitMask)
            .flip();
    }
}
