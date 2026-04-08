package sh.adelessfox.odradek.export.wave;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.AudioCodec;
import sh.adelessfox.odradek.game.Exporter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;

public final class Atrac9Exporter implements Exporter<Audio> {
    @Override
    public void export(Audio object, WritableByteChannel channel) throws IOException {
        if (!(object.codec() instanceof AudioCodec.Atrac9)) {
            throw new IllegalArgumentException("Audio must be in Atrac9 format");
        }
        channel.write(ByteBuffer.wrap(object.data()));
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
