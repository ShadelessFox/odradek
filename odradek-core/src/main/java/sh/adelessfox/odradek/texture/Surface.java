package sh.adelessfox.odradek.texture;

import sh.adelessfox.odradek.util.Arrays;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteOrder;

public record Surface(int width, int height, byte[] data) {
    public Surface {
        if (width <= 0) {
            throw new IllegalArgumentException("Width must be greater than 0");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("Height must be greater than 0");
        }
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data must not be null or empty");
        }
    }

    public static Surface create(int width, int height, TextureFormat format) {
        var size = format.block().surfaceSize(width, height);
        var data = new byte[size];
        return new Surface(width, height, data);
    }

    public static Surface create(int width, int height, TextureFormat format, byte[] data) {
        var size = format.block().surfaceSize(width, height);
        if (size != data.length) {
            throw new IllegalArgumentException("Data size does not match the expected size for the given format and dimensions");
        }
        return new Surface(width, height, data);
    }

    public Surface convert(TextureFormat sourceFormat, TextureFormat targetFormat) {
        int size = sourceFormat.block().surfaceSize(width, height);
        if (data.length != size) {
            throw new IllegalArgumentException("Surface data size does not match expected size for source format");
        }
        return TextureConverter.convert(this, sourceFormat, targetFormat);
    }

    public <T> T convert(TextureFormat format, Converter<T> converter) {
        return converter.convert(format, this);
    }

    public interface Converter<T> {
        T convert(TextureFormat format, Surface surface);

        final class AWT implements Converter<BufferedImage> {
            @Override
            public BufferedImage convert(TextureFormat format, Surface surface) {
                var converted = surface.convert(format, TextureFormat.B8G8R8A8_UNORM);

                var image = new BufferedImage(converted.width(), converted.height(), BufferedImage.TYPE_INT_ARGB);
                var buffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

                for (int i = 0, o = 0, len = converted.data().length; i < len; i += 4, o++) {
                    buffer[o] = Arrays.getInt(converted.data(), i, ByteOrder.LITTLE_ENDIAN);
                }

                return image;
            }
        }
    }
}
