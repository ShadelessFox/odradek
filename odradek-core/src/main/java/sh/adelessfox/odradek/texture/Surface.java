package sh.adelessfox.odradek.texture;

import sh.adelessfox.odradek.util.Handles;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.nio.ByteOrder;

public record Surface(int width, int height, TextureFormat format, TextureColorSpace colorSpace, byte[] data) {
    public Surface {
        if (width <= 0) {
            throw new IllegalArgumentException("Width must be greater than 0");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("Height must be greater than 0");
        }
        int size = format.block().surfaceSize(width, height);
        if (data.length != size) {
            throw new IllegalArgumentException("Surface data size does not match expected size for source format");
        }
    }

    public static Surface create(int width, int height, TextureFormat format, TextureColorSpace colorSpace) {
        var size = format.block().surfaceSize(width, height);
        var data = new byte[size];
        return new Surface(width, height, format, colorSpace, data);
    }

    public static Surface create(
        int width,
        int height,
        TextureFormat format,
        TextureColorSpace colorSpace,
        byte[] data
    ) {
        var size = format.block().surfaceSize(width, height);
        if (size != data.length) {
            throw new IllegalArgumentException("Data size does not match the expected size for the given format and dimensions");
        }
        return new Surface(width, height, format, colorSpace, data);
    }

    public int offset(int x, int y, int z) {
        return ((z * height + y) * width + x) * format.block().size();
    }

    public Surface convert(TextureFormat target) {
        return TextureConverter.convert(this, target, colorSpace);
    }

    public Surface convert(TextureFormat target, TextureColorSpace colorSpace) {
        return TextureConverter.convert(this, target, colorSpace);
    }

    public <T> T convert(Converter<T> converter) {
        return converter.convert(this);
    }

    public interface Converter<T> {
        T convert(Surface surface);

        final class AWT implements Converter<BufferedImage> {
            @Override
            public BufferedImage convert(Surface surface) {
                var grayscale = surface.format().channels() == 1;
                var converted = surface.convert(grayscale ? TextureFormat.R8_UNORM : TextureFormat.B8G8R8A8_UNORM);
                var image = new BufferedImage(converted.width(), converted.height(), grayscale ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_INT_ARGB);

                if (grayscale) {
                    var buffer = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                    System.arraycopy(converted.data(), 0, buffer, 0, buffer.length);
                } else {
                    var buffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
                    for (int i = 0, o = 0, len = converted.data().length; i < len; i += 4, o++) {
                        buffer[o] = Handles.getInt(converted.data(), i, ByteOrder.LITTLE_ENDIAN);
                    }
                }

                return image;
            }
        }
    }
}
