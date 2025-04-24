package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EVertexElement;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.MurmurHashValue;
import sh.adelessfox.odradek.game.hfw.rtti.data.EVertexElementStorageType;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.Attr;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;
import java.util.List;

public class VertexArrayResourceCallback implements ExtraBinaryDataCallback<VertexArrayResourceCallback.VertexArrayData> {
    public interface VertexArrayData {
        @Attr(name = "Count", type = "uint32", position = 0, offset = 0)
        int count();

        void count(int value);

        @Attr(name = "IsStreaming", type = "bool", position = 0, offset = 0)
        boolean streaming();

        void streaming(boolean value);

        @Attr(name = "Streams", type = "Array<VertexStream>", position = 0, offset = 0)
        List<VertexStream> streams();

        void streams(List<VertexStream> value);
    }

    public interface VertexStream {
        @Attr(name = "Flags", type = "uint32", position = 0, offset = 0)
        int flags();

        void flags(int value);

        @Attr(name = "Stride", type = "uint32", position = 1, offset = 0)
        int stride();

        void stride(int value);

        @Attr(name = "Elements", type = "Array<VertexStreamElement>", position = 2, offset = 0)
        List<VertexStreamElement> elements();

        void elements(List<VertexStreamElement> value);

        @Attr(name = "Hash", type = "MurmurHashValue", position = 3, offset = 0)
        MurmurHashValue hash();

        void hash(MurmurHashValue value);

        @Attr(name = "Data", type = "Array<uint8>", position = 4, offset = 0)
        byte[] data();

        void data(byte[] value);

        static VertexStream read(BinaryReader reader, TypeFactory factory, int numVertices, boolean streaming) throws IOException {
            var flags = reader.readInt();
            var stride = reader.readInt();
            var elements = reader.readObjects(reader.readInt(), r -> VertexStreamElement.read(r, factory));
            var hash = HFWTypeReader.readCompound(MurmurHashValue.class, reader, factory);
            var data = streaming ? null : reader.readBytes(stride * numVertices);

            var stream = factory.newInstance(VertexStream.class);
            stream.flags(flags);
            stream.stride(stride);
            stream.elements(elements);
            stream.hash(hash);
            stream.data(data);

            return stream;
        }
    }

    public interface VertexStreamElement {
        @Attr(name = "Offset", type = "uint8", position = 0, offset = 0)
        byte offset();

        void offset(byte value);

        @Attr(name = "StorageType", type = "EVertexElementStorageType", position = 1, offset = 0)
        EVertexElementStorageType storageType();

        void storageType(EVertexElementStorageType value);

        @Attr(name = "SlotsUsed", type = "uint8", position = 2, offset = 0)
        byte slotsUsed();

        void slotsUsed(byte value);

        @Attr(name = "Element", type = "EVertexElement", position = 3, offset = 0)
        EVertexElement element();

        void element(EVertexElement value);

        static VertexStreamElement read(BinaryReader reader, TypeFactory factory) throws IOException {
            var offset = reader.readByte();
            var storageType = EVertexElementStorageType.valueOf(reader.readByte());
            var slotsUsed = reader.readByte();
            var element = EVertexElement.valueOf(reader.readByte());

            var object = factory.newInstance(VertexStreamElement.class);
            object.offset(offset);
            object.storageType(storageType);
            object.slotsUsed(slotsUsed);
            object.element(element);

            return object;
        }
    }

    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, VertexArrayData object) throws IOException {
        var numVertices = reader.readInt();
        var numStreams = reader.readInt();
        var streaming = reader.readByteBoolean();
        var streams = reader.readObjects(numStreams, r -> VertexStream.read(r, factory, numVertices, streaming));

        object.count(numVertices);
        object.streams(streams);
        object.streaming(streaming);
    }
}
