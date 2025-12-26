package sh.adelessfox.odradek.viewer.shader.win32.d3d;

import sh.adelessfox.odradek.viewer.shader.win32.com.IUnknown;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

public final class IDxcBlob extends IUnknown {
    private final MethodHandle GetBufferPointer;
    private final MethodHandle GetBufferSize;

    public IDxcBlob(MemorySegment segment) {
        super(segment);

        GetBufferPointer = downcallHandle(3, FunctionDescriptor.of(ADDRESS));
        GetBufferSize = downcallHandle(4, FunctionDescriptor.of(JAVA_LONG));
    }

    public MemorySegment getBuffer() {
        return getBufferPointer().reinterpret(getBufferSize());
    }

    public MemorySegment getBufferPointer() {
        try {
            return (MemorySegment) GetBufferPointer.invokeExact();
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    public long getBufferSize() {
        try {
            return (long) GetBufferSize.invokeExact();
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }
}
