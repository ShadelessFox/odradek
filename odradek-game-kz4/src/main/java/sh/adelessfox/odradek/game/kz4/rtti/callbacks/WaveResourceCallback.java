package sh.adelessfox.odradek.game.kz4.rtti.callbacks;

import sh.adelessfox.odradek.game.kz4.rtti.Killzone4.WaveResource;
import sh.adelessfox.odradek.game.kz4.rtti.data.StreamingDataSource;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.Attr;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class WaveResourceCallback implements ExtraBinaryDataCallback<WaveResourceCallback.WaveData> {
    public interface WaveData {
        @Attr(name = "EmbeddedData", type = "Array<uint8>", position = 1, offset = 0)
        byte[] embeddedData();

        void embeddedData(byte[] value);

        @Attr(name = "StreamingDataSource", type = "StreamingDataSource", position = 1, offset = 0)
        StreamingDataSource streamingDataSource();

        void streamingDataSource(StreamingDataSource value);
    }

    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, WaveData object) throws IOException {
        // TODO: Think about how to pass the host object as T
        var resource = (WaveResource) object;
        var size = reader.readInt();

        if (resource.format().isStreaming()) {
            object.streamingDataSource(StreamingDataSource.read(reader, factory));
        } else {
            object.embeddedData(reader.readBytes(size));
        }
    }
}
