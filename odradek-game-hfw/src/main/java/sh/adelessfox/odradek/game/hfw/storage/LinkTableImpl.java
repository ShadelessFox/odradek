package sh.adelessfox.odradek.game.hfw.storage;

import sh.adelessfox.odradek.game.decima.LinkTable;
import sh.adelessfox.odradek.hashing.HashCode;
import sh.adelessfox.odradek.hashing.HashFunction;

import java.nio.ByteBuffer;
import java.util.OptionalInt;

public final class LinkTableImpl implements LinkTable {
    private final byte[] data;

    public LinkTableImpl(byte[] data) {
        this.data = data;
    }

    @Override
    public Result read(int position) {
        return readLink(ByteBuffer.wrap(data, position, data.length - position));
    }

    @Override
    public HashCode checksum() {
        return HashFunction.murmur3().hash(data);
    }

    private static Result readLink(ByteBuffer buffer) {
        OptionalInt linkGroup;
        int linkIndex;

        int first = buffer.get();
        if ((first & 0x40) != 0) {
            linkGroup = OptionalInt.of(readVarInt(buffer, first & 0xbf));
            linkIndex = readVarInt(buffer, buffer.get());
        } else {
            linkGroup = OptionalInt.empty();
            linkIndex = readVarInt(buffer, first & 0xbf);
        }

        return new Result(buffer.position(), linkGroup, linkIndex);
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
