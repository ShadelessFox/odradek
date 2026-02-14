package sh.adelessfox.odradek.game.untildawn.rtti.callbacks;

import sh.adelessfox.odradek.game.untildawn.rtti.UntilDawn.VertexArrayResource;
import sh.adelessfox.odradek.game.untildawn.rtti.UntilDawn.VertexArrayStreamElementInfo;
import sh.adelessfox.odradek.game.untildawn.rtti.UntilDawn.VertexArrayStreamInfo;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class VertexArrayResourceCallback implements ExtraBinaryDataCallback<VertexArrayResource> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, VertexArrayResource object) throws IOException {
        int numVertices = reader.readInt();
        var numStreams = reader.readInt();

        var streams = reader.readObjects(numStreams, r -> readVertexStream(r, factory));
        var data = reader.readBytes(streams.stream().mapToInt(x -> x.stride() * numVertices).sum());

        object.count(numVertices);
        object.streams(streams);
        object.data(data);
    }

    private static VertexArrayStreamInfo readVertexStream(
        BinaryReader reader,
        TypeFactory factory
    ) throws IOException {
        var stream = factory.newInstance(VertexArrayStreamInfo.class);
        stream.flags(reader.readInt());
        stream.stride(reader.readInt());
        stream.elements(reader.readObjects(reader.readInt(), r -> readVertexStreamElement(r, factory)));

        return stream;
    }

    private static VertexArrayStreamElementInfo readVertexStreamElement(
        BinaryReader reader,
        TypeFactory factory
    ) throws IOException {
        var object = factory.newInstance(VertexArrayStreamElementInfo.class);
        object.unk01(reader.readInt());
        object.offset(reader.readByte());
        object.storageType(reader.readByte());
        object.slotsUsed(reader.readByte());
        object.element(reader.readByte());

        return object;
    }
}
