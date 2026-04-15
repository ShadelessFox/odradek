package sh.adelessfox.odradek.export.dds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.texture.TextureSet;
import sh.adelessfox.odradek.util.Filenames;

import java.io.IOException;
import java.util.Optional;

public final class TextureSetDdsExporter implements Exporter.OfMultipleOutputs<TextureSet> {
    private static final Logger log = LoggerFactory.getLogger(TextureSetDdsExporter.class);

    @Override
    public void export(TextureSet object, OutputProvider provider) throws IOException {
        for (TextureSet.SourceTexture source : object.sourceTextures()) {
            var unpacked = object.unpack(source).orElse(null);
            if (unpacked == null) {
                log.warn("Source texture {} of type {} is not packed in any texture, skipping", source.path(), source.type());
                continue;
            }

            String name = Filenames.withSuffix(Filenames.filename(source.path()), ".dds");
            DdsWriter.write(unpacked, provider.channel(name));
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
