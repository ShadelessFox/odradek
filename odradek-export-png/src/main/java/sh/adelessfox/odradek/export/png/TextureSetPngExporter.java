package sh.adelessfox.odradek.export.png;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.texture.TextureSet;
import sh.adelessfox.odradek.util.Filenames;

import java.io.IOException;
import java.util.Optional;

public final class TextureSetPngExporter implements Exporter.OfMultipleOutputs<TextureSet> {

    private static final Logger log = LoggerFactory.getLogger(TextureSetPngExporter.class);

    @Override
    public void export(TextureSet object, OutputProvider provider) throws IOException {
        for (TextureSet.SourceTexture source : object.sourceTextures()) {
            var unpacked = object.unpack(source).orElse(null);
            if (unpacked == null) {
                log.warn("Source texture {} of type {} is not packed in any texture, skipping", source.path(), source.type());
                continue;
            }

            var name = Filenames.withSuffix(Filenames.filename(source.path()), ".png");
            var channel = provider.channel(name);

            PngWriterHelper.writeSingle(unpacked, channel);
        }
    }

    @Override
    public String id() {
        return "image.set.png";
    }

    @Override
    public String name() {
        return "PNG (Portable Network Graphics)";
    }

    @Override
    public Optional<String> icon() {
        return Optional.of("fugue:image");
    }
}
