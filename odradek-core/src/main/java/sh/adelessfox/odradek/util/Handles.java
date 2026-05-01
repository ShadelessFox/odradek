package sh.adelessfox.odradek.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

public final class Handles {
    private static final VarHandle asShortLE = MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.LITTLE_ENDIAN).withInvokeExactBehavior();
    private static final VarHandle asShortBE = MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.BIG_ENDIAN).withInvokeExactBehavior();
    private static final VarHandle asIntLE = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN).withInvokeExactBehavior();
    private static final VarHandle asIntBE = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.BIG_ENDIAN).withInvokeExactBehavior();
    private static final VarHandle asLongLE = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN).withInvokeExactBehavior();
    private static final VarHandle asLongBE = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN).withInvokeExactBehavior();
    private static final VarHandle asFloatLE = MethodHandles.byteArrayViewVarHandle(float[].class, ByteOrder.LITTLE_ENDIAN).withInvokeExactBehavior();
    private static final VarHandle asFloatBE = MethodHandles.byteArrayViewVarHandle(float[].class, ByteOrder.BIG_ENDIAN).withInvokeExactBehavior();
    private static final VarHandle asDoubleLE = MethodHandles.byteArrayViewVarHandle(double[].class, ByteOrder.LITTLE_ENDIAN).withInvokeExactBehavior();
    private static final VarHandle asDoubleBE = MethodHandles.byteArrayViewVarHandle(double[].class, ByteOrder.BIG_ENDIAN).withInvokeExactBehavior();

    private Handles() {
    }

    public static short getShort(byte[] array, int index, ByteOrder order) {
        var handle = order == ByteOrder.LITTLE_ENDIAN ? asShortLE : asShortBE;
        return (short) handle.get(array, index);
    }

    public static int getInt(byte[] array, int index, ByteOrder order) {
        var handle = order == ByteOrder.LITTLE_ENDIAN ? asIntLE : asIntBE;
        return (int) handle.get(array, index);
    }

    public static long getLong(byte[] array, int index, ByteOrder order) {
        var handle = order == ByteOrder.LITTLE_ENDIAN ? asLongLE : asLongBE;
        return (long) handle.get(array, index);
    }

    public static float getFloat(byte[] array, int index, ByteOrder order) {
        var handle = order == ByteOrder.LITTLE_ENDIAN ? asFloatLE : asFloatBE;
        return (float) handle.get(array, index);
    }

    public static double getDouble(byte[] array, int index, ByteOrder order) {
        var handle = order == ByteOrder.LITTLE_ENDIAN ? asDoubleLE : asDoubleBE;
        return (double) handle.get(array, index);
    }

    public static void setShort(byte[] array, int index, short value, ByteOrder order) {
        var handle = order == ByteOrder.LITTLE_ENDIAN ? asShortLE : asShortBE;
        handle.set(array, index, value);
    }

    public static void setInt(byte[] array, int index, int value, ByteOrder order) {
        var handle = order == ByteOrder.LITTLE_ENDIAN ? asIntLE : asIntBE;
        handle.set(array, index, value);
    }

    public static void setLong(byte[] array, int index, long value, ByteOrder order) {
        var handle = order == ByteOrder.LITTLE_ENDIAN ? asLongLE : asLongBE;
        handle.set(array, index, value);
    }

    public static void setFloat(byte[] array, int index, float value, ByteOrder order) {
        var handle = order == ByteOrder.LITTLE_ENDIAN ? asFloatLE : asFloatBE;
        handle.set(array, index, value);
    }

    public static void setDouble(byte[] array, int index, double value, ByteOrder order) {
        var handle = order == ByteOrder.LITTLE_ENDIAN ? asDoubleLE : asDoubleBE;
        handle.set(array, index, value);
    }
}
