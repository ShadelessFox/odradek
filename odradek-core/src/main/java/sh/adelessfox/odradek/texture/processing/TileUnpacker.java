package sh.adelessfox.odradek.texture.processing;

import be.twofold.tinybcdec.BlockDecoder;
import sh.adelessfox.odradek.texture.Surface;
import sh.adelessfox.odradek.texture.TextureColorSpace;
import sh.adelessfox.odradek.util.Srgb;
import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.collect.Floats;
import wtf.reversed.toolbox.math.FloatMath;

@FunctionalInterface
public interface TileUnpacker {
    void unpack(TileContext ctx, Floats.Mutable dst);

    static TileUnpacker from(Surface surface) {
        var src = surface.format().isCompressed()
            ? decompress(surface)
            : surface;
        return switch (src.format()) {
            case R8_UNORM -> (ctx, dst) -> unpackR8Unorm(ctx, src, dst);
            case R8G8_UNORM -> (ctx, dst) -> unpackR8G8Unorm(ctx, src, dst);
            case R8G8B8_UNORM -> (ctx, dst) -> unpackR8G8B8Unorm(ctx, src, dst);
            case R8G8B8A8_UNORM -> (ctx, dst) -> unpackR8G8B8A8Unorm(ctx, src, dst);
            case R16_UNORM -> (ctx, dst) -> unpackR16Unorm(ctx, src, dst);
            case R16G16_UNORM -> (ctx, dst) -> unpackR16G16Unorm(ctx, src, dst);
            case R16G16B16_UNORM -> (ctx, dst) -> unpackR16G16B16Unorm(ctx, src, dst);
            case R16G16B16A16_UNORM -> (ctx, dst) -> unpackR16G16B16A16Unorm(ctx, src, dst);
            case R16_SFLOAT -> (ctx, dst) -> unpackR16Sfloat(ctx, src, dst);
            case R16G16_SFLOAT -> (ctx, dst) -> unpackR16G16Sfloat(ctx, src, dst);
            case R16G16B16_SFLOAT -> (ctx, dst) -> unpackR16G16B16Sfloat(ctx, src, dst);
            case R16G16B16A16_SFLOAT -> (ctx, dst) -> unpackR16G16B16A16Sfloat(ctx, src, dst);
            case R32_SFLOAT -> (ctx, dst) -> unpackR32Sfloat(ctx, src, dst);
            case R32G32_SFLOAT -> (ctx, dst) -> unpackR32G32Sfloat(ctx, src, dst);
            case R32G32B32_SFLOAT -> (ctx, dst) -> unpackR32G32B32Sfloat(ctx, src, dst);
            case R32G32B32A32_SFLOAT -> (ctx, dst) -> unpackR32G32B32A32Sfloat(ctx, src, dst);
            case B8G8R8_UNORM -> (ctx, dst) -> unpackB8G8R8Unorm(ctx, src, dst);
            case B8G8R8A8_UNORM -> (ctx, dst) -> unpackB8G8R8A8Unorm(ctx, src, dst);
            default -> throw new UnsupportedOperationException(src.format().name());
        };
    }

    private static Surface decompress(Surface src) {
        var decoder = switch (src.format()) {
            case BC1_UNORM -> BlockDecoder.bc1(false);
            case BC2_UNORM -> BlockDecoder.bc2();
            case BC3_UNORM -> BlockDecoder.bc3();
            case BC4_UNORM -> BlockDecoder.bc4(false);
            case BC4_SNORM -> BlockDecoder.bc4(true);
            case BC5_UNORM -> BlockDecoder.bc5(false);
            case BC5_SNORM -> BlockDecoder.bc5(true);
            case BC6_UNORM -> BlockDecoder.bc6h(false);
            case BC6_SNORM -> BlockDecoder.bc6h(true);
            case BC7_UNORM -> BlockDecoder.bc7();
            default -> throw new UnsupportedOperationException(src.format().name());
        };
        var format = src.format().decompressed();
        var result = Surface.create(src.width(), src.height(), format, src.colorSpace());
        decoder.decode(src.data(), 0, src.width(), src.height(), result.data(), 0);
        return result;
    }

    private static void unpackR8Unorm(TileContext ctx, Surface src, Floats.Mutable dst) {
        var srgb = src.colorSpace() == TextureColorSpace.SRGB;
        var data = src.data();
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = src.offset(ctx.x, ctx.y + row, ctx.z);
            int dstOff = row * ctx.width * 4;
            for (int col = 0; col < ctx.width; col++, srcOff++, dstOff += 4) {
                dst.set(dstOff/**/, unpackUNorm8(data[srcOff], srgb));
                dst.set(dstOff + 1, 0.0f);
                dst.set(dstOff + 2, 0.0f);
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR8G8Unorm(TileContext ctx, Surface src, Floats.Mutable dst) {
        var srgb = src.colorSpace() == TextureColorSpace.SRGB;
        var data = src.data();
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = src.offset(ctx.x, ctx.y + row, ctx.z);
            int dstOff = row * ctx.width * 4;
            for (int col = 0; col < ctx.width; col++, srcOff += 2, dstOff += 4) {
                dst.set(dstOff/**/, unpackUNorm8(data[srcOff/**/], srgb));
                dst.set(dstOff + 1, unpackUNorm8(data[srcOff + 1], srgb));
                dst.set(dstOff + 2, 0.0f);
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR8G8B8Unorm(TileContext ctx, Surface src, Floats.Mutable dst) {
        var srgb = src.colorSpace() == TextureColorSpace.SRGB;
        var data = src.data();
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = src.offset(ctx.x, ctx.y + row, ctx.z);
            int dstOff = row * ctx.width * 4;
            for (int col = 0; col < ctx.width; col++, srcOff += 3, dstOff += 4) {
                dst.set(dstOff/**/, unpackUNorm8(data[srcOff/**/], srgb));
                dst.set(dstOff + 1, unpackUNorm8(data[srcOff + 1], srgb));
                dst.set(dstOff + 2, unpackUNorm8(data[srcOff + 2], srgb));
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR8G8B8A8Unorm(TileContext ctx, Surface src, Floats.Mutable dst) {
        var srgb = src.colorSpace() == TextureColorSpace.SRGB;
        var data = src.data();
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = src.offset(ctx.x, ctx.y + row, ctx.z);
            int dstOff = row * ctx.width * 4;
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 4) {
                dst.set(dstOff/**/, unpackUNorm8(data[srcOff/**/], srgb));
                dst.set(dstOff + 1, unpackUNorm8(data[srcOff + 1], srgb));
                dst.set(dstOff + 2, unpackUNorm8(data[srcOff + 2], srgb));
                dst.set(dstOff + 3, unpackUNorm8(data[srcOff + 3], false)); // alpha is always linear
            }
        }
    }

    private static void unpackR16Unorm(TileContext ctx, Surface src, Floats.Mutable dst) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = src.offset(ctx.x, ctx.y + row, ctx.z);
            int dstOff = row * ctx.width * 4;
            for (int col = 0; col < ctx.width; col++, srcOff += 2, dstOff += 4) {
                dst.set(dstOff/**/, FloatMath.unpackUNorm16(data.getShort(srcOff)));
                dst.set(dstOff + 1, 0.0f);
                dst.set(dstOff + 2, 0.0f);
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR16G16Unorm(TileContext ctx, Surface src, Floats.Mutable dst) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = src.offset(ctx.x, ctx.y + row, ctx.z);
            int dstOff = row * ctx.width * 4;
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 4) {
                dst.set(dstOff/**/, FloatMath.unpackUNorm16(data.getShort(srcOff/**/)));
                dst.set(dstOff + 1, FloatMath.unpackUNorm16(data.getShort(srcOff + 2)));
                dst.set(dstOff + 2, 0.0f);
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR16G16B16Unorm(TileContext ctx, Surface src, Floats.Mutable dst) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = src.offset(ctx.x, ctx.y + row, ctx.z);
            int dstOff = row * ctx.width * 4;
            for (int col = 0; col < ctx.width; col++, srcOff += 6, dstOff += 4) {
                dst.set(dstOff/**/, FloatMath.unpackUNorm16(data.getShort(srcOff/**/)));
                dst.set(dstOff + 1, FloatMath.unpackUNorm16(data.getShort(srcOff + 2)));
                dst.set(dstOff + 2, FloatMath.unpackUNorm16(data.getShort(srcOff + 4)));
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR16G16B16A16Unorm(TileContext ctx, Surface src, Floats.Mutable dst) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = src.offset(ctx.x, ctx.y + row, ctx.z);
            int dstOff = row * ctx.width * 4;
            for (int col = 0; col < ctx.width; col++, srcOff += 8, dstOff += 4) {
                dst.set(dstOff/**/, FloatMath.unpackUNorm16(data.getShort(srcOff/**/)));
                dst.set(dstOff + 1, FloatMath.unpackUNorm16(data.getShort(srcOff + 2)));
                dst.set(dstOff + 2, FloatMath.unpackUNorm16(data.getShort(srcOff + 4)));
                dst.set(dstOff + 3, FloatMath.unpackUNorm16(data.getShort(srcOff + 6)));
            }
        }
    }

    private static void unpackR16Sfloat(TileContext ctx, Surface src, Floats.Mutable dst) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = src.offset(ctx.x, ctx.y + row, ctx.z);
            int dstOff = row * ctx.width * 4;
            for (int col = 0; col < ctx.width; col++, srcOff += 2, dstOff += 4) {
                dst.set(dstOff/**/, Float.float16ToFloat(data.getShort(srcOff)));
                dst.set(dstOff + 1, 0.0f);
                dst.set(dstOff + 2, 0.0f);
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR16G16Sfloat(TileContext ctx, Surface src, Floats.Mutable dst) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = src.offset(ctx.x, ctx.y + row, ctx.z);
            int dstOff = row * ctx.width * 4;
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 4) {
                dst.set(dstOff/**/, Float.float16ToFloat(data.getShort(srcOff/**/)));
                dst.set(dstOff + 1, Float.float16ToFloat(data.getShort(srcOff + 2)));
                dst.set(dstOff + 2, 0.0f);
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR16G16B16Sfloat(TileContext ctx, Surface src, Floats.Mutable dst) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = src.offset(ctx.x, ctx.y + row, ctx.z);
            int dstOff = row * ctx.width * 4;
            for (int col = 0; col < ctx.width; col++, srcOff += 6, dstOff += 4) {
                dst.set(dstOff/**/, Float.float16ToFloat(data.getShort(srcOff/**/)));
                dst.set(dstOff + 1, Float.float16ToFloat(data.getShort(srcOff + 2)));
                dst.set(dstOff + 2, Float.float16ToFloat(data.getShort(srcOff + 4)));
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR16G16B16A16Sfloat(TileContext ctx, Surface src, Floats.Mutable dst) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = src.offset(ctx.x, ctx.y + row, ctx.z);
            int dstOff = row * ctx.width * 4;
            for (int col = 0; col < ctx.width; col++, srcOff += 8, dstOff += 4) {
                dst.set(dstOff/**/, Float.float16ToFloat(data.getShort(srcOff/**/)));
                dst.set(dstOff + 1, Float.float16ToFloat(data.getShort(srcOff + 2)));
                dst.set(dstOff + 2, Float.float16ToFloat(data.getShort(srcOff + 4)));
                dst.set(dstOff + 3, Float.float16ToFloat(data.getShort(srcOff + 6)));
            }
        }
    }

    private static void unpackR32Sfloat(TileContext ctx, Surface src, Floats.Mutable dst) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = src.offset(ctx.x, ctx.y + row, ctx.z);
            int dstOff = row * ctx.width * 4;
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 4) {
                dst.set(dstOff/**/, Float.intBitsToFloat(data.getInt(srcOff)));
                dst.set(dstOff + 1, 0.0f);
                dst.set(dstOff + 2, 0.0f);
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR32G32Sfloat(TileContext ctx, Surface src, Floats.Mutable dst) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = src.offset(ctx.x, ctx.y + row, ctx.z);
            int dstOff = row * ctx.width * 4;
            for (int col = 0; col < ctx.width; col++, srcOff += 8, dstOff += 4) {
                dst.set(dstOff/**/, Float.intBitsToFloat(data.getInt(srcOff/**/)));
                dst.set(dstOff + 1, Float.intBitsToFloat(data.getInt(srcOff + 4)));
                dst.set(dstOff + 2, 0.0f);
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR32G32B32Sfloat(TileContext ctx, Surface src, Floats.Mutable dst) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = src.offset(ctx.x, ctx.y + row, ctx.z);
            int dstOff = row * ctx.width * 4;
            for (int col = 0; col < ctx.width; col++, srcOff += 12, dstOff += 4) {
                dst.set(dstOff/**/, Float.intBitsToFloat(data.getInt(srcOff/**/)));
                dst.set(dstOff + 1, Float.intBitsToFloat(data.getInt(srcOff + 4)));
                dst.set(dstOff + 2, Float.intBitsToFloat(data.getInt(srcOff + 8)));
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR32G32B32A32Sfloat(TileContext ctx, Surface src, Floats.Mutable dst) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = src.offset(ctx.x, ctx.y + row, ctx.z);
            int dstOff = row * ctx.width * 4;
            for (int col = 0; col < ctx.width; col++, srcOff += 16, dstOff += 4) {
                dst.set(dstOff/**/, Float.intBitsToFloat(data.getInt(srcOff/**/)));
                dst.set(dstOff + 1, Float.intBitsToFloat(data.getInt(srcOff + 4)));
                dst.set(dstOff + 2, Float.intBitsToFloat(data.getInt(srcOff + 8)));
                dst.set(dstOff + 3, Float.intBitsToFloat(data.getInt(srcOff + 12)));
            }
        }
    }


    private static void unpackB8G8R8Unorm(TileContext ctx, Surface src, Floats.Mutable dst) {
        var srgb = src.colorSpace() == TextureColorSpace.SRGB;
        var data = src.data();
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = src.offset(ctx.x, ctx.y + row, ctx.z);
            int dstOff = row * ctx.width * 4;
            for (int col = 0; col < ctx.width; col++, srcOff += 3, dstOff += 4) {
                dst.set(dstOff/**/, unpackUNorm8(data[srcOff/**/], srgb));
                dst.set(dstOff + 1, unpackUNorm8(data[srcOff + 1], srgb));
                dst.set(dstOff + 2, unpackUNorm8(data[srcOff + 2], srgb));
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackB8G8R8A8Unorm(TileContext ctx, Surface src, Floats.Mutable dst) {
        var srgb = src.colorSpace() == TextureColorSpace.SRGB;
        var data = src.data();
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = src.offset(ctx.x, ctx.y + row, ctx.z);
            int dstOff = row * ctx.width * 4;
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 4) {
                dst.set(dstOff/**/, unpackUNorm8(data[srcOff + 2], srgb));
                dst.set(dstOff + 1, unpackUNorm8(data[srcOff + 1], srgb));
                dst.set(dstOff + 2, unpackUNorm8(data[srcOff/**/], srgb));
                dst.set(dstOff + 3, unpackUNorm8(data[srcOff + 3], false)); // alpha is always linear
            }
        }
    }

    private static float unpackUNorm8(byte b, boolean srgb) {
        return srgb ? Srgb.srgbByteToLinear(b) : FloatMath.unpackUNorm8(b);
    }
}
