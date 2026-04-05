package sh.adelessfox.vgmstream.util;

import sh.adelessfox.vgmstream.StreamFile;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;

public record MemoryStreamFile(MemorySegment filename, MemorySegment data) implements StreamFile {
    public static MemoryStreamFile allocate(SegmentAllocator allocator, String filename, byte[] data) {
        return new MemoryStreamFile(
            allocator.allocateFrom(filename),
            allocator.allocateFrom(ValueLayout.JAVA_BYTE, data));
    }

    @Override
    public int read(MemorySegment userdata, MemorySegment dst, long offset, int length) {
        int off = Math.toIntExact(offset);
        int len = Math.toIntExact(Math.min(data.byteSize() - offset, length));
        MemorySegment.copy(data, off, dst, 0, len);
        return len;
    }

    @Override
    public long getSize(MemorySegment userdata) {
        return data.byteSize();
    }

    @Override
    public MemorySegment getName(MemorySegment userdata) {
        return filename;
    }

    @Override
    public MemorySegment open(MemorySegment userdata, MemorySegment filename) {
        if (MemorySegment.mismatch(filename(), 0, filename().byteSize(), filename, 0, filename().byteSize()) < 0) {
            return userdata;
        }
        return MemorySegment.NULL;
    }

    @Override
    public void close(MemorySegment userdata) {
        // do nothing
    }
}
