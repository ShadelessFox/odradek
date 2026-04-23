package sh.adelessfox.odradek.export.png;

import sh.adelessfox.odradek.export.png.format.PngColorType;
import sh.adelessfox.odradek.export.png.format.PngFormat;
import sh.adelessfox.odradek.export.png.format.PngWriter;
import sh.adelessfox.odradek.texture.Surface;
import sh.adelessfox.odradek.texture.TextureFormat;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

final class PngWriterHelper {
    private PngWriterHelper() {
    }

    static void write(Surface surface, TextureFormat format, WritableByteChannel channel) throws IOException {
        var desiredFormat = PngWriterHelper.pickDesiredFormat(format);
        var pngFormat = PngWriterHelper.mapPngFormat(surface.width(), surface.height(), desiredFormat);

        try (var writer = PngWriter.of(pngFormat, channel)) {
            writer.write(surface.convert(format, desiredFormat).data());
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
}
