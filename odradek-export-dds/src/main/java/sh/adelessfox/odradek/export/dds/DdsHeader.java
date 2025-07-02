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
    static final int DDS_HEADER_FLAGS_TEXTURE = 0x00001007; // DDSD_CAPS | DDSD_HEIGHT | DDSD_WIDTH | DDSD_PIXELFORMAT
    static final int DDS_HEADER_FLAGS_MIPMAP = 0x00020000; // DDSD_MIPMAPCOUNT
    static final int DDS_HEADER_FLAGS_VOLUME = 0x00800000; // DDSD_DEPTH
    static final int DDS_HEADER_FLAGS_PITCH = 0x00000008; // DDSD_PITCH
    static final int DDS_HEADER_FLAGS_LINEARSIZE = 0x00080000; // DDSD_LINEARSIZE

    // caps
    static final int DDS_SURFACE_FLAGS_TEXTURE = 0x00001000; // DDSCAPS_TEXTURE
    static final int DDS_SURFACE_FLAGS_MIPMAP = 0x00400008; // DDSCAPS_COMPLEX | DDSCAPS_MIPMAP
    static final int DDS_SURFACE_FLAGS_CUBEMAP = 0x00000008; // DDSCAPS_COMPLEX

    // caps2
    static final int DDS_CUBEMAP_ALLFACES = 0x0000fe00;  // DDSCAPS2_CUBEMAP | DDSCAPS2_CUBEMAP_XXX
    static final int DDS_VOLUME = 0x00200000; // DDSCAPS2_VOLUME

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
