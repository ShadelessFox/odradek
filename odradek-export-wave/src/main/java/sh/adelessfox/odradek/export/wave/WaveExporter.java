package sh.adelessfox.odradek.export.wave;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.game.Exporter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;

public final class WaveExporter implements Exporter<Audio> {
    private static final int RIFF_CHUNK_ID = 'R' | 'I' << 8 | 'F' << 16 | 'F' << 24;
    private static final int RIFF_FORMAT_ID = 'W' | 'A' << 8 | 'V' << 16 | 'E' << 24;
    private static final int RIFF_CHUNK_FMT_ID = 'f' | 'm' << 8 | 't' << 16 | ' ' << 24;
    private static final int RIFF_CHUNK_DATA_ID = 'd' | 'a' << 8 | 't' << 16 | 'a' << 24;
    private static final short WAVE_FORMAT_PCM = 0x0001;

    @Override
    public void export(Audio object, WritableByteChannel channel) throws IOException {
        var pcm16 = object.toPcm16();
        int channels = pcm16.format().channels();
        int sampleRate = pcm16.format().sampleRate();

        var fmt = ByteBuffer.allocate(24)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(RIFF_CHUNK_FMT_ID) // chunkID
            .putInt(16) // chunkSize
            .putShort(WAVE_FORMAT_PCM) // wFormatTag
            .putShort((short) channels) // wChannels
            .putInt(sampleRate) // dwSamplesPerSec
            .putInt(sampleRate) // dwAvgBytesPerSec
            .putShort((short) (2 * channels)) // wBlockAlign
            .putShort((short) 16) // wBitsPerSample
            .flip();

        var data = ByteBuffer.allocate(8 + pcm16.data().length)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(RIFF_CHUNK_DATA_ID) // chunkID
            .putInt(pcm16.data().length) // chunkSize
            .put(pcm16.data()) // data
            .flip();

        var header = ByteBuffer.allocate(12)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(RIFF_CHUNK_ID) // chunkID
            .putInt(fmt.remaining() + data.remaining() + 4) // chunkSize
            .putInt(RIFF_FORMAT_ID) // format
            .flip();

        channel.write(header);
        channel.write(fmt);
        channel.write(data);
    }

    @Override
    public String id() {
        return "wave";
    }

    @Override
    public String name() {
        return "WAV (Waveform Audio File Format)";
    }

    @Override
    public String extension() {
        return "wav";
    }

    @Override
    public Optional<String> icon() {
        return Optional.of("fugue:music");
    }
}
