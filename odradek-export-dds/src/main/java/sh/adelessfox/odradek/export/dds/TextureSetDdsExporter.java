package sh.adelessfox.odradek.export.dds;

import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.texture.TextureSet;

import java.io.IOException;
import java.util.Optional;

public final class TextureSetDdsExporter implements Exporter.OfMultipleOutputs<TextureSet> {
    @Override
    public void export(TextureSet object, OutputProvider provider) throws IOException {
        int index = 0;
        for (TextureSet.PackedTexture texture : object.packedTextures()) {
            DdsWriter.write(texture.texture(), provider.channel((index++) + ".dds"));
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
}
