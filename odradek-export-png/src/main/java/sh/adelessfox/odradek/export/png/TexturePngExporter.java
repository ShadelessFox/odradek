package sh.adelessfox.odradek.export.png;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.export.png.format.PngBlendMethod;
import sh.adelessfox.odradek.export.png.format.PngDisposeMethod;
import sh.adelessfox.odradek.export.png.format.PngWriter;
import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.texture.Surface;
import sh.adelessfox.odradek.texture.Texture;
import sh.adelessfox.odradek.texture.TextureType;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;

public final class TexturePngExporter implements Exporter.OfSingleOutput<Texture> {
    private static final Logger log = LoggerFactory.getLogger(TexturePngExporter.class);

    @Override
    public void export(Texture object, WritableByteChannel channel) throws IOException {
        if (object.duration().isPresent()) {
            writeAnimated(object, channel);
        } else {
            if (object.type() != TextureType.SURFACE) {
                log.warn(
                    "Texture of type {} can't be properly exported as PNG, " +
                        "just the first surface will be exported", object.type());
            }
            writeSingle(object, channel);
        }
    }

    private void writeSingle(Texture texture, WritableByteChannel channel) throws IOException {
        PngWriterHelper.write(texture.surfaces().getFirst(), texture.format(), channel);
    }

    private static void writeAnimated(Texture texture, WritableByteChannel channel) throws IOException {
        var desiredFormat = PngWriterHelper.pickDesiredFormat(texture.format());
        var pngFormat = PngWriterHelper.mapPngFormat(texture.width(), texture.height(), desiredFormat);

        int frames = texture.surfaces().size();
        var duration = texture.duration().orElseThrow();

        try (var writer = PngWriter.ofAnimated(pngFormat, frames, 0, channel)) {
            for (Surface surface : texture.surfaces()) {
                writer.write(
                    surface.convert(texture.format(), desiredFormat).data(),
                    duration,
                    PngDisposeMethod.BACKGROUND,
                    PngBlendMethod.SOURCE);
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
    public String extension() {
        return "png";
    }

    @Override
    public Optional<String> icon() {
        return Optional.of("fugue:image");
    }
}
