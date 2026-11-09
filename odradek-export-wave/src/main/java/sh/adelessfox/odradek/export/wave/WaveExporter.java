package sh.adelessfox.odradek.export.wave;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.AudioCodec;
import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.io.BinaryWriter;
import wtf.reversed.toolbox.collect.Bytes;

import java.io.IOException;
import java.util.Optional;

public final class WaveExporter implements Exporter.OfSingleOutput<Audio> {
    private static final int RIFF_CHUNK_ID = 'R' | 'I' << 8 | 'F' << 16 | 'F' << 24;
    private static final int RIFF_FORMAT_ID = 'W' | 'A' << 8 | 'V' << 16 | 'E' << 24;
    private static final int RIFF_CHUNK_FMT_ID = 'f' | 'm' << 8 | 't' << 16 | ' ' << 24;
    private static final int RIFF_CHUNK_DATA_ID = 'd' | 'a' << 8 | 't' << 16 | 'a' << 24;
    private static final short WAVE_FORMAT_PCM = 0x0001;

    @Override
    public void export(Audio object, BinaryWriter writer) throws IOException {
        var codec = AudioCodec.Pcm.S16LE;
        var converted = object.convert(codec);
        var format = converted.format();

        var fmt = Bytes.allocate(24)
            .setInt(0x00, RIFF_CHUNK_FMT_ID) // chunkID
            .setInt(0x04, 16) // chunkSize
            .setShort(0x08, WAVE_FORMAT_PCM) // wFormatTag
            .setShort(0x0A, (short) format.channels()) // wChannels
            .setInt(0x0C, format.sampleRate()) // dwSamplesPerSec
            .setInt(0x10, format.sampleRate() * codec.sizeBytes() * format.channels()) // dwAvgBytesPerSec
            .setShort(0x14, (short) (codec.sizeBytes() * format.channels())) // wBlockAlign
            .setShort(0x16, (short) (codec.sizeBytes() * 8)); // wBitsPerSample

        var dataHeader = Bytes.allocate(8)
            .setInt(0x00, RIFF_CHUNK_DATA_ID) // chunkID
            .setInt(0x04, converted.data().length()); // chunkSize
        var data = converted.data();

        var header = Bytes.allocate(12)
            .setInt(0x00, RIFF_CHUNK_ID) // chunkID
            .setInt(0x04, fmt.length() + dataHeader.length() + data.length() + 4) // chunkSize
            .setInt(0x08, RIFF_FORMAT_ID); // format

        writer.writeBytes(header);
        writer.writeBytes(fmt);
        writer.writeBytes(dataHeader);
        writer.writeBytes(data);
    }

    @Override
    public String id() {
        return "audio.wave";
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
