package sh.adelessfox.odradek.rtti.generator.data;

public enum EnumValueSize {
    INT8(byte.class, Byte.BYTES),
    INT16(short.class, Short.BYTES),
    INT32(int.class, Integer.BYTES);

    private final Class<? extends Number> type;
    private final int bytes;

    EnumValueSize(Class<? extends Number> type, int bytes) {
        this.type = type;
        this.bytes = bytes;
    }

    public Class<? extends Number> type() {
        return type;
    }

    public int bytes() {
        return bytes;
    }
}
