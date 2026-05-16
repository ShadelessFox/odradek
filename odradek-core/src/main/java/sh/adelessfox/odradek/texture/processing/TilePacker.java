package sh.adelessfox.odradek.texture.processing;

import sh.adelessfox.odradek.texture.Surface;
import sh.adelessfox.odradek.texture.TextureColorSpace;
import sh.adelessfox.odradek.util.Srgb;
import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.collect.Floats;
import wtf.reversed.toolbox.math.FloatMath;

@FunctionalInterface
public interface TilePacker {
    void pack(TileContext ctx, Floats src);

    static TilePacker into(Surface dst) {
        if (dst.format().isCompressed()) {
            throw new UnsupportedOperationException("Tile packing is not supported for compressed formats");
        }
        return switch (dst.format()) {
            case R8_UNORM -> (ctx, src) -> packR8Unorm(ctx, src, dst);
            case R8G8_UNORM -> (ctx, src) -> packR8G8Unorm(ctx, src, dst);
            case R8G8B8_UNORM -> (ctx, src) -> packR8G8B8Unorm(ctx, src, dst);
            case R8G8B8A8_UNORM -> (ctx, src) -> packR8G8B8A8Unorm(ctx, src, dst);
            case R16_UNORM -> (ctx, src) -> packR16Unorm(ctx, src, dst);
            case R16G16_UNORM -> (ctx, src) -> packR16G16Unorm(ctx, src, dst);
            case R16G16B16_UNORM -> (ctx, src) -> packR16G16B16Unorm(ctx, src, dst);
            case R16G16B16A16_UNORM -> (ctx, src) -> packR16G16B16A16Unorm(ctx, src, dst);
            case R16_SFLOAT -> (ctx, src) -> packR16Sfloat(ctx, src, dst);
            case R16G16_SFLOAT -> (ctx, src) -> packR16G16Sfloat(ctx, src, dst);
            case R16G16B16_SFLOAT -> (ctx, src) -> packR16G16B16Sfloat(ctx, src, dst);
            case R16G16B16A16_SFLOAT -> (ctx, src) -> packR16G16B16A16Sfloat(ctx, src, dst);
            case R32_SFLOAT -> (ctx, src) -> packR32Sfloat(ctx, src, dst);
            case R32G32_SFLOAT -> (ctx, src) -> packR32G32Sfloat(ctx, src, dst);
            case R32G32B32_SFLOAT -> (ctx, src) -> packR32G32B32Sfloat(ctx, src, dst);
            case R32G32B32A32_SFLOAT -> (ctx, src) -> packR32G32B32A32Sfloat(ctx, src, dst);
            case B8G8R8_UNORM -> (ctx, src) -> packB8G8R8Unorm(ctx, src, dst);
            case B8G8R8A8_UNORM -> (ctx, src) -> packB8G8R8A8Unorm(ctx, src, dst);
            default -> throw new UnsupportedOperationException(dst.format().name());
        };
    }

    private static void packR8Unorm(TileContext ctx, Floats src, Surface dst) {
        var srgb = dst.colorSpace() == TextureColorSpace.SRGB;
        var data = dst.data();
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = row * ctx.width * 4;
            int dstOff = dst.offset(ctx.x, ctx.y + row, ctx.z);
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff++) {
                data[dstOff] = packUNorm8(src.get(srcOff), srgb);
            }
        }
    }

    private static void packR8G8Unorm(TileContext ctx, Floats src, Surface dst) {
        var srgb = dst.colorSpace() == TextureColorSpace.SRGB;
        var data = dst.data();
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = row * ctx.width * 4;
            int dstOff = dst.offset(ctx.x, ctx.y + row, ctx.z);
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 2) {
                data[dstOff/**/] = packUNorm8(src.get(srcOff/**/), srgb);
                data[dstOff + 1] = packUNorm8(src.get(srcOff + 1), srgb);
            }
        }
    }

    private static void packR8G8B8Unorm(TileContext ctx, Floats src, Surface dst) {
        var srgb = dst.colorSpace() == TextureColorSpace.SRGB;
        var data = dst.data();
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = row * ctx.width * 4;
            int dstOff = dst.offset(ctx.x, ctx.y + row, ctx.z);
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 3) {
                data[dstOff/**/] = packUNorm8(src.get(srcOff/**/), srgb);
                data[dstOff + 1] = packUNorm8(src.get(srcOff + 1), srgb);
                data[dstOff + 2] = packUNorm8(src.get(srcOff + 2), srgb);
            }
        }
    }

    private static void packR8G8B8A8Unorm(TileContext ctx, Floats src, Surface dst) {
        var srgb = dst.colorSpace() == TextureColorSpace.SRGB;
        var data = dst.data();
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = row * ctx.width * 4;
            int dstOff = dst.offset(ctx.x, ctx.y + row, ctx.z);
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 4) {
                data[dstOff/**/] = packUNorm8(src.get(srcOff/**/), srgb);
                data[dstOff + 1] = packUNorm8(src.get(srcOff + 1), srgb);
                data[dstOff + 2] = packUNorm8(src.get(srcOff + 2), srgb);
                data[dstOff + 3] = packUNorm8(src.get(srcOff + 3), false); // alpha is always linear
            }
        }
    }

    private static void packR16Unorm(TileContext ctx, Floats src, Surface dst) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = row * ctx.width * 4;
            int dstOff = dst.offset(ctx.x, ctx.y + row, ctx.z);
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 2) {
                data.setShort(dstOff, FloatMath.packUNorm16(src.get(srcOff)));
            }
        }
    }

    private static void packR16G16Unorm(TileContext ctx, Floats src, Surface dst) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = row * ctx.width * 4;
            int dstOff = dst.offset(ctx.x, ctx.y + row, ctx.z);
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 4) {
                data.setShort(dstOff/**/, FloatMath.packUNorm16(src.get(srcOff/**/)));
                data.setShort(dstOff + 2, FloatMath.packUNorm16(src.get(srcOff + 1)));
            }
        }
    }

    private static void packR16G16B16Unorm(TileContext ctx, Floats src, Surface dst) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = row * ctx.width * 4;
            int dstOff = dst.offset(ctx.x, ctx.y + row, ctx.z);
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 6) {
                data.setShort(dstOff/**/, FloatMath.packUNorm16(src.get(srcOff/**/)));
                data.setShort(dstOff + 2, FloatMath.packUNorm16(src.get(srcOff + 1)));
                data.setShort(dstOff + 4, FloatMath.packUNorm16(src.get(srcOff + 2)));
            }
        }
    }

    private static void packR16G16B16A16Unorm(TileContext ctx, Floats src, Surface dst) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = row * ctx.width * 4;
            int dstOff = dst.offset(ctx.x, ctx.y + row, ctx.z);
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 8) {
                data.setShort(dstOff/**/, FloatMath.packUNorm16(src.get(srcOff/**/)));
                data.setShort(dstOff + 2, FloatMath.packUNorm16(src.get(srcOff + 1)));
                data.setShort(dstOff + 4, FloatMath.packUNorm16(src.get(srcOff + 2)));
                data.setShort(dstOff + 6, FloatMath.packUNorm16(src.get(srcOff + 3)));
            }
        }
    }

    private static void packR16Sfloat(TileContext ctx, Floats src, Surface dst) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = row * ctx.width * 4;
            int dstOff = dst.offset(ctx.x, ctx.y + row, ctx.z);
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 2) {
                data.setShort(dstOff, Float.floatToFloat16(src.get(srcOff)));
            }
        }
    }

    private static void packR16G16Sfloat(TileContext ctx, Floats src, Surface dst) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = row * ctx.width * 4;
            int dstOff = dst.offset(ctx.x, ctx.y + row, ctx.z);
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 4) {
                data.setShort(dstOff/**/, Float.floatToFloat16(src.get(srcOff/**/)));
                data.setShort(dstOff + 2, Float.floatToFloat16(src.get(srcOff + 1)));
            }
        }
    }

    private static void packR16G16B16Sfloat(TileContext ctx, Floats src, Surface dst) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = row * ctx.width * 4;
            int dstOff = dst.offset(ctx.x, ctx.y + row, ctx.z);
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 6) {
                data.setShort(dstOff/**/, Float.floatToFloat16(src.get(srcOff/**/)));
                data.setShort(dstOff + 2, Float.floatToFloat16(src.get(srcOff + 1)));
                data.setShort(dstOff + 4, Float.floatToFloat16(src.get(srcOff + 2)));
            }
        }
    }

    private static void packR16G16B16A16Sfloat(TileContext ctx, Floats src, Surface dst) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = row * ctx.width * 4;
            int dstOff = dst.offset(ctx.x, ctx.y + row, ctx.z);
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 8) {
                data.setShort(dstOff/**/, Float.floatToFloat16(src.get(srcOff/**/)));
                data.setShort(dstOff + 2, Float.floatToFloat16(src.get(srcOff + 1)));
                data.setShort(dstOff + 4, Float.floatToFloat16(src.get(srcOff + 2)));
                data.setShort(dstOff + 6, Float.floatToFloat16(src.get(srcOff + 3)));
            }
        }
    }

    private static void packR32Sfloat(TileContext ctx, Floats src, Surface dst) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = row * ctx.width * 4;
            int dstOff = dst.offset(ctx.x, ctx.y + row, ctx.z);
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 4) {
                data.setInt(dstOff, Float.floatToIntBits(src.get(srcOff)));
            }
        }
    }

    private static void packR32G32Sfloat(TileContext ctx, Floats src, Surface dst) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = row * ctx.width * 4;
            int dstOff = dst.offset(ctx.x, ctx.y + row, ctx.z);
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 8) {
                data.setInt(dstOff/**/, Float.floatToIntBits(src.get(srcOff/**/)));
                data.setInt(dstOff + 4, Float.floatToIntBits(src.get(srcOff + 1)));
            }
        }
    }

    private static void packR32G32B32Sfloat(TileContext ctx, Floats src, Surface dst) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = row * ctx.width * 4;
            int dstOff = dst.offset(ctx.x, ctx.y + row, ctx.z);
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 12) {
                data.setInt(dstOff/**/, Float.floatToIntBits(src.get(srcOff/**/)));
                data.setInt(dstOff + 4, Float.floatToIntBits(src.get(srcOff + 1)));
                data.setInt(dstOff + 8, Float.floatToIntBits(src.get(srcOff + 2)));
            }
        }
    }

    private static void packR32G32B32A32Sfloat(TileContext ctx, Floats src, Surface dst) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = row * ctx.width * 4;
            int dstOff = dst.offset(ctx.x, ctx.y + row, ctx.z);
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 16) {
                data.setInt(dstOff/**/, Float.floatToIntBits(src.get(srcOff/**/)));
                data.setInt(dstOff + 4, Float.floatToIntBits(src.get(srcOff + 1)));
                data.setInt(dstOff + 8, Float.floatToIntBits(src.get(srcOff + 2)));
                data.setInt(dstOff + 12, Float.floatToIntBits(src.get(srcOff + 3)));
            }
        }
    }

    private static void packB8G8R8Unorm(TileContext ctx, Floats src, Surface dst) {
        var srgb = dst.colorSpace() == TextureColorSpace.SRGB;
        var data = dst.data();
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = row * ctx.width * 4;
            int dstOff = dst.offset(ctx.x, ctx.y + row, ctx.z);
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 3) {
                data[dstOff/**/] = packUNorm8(src.get(srcOff + 2), srgb);
                data[dstOff + 1] = packUNorm8(src.get(srcOff + 1), srgb);
                data[dstOff + 2] = packUNorm8(src.get(srcOff/**/), srgb);
            }
        }
    }

    private static void packB8G8R8A8Unorm(TileContext ctx, Floats src, Surface dst) {
        var srgb = dst.colorSpace() == TextureColorSpace.SRGB;
        var data = dst.data();
        for (int row = 0; row < ctx.height; row++) {
            int srcOff = row * ctx.width * 4;
            int dstOff = dst.offset(ctx.x, ctx.y + row, ctx.z);
            for (int col = 0; col < ctx.width; col++, srcOff += 4, dstOff += 4) {
                data[dstOff/**/] = packUNorm8(src.get(srcOff + 2), srgb);
                data[dstOff + 1] = packUNorm8(src.get(srcOff + 1), srgb);
                data[dstOff + 2] = packUNorm8(src.get(srcOff/**/), srgb);
                data[dstOff + 3] = packUNorm8(src.get(srcOff + 3), false); // alpha is always linear
            }
        }
    }

    private static byte packUNorm8(float f, boolean srgb) {
        return srgb ? Srgb.linearToSrgbByte(f) : FloatMath.packUNorm8(f);
    }
}
