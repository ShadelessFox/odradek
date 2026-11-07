package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;
import wtf.reversed.toolbox.io.BoolFormat;

import java.io.IOException;

public class IndexArrayResourceCallback implements ExtraBinaryDataCallback<HFW.IndexArrayResource> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, HFW.IndexArrayResource object) throws IOException {
        var count = source.readInt();
        var flags = source.readInt();
        var format = HFW.EIndexFormat.valueOf(source.readInt());
        var streaming = source.readBool(BoolFormat.INT);
        var hash = HFWTypeReader.readCompound(HFW.MurmurHashValue.class, source, factory);
        var data = streaming ? null : source.readBytes(count * format.stride());

        object.count(count);
        object.flags(flags);
        object.format(format);
        object.isStreaming(streaming);
        object.checksum(hash);
        object.data(data);
    }
}
