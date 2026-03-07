package sh.adelessfox.odradek.export.png;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.texture.Surface;
import sh.adelessfox.odradek.texture.Texture;
import sh.adelessfox.odradek.texture.TextureType;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;

public final class PngExporter implements Exporter<Texture> {
    private static final Logger log = LoggerFactory.getLogger(PngExporter.class);

    @Override
    public void export(Texture object, WritableByteChannel channel) throws IOException {
        if (object.type() != TextureType.SURFACE) {
            log.warn("Texture of type {} can't be properly exported as PNG, " +
                "just the first surface will be exported", object.type());
        }

        var surface = object.surfaces().getFirst();
        var image = surface.convert(object.format(), new Surface.Converter.AWT());

        var buffer = new ByteArrayOutputStream();
        ImageIO.write(image, "png", buffer);

        channel.write(ByteBuffer.wrap(buffer.toByteArray()));
    }

    @Override
    public String id() {
        return "png";
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
