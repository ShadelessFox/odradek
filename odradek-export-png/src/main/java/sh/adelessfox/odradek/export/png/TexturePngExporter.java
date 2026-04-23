package sh.adelessfox.odradek.export.png;

import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.texture.Texture;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;

public final class TexturePngExporter implements Exporter.OfSingleOutput<Texture> {
    @Override
    public void export(Texture object, WritableByteChannel channel) throws IOException {
        if (object.duration().isPresent()) {
            PngWriterHelper.writeAnimated(object, channel);
        } else {
            PngWriterHelper.writeSingle(object, channel);
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
    public String extension() {
        return "png";
    }

    @Override
    public Optional<String> icon() {
        return Optional.of("fugue:image");
    }
}
