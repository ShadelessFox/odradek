package sh.adelessfox.odradek.texture;

import java.util.List;
import java.util.Optional;

/**
 * A collection of textures channel-packed together.
 *
 * @param sourceTextures The source textures used to create the packed textures.
 * @param packedTextures The packed textures.
 */
public record TextureSet(
    List<SourceTexture> sourceTextures,
    List<PackedTexture> packedTextures
) {
    public record SourceTexture(String path, String type) {
    }

    public record PackedTexture(Texture texture, ChannelPacking packing) {
    }

    public record ChannelPacking(
        Optional<String> red,
        Optional<String> green,
        Optional<String> blue,
        Optional<String> alpha
    ) {
        public boolean contains(String type) {
            return type.equals(red.orElse(null))
                || type.equals(green.orElse(null))
                || type.equals(blue.orElse(null))
                || type.equals(alpha.orElse(null));
        }
    }
}
