package sh.adelessfox.odradek.texture.processing;

public final class TileContext {
    final int x;
    final int y;
    final int z;
    final int width;
    final int height;

    public TileContext(int x, int y, int z, int width, int height) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
    }

    public int pixelCount() {
        return width * height;
    }
}
