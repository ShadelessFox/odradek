package sh.adelessfox.odradek.export.dds;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

record DdsHeader(
    int flags,
    int height,
    int width,
    int pitchOrLinearSize,
    int depth,
    int mipMapCount,
    DdsPixelFormat pixelFormat,
    int caps,
    int caps2,
    int caps3,
    int caps4,
    DdsHeaderDxt10 dxt10
) {
    static final int BYTES = 124;

    // flags
    static final int DDSD_CAPS = 0x1;
    static final int DDSD_HEIGHT = 0x2;
    static final int DDSD_WIDTH = 0x4;
    static final int DDSD_PIXELFORMAT = 0x1000;
    static final int DDSD_MIPMAPCOUNT = 0x20000;
    static final int DDSD_DEPTH = 0x800000;

    // caps
    static final int DDSCAPS_COMPLEX = 0x8;
    static final int DDSCAPS_TEXTURE = 0x1000;
    static final int DDSCAPS_MIPMAP = 0x400000;

    // caps2
    static final int DDSCAPS2_CUBEMAP = 0x200;
    static final int DDSCAPS2_CUBEMAP_POSITIVEX = 0x400;
    static final int DDSCAPS2_CUBEMAP_NEGATIVEX = 0x800;
    static final int DDSCAPS2_CUBEMAP_POSITIVEY = 0x1000;
    static final int DDSCAPS2_CUBEMAP_NEGATIVEY = 0x2000;
    static final int DDSCAPS2_CUBEMAP_POSITIVEZ = 0x4000;
    static final int DDSCAPS2_CUBEMAP_NEGATIVEZ = 0x8000;
    static final int DDSCAPS2_VOLUME = 0x200000;

    public ByteBuffer toBuffer() {
        return ByteBuffer.allocate(4 + BYTES + DdsHeaderDxt10.BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt('D' | 'D' << 8 | 'S' << 16 | ' ' << 24)
            .putInt(BYTES)
            .putInt(flags)
            .putInt(height)
            .putInt(width)
            .putInt(pitchOrLinearSize)
            .putInt(depth)
            .putInt(mipMapCount)
            .position(0x4c) // dwReserved1
            .put(pixelFormat.toBuffer())
            .putInt(caps)
            .putInt(caps2)
            .putInt(caps3)
            .putInt(caps4)
            .position(0x80) // dwReserved2
            .put(dxt10.toBuffer())
            .flip();
    }
}
