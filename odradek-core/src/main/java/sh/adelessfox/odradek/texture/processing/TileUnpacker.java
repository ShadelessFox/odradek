package sh.adelessfox.odradek.texture.processing;

import be.twofold.tinybcdec.BlockDecoder;
import sh.adelessfox.odradek.texture.Surface;
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
            // @formatter:off
            case R8_UNORM ->
                (ctx, dst) -> unpackR8Unorm(src, ctx.x, ctx.y, ctx.z, dst, ctx.width, ctx.height);
            case R8G8_UNORM ->
                (ctx, dst) -> unpackR8G8Unorm(src, ctx.x, ctx.y, ctx.z, dst, ctx.width, ctx.height);
            case R8G8B8_UNORM ->
                (ctx, dst) -> unpackR8G8B8Unorm(src, ctx.x, ctx.y, ctx.z, dst, ctx.width, ctx.height);
            case R8G8B8A8_UNORM ->
                (ctx, dst) -> unpackR8G8B8A8Unorm(src, ctx.x, ctx.y, ctx.z, dst, ctx.width, ctx.height);
            case R16_UNORM ->
                (ctx, dst) -> unpackR16Unorm(src, ctx.x, ctx.y, ctx.z, dst, ctx.width, ctx.height);
            case R16G16_UNORM ->
                (ctx, dst) -> unpackR16G16Unorm(src, ctx.x, ctx.y, ctx.z, dst, ctx.width, ctx.height);
            case R16G16B16_UNORM ->
                (ctx, dst) -> unpackR16G16B16Unorm(src, ctx.x, ctx.y, ctx.z, dst, ctx.width, ctx.height);
            case R16G16B16A16_UNORM ->
                (ctx, dst) -> unpackR16G16B16A16Unorm(src, ctx.x, ctx.y, ctx.z, dst, ctx.width, ctx.height);
            case R16_SFLOAT ->
                (ctx, dst) -> unpackR16Sfloat(src, ctx.x, ctx.y, ctx.z, dst, ctx.width, ctx.height);
            case R16G16_SFLOAT ->
                (ctx, dst) -> unpackR16G16Sfloat(src, ctx.x, ctx.y, ctx.z, dst, ctx.width, ctx.height);
            case R16G16B16_SFLOAT ->
                (ctx, dst) -> unpackR16G16B16Sfloat(src, ctx.x, ctx.y, ctx.z, dst, ctx.width, ctx.height);
            case R16G16B16A16_SFLOAT ->
                (ctx, dst) -> unpackR16G16B16A16Sfloat(src, ctx.x, ctx.y, ctx.z, dst, ctx.width, ctx.height);
            case R32_SFLOAT ->
                (ctx, dst) -> unpackR32Sfloat(src, ctx.x, ctx.y, ctx.z, dst, ctx.width, ctx.height);
            case R32G32_SFLOAT ->
                (ctx, dst) -> unpackR32G32Sfloat(src, ctx.x, ctx.y, ctx.z, dst, ctx.width, ctx.height);
            case R32G32B32_SFLOAT ->
                (ctx, dst) -> unpackR32G32B32Sfloat(src, ctx.x, ctx.y, ctx.z, dst, ctx.width, ctx.height);
            case R32G32B32A32_SFLOAT ->
                (ctx, dst) -> unpackR32G32B32A32Sfloat(src, ctx.x, ctx.y, ctx.z, dst, ctx.width, ctx.height);
            case B8G8R8_UNORM ->
                (ctx, dst) -> unpackB8G8R8Unorm(src, ctx.x, ctx.y, ctx.z, dst, ctx.width, ctx.height);
            case B8G8R8A8_UNORM ->
                (ctx, dst) -> unpackB8G8R8A8Unorm(src, ctx.x, ctx.y, ctx.z, dst, ctx.width, ctx.height);
            default -> throw new UnsupportedOperationException(src.format().name());
            // @formatter:on
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
        var result = Surface.create(src.width(), src.height(), format);
        decoder.decode(src.data(), 0, src.width(), src.height(), result.data(), 0);
        return result;
    }

    private static void unpackR8Unorm(Surface src, int x, int y, int z, Floats.Mutable dst, int width, int height) {
        var data = src.data();
        for (int row = 0; row < height; row++) {
            int srcOff = src.offset(x, y + row, z);
            int dstOff = row * width * 4;
            for (int col = 0; col < width; col++, srcOff++, dstOff += 4) {
                dst.set(dstOff/**/, FloatMath.unpackUNorm8(data[srcOff]));
                dst.set(dstOff + 1, 0.0f);
                dst.set(dstOff + 2, 0.0f);
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR8G8Unorm(Surface src, int x, int y, int z, Floats.Mutable dst, int width, int height) {
        var data = src.data();
        for (int row = 0; row < height; row++) {
            int srcOff = src.offset(x, y + row, z);
            int dstOff = row * width * 4;
            for (int col = 0; col < width; col++, srcOff += 2, dstOff += 4) {
                dst.set(dstOff/**/, FloatMath.unpackUNorm8(data[srcOff/**/]));
                dst.set(dstOff + 1, FloatMath.unpackUNorm8(data[srcOff + 1]));
                dst.set(dstOff + 2, 0.0f);
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR8G8B8Unorm(Surface src, int x, int y, int z, Floats.Mutable dst, int width, int height) {
        var data = src.data();
        for (int row = 0; row < height; row++) {
            int srcOff = src.offset(x, y + row, z);
            int dstOff = row * width * 4;
            for (int col = 0; col < width; col++, srcOff += 3, dstOff += 4) {
                dst.set(dstOff/**/, FloatMath.unpackUNorm8(data[srcOff/**/]));
                dst.set(dstOff + 1, FloatMath.unpackUNorm8(data[srcOff + 1]));
                dst.set(dstOff + 2, FloatMath.unpackUNorm8(data[srcOff + 2]));
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR8G8B8A8Unorm(Surface src, int x, int y, int z, Floats.Mutable dst, int width, int height) {
        var data = src.data();
        for (int row = 0; row < height; row++) {
            int srcOff = src.offset(x, y + row, z);
            int dstOff = row * width * 4;
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 4) {
                dst.set(dstOff/**/, FloatMath.unpackUNorm8(data[srcOff/**/]));
                dst.set(dstOff + 1, FloatMath.unpackUNorm8(data[srcOff + 1]));
                dst.set(dstOff + 2, FloatMath.unpackUNorm8(data[srcOff + 2]));
                dst.set(dstOff + 3, FloatMath.unpackUNorm8(data[srcOff + 3]));
            }
        }
    }

    private static void unpackR16Unorm(Surface src, int x, int y, int z, Floats.Mutable dst, int width, int height) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < height; row++) {
            int srcOff = src.offset(x, y + row, z);
            int dstOff = row * width * 4;
            for (int col = 0; col < width; col++, srcOff += 2, dstOff += 4) {
                dst.set(dstOff/**/, FloatMath.unpackUNorm16(data.getShort(srcOff)));
                dst.set(dstOff + 1, 0.0f);
                dst.set(dstOff + 2, 0.0f);
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR16G16Unorm(Surface src, int x, int y, int z, Floats.Mutable dst, int width, int height) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < height; row++) {
            int srcOff = src.offset(x, y + row, z);
            int dstOff = row * width * 4;
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 4) {
                dst.set(dstOff/**/, FloatMath.unpackUNorm16(data.getShort(srcOff/**/)));
                dst.set(dstOff + 1, FloatMath.unpackUNorm16(data.getShort(srcOff + 2)));
                dst.set(dstOff + 2, 0.0f);
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR16G16B16Unorm(Surface src, int x, int y, int z, Floats.Mutable dst, int width, int height) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < height; row++) {
            int srcOff = src.offset(x, y + row, z);
            int dstOff = row * width * 4;
            for (int col = 0; col < width; col++, srcOff += 6, dstOff += 4) {
                dst.set(dstOff/**/, FloatMath.unpackUNorm16(data.getShort(srcOff/**/)));
                dst.set(dstOff + 1, FloatMath.unpackUNorm16(data.getShort(srcOff + 2)));
                dst.set(dstOff + 2, FloatMath.unpackUNorm16(data.getShort(srcOff + 4)));
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR16G16B16A16Unorm(Surface src, int x, int y, int z, Floats.Mutable dst, int width, int height) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < height; row++) {
            int srcOff = src.offset(x, y + row, z);
            int dstOff = row * width * 4;
            for (int col = 0; col < width; col++, srcOff += 8, dstOff += 4) {
                dst.set(dstOff/**/, FloatMath.unpackUNorm16(data.getShort(srcOff/**/)));
                dst.set(dstOff + 1, FloatMath.unpackUNorm16(data.getShort(srcOff + 2)));
                dst.set(dstOff + 2, FloatMath.unpackUNorm16(data.getShort(srcOff + 4)));
                dst.set(dstOff + 3, FloatMath.unpackUNorm16(data.getShort(srcOff + 6)));
            }
        }
    }

    private static void unpackR16Sfloat(Surface src, int x, int y, int z, Floats.Mutable dst, int width, int height) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < height; row++) {
            int srcOff = src.offset(x, y + row, z);
            int dstOff = row * width * 4;
            for (int col = 0; col < width; col++, srcOff += 2, dstOff += 4) {
                dst.set(dstOff/**/, Float.float16ToFloat(data.getShort(srcOff)));
                dst.set(dstOff + 1, 0.0f);
                dst.set(dstOff + 2, 0.0f);
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR16G16Sfloat(Surface src, int x, int y, int z, Floats.Mutable dst, int width, int height) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < height; row++) {
            int srcOff = src.offset(x, y + row, z);
            int dstOff = row * width * 4;
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 4) {
                dst.set(dstOff/**/, Float.float16ToFloat(data.getShort(srcOff/**/)));
                dst.set(dstOff + 1, Float.float16ToFloat(data.getShort(srcOff + 2)));
                dst.set(dstOff + 2, 0.0f);
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR16G16B16Sfloat(Surface src, int x, int y, int z, Floats.Mutable dst, int width, int height) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < height; row++) {
            int srcOff = src.offset(x, y + row, z);
            int dstOff = row * width * 4;
            for (int col = 0; col < width; col++, srcOff += 6, dstOff += 4) {
                dst.set(dstOff/**/, Float.float16ToFloat(data.getShort(srcOff/**/)));
                dst.set(dstOff + 1, Float.float16ToFloat(data.getShort(srcOff + 2)));
                dst.set(dstOff + 2, Float.float16ToFloat(data.getShort(srcOff + 4)));
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR16G16B16A16Sfloat(Surface src, int x, int y, int z, Floats.Mutable dst, int width, int height) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < height; row++) {
            int srcOff = src.offset(x, y + row, z);
            int dstOff = row * width * 4;
            for (int col = 0; col < width; col++, srcOff += 8, dstOff += 4) {
                dst.set(dstOff/**/, Float.float16ToFloat(data.getShort(srcOff/**/)));
                dst.set(dstOff + 1, Float.float16ToFloat(data.getShort(srcOff + 2)));
                dst.set(dstOff + 2, Float.float16ToFloat(data.getShort(srcOff + 4)));
                dst.set(dstOff + 3, Float.float16ToFloat(data.getShort(srcOff + 6)));
            }
        }
    }

    private static void unpackR32Sfloat(Surface src, int x, int y, int z, Floats.Mutable dst, int width, int height) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < height; row++) {
            int srcOff = src.offset(x, y + row, z);
            int dstOff = row * width * 4;
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 4) {
                dst.set(dstOff/**/, Float.intBitsToFloat(data.getInt(srcOff)));
                dst.set(dstOff + 1, 0.0f);
                dst.set(dstOff + 2, 0.0f);
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR32G32Sfloat(Surface src, int x, int y, int z, Floats.Mutable dst, int width, int height) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < height; row++) {
            int srcOff = src.offset(x, y + row, z);
            int dstOff = row * width * 4;
            for (int col = 0; col < width; col++, srcOff += 8, dstOff += 4) {
                dst.set(dstOff/**/, Float.intBitsToFloat(data.getInt(srcOff/**/)));
                dst.set(dstOff + 1, Float.intBitsToFloat(data.getInt(srcOff + 4)));
                dst.set(dstOff + 2, 0.0f);
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR32G32B32Sfloat(Surface src, int x, int y, int z, Floats.Mutable dst, int width, int height) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < height; row++) {
            int srcOff = src.offset(x, y + row, z);
            int dstOff = row * width * 4;
            for (int col = 0; col < width; col++, srcOff += 12, dstOff += 4) {
                dst.set(dstOff/**/, Float.intBitsToFloat(data.getInt(srcOff/**/)));
                dst.set(dstOff + 1, Float.intBitsToFloat(data.getInt(srcOff + 4)));
                dst.set(dstOff + 2, Float.intBitsToFloat(data.getInt(srcOff + 8)));
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackR32G32B32A32Sfloat(Surface src, int x, int y, int z, Floats.Mutable dst, int width, int height) {
        var data = Bytes.wrap(src.data());
        for (int row = 0; row < height; row++) {
            int srcOff = src.offset(x, y + row, z);
            int dstOff = row * width * 4;
            for (int col = 0; col < width; col++, srcOff += 16, dstOff += 4) {
                dst.set(dstOff/**/, Float.intBitsToFloat(data.getInt(srcOff/**/)));
                dst.set(dstOff + 1, Float.intBitsToFloat(data.getInt(srcOff + 4)));
                dst.set(dstOff + 2, Float.intBitsToFloat(data.getInt(srcOff + 8)));
                dst.set(dstOff + 3, Float.intBitsToFloat(data.getInt(srcOff + 12)));
            }
        }
    }


    private static void unpackB8G8R8Unorm(Surface src, int x, int y, int z, Floats.Mutable dst, int width, int height) {
        var data = src.data();
        for (int row = 0; row < height; row++) {
            int srcOff = src.offset(x, y + row, z);
            int dstOff = row * width * 4;
            for (int col = 0; col < width; col++, srcOff += 3, dstOff += 4) {
                dst.set(dstOff/**/, FloatMath.unpackUNorm8(data[srcOff/**/]));
                dst.set(dstOff + 1, FloatMath.unpackUNorm8(data[srcOff + 1]));
                dst.set(dstOff + 2, FloatMath.unpackUNorm8(data[srcOff + 2]));
                dst.set(dstOff + 3, 1.0f);
            }
        }
    }

    private static void unpackB8G8R8A8Unorm(Surface src, int x, int y, int z, Floats.Mutable dst, int width, int height) {
        var data = src.data();
        for (int row = 0; row < height; row++) {
            int srcOff = src.offset(x, y + row, z);
            int dstOff = row * width * 4;
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 4) {
                dst.set(dstOff/**/, FloatMath.unpackUNorm8(data[srcOff + 2]));
                dst.set(dstOff + 1, FloatMath.unpackUNorm8(data[srcOff + 1]));
                dst.set(dstOff + 2, FloatMath.unpackUNorm8(data[srcOff/**/]));
                dst.set(dstOff + 3, FloatMath.unpackUNorm8(data[srcOff + 3]));
            }
        }
    }
}
