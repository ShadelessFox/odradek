package sh.adelessfox.odradek.export.wave;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.AudioCodec;
import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.io.BinaryWriter;

import java.io.IOException;
import java.util.Optional;

public final class WwiseExporter implements Exporter.OfSingleOutput<Audio> {
    @Override
    public void export(Audio object, BinaryWriter writer) throws IOException {
        if (!(object.codec() instanceof AudioCodec.Wwise)) {
            throw new IllegalArgumentException("Audio must be in Wwise format");
        }
        writer.writeBytes(object.data());
    }

    @Override
    public String id() {
        return "audio.wwise";
    }

    @Override
    public String name() {
        return "Audiokinetic Wwise";
    }

    @Override
    public String extension() {
        return "wem";
    }

    @Override
    public Optional<String> icon() {
        return Optional.of("fugue:music");
    }
}
