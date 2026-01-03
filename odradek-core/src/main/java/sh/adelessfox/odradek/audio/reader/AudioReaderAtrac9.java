package sh.adelessfox.odradek.audio.reader;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.audio.AudioFormat;
import sh.adelessfox.odradek.audio.codec.AudioCodecAtrac9;
import sh.adelessfox.odradek.audio.container.at9.Atrac9FactChunk;
import sh.adelessfox.odradek.audio.container.at9.Atrac9FmtChunk;
import sh.adelessfox.odradek.audio.container.riff.RiffParser;
import sh.adelessfox.odradek.audio.container.wave.WaveDataChunk;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public final class AudioReaderAtrac9 implements AudioReader {
    @Override
    public Audio read(BinaryReader reader) throws IOException {
        var file = new RiffParser()
            .type("WAVE")
            .reader(Atrac9FmtChunk.ID, Atrac9FmtChunk.reader())
            .reader(Atrac9FactChunk.ID, Atrac9FactChunk.reader())
            .reader(WaveDataChunk.ID, WaveDataChunk.reader())
            .parse(reader);

        var fmt = file.get(Atrac9FmtChunk.ID).orElseThrow();
        var fact = file.get(Atrac9FactChunk.ID).orElseThrow();
        var data = file.get(WaveDataChunk.ID).orElseThrow();

        var codec = new AudioCodecAtrac9(
            fmt.extension().configData(),
            fact.encoderDelaySamples()
        );

        var format = new AudioFormat(
            fmt.sampleRate(),
            fmt.channelCount()
        );

        return new Audio(
            codec,
            format,
            fact.sampleCount(),
            data.data()
        );
    }
}
