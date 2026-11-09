package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.data.Value;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;
import wtf.reversed.toolbox.io.BoolFormat;

import java.io.IOException;

public final class DataBufferResourceCallback implements ExtraBinaryDataCallback<DS2.DataBufferResource> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, DS2.DataBufferResource object) throws IOException {
        var count = source.readInt();
        if (count == 0) {
            return;
        }

        var streaming = source.readBool(BoolFormat.INT);
        var flags = source.readInt();
        var format = Value.valueOf(DS2.EDataBufferFormat.class, source.readInt());
        var stride = source.readInt();
        var data = streaming ? null : source.readBytes(stride * count);

        object.count(count);
        object.isStreaming(streaming);
        object.flags(flags);
        object.format(format);
        object.stride(stride);
        object.data(data);
    }
}
