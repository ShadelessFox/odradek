package sh.adelessfox.odradek.game.hfw.storage;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingDataSource;

import java.io.IOException;

public final class ObjectStreamingSystem {
    /**
     * Represents the result of reading a link from the link table.
     *
     * @param position The new position of the link in the link table
     * @param group    The group index
     * @param index    The index of object within the group
     */
    public record LinkReadResult(int position, int group, int index) {
    }

    private final StorageReadDevice device;
    private final StreamingGraphResource graph;
    private final byte[] links;

    public ObjectStreamingSystem(StorageReadDevice device, StreamingGraphResource graph) throws IOException {
        this.device = device;
        this.graph = graph;
        this.links = getFileData(Math.toIntExact(graph.linkTableID()), 0, graph.linkTableSize());
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
        int v7 = links[position++];

        int linkIndex = v7 & 0x3f;
        if ((v7 & 0x80) != 0) {
            byte v10;
            do {
                v10 = links[position++];
                linkIndex = (linkIndex << 7) | (v10 & 0x7f);
            } while ((v10 & 0x80) != 0);
        }

        var linkGroup = -1;
        if ((v7 & 0x40) != 0) {
            linkGroup = linkIndex;
            var v14 = links[position++];
            linkIndex = v14 & 0x7f;
            if ((v14 & 0x80) != 0) {
                byte v16;
                do {
                    v16 = links[position++];
                    linkIndex = (linkIndex << 7) | (v16 & 0x7f);
                } while ((v16 & 0x80) != 0);
            }
        }

        return new LinkReadResult(position, linkGroup, linkIndex);
    }
}
