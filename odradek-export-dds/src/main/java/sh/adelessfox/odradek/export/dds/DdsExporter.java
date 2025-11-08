package sh.adelessfox.odradek.export.dds;

import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.texture.Texture;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;

public final class DdsExporter implements Exporter<Texture> {
    @Override
    public void export(Texture object, WritableByteChannel channel) throws IOException {
        DdsWriter.write(object, channel);
    }

    @Override
    public String id() {
        return "dds";
    }

    @Override
    public String name() {
        return "DDS (DirectDraw Surface)";
    }

    @Override
    public String extension() {
        return "dds";
    }

    @Override
    public Optional<String> icon() {
        return Optional.of("fugue:image");
    }
}
