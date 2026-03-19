package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.rtti.DS2.EIndexFormat;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.IndexArrayResource;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.MurmurHashValue;
import sh.adelessfox.odradek.game.ds2.rtti.DS2TypeReader;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public final class IndexArrayResourceCallback implements ExtraBinaryDataCallback<IndexArrayResource> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, IndexArrayResource object) throws IOException {
        var count = reader.readInt();
        var flags = reader.readInt();
        var format = EIndexFormat.valueOf(reader.readInt());
        var streaming = reader.readIntBoolean();
        var hash = DS2TypeReader.readCompound(MurmurHashValue.class, reader, factory);
        var data = streaming ? null : reader.readBytes(count * format.stride());

        object.count(count);
        object.flags(flags);
        object.format(format);
        object.isStreaming(streaming);
        object.checksum(hash);
        object.data(data);
    }
}
