package sh.adelessfox.odradek.export.dds;

import sh.adelessfox.odradek.texture.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

final class DdsWriter {
    static void write(Texture texture, WritableByteChannel channel) throws IOException {
        writeHeader(texture, channel);
        writeData(texture, channel);
    }

    private static void writeHeader(Texture texture, WritableByteChannel channel) throws IOException {
        channel.write(createHeader(texture).toBuffer());
    }

    private static void writeData(Texture texture, WritableByteChannel channel) throws IOException {
        if (texture.type() == TextureType.VOLUME) {
            // The order in which surfaces are stored matches the order DDS uses.
            for (Surface surface : texture.surfaces()) {
                channel.write(ByteBuffer.wrap(surface.data()));
            }
        } else {
            int mips = texture.mips();
            int elements = switch (texture.type()) {
                case ARRAY -> texture.arraySize().orElseThrow();
                case CUBEMAP -> 6;
                default -> 1;
            };
            for (int element = 0; element < elements; element++) {
                for (int mip = 0; mip < mips; mip++) {
                    Surface surface = texture.surfaces().get(mip * elements + element);
                    channel.write(ByteBuffer.wrap(surface.data()));
                }
            }
        }
    }

    private static DdsHeader createHeader(Texture texture) {
        int width = texture.width();
        int height = texture.height();
        var format = texture.format();
        int pitchOrLinearSize;

        if (format.isCompressed()) {
            pitchOrLinearSize = computeLinearSize(width, height, format);
        } else {
            pitchOrLinearSize = computePitch(width, format);
        }

        return new DdsHeader(
            computeFlags(texture),
            height,
            width,
            pitchOrLinearSize,
            texture.depth().orElse(1),
            texture.mips(),
            createPixelFormat(),
            computeCapsFlags(texture),
            computeCaps2Flags(texture),
            0,
            0,
            createHeaderDxt10(texture)
        );
    }

    private static DdsHeaderDxt10 createHeaderDxt10(Texture texture) {
        int dxgiFormat = mapFormat(texture);
        int resourceDimension = mapDimension(texture);
        int miscFlag = computeMiscFlags(texture);
        int arraySize = texture.arraySize().orElse(1);
        int miscFlags2 = DdsHeaderDxt10.DDS_ALPHA_MODE_UNKNOWN;
        return new DdsHeaderDxt10(dxgiFormat, resourceDimension, miscFlag, arraySize, miscFlags2);
    }

    private static DdsPixelFormat createPixelFormat() {
        int flags = DdsPixelFormat.DDPF_FOURCC;
        int fourcc = 'D' | 'X' << 8 | '1' << 16 | '0' << 24;
        return new DdsPixelFormat(flags, fourcc, 0, 0, 0, 0, 0);
    }

    private static int computeLinearSize(int width, int height, TextureFormat format) {
        return format.block().surfaceSize(width, height);
    }

    private static int computePitch(int width, TextureFormat format) {
        return width * format.block().size();
    }

    private static int computeFlags(Texture texture) {
        int flags = DdsHeader.DDS_HEADER_FLAGS_TEXTURE;
        if (texture.mips() > 0) {
            flags |= DdsHeader.DDS_HEADER_FLAGS_MIPMAP;
        }
        if (texture.type() == TextureType.VOLUME) {
            flags |= DdsHeader.DDS_HEADER_FLAGS_VOLUME;
        }
        if (texture.format().isCompressed()) {
            flags |= DdsHeader.DDS_HEADER_FLAGS_LINEARSIZE;
        } else {
            flags |= DdsHeader.DDS_HEADER_FLAGS_PITCH;
        }
        return flags;
    }

    private static int computeCapsFlags(Texture texture) {
        int flags = DdsHeader.DDS_SURFACE_FLAGS_TEXTURE;
        if (texture.mips() > 1) {
            flags |= DdsHeader.DDS_SURFACE_FLAGS_MIPMAP;
        }
        if (texture.type() == TextureType.CUBEMAP) {
            flags |= DdsHeader.DDS_SURFACE_FLAGS_CUBEMAP;
        }
        return flags;
    }

    private static int computeCaps2Flags(Texture texture) {
        return switch (texture.type()) {
            case CUBEMAP -> DdsHeader.DDS_CUBEMAP_ALLFACES;
            case VOLUME -> DdsHeader.DDS_VOLUME;
            default -> 0;
        };
    }

    private static int computeMiscFlags(Texture texture) {
        return switch (texture.type()) {
            case CUBEMAP -> DdsHeaderDxt10.DDS_RESOURCE_MISC_TEXTURECUBE;
            default -> 0;
        };
    }

    private static int mapFormat(Texture texture) {
        return switch (texture.format()) {
            // Uncompressed
            case R8G8B8A8_UNORM -> mapColorSpace(texture.colorSpace(), DdsHeaderDxt10.DXGI_FORMAT_R8G8B8A8_UNORM);

            // Compressed
            case BC1_UNORM -> mapColorSpace(texture.colorSpace(), DdsHeaderDxt10.DXGI_FORMAT_BC1_UNORM);
            case BC2_UNORM -> mapColorSpace(texture.colorSpace(), DdsHeaderDxt10.DXGI_FORMAT_BC2_UNORM);
            case BC3_UNORM -> mapColorSpace(texture.colorSpace(), DdsHeaderDxt10.DXGI_FORMAT_BC3_UNORM);
            case BC4_UNORM -> DdsHeaderDxt10.DXGI_FORMAT_BC4_UNORM;
            case BC4_SNORM -> DdsHeaderDxt10.DXGI_FORMAT_BC4_SNORM;
            case BC5_UNORM -> DdsHeaderDxt10.DXGI_FORMAT_BC5_UNORM;
            case BC5_SNORM -> DdsHeaderDxt10.DXGI_FORMAT_BC5_SNORM;
            case BC6_UNORM -> DdsHeaderDxt10.DXGI_FORMAT_BC6H_UF16;
            case BC6_SNORM -> DdsHeaderDxt10.DXGI_FORMAT_BC6H_SF16;
            case BC7_UNORM -> mapColorSpace(texture.colorSpace(), DdsHeaderDxt10.DXGI_FORMAT_BC7_UNORM);

            default -> throw new IllegalStateException("Unsupported texture format: " + texture.format());
        };
    }

    private static int mapColorSpace(TextureColorSpace colorSpace, int format) {
        return switch (colorSpace) {
            case LINEAR -> format;
            case SRGB -> format + 1;
        };
    }

    private static int mapDimension(Texture texture) {
        return switch (texture.type()) {
            case SURFACE, ARRAY, CUBEMAP -> DdsHeaderDxt10.DDS_DIMENSION_TEXTURE2D;
            case VOLUME -> DdsHeaderDxt10.DDS_DIMENSION_TEXTURE3D;
        };
    }
}
