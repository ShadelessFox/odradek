package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.DataBufferResource;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class DataBufferResourceCallback implements ExtraBinaryDataCallback<DataBufferResource> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, DataBufferResource object) throws IOException {
        var count = reader.readInt();
        if (count == 0) {
            return;
        }

        var streaming = reader.readIntBoolean();
        var flags = reader.readInt();
        var format = reader.readInt();
        var stride = reader.readInt();
        var data = streaming ? null : reader.readBytes(stride * count);

        object.count(count);
        object.streaming(streaming);
        object.flags(flags);
        object.format(format);
        object.stride(stride);
        object.data(data);
    }
}
