package sh.adelessfox.odradek.export.wave;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.AudioCodec;
import sh.adelessfox.odradek.game.Exporter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;

public final class WwiseExporter implements Exporter<Audio> {
    @Override
    public void export(Audio object, WritableByteChannel channel) throws IOException {
        if (!(object.codec() instanceof AudioCodec.Wwise)) {
            throw new IllegalArgumentException("Audio must be in Wwise format");
        }
        channel.write(ByteBuffer.wrap(object.data()));
    }

    @Override
    public String id() {
        return "wwise";
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
