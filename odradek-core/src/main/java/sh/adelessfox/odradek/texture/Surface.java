package sh.adelessfox.odradek.texture;

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
}
