package sh.adelessfox.libatrac9.util;

public final class BitReader {
    private byte[] Buffer;
    private int LengthBits;
    public int Position;

    public BitReader(byte[] buffer) {
        SetBuffer(buffer);
    }

    public void SetBuffer(byte[] buffer) {
        Buffer = buffer;
        LengthBits = Buffer != null ? Buffer.length * 8 : 0;
        Position = 0;
    }

    public int ReadInt(int bitCount) {
        int value = PeekInt(bitCount);
        Position += bitCount;
        return value;
    }

    public int ReadSignedInt(int bitCount) {
        int value = PeekInt(bitCount);
        Position += bitCount;
        return Bit.SignExtend32(value, bitCount);
    }

    public boolean ReadBool() {
        return ReadInt(1) == 1;
    }

    public int ReadOffsetBinary(int bitCount, OffsetBias bias) {
        int offset = (1 << (bitCount - 1)) - bias.ordinal();
        int value = PeekInt(bitCount) - offset;
        Position += bitCount;
        return value;
    }

    public void AlignPosition(int multiple) {
        Position = Helpers.GetNextMultiple(Position, multiple);
    }

    public int PeekInt(int bitCount) {
        assert bitCount >= 0 && bitCount <= 32;

        if (bitCount > Remaining()) {
            if (Position >= LengthBits) {
                return 0;
            }

            int extraBits = bitCount - Remaining();
            return PeekIntFallback(Remaining()) << extraBits;
        }

        int byteIndex = Position / 8;
        int bitIndex = Position % 8;

        if (bitCount <= 9 && Remaining() >= 16) {
            int value = Byte.toUnsignedInt(Buffer[byteIndex]) << 8 | Byte.toUnsignedInt(Buffer[byteIndex + 1]);
            value &= 0xFFFF >>> bitIndex;
            value >>= 16 - bitCount - bitIndex;
            return value;
        }

        if (bitCount <= 17 && Remaining() >= 24) {
            int value = Byte.toUnsignedInt(Buffer[byteIndex]) << 16 | Byte.toUnsignedInt(Buffer[byteIndex + 1]) << 8 | Byte.toUnsignedInt(Buffer[byteIndex + 2]);
            value &= 0xFFFFFF >>> bitIndex;
            value >>= 24 - bitCount - bitIndex;
            return value;
        }

        if (bitCount <= 25 && Remaining() >= 32) {
            int value = Byte.toUnsignedInt(Buffer[byteIndex]) << 24 | Byte.toUnsignedInt(Buffer[byteIndex + 1]) << 16 | Byte.toUnsignedInt(Buffer[byteIndex + 2]) << 8 | Byte.toUnsignedInt(Buffer[byteIndex + 3]);
            value &= (int) (0xFFFFFFFF >>> bitIndex);
            value >>= 32 - bitCount - bitIndex;
            return value;
        }
        return PeekIntFallback(bitCount);
    }

    private int PeekIntFallback(int bitCount) {
        int value = 0;
        int byteIndex = Position / 8;
        int bitIndex = Position % 8;

        while (bitCount > 0) {
            if (bitIndex >= 8) {
                bitIndex = 0;
                byteIndex++;
            }

            int bitsToRead = Math.min(bitCount, 8 - bitIndex);
            int mask = 0xFF >>> bitIndex;
            int currentByte = (mask & Byte.toUnsignedInt(Buffer[byteIndex])) >>> (8 - bitIndex - bitsToRead);

            value = (value << bitsToRead) | currentByte;
            bitIndex += bitsToRead;
            bitCount -= bitsToRead;
        }
        return value;
    }

    private int Remaining() {
        return LengthBits - Position;
    }

    /// <summary>
    /// Specifies the bias of an offset binary value. A positive bias can represent one more
    /// positive value than negative value, and a negative bias can represent one more
    /// negative value than positive value.
    /// </summary>
    /// <remarks>Example:
    /// A 4-bit offset binary value with a positive bias can store
    /// the values 8 through -7 inclusive.
    /// A 4-bit offset binary value with a positive bias can store
    /// the values 7 through -8 inclusive.</remarks>
    public enum OffsetBias {
        Negative
    }
}
