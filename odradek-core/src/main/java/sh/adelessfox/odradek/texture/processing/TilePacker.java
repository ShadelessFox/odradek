package sh.adelessfox.odradek.texture.processing;

import sh.adelessfox.odradek.texture.Surface;
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
            // @formatter:off
            case R8_UNORM ->
                (ctx, src) -> packR8Unorm(src, ctx.width, ctx.height, dst, ctx.x, ctx.y, ctx.z);
            case R8G8_UNORM ->
                (ctx, src) -> packR8G8Unorm(src, ctx.width, ctx.height, dst, ctx.x, ctx.y, ctx.z);
            case R8G8B8_UNORM ->
                (ctx, src) -> packR8G8B8Unorm(src, ctx.width, ctx.height, dst, ctx.x, ctx.y, ctx.z);
            case R8G8B8A8_UNORM ->
                (ctx, src) -> packR8G8B8A8Unorm(src, ctx.width, ctx.height, dst, ctx.x, ctx.y, ctx.z);
            case R16_UNORM ->
                (ctx, src) -> packR16Unorm(src, ctx.width, ctx.height, dst, ctx.x, ctx.y, ctx.z);
            case R16G16_UNORM ->
                (ctx, src) -> packR16G16Unorm(src, ctx.width, ctx.height, dst, ctx.x, ctx.y, ctx.z);
            case R16G16B16_UNORM ->
                (ctx, src) -> packR16G16B16Unorm(src, ctx.width, ctx.height, dst, ctx.x, ctx.y, ctx.z);
            case R16G16B16A16_UNORM ->
                (ctx, src) -> packR16G16B16A16Unorm(src, ctx.width, ctx.height, dst, ctx.x, ctx.y, ctx.z);
            case R16_SFLOAT ->
                (ctx, src) -> packR16Sfloat(src, ctx.width, ctx.height, dst, ctx.x, ctx.y, ctx.z);
            case R16G16_SFLOAT ->
                (ctx, src) -> packR16G16Sfloat(src, ctx.width, ctx.height, dst, ctx.x, ctx.y, ctx.z);
            case R16G16B16_SFLOAT ->
                (ctx, src) -> packR16G16B16Sfloat(src, ctx.width, ctx.height, dst, ctx.x, ctx.y, ctx.z);
            case R16G16B16A16_SFLOAT ->
                (ctx, src) -> packR16G16B16A16Sfloat(src, ctx.width, ctx.height, dst, ctx.x, ctx.y, ctx.z);
            case R32_SFLOAT ->
                (ctx, src) -> packR32Sfloat(src, ctx.width, ctx.height, dst, ctx.x, ctx.y, ctx.z);
            case R32G32_SFLOAT ->
                (ctx, src) -> packR32G32Sfloat(src, ctx.width, ctx.height, dst, ctx.x, ctx.y, ctx.z);
            case R32G32B32_SFLOAT ->
                (ctx, src) -> packR32G32B32Sfloat(src, ctx.width, ctx.height, dst, ctx.x, ctx.y, ctx.z);
            case R32G32B32A32_SFLOAT ->
                (ctx, src) -> packR32G32B32A32Sfloat(src, ctx.width, ctx.height, dst, ctx.x, ctx.y, ctx.z);
            case B8G8R8_UNORM ->
                (ctx, src) -> packB8G8R8Unorm(src, ctx.width, ctx.height, dst, ctx.x, ctx.y, ctx.z);
            case B8G8R8A8_UNORM ->
                (ctx, src) -> packB8G8R8A8Unorm(src, ctx.width, ctx.height, dst, ctx.x, ctx.y, ctx.z);
            default -> throw new UnsupportedOperationException(dst.format().name());
            // @formatter:on
        };
    }

    private static void packR8Unorm(Floats src, int width, int height, Surface dst, int x, int y, int z) {
        byte[] data = dst.data();
        for (int row = 0; row < height; row++) {
            int srcOff = row * width * 4;
            int dstOff = dst.offset(x, y + row, z);
            for (int col = 0; col < width; col++, srcOff += 4, dstOff++) {
                data[dstOff] = FloatMath.packUNorm8(src.get(srcOff));
            }
        }
    }

    private static void packR8G8Unorm(Floats src, int width, int height, Surface dst, int x, int y, int z) {
        byte[] data = dst.data();
        for (int row = 0; row < height; row++) {
            int srcOff = row * width * 4;
            int dstOff = dst.offset(x, y + row, z);
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 2) {
                data[dstOff/**/] = FloatMath.packUNorm8(src.get(srcOff/**/));
                data[dstOff + 1] = FloatMath.packUNorm8(src.get(srcOff + 1));
            }
        }
    }

    private static void packR8G8B8Unorm(Floats src, int width, int height, Surface dst, int x, int y, int z) {
        byte[] data = dst.data();
        for (int row = 0; row < height; row++) {
            int srcOff = row * width * 4;
            int dstOff = dst.offset(x, y + row, z);
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 3) {
                data[dstOff/**/] = FloatMath.packUNorm8(src.get(srcOff/**/));
                data[dstOff + 1] = FloatMath.packUNorm8(src.get(srcOff + 1));
                data[dstOff + 2] = FloatMath.packUNorm8(src.get(srcOff + 2));
            }
        }
    }

    private static void packR8G8B8A8Unorm(Floats src, int width, int height, Surface dst, int x, int y, int z) {
        byte[] data = dst.data();
        for (int row = 0; row < height; row++) {
            int srcOff = row * width * 4;
            int dstOff = dst.offset(x, y + row, z);
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 4) {
                data[dstOff/**/] = FloatMath.packUNorm8(src.get(srcOff/**/));
                data[dstOff + 1] = FloatMath.packUNorm8(src.get(srcOff + 1));
                data[dstOff + 2] = FloatMath.packUNorm8(src.get(srcOff + 2));
                data[dstOff + 3] = FloatMath.packUNorm8(src.get(srcOff + 3));
            }
        }
    }

    private static void packR16Unorm(Floats src, int width, int height, Surface dst, int x, int y, int z) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < height; row++) {
            int srcOff = row * width * 4;
            int dstOff = dst.offset(x, y + row, z);
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 2) {
                data.setShort(dstOff, FloatMath.packUNorm16(src.get(srcOff)));
            }
        }
    }

    private static void packR16G16Unorm(Floats src, int width, int height, Surface dst, int x, int y, int z) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < height; row++) {
            int srcOff = row * width * 4;
            int dstOff = dst.offset(x, y + row, z);
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 4) {
                data.setShort(dstOff/**/, FloatMath.packUNorm16(src.get(srcOff/**/)));
                data.setShort(dstOff + 2, FloatMath.packUNorm16(src.get(srcOff + 1)));
            }
        }
    }

    private static void packR16G16B16Unorm(Floats src, int width, int height, Surface dst, int x, int y, int z) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < height; row++) {
            int srcOff = row * width * 4;
            int dstOff = dst.offset(x, y + row, z);
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 6) {
                data.setShort(dstOff/**/, FloatMath.packUNorm16(src.get(srcOff/**/)));
                data.setShort(dstOff + 2, FloatMath.packUNorm16(src.get(srcOff + 1)));
                data.setShort(dstOff + 4, FloatMath.packUNorm16(src.get(srcOff + 2)));
            }
        }
    }

    private static void packR16G16B16A16Unorm(Floats src, int width, int height, Surface dst, int x, int y, int z) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < height; row++) {
            int srcOff = row * width * 4;
            int dstOff = dst.offset(x, y + row, z);
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 8) {
                data.setShort(dstOff/**/, FloatMath.packUNorm16(src.get(srcOff/**/)));
                data.setShort(dstOff + 2, FloatMath.packUNorm16(src.get(srcOff + 1)));
                data.setShort(dstOff + 4, FloatMath.packUNorm16(src.get(srcOff + 2)));
                data.setShort(dstOff + 6, FloatMath.packUNorm16(src.get(srcOff + 3)));
            }
        }
    }

    private static void packR16Sfloat(Floats src, int width, int height, Surface dst, int x, int y, int z) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < height; row++) {
            int srcOff = row * width * 4;
            int dstOff = dst.offset(x, y + row, z);
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 2) {
                data.setShort(dstOff, Float.floatToFloat16(src.get(srcOff)));
            }
        }
    }

    private static void packR16G16Sfloat(Floats src, int width, int height, Surface dst, int x, int y, int z) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < height; row++) {
            int srcOff = row * width * 4;
            int dstOff = dst.offset(x, y + row, z);
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 4) {
                data.setShort(dstOff/**/, Float.floatToFloat16(src.get(srcOff/**/)));
                data.setShort(dstOff + 2, Float.floatToFloat16(src.get(srcOff + 1)));
            }
        }
    }

    private static void packR16G16B16Sfloat(Floats src, int width, int height, Surface dst, int x, int y, int z) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < height; row++) {
            int srcOff = row * width * 4;
            int dstOff = dst.offset(x, y + row, z);
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 6) {
                data.setShort(dstOff/**/, Float.floatToFloat16(src.get(srcOff/**/)));
                data.setShort(dstOff + 2, Float.floatToFloat16(src.get(srcOff + 1)));
                data.setShort(dstOff + 4, Float.floatToFloat16(src.get(srcOff + 2)));
            }
        }
    }

    private static void packR16G16B16A16Sfloat(Floats src, int width, int height, Surface dst, int x, int y, int z) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < height; row++) {
            int srcOff = row * width * 4;
            int dstOff = dst.offset(x, y + row, z);
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 8) {
                data.setShort(dstOff/**/, Float.floatToFloat16(src.get(srcOff/**/)));
                data.setShort(dstOff + 2, Float.floatToFloat16(src.get(srcOff + 1)));
                data.setShort(dstOff + 4, Float.floatToFloat16(src.get(srcOff + 2)));
                data.setShort(dstOff + 6, Float.floatToFloat16(src.get(srcOff + 3)));
            }
        }
    }

    private static void packR32Sfloat(Floats src, int width, int height, Surface dst, int x, int y, int z) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < height; row++) {
            int srcOff = row * width * 4;
            int dstOff = dst.offset(x, y + row, z);
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 4) {
                data.setInt(dstOff, Float.floatToIntBits(src.get(srcOff)));
            }
        }
    }

    private static void packR32G32Sfloat(Floats src, int width, int height, Surface dst, int x, int y, int z) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < height; row++) {
            int srcOff = row * width * 4;
            int dstOff = dst.offset(x, y + row, z);
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 8) {
                data.setInt(dstOff/**/, Float.floatToIntBits(src.get(srcOff/**/)));
                data.setInt(dstOff + 4, Float.floatToIntBits(src.get(srcOff + 1)));
            }
        }
    }

    private static void packR32G32B32Sfloat(Floats src, int width, int height, Surface dst, int x, int y, int z) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < height; row++) {
            int srcOff = row * width * 4;
            int dstOff = dst.offset(x, y + row, z);
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 12) {
                data.setInt(dstOff/**/, Float.floatToIntBits(src.get(srcOff/**/)));
                data.setInt(dstOff + 4, Float.floatToIntBits(src.get(srcOff + 1)));
                data.setInt(dstOff + 8, Float.floatToIntBits(src.get(srcOff + 2)));
            }
        }
    }

    private static void packR32G32B32A32Sfloat(Floats src, int width, int height, Surface dst, int x, int y, int z) {
        var data = Bytes.Mutable.wrap(dst.data());
        for (int row = 0; row < height; row++) {
            int srcOff = row * width * 4;
            int dstOff = dst.offset(x, y + row, z);
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 16) {
                data.setInt(dstOff/**/, Float.floatToIntBits(src.get(srcOff/**/)));
                data.setInt(dstOff + 4, Float.floatToIntBits(src.get(srcOff + 1)));
                data.setInt(dstOff + 8, Float.floatToIntBits(src.get(srcOff + 2)));
                data.setInt(dstOff + 12, Float.floatToIntBits(src.get(srcOff + 3)));
            }
        }
    }

    private static void packB8G8R8Unorm(Floats src, int width, int height, Surface dst, int x, int y, int z) {
        byte[] data = dst.data();
        for (int row = 0; row < height; row++) {
            int srcOff = row * width * 4;
            int dstOff = dst.offset(x, y + row, z);
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 3) {
                data[dstOff/**/] = FloatMath.packUNorm8(src.get(srcOff + 2));
                data[dstOff + 1] = FloatMath.packUNorm8(src.get(srcOff + 1));
                data[dstOff + 2] = FloatMath.packUNorm8(src.get(srcOff/**/));
            }
        }
    }

    private static void packB8G8R8A8Unorm(Floats src, int width, int height, Surface dst, int x, int y, int z) {
        byte[] data = dst.data();
        for (int row = 0; row < height; row++) {
            int srcOff = row * width * 4;
            int dstOff = dst.offset(x, y + row, z);
            for (int col = 0; col < width; col++, srcOff += 4, dstOff += 4) {
                data[dstOff/**/] = FloatMath.packUNorm8(src.get(srcOff + 2));
                data[dstOff + 1] = FloatMath.packUNorm8(src.get(srcOff + 1));
                data[dstOff + 2] = FloatMath.packUNorm8(src.get(srcOff/**/));
                data[dstOff + 3] = FloatMath.packUNorm8(src.get(srcOff + 3));
            }
        }
    }
}
