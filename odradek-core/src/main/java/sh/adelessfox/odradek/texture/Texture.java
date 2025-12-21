package sh.adelessfox.odradek.texture;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * A representation of a texture.
 * <p>
 * Surfaces must be ordered in such a way so that the highest mip comes first. This rule holds for all texture types.
 * <p>
 * For example, an imaginary array texture of two elements of size 32x32 would have its surfaces
 * stored in the following order:
 * <ul>
 *     <li>Surface 0, Main (32x32)</li>
 *     <li>Surface 1, Main (32x32)</li>
 *     <li>Surface 0, Mip  (16x16)</li>
 *     <li>Surface 1, Mip  (16x16)</li>
 *     ... 3 more mip levels ...
 *     <li>Surface 0, Mip  (1x1)</li>
 *     <li>Surface 1, Mip  (1x1)</li>
 * </ul>
 *
 * @param format     The format of the texture
 * @param type       The type of the texture
 * @param colorSpace The color space of the texture
 * @param surfaces   The list of all surfaces in the texture
 * @param mips       The number of mipmaps in the texture, including the <i>main</i> images.
 * @param depth      The depth of the texture, in pixels, if {@link #type()} is {@link TextureType#VOLUME},
 *                   or {@link OptionalInt#empty()} if the texture is not a volume texture
 * @param arraySize  The number of elements in the texture if {@link #type()} is {@link TextureType#ARRAY},
 *                   or {@link OptionalInt#empty()} if the texture is not an array texture
 * @param duration   The duration of the texture animation, if the texture is animated; only {@link TextureType#ARRAY}
 *                   could be animated. Frame count is determined by {@link #arraySize()}. If the texture is not animated,
 *                   this is {@link Optional#empty()}.
 */
public record Texture(
    TextureFormat format,
    TextureType type,
    TextureColorSpace colorSpace,
    List<Surface> surfaces,
    int mips,
    OptionalInt depth,
    OptionalInt arraySize,
    Optional<Duration> duration
) {
    public Texture {
        if (surfaces.isEmpty()) {
            throw new IllegalArgumentException("Surfaces must not be empty");
        }
        if (mips < 1) {
            throw new IllegalArgumentException("Mipmaps must be at least 1");
        }
        if (type == TextureType.VOLUME != depth.isPresent()) {
            throw new IllegalArgumentException("Depth must be present for volume textures");
        }
        if (type == TextureType.ARRAY != arraySize.isPresent()) {
            throw new IllegalArgumentException("Array size must be present for array textures");
        }
        if (type != TextureType.ARRAY && duration.isPresent()) {
            throw new IllegalArgumentException("Only array textures can be animated");
        }
        for (Surface surface : surfaces) {
            int size = format.block().surfaceSize(surface.width(), surface.height());
            if (surface.data().length != size) {
                throw new IllegalArgumentException("Surface data size does not match expected size: "
                    + surface.data().length + " != " + size);
            }
        }
        surfaces = List.copyOf(surfaces);
    }

    public static Texture of2D(TextureFormat format, TextureColorSpace colorSpace, Surface surface) {
        return of2D(format, colorSpace, List.of(surface), 1);
    }

    public static Texture of2D(TextureFormat format, TextureColorSpace colorSpace, List<Surface> surfaces, int mips) {
        return new Texture(format, TextureType.SURFACE, colorSpace, surfaces, mips, OptionalInt.empty(), OptionalInt.empty(), Optional.empty());
    }

    public static Texture ofAnimated2D(TextureFormat format, TextureColorSpace colorSpace, List<Surface> surfaces, int frames, Duration duration) {
        return new Texture(format, TextureType.ARRAY, colorSpace, surfaces, 1, OptionalInt.empty(), OptionalInt.of(frames), Optional.of(duration));
    }

    public Texture convert(TextureFormat targetFormat) {
        return TextureConverter.convert(this, targetFormat);
    }

    public int width() {
        return surfaces.getFirst().width();
    }

    public int height() {
        return surfaces.getFirst().height();
    }
}
