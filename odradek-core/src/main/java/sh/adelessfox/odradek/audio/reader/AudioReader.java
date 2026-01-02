package sh.adelessfox.odradek.audio.reader;

import sh.adelessfox.odradek.audio.Audio;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public interface AudioReader {
    Audio read(BinaryReader reader) throws IOException;
}
