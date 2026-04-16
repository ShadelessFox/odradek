package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class IndexArrayResourceCallback implements ExtraBinaryDataCallback<HFW.IndexArrayResource> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, HFW.IndexArrayResource object) throws IOException {
        var count = reader.readInt();
        var flags = reader.readInt();
        var format = HFW.EIndexFormat.valueOf(reader.readInt());
        var streaming = reader.readIntBoolean();
        var hash = HFWTypeReader.readCompound(HFW.MurmurHashValue.class, reader, factory);
        var data = streaming ? null : reader.readBytes(count * format.stride());

        object.count(count);
        object.flags(flags);
        object.format(format);
        object.isStreaming(streaming);
        object.checksum(hash);
        object.data(data);
    }
}
