package sh.adelessfox.odradek.export.dds;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

record DdsHeaderDxt10(
    int dxgiFormat,
    int resourceDimension,
    int miscFlag,
    int arraySize,
    int miscFlags2
) {
    public static final int BYTES = 20;

    // dxgiFormat
    static final int DXGI_FORMAT_R8G8B8A8_UNORM = 28;
    static final int DXGI_FORMAT_BC1_UNORM = 71;
    static final int DXGI_FORMAT_BC2_UNORM = 74;
    static final int DXGI_FORMAT_BC3_UNORM = 77;
    static final int DXGI_FORMAT_BC4_UNORM = 80;
    static final int DXGI_FORMAT_BC4_SNORM = 81;
    static final int DXGI_FORMAT_BC5_UNORM = 83;
    static final int DXGI_FORMAT_BC5_SNORM = 84;
    static final int DXGI_FORMAT_BC6H_UF16 = 95;
    static final int DXGI_FORMAT_BC6H_SF16 = 96;
    static final int DXGI_FORMAT_BC7_UNORM = 98;

    // resourceDimension
    static final int DDS_DIMENSION_TEXTURE2D = 3;
    static final int DDS_DIMENSION_TEXTURE3D = 4;
    static final int DDS_RESOURCE_MISC_TEXTURECUBE = 0x4;

    // miscFlags2
    public static final int DDS_ALPHA_MODE_UNKNOWN = 0x0;

    public ByteBuffer toBuffer() {
        return ByteBuffer.allocate(BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(dxgiFormat)
            .putInt(resourceDimension)
            .putInt(miscFlag)
            .putInt(arraySize)
            .putInt(miscFlags2)
            .flip();
    }
}
