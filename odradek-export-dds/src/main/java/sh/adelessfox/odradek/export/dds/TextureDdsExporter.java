package sh.adelessfox.odradek.export.dds;

import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.io.BinaryWriter;
import sh.adelessfox.odradek.texture.Texture;

import java.io.IOException;
import java.util.Optional;

public final class TextureDdsExporter implements Exporter.OfSingleOutput<Texture> {
    @Override
    public void export(Texture object, BinaryWriter writer) throws IOException {
        DdsWriter.write(object, writer);
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
    public String extension() {
        return "dds";
    }

    @Override
    public Optional<String> icon() {
        return Optional.of("fugue:image");
    }
}
