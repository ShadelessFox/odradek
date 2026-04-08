package sh.adelessfox.odradek.export.png;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.export.png.format.*;
import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.texture.Surface;
import sh.adelessfox.odradek.texture.Texture;
import sh.adelessfox.odradek.texture.TextureFormat;
import sh.adelessfox.odradek.texture.TextureType;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;

public final class PngExporter implements Exporter<Texture> {
    private static final Logger log = LoggerFactory.getLogger(PngExporter.class);

    @Override
    public void export(Texture object, WritableByteChannel channel) throws IOException {
        var format = new PngFormat(object.width(), object.height(), PngColorType.RGBA8);

        if (object.duration().isPresent()) {
            writeAnimated(object, channel, format);
        } else {
            if (object.type() != TextureType.SURFACE) {
                log.warn("Texture of type {} can't be properly exported as PNG, " +
                    "just the first surface will be exported", object.type());
            }
            writeSingle(object, channel, format);
        }
    }

    private static void writeSingle(Texture texture, WritableByteChannel channel, PngFormat format) throws IOException {
        var surface = texture.surfaces().getFirst();
        var converted = surface.convert(texture.format(), TextureFormat.R8G8B8A8_UNORM);

        try (var writer = PngWriter.of(format, channel)) {
            writer.write(converted.data());
        }
    }

    private static void writeAnimated(Texture texture, WritableByteChannel channel, PngFormat format) throws IOException {
        int frames = texture.surfaces().size();
        var duration = texture.duration().orElseThrow();

        try (var writer = PngWriter.ofAnimated(format, frames, 0, channel)) {
            for (Surface surface : texture.surfaces()) {
                writer.write(
                    surface.convert(texture.format(), TextureFormat.R8G8B8A8_UNORM).data(),
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
