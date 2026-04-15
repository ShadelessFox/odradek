package sh.adelessfox.odradek.export.png;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.export.png.format.PngColorType;
import sh.adelessfox.odradek.export.png.format.PngFormat;
import sh.adelessfox.odradek.export.png.format.PngWriter;
import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.texture.TextureFormat;
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

            var surface = unpacked.surfaces().getFirst().convert(unpacked.format(), TextureFormat.R8G8B8A8_UNORM);
            var format = new PngFormat(surface.width(), surface.height(), PngColorType.RGBA8);

            var name = Filenames.withSuffix(Filenames.filename(source.path()), ".png");
            var channel = provider.channel(name);

            try (var writer = PngWriter.of(format, channel)) {
                writer.write(surface.data());
            }
        }
    }

    @Override
    public String id() {
        return "image.png";
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
