package sh.adelessfox.vgmstream;

import sh.adelessfox.vgmstream.libvgmstream.libstreamfile_t;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public interface StreamFile {
    default MemorySegment allocate(Arena arena) {
        var streamfile = libstreamfile_t.allocate(arena);
        libstreamfile_t.user_data(streamfile, streamfile);
        libstreamfile_t.read(streamfile, libstreamfile_t.read.allocate(this::read, arena));
        libstreamfile_t.get_size(streamfile, libstreamfile_t.get_size.allocate(this::getSize, arena));
        libstreamfile_t.get_name(streamfile, libstreamfile_t.get_name.allocate(this::getName, arena));
        libstreamfile_t.open(streamfile, libstreamfile_t.open.allocate(this::open, arena));
        libstreamfile_t.close(streamfile, libstreamfile_t.close.allocate(this::close, arena));
        return streamfile;
    }

    int read(MemorySegment userdata, MemorySegment dst, long offset, int length);

    long getSize(MemorySegment userdata);

    MemorySegment getName(MemorySegment userdata);

    MemorySegment open(MemorySegment userdata, MemorySegment filename);

    void close(MemorySegment userdata);
}
