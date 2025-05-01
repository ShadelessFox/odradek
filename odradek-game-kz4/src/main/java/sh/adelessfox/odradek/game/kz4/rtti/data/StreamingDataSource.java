package sh.adelessfox.odradek.game.kz4.rtti.data;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.Attr;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public interface StreamingDataSource {
    @Attr(name = "Location", type = "String", position = 0, offset = 0)
    String location();

    void location(String value);

    @Attr(name = "Offset", type = "uint64", position = 1, offset = 0)
    long offset();

    void offset(long value);

    @Attr(name = "Length", type = "uint64", position = 2, offset = 0)
    long length();

    void length(long value);

    static StreamingDataSource read(BinaryReader reader, TypeFactory factory) throws IOException {
        StreamingDataSource source = factory.newInstance(StreamingDataSource.class);
        source.location(reader.readString(reader.readInt()));
        source.offset(reader.readLong());
        source.length(reader.readLong());
        return source;
    }
}
