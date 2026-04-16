package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class VertexArrayResourceCallback implements ExtraBinaryDataCallback<HFW.VertexArrayResource> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, HFW.VertexArrayResource object) throws IOException {
        var numVertices = reader.readInt();
        var numStreams = reader.readInt();
        var streaming = reader.readByteBoolean();
        var streams = reader.readObjects(numStreams, r -> readVertexStream(r, factory, numVertices, streaming));

        object.count(numVertices);
        object.streams(streams);
        object.isStreaming(streaming);
    }

    private static HFW.VertexArrayStreamInfo readVertexStream(
        BinaryReader reader,
        TypeFactory factory,
        int numVertices,
        boolean streaming
    ) throws IOException {
        var flags = reader.readInt();
        var stride = reader.readInt();
        var elements = reader.readObjects(reader.readInt(), r -> readVertexStreamElement(r, factory));
        var hash = HFWTypeReader.readCompound(HFW.MurmurHashValue.class, reader, factory);
        var data = streaming ? null : reader.readBytes(stride * numVertices);

        var stream = factory.newInstance(HFW.VertexArrayStreamInfo.class);
        stream.flags(flags);
        stream.stride(stride);
        stream.elements(elements);
        stream.hash(hash);
        stream.data(data);

        return stream;
    }

    private static HFW.VertexArrayStreamElementInfo readVertexStreamElement(
        BinaryReader reader,
        TypeFactory factory
    ) throws IOException {
        var offset = reader.readByte();
        var storageType = HFW.EVertexElementStorageType.valueOf(reader.readByte());
        var slotsUsed = reader.readByte();
        var element = HFW.EVertexElement.valueOf(reader.readByte());

        var object = factory.newInstance(HFW.VertexArrayStreamElementInfo.class);
        object.offset(offset);
        object.storageType(storageType);
        object.slotsUsed(slotsUsed);
        object.element(element);

        return object;
    }
}
