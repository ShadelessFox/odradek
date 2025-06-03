package sh.adelessfox.odradek.texture;

import be.twofold.tinybcdec.BlockDecoder;
import be.twofold.tinybcdec.BlockFormat;

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
            .map(c -> decompress(c.format(), format).map(c::andThen).orElse(c))
            .map(c -> unpack(c.format(), format).map(c::andThen).orElse(c))
            .map(c -> swizzle(c.format(), format).map(c::andThen).orElse(c))
            .orElseThrow();

        if (converter.format() != format) {
            throw new UnsupportedOperationException("Could not convert texture from " + texture.format() + " to " + format);
        }

        return map(texture, format, converter.operator);
    }

    private static Optional<Converter> decompress(TextureFormat srcFormat, TextureFormat dstFormat) {
        if (!srcFormat.isCompressed()) {
            return Optional.empty();
        }

        var format = switch (srcFormat) {
            case BC1, BC2, BC3, BC7 -> TextureFormat.R8G8B8A8_UNORM;
            case BC4U, BC4S -> TextureFormat.R8_UNORM;
            case BC5U, BC5S -> TextureFormat.R8G8B8_UNORM;
            default -> throw new UnsupportedOperationException(srcFormat.name());
        };

        var decoder = BlockDecoder.create(switch (srcFormat) {
            case BC1 -> BlockFormat.BC1;
            case BC2 -> BlockFormat.BC2;
            case BC3 -> BlockFormat.BC3;
            case BC4U -> BlockFormat.BC4U;
            case BC4S -> BlockFormat.BC4S;
            case BC5U -> BlockFormat.BC5U;
            case BC5S -> BlockFormat.BC5S;
            case BC7 -> BlockFormat.BC7;
            default -> throw new UnsupportedOperationException(srcFormat.name());
        });

        UnaryOperator<Surface> operator = surface -> {
            var result = Surface.create(surface.width(), surface.height(), dstFormat);
            decoder.decode(surface.width(), surface.height(), surface.data(), 0, result.data(), 0);
            return result;
        };

        return Optional.of(new Converter(operator, format));
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
            surfaces,
            source.mips(),
            source.depth(),
            source.arraySize()
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
