package sh.adelessfox.odradek.export.png;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.export.png.format.*;
import sh.adelessfox.odradek.texture.Texture;
import sh.adelessfox.odradek.texture.TextureFormat;
import sh.adelessfox.odradek.texture.TextureType;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

final class PngWriterHelper {
    private static final Logger log = LoggerFactory.getLogger(PngWriterHelper.class);

    private PngWriterHelper() {
    }

    static void writeSingle(Texture texture, WritableByteChannel channel) throws IOException {
        if (texture.type() != TextureType.SURFACE) {
            log.warn(
                "Texture of type {} can't be properly exported as PNG, " +
                    "just the first surface will be exported", texture.type());
        }

        var desiredFormat = PngWriterHelper.pickDesiredFormat(texture.format());
        var pngFormat = PngWriterHelper.mapPngFormat(texture.width(), texture.height(), desiredFormat);

        try (var writer = PngWriter.of(pngFormat, channel)) {
            var surface = texture.surfaces().getFirst();
            var data = surface.convert(texture.format(), desiredFormat).data();
            flip(data, pngFormat.bytesPerPixel());
            writer.write(data);
        }
    }

    static void writeAnimated(
        Texture texture,
        WritableByteChannel channel
    ) throws IOException {
        var desiredFormat = PngWriterHelper.pickDesiredFormat(texture.format());
        var pngFormat = PngWriterHelper.mapPngFormat(texture.width(), texture.height(), desiredFormat);

        int frames = texture.surfaces().size();
        var duration = texture.duration().orElseThrow();

        try (var writer = PngWriter.ofAnimated(pngFormat, frames, 0, channel)) {
            for (var surface : texture.surfaces()) {
                var data = surface.convert(texture.format(), desiredFormat).data();
                flip(data, pngFormat.bytesPerPixel());
                writer.write(data, duration, PngDisposeMethod.BACKGROUND, PngBlendMethod.SOURCE);
            }
        }
    }

    static TextureFormat pickDesiredFormat(TextureFormat format) {
        return switch (format) {
            case R8_UNORM,
                 BC4_UNORM,
                 BC4_SNORM -> TextureFormat.R8_UNORM;

            case R8G8_UNORM,
                 R8G8B8_UNORM,
                 B8G8R8_UNORM,
                 BC5_UNORM,
                 BC5_SNORM -> TextureFormat.R8G8B8_UNORM;

            case R8G8B8A8_UNORM,
                 B8G8R8A8_UNORM,
                 BC1_UNORM,
                 BC2_UNORM,
                 BC3_UNORM,
                 BC7_UNORM -> TextureFormat.R8G8B8A8_UNORM;

            case R16_UNORM,
                 R16_SFLOAT,
                 R32_SFLOAT -> TextureFormat.R16_UNORM;

            case R16G16_UNORM,
                 R16G16_SFLOAT,
                 R16G16B16_UNORM,
                 R16G16B16_SFLOAT,
                 BC6_UNORM,
                 BC6_SNORM -> TextureFormat.R16G16B16_UNORM;

            case R16G16B16A16_UNORM,
                 R16G16B16A16_SFLOAT -> TextureFormat.R16G16B16A16_UNORM;
        };
    }

    static PngFormat mapPngFormat(int width, int height, TextureFormat format) {
        return switch (format) {
            case TextureFormat.R8_UNORM -> new PngFormat(width, height, new PngColorType.Greyscale(false, 8));
            case TextureFormat.R16_UNORM -> new PngFormat(width, height, new PngColorType.Greyscale(false, 16));
            case TextureFormat.R8G8B8_UNORM -> new PngFormat(width, height, new PngColorType.TrueColor(false, 8));
            case TextureFormat.R8G8B8A8_UNORM -> new PngFormat(width, height, new PngColorType.TrueColor(true, 8));
            case TextureFormat.R16G16B16_UNORM -> new PngFormat(width, height, new PngColorType.TrueColor(false, 16));
            case TextureFormat.R16G16B16A16_UNORM -> new PngFormat(width, height, new PngColorType.Greyscale(true, 16));
            default -> throw new IllegalArgumentException("Unexpected texture format: " + format);
        };
    }

    private static void flip(byte[] array, int bpp) {
        if (bpp == 2) {
            for (int i = 0; i < array.length; i += 2) {
                swap(array, i, i + 1);
            }
        } else if (bpp == 4) {
            for (int i = 0; i < array.length; i += 4) {
                swap(array, i/**/, i + 3);
                swap(array, i + 1, i + 2);
            }
        }
    }

    private static void swap(byte[] arr, int i, int j) {
        byte temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
