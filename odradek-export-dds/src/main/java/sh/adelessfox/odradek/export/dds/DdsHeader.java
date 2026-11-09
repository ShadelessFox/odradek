package sh.adelessfox.odradek.export.dds;

import wtf.reversed.toolbox.collect.Bytes;

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

    public Bytes toBytes() {
        var bytes = Bytes.allocate(4 + BYTES + DdsHeaderDxt10.BYTES)
            .setInt(0x00, 'D' | 'D' << 8 | 'S' << 16 | ' ' << 24)
            .setInt(0x04, BYTES)
            .setInt(0x08, flags)
            .setInt(0x0C, height)
            .setInt(0x10, width)
            .setInt(0x14, pitchOrLinearSize)
            .setInt(0x18, depth)
            .setInt(0x1C, mipMapCount)
            .setInt(0x6C, caps)
            .setInt(0x70, caps2)
            .setInt(0x74, caps3)
            .setInt(0x78, caps4);
        pixelFormat.toBytes().copyTo(bytes, 0x4c);
        dxt10.toBytes().copyTo(bytes, 0x80);
        return bytes;
    }
}
