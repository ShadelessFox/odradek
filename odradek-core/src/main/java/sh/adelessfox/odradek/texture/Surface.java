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
}
