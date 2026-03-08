package sh.adelessfox.odradek.export.png.format;

public record PngFormat(
    int width,
    int height,
    PngColorType colorType
) {
    public PngFormat {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be greater than 0");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be greater than 0");
        }
    }

    public int bytesPerPixel() {
        return colorType.channels() * colorType.bitDepth() / Byte.SIZE;
    }

    public int bytesPerRow() {
        return bytesPerPixel() * width;
    }

    public int bytesPerImage() {
        return bytesPerRow() * height;
    }
}
