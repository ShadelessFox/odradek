package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.game.ds2.rtti.DS2TypeReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;
import wtf.reversed.toolbox.io.BoolFormat;

import java.io.IOException;

public final class IndexArrayResourceCallback implements ExtraBinaryDataCallback<DS2.IndexArrayResource> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, DS2.IndexArrayResource object) throws IOException {
        var count = source.readInt();
        var flags = source.readInt();
        var format = DS2.EIndexFormat.valueOf(source.readInt());
        var streaming = source.readBool(BoolFormat.INT);
        var hash = DS2TypeReader.readCompound(DS2.MurmurHashValue.class, source, factory);
        var data = streaming ? null : source.readBytes(count * format.stride());

        object.count(count);
        object.flags(flags);
        object.format(format);
        object.isStreaming(streaming);
        object.checksum(hash);
        object.data(data);
    }
}
