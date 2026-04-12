package sh.adelessfox.odradek.export.dds;

import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.texture.Channel;
import sh.adelessfox.odradek.texture.TextureSet;

import java.io.IOException;
import java.util.Optional;

public final class TextureSetDdsExporter implements Exporter.OfMultipleOutputs<TextureSet> {
    @Override
    public void export(TextureSet object, OutputProvider provider) throws IOException {
        for (TextureSet.SourceTexture sourceTexture : object.sourceTextures()) {
            var packedTexture = object.packedTextures().stream()
                .filter(pt -> pt.packing().contains(sourceTexture.type()))
                .findFirst().orElse(null);

            if (packedTexture == null) {
                continue;
            }

            var red = unpackChannel(packedTexture.packing().red(), sourceTexture.type());
            var green = unpackChannel(packedTexture.packing().green(), sourceTexture.type());
            var blue = unpackChannel(packedTexture.packing().blue(), sourceTexture.type());
            var alpha = unpackChannel(packedTexture.packing().alpha(), sourceTexture.type());

            var texture = packedTexture.texture().unpack(red, green, blue, alpha);
            var filename = extractFilename(sourceTexture.path()) + ".dds";
            DdsWriter.write(texture, provider.channel(filename));
        }
    }

    @Override
    public String id() {
        return "image.dds";
    }

    @Override
    public String name() {
        return "DDS (DirectDraw Surface)";
    }

    @Override
    public Optional<String> icon() {
        return Optional.of("fugue:image");
    }

    private static String extractFilename(String path) {
        int sep = path.lastIndexOf('/');
        if (sep >= 0) {
            path = path.substring(sep + 1);
        }
        int ext = path.lastIndexOf('.');
        if (ext >= 0) {
            path = path.substring(0, ext);
        }
        return path;
    }

    private static Optional<Channel> unpackChannel(Optional<TextureSet.PackingChannel> channel, String type) {
        return channel
            .filter(pc -> pc.type().equals(type))
            .map(TextureSet.PackingChannel::channel);
    }
}
