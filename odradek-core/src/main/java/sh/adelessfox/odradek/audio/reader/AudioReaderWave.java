package sh.adelessfox.odradek.audio.reader;

import sh.adelessfox.odradek.NotImplementedException;
import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public class AudioReaderWave implements AudioReader {
    @Override
    public Audio read(BinaryReader reader) throws IOException {
        throw new NotImplementedException(); // TODO
    }
}
