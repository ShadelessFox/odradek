package sh.adelessfox.odradek.viewer.shader.win32.com;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;

public class IInterface {
    protected final MemorySegment segment;

    protected IInterface(MemorySegment segment) {
        this.segment = segment.reinterpret(ADDRESS.byteSize());
    }

    public MemorySegment segment() {
        return segment;
    }

    protected MethodHandle downcallHandle(int vtableIndex, FunctionDescriptor descriptor) {
        var vtable = segment.get(ADDRESS, 0);
        var address = vtable
            .reinterpret(ADDRESS.byteSize() * (vtableIndex + 1))
            .get(ADDRESS, ADDRESS.byteSize() * vtableIndex);
        return Linker.nativeLinker()
            .downcallHandle(address, descriptor.insertArgumentLayouts(0, ADDRESS))
            .bindTo(segment);
    }
}
