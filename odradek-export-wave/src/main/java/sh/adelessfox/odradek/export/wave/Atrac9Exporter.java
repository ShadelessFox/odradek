package sh.adelessfox.odradek.export.wave;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.AudioCodec;
import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.io.BinaryWriter;

import java.io.IOException;
import java.util.Optional;

public final class Atrac9Exporter implements Exporter.OfSingleOutput<Audio> {
    @Override
    public void export(Audio object, BinaryWriter writer) throws IOException {
        if (!(object.codec() instanceof AudioCodec.Atrac9)) {
            throw new IllegalArgumentException("Audio must be in Atrac9 format");
        }
        writer.writeBytes(object.data());
    }

    @Override
    public String id() {
        return "audio.atrac9";
    }

    @Override
    public String name() {
        return "Sony Atrac9";
    }

    @Override
    public String extension() {
        return "at9";
    }

    @Override
    public Optional<String> icon() {
        return Optional.of("fugue:music");
    }
}
