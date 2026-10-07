package sh.adelessfox.odradek.game;

import sh.adelessfox.odradek.scene.Scene;
import sh.adelessfox.odradek.texture.TextureSet;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A model paired with every TextureSet reachable from its source object.
 * Used by bundle exporters that write a mesh file alongside its textures.
 * <p>
 * The texture-set list is resolved lazily on first call to {@link #textureSets()}
 * — collecting them can be expensive (a transitive ref walk), and we only want to
 * pay that cost when the user actually triggers an export.
 */
public final class ModelBundle {
    private final Scene scene;
    private final Supplier<List<TextureSet>> textureSetSupplier;
    private volatile List<TextureSet> resolved;

    public ModelBundle(Scene scene, Supplier<List<TextureSet>> textureSetSupplier) {
        this.scene = Objects.requireNonNull(scene, "scene");
        this.textureSetSupplier = Objects.requireNonNull(textureSetSupplier, "textureSetSupplier");
    }

    public Scene scene() {
        return scene;
    }

    public List<TextureSet> textureSets() {
        var local = resolved;
        if (local == null) {
            synchronized (this) {
                local = resolved;
                if (local == null) {
                    local = List.copyOf(textureSetSupplier.get());
                    resolved = local;
                }
            }
        }
        return local;
    }
}
