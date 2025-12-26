package sh.adelessfox.odradek.viewer.shader.win32.com;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.JAVA_INT;

public class IUnknown extends IInterface implements AutoCloseable {
    private final MethodHandle Release;

    public IUnknown(MemorySegment segment) {
        super(segment);

        Release = downcallHandle(2, FunctionDescriptor.of(JAVA_INT));
    }

    @Override
    public void close() {
        try {
            Release.invoke();
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }
}
