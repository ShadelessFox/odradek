package sh.adelessfox.atrac9j.util;

import java.util.Objects;

public final class BitReader {
    private byte[] buffer;
    private int lengthBits;
    public int position;

    public BitReader() {
    }

    public BitReader(byte[] buffer) {
        setBuffer(buffer);
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
        lengthBits = this.buffer.length * 8;
        position = 0;
    }

    public int readInt(int bitCount) {
        int value = peekInt(bitCount);
        position += bitCount;
        return value;
    }

    public int readSignedInt(int bitCount) {
        return Bit.signExtend32(readInt(bitCount), bitCount);
    }

    public boolean readBool() {
        return readInt(1) == 1;
    }

    public int readOffsetBinary(int bitCount) {
        int offset = (1 << (bitCount - 1));
        int value = peekInt(bitCount) - offset;
        position += bitCount;
        return value;
    }

    public int peekInt(int bitCount) {
        assert bitCount >= 0 && bitCount <= 32;

        if (bitCount > remaining()) {
            if (position >= lengthBits) {
                return 0;
            }

            int extraBits = bitCount - remaining();
            return peekIntFallback(remaining()) << extraBits;
        }

        int byteIndex = position / 8;
        int bitIndex = position % 8;

        if (bitCount <= 9 && remaining() >= 16) {
            int value = Byte.toUnsignedInt(buffer[byteIndex]) << 8
                | Byte.toUnsignedInt(buffer[byteIndex + 1]);
            value &= 0xFFFF >>> bitIndex;
            value >>= 16 - bitCount - bitIndex;
            return value;
        }

        if (bitCount <= 17 && remaining() >= 24) {
            int value = Byte.toUnsignedInt(buffer[byteIndex]) << 16
                | Byte.toUnsignedInt(buffer[byteIndex + 1]) << 8
                | Byte.toUnsignedInt(buffer[byteIndex + 2]);
            value &= 0xFFFFFF >>> bitIndex;
            value >>= 24 - bitCount - bitIndex;
            return value;
        }

        if (bitCount <= 25 && remaining() >= 32) {
            int value = Byte.toUnsignedInt(buffer[byteIndex]) << 24
                | Byte.toUnsignedInt(buffer[byteIndex + 1]) << 16
                | Byte.toUnsignedInt(buffer[byteIndex + 2]) << 8
                | Byte.toUnsignedInt(buffer[byteIndex + 3]);
            value &= 0xFFFFFFFF >>> bitIndex;
            value >>= 32 - bitCount - bitIndex;
            return value;
        }
        return peekIntFallback(bitCount);
    }

    public int position() {
        return position;
    }

    public void position(int position) {
        Objects.checkIndex(position, lengthBits + 1);
        this.position = position;
    }

    public void align(int multiple) {
        position = Helpers.getNextMultiple(position, multiple);
    }

    private int peekIntFallback(int bitCount) {
        int value = 0;
        int byteIndex = position / 8;
        int bitIndex = position % 8;

        while (bitCount > 0) {
            if (bitIndex >= 8) {
                bitIndex = 0;
                byteIndex++;
            }

            int bitsToRead = Math.min(bitCount, 8 - bitIndex);
            int mask = 0xFF >>> bitIndex;
            int currentByte = (mask & Byte.toUnsignedInt(buffer[byteIndex])) >>> (8 - bitIndex - bitsToRead);

            value = (value << bitsToRead) | currentByte;
            bitIndex += bitsToRead;
            bitCount -= bitsToRead;
        }
        return value;
    }

    private int remaining() {
        return lengthBits - position;
    }
}
