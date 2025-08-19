package sh.adelessfox.odradek.game.hfw.storage;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingDataSource;

import java.io.IOException;
import java.nio.ByteBuffer;

public final class ObjectStreamingSystem {
    /**
     * Represents the result of reading a link from the link table.
     *
     * @param position The new position of the link in the link table
     * @param group    The relative subgroup index of a {@code Ref}, or the {@code groupID} of a {@code StreamingRef}
     * @param index    The index of object within the group
     */
    public record LinkReadResult(int position, int group, int index) {
    }

    private final StorageReadDevice device;
    private final StreamingGraphResource graph;
    private final ByteBuffer links;

    public ObjectStreamingSystem(StorageReadDevice device, StreamingGraphResource graph) throws IOException {
        this.device = device;
        this.graph = graph;
        this.links = ByteBuffer.wrap(getFileData(Math.toIntExact(graph.linkTableID()), 0, graph.linkTableSize()));
    }

    public byte[] getDataSourceData(StreamingDataSource dataSource) throws IOException {
        return getDataSourceData(dataSource, dataSource.offset(), dataSource.length());
    }

    public byte[] getDataSourceData(StreamingDataSource dataSource, int offset, int length) throws IOException {
        return getFileData(dataSource.fileId(), (long) dataSource.fileOffset() + offset, length);
    }

    public byte[] getFileData(int fileId, long offset, long length) throws IOException {
        return getFileData(graph.files().get(fileId), offset, length);
    }

    public byte[] getFileData(String file, long offset, long length) throws IOException {
        var reader = device.resolve(file);
        var buffer = new byte[Math.toIntExact(length)];

        if (length == 0) {
            return buffer;
        }

        synchronized (reader) {
            reader.position(offset);
            reader.readBytes(buffer, 0, buffer.length);
        }

        return buffer;
    }

    public StreamingGraphResource graph() {
        return graph;
    }

    public LinkReadResult readLink(int position) {
        return readLink(links.position(position));
    }

    private static LinkReadResult readLink(ByteBuffer buffer) {
        int linkIndex;
        int linkGroup;

        int first = buffer.get();
        if ((first & 0x40) != 0) {
            linkGroup = readVarInt(buffer, first & 0xbf);
            linkIndex = readVarInt(buffer, buffer.get());
        } else {
            linkGroup = -1;
            linkIndex = readVarInt(buffer, first & 0xbf);
        }

        return new LinkReadResult(buffer.position(), linkGroup, linkIndex);
    }

    private static int readVarInt(ByteBuffer buffer, int initial) {
        int temp = initial;
        int value = initial & 0x7f;
        while ((temp & 0x80) != 0) {
            temp = buffer.get();
            value = (value << 7) | (temp & 0x7f);
        }
        return value;
    }
}
