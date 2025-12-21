package sh.adelessfox.odradek.texture;

import be.twofold.tinybcdec.BlockDecoder;
import sh.adelessfox.odradek.util.Arrays;

import java.nio.ByteOrder;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

final class TextureConverter {
    public static Texture convert(Texture texture, TextureFormat format) {
        if (texture.format() == format) {
            return texture;
        }

        if (format.isCompressed()) {
            throw new UnsupportedOperationException("Compressing textures is not supported");
        }

        var converter = Optional.of(Converter.noop(texture.format()))
            .map(c -> decompress(c.format()).map(c::andThen).orElse(c))
            .map(c -> tonemap(c.format()).map(c::andThen).orElse(c))
            .map(c -> unpack(c.format(), format).map(c::andThen).orElse(c))
            .map(c -> swizzle(c.format(), format).map(c::andThen).orElse(c))
            .orElseThrow();

        if (converter.format() != format) {
            throw new UnsupportedOperationException("Could not convert texture from " + texture.format() + " to " + format);
        }

        return map(texture, format, converter.operator);
    }

    private static Optional<Converter> decompress(TextureFormat srcFormat) {
        if (!srcFormat.isCompressed()) {
            return Optional.empty();
        }

        var format = switch (srcFormat) {
            case BC1_UNORM, BC2_UNORM, BC3_UNORM, BC7_UNORM -> TextureFormat.R8G8B8A8_UNORM;
            case BC4_UNORM, BC4_SNORM -> TextureFormat.R8_UNORM;
            case BC5_UNORM, BC5_SNORM -> TextureFormat.R8G8_UNORM;
            case BC6_UNORM, BC6_SNORM -> TextureFormat.R16G16B16_SFLOAT;
            default -> throw new UnsupportedOperationException(srcFormat.name());
        };

        var decoder = switch (srcFormat) {
            case BC1_UNORM -> BlockDecoder.bc1(true);
            case BC2_UNORM -> BlockDecoder.bc2();
            case BC3_UNORM -> BlockDecoder.bc3();
            case BC4_UNORM -> BlockDecoder.bc4(false);
            case BC4_SNORM -> BlockDecoder.bc4(true);
            case BC5_UNORM -> BlockDecoder.bc5(false);
            case BC5_SNORM -> BlockDecoder.bc5(true);
            case BC6_UNORM -> BlockDecoder.bc6h(false);
            case BC6_SNORM -> BlockDecoder.bc6h(true);
            case BC7_UNORM -> BlockDecoder.bc7();
            default -> throw new UnsupportedOperationException(srcFormat.name());
        };

        UnaryOperator<Surface> operator = surface -> {
            var result = Surface.create(surface.width(), surface.height(), format);
            decoder.decode(surface.data(), 0, surface.width(), surface.height(), result.data(), 0);
            return result;
        };

        return Optional.of(new Converter(operator, format));
    }

    private static Optional<Converter> tonemap(TextureFormat srcFormat) {
        Converter operator = switch (srcFormat) {
            case R16G16B16_SFLOAT -> new Converter(surface -> tonemapF16(surface, TextureFormat.R8G8B8_UNORM), TextureFormat.R8G8B8_UNORM);
            default -> null;
        };

        return Optional.ofNullable(operator);
    }

    private static Optional<Converter> swizzle(TextureFormat srcFormat, TextureFormat dstFormat) {
        if (srcFormat == dstFormat) {
            return Optional.empty();
        }

        var operator = swizzleOperator(srcFormat, dstFormat);
        if (operator == null) {
            return Optional.empty();
        }

        return Optional.of(new Converter(operator, dstFormat));
    }

    private static Optional<Converter> unpack(TextureFormat srcFormat, TextureFormat dstFormat) {
        if (srcFormat == dstFormat) {
            return Optional.empty();
        }

        var operation = unpackOperation(srcFormat, dstFormat);
        if (operation == null) {
            return Optional.empty();
        }

        UnaryOperator<Surface> operator = surface -> {
            var target = Surface.create(surface.width(), surface.height(), dstFormat);

            var src = surface.data();
            var dst = target.data();
            var srcStride = srcFormat.block().size();
            var dstStride = dstFormat.block().size();

            for (int i = 0, o = 0; i < src.length; i += srcStride, o += dstStride) {
                operation.apply(src, i, dst, o);
            }

            return target;
        };

        return Optional.of(new Converter(operator, dstFormat));
    }

    private static UnpackOperation unpackOperation(TextureFormat srcFormat, TextureFormat dstFormat) {
        return switch (srcFormat) {
            case R8_UNORM -> switch (dstFormat) {
                case R8G8_UNORM, R8G8B8_UNORM -> (src, srcPos, dst, dstPos) -> {
                    dst[dstPos/**/] = src[srcPos/**/];
                };
                case B8G8R8_UNORM -> (src, srcPos, dst, dstPos) -> {
                    dst[dstPos + 2] = src[srcPos];
                };
                case R8G8B8A8_UNORM -> (src, srcPos, dst, dstPos) -> {
                    dst[dstPos/**/] = src[srcPos/**/];
                    dst[dstPos + 3] = (byte) 0xFF;
                };
                case B8G8R8A8_UNORM -> (src, srcPos, dst, dstPos) -> {
                    dst[dstPos + 2] = src[srcPos];
                    dst[dstPos + 3] = (byte) 0xFF;
                };
                default -> null;
            };
            case R8G8_UNORM -> switch (dstFormat) {
                case R8G8B8_UNORM -> (src, srcPos, dst, dstPos) -> {
                    dst[dstPos/**/] = src[srcPos/**/];
                    dst[dstPos + 1] = src[srcPos + 1];
                };
                case B8G8R8_UNORM -> (src, srcPos, dst, dstPos) -> {
                    dst[dstPos + 1] = src[srcPos + 1];
                    dst[dstPos + 2] = src[srcPos/**/];
                };
                case R8G8B8A8_UNORM -> (src, srcPos, dst, dstPos) -> {
                    dst[dstPos/**/] = src[srcPos/**/];
                    dst[dstPos + 1] = src[srcPos + 1];
                    dst[dstPos + 3] = (byte) 0xFF;
                };
                case B8G8R8A8_UNORM -> (src, srcPos, dst, dstPos) -> {
                    dst[dstPos + 1] = src[srcPos + 1];
                    dst[dstPos + 2] = src[srcPos/**/];
                    dst[dstPos + 3] = (byte) 0xFF;
                };
                default -> null;
            };
            case R8G8B8_UNORM -> switch (dstFormat) {
                case R8G8B8A8_UNORM -> (src, srcPos, dst, dstPos) -> {
                    dst[dstPos/**/] = src[srcPos/**/];
                    dst[dstPos + 1] = src[srcPos + 1];
                    dst[dstPos + 2] = src[srcPos + 2];
                    dst[dstPos + 3] = (byte) 0xFF;
                };
                case B8G8R8A8_UNORM -> (src, srcPos, dst, dstPos) -> {
                    dst[dstPos/**/] = src[srcPos + 2];
                    dst[dstPos + 1] = src[srcPos + 1];
                    dst[dstPos + 2] = src[srcPos/**/];
                    dst[dstPos + 3] = (byte) 0xFF;
                };
                default -> null;
            };
            case B8G8R8_UNORM -> switch (dstFormat) {
                case R8G8B8A8_UNORM -> (src, srcPos, dst, dstPos) -> {
                    dst[dstPos/**/] = src[srcPos + 2];
                    dst[dstPos + 1] = src[srcPos + 1];
                    dst[dstPos + 2] = src[srcPos/**/];
                    dst[dstPos + 3] = (byte) 0xFF;
                };
                case B8G8R8A8_UNORM -> (src, srcPos, dst, dstPos) -> {
                    dst[dstPos/**/] = src[srcPos/**/];
                    dst[dstPos + 1] = src[srcPos + 1];
                    dst[dstPos + 2] = src[srcPos + 2];
                    dst[dstPos + 3] = (byte) 0xFF;
                };
                default -> null;
            };

            default -> null;
        };
    }

    private static UnaryOperator<Surface> swizzleOperator(TextureFormat srcFormat, TextureFormat dstFormat) {
        if (srcFormat == TextureFormat.R8G8B8_UNORM && dstFormat == TextureFormat.B8G8R8_UNORM ||
            srcFormat == TextureFormat.B8G8R8_UNORM && dstFormat == TextureFormat.R8G8B8_UNORM) {
            return surface -> rgba2bgra(surface, 3);
        }
        if (srcFormat == TextureFormat.R8G8B8A8_UNORM && dstFormat == TextureFormat.B8G8R8A8_UNORM ||
            srcFormat == TextureFormat.B8G8R8A8_UNORM && dstFormat == TextureFormat.R8G8B8A8_UNORM) {
            return surface -> rgba2bgra(surface, 4);
        }
        return null;
    }

    private static Surface tonemapF16(Surface surface, TextureFormat format) {
        Surface target = Surface.create(surface.width(), surface.height(), format);

        var src = surface.data();
        var dst = target.data();
        for (int i = 0, o = 0; i < src.length; i += 2, o++) {
            dst[o] = packUNorm8(Float.float16ToFloat(Arrays.getShort(src, i, ByteOrder.LITTLE_ENDIAN)));
        }
        return target;
    }

    private static byte packUNorm8(float value) {
        return (byte) Math.fma(Math.clamp(value, 0.0f, 1.0f), 255.0f, 0.5f);
    }

    private static Surface rgba2bgra(Surface surface, int stride) {
        var data = surface.data();
        for (int i = 0; i < data.length; i += stride) {
            swap(data, i, i + 2);
        }
        return surface;
    }

    private static void swap(byte[] array, int i, int j) {
        byte tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    private static Texture map(Texture source, TextureFormat format, Function<Surface, Surface> mapper) {
        var surfaces = source.surfaces().stream()
            .map(mapper)
            .toList();

        return new Texture(
            format,
            source.type(),
            source.colorSpace(),
            surfaces,
            source.mips(),
            source.depth(),
            source.arraySize(),
            source.duration()
        );
    }

    @FunctionalInterface
    private interface UnpackOperation {
        void apply(byte[] src, int srcPos, byte[] dst, int dstPos);
    }

    private record Converter(Function<Surface, Surface> operator, TextureFormat format) {
        static Converter noop(TextureFormat format) {
            return new Converter(Function.identity(), format);
        }

        Converter andThen(Converter after) {
            return new Converter(operator.andThen(after.operator), after.format);
        }
    }
}
