package sh.adelessfox.odradek.texture;

import java.util.List;
import java.util.NoSuchElementException;
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

    public List<String> types() {
        return sourceTextures.stream()
            .map(SourceTexture::type)
            .toList();
    }

    public Optional<SourceTexture> findSourceTexture(String type) {
        return sourceTextures.stream()
            .filter(st -> st.type().equals(type))
            .findFirst();
    }

    public Optional<PackedTexture> findPackedTexture(String type) {
        return packedTextures.stream()
            .filter(pt -> pt.packing().contains(type))
            .findFirst();
    }

    public Optional<Texture> unpack(SourceTexture source) {
        var packed = findPackedTexture(source.type());
        return packed.map(p -> unpack(source, p));
    }

    public Optional<Texture> unpack(String type) {
        var source = findSourceTexture(type).orElseThrow(() -> new NoSuchElementException(type));
        return unpack(source);
    }

    private static Texture unpack(SourceTexture sourceTexture, PackedTexture packedTexture) {
        var red = unpackChannel(packedTexture.packing().red(), sourceTexture.type());
        var green = unpackChannel(packedTexture.packing().green(), sourceTexture.type());
        var blue = unpackChannel(packedTexture.packing().blue(), sourceTexture.type());
        var alpha = unpackChannel(packedTexture.packing().alpha(), sourceTexture.type());

        return packedTexture.texture()
            .unpack(red, green, blue, alpha)
            .withColorSpace(sourceTexture.colorSpace());
    }

    private static Optional<Channel> unpackChannel(Optional<TextureSet.PackingChannel> channel, String type) {
        return channel
            .filter(pc -> pc.type().equals(type))
            .map(TextureSet.PackingChannel::channel);
    }
}
