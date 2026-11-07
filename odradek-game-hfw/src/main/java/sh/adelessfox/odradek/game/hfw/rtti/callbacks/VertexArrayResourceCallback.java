package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;
import wtf.reversed.toolbox.io.BoolFormat;

import java.io.IOException;

public class VertexArrayResourceCallback implements ExtraBinaryDataCallback<HFW.VertexArrayResource> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, HFW.VertexArrayResource object) throws IOException {
        var numVertices = source.readInt();
        var numStreams = source.readInt();
        var streaming = source.readBool(BoolFormat.BYTE);
        var streams = source.readObjects(numStreams, r -> readVertexStream(r, factory, numVertices, streaming));

        object.count(numVertices);
        object.streams(streams);
        object.isStreaming(streaming);
    }

    private static HFW.VertexArrayStreamInfo readVertexStream(
        BinarySource source,
        TypeFactory factory,
        int numVertices,
        boolean streaming
    ) throws IOException {
        var flags = source.readInt();
        var stride = source.readInt();
        var elements = source.readObjects(source.readInt(), r -> readVertexStreamElement(r, factory));
        var hash = HFWTypeReader.readCompound(HFW.MurmurHashValue.class, source, factory);
        var data = streaming ? null : source.readBytes(stride * numVertices);

        var stream = factory.newInstance(HFW.VertexArrayStreamInfo.class);
        stream.flags(flags);
        stream.stride(stride);
        stream.elements(elements);
        stream.hash(hash);
        stream.data(data);

        return stream;
    }

    private static HFW.VertexArrayStreamElementInfo readVertexStreamElement(
        BinarySource source,
        TypeFactory factory
    ) throws IOException {
        var offset = source.readByte();
        var storageType = HFW.EVertexElementStorageType.valueOf(source.readByte());
        var slotsUsed = source.readByte();
        var element = HFW.EVertexElement.valueOf(source.readByte());

        var object = factory.newInstance(HFW.VertexArrayStreamElementInfo.class);
        object.offset(offset);
        object.storageType(storageType);
        object.slotsUsed(slotsUsed);
        object.element(element);

        return object;
    }
}
