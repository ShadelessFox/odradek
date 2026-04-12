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
    public record SourceTexture(String path, String type, TextureColorSpace colorSpace) {
    }

    public record PackedTexture(Texture texture, Packing packing) {
    }

    public record Packing(
        Optional<PackingChannel> red,
        Optional<PackingChannel> green,
        Optional<PackingChannel> blue,
        Optional<PackingChannel> alpha
    ) {
        public boolean contains(String type) {
            return red.filter(x -> x.type().equals(type)).isPresent()
                || green.filter(x -> x.type().equals(type)).isPresent()
                || blue.filter(x -> x.type().equals(type)).isPresent()
                || alpha.filter(x -> x.type().equals(type)).isPresent();
        }
    }

    public record PackingChannel(String type, Channel channel) {
    }

}
