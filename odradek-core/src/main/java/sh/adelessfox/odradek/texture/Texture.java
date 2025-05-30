package sh.adelessfox.odradek.texture;

import java.util.List;

public record Texture(
    TextureFormat format,
    TextureType type,
    List<Surface> surfaces,
    int mips
) {
    public Texture {
        if (surfaces.isEmpty()) {
            throw new IllegalArgumentException("Surfaces must not be empty");
        }
        if (mips < 0) {
            throw new IllegalArgumentException("Mips must be non-negative");
        }
        surfaces = List.copyOf(surfaces);
    }

    public int width() {
        return surfaces.getFirst().width();
    }

    public int height() {
        return surfaces.getFirst().height();
    }
}
