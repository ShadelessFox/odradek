package sh.adelessfox.odradek.audio.libatrac9;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;

import static java.lang.foreign.ValueLayout.*;

public final class Atrac9Decoder implements AutoCloseable {
    private final Arena arena;
    private final Lib lib;
    private final MemorySegment handle;

    public Atrac9Decoder(Arena arena, Path path) {
        this.arena = arena;
        this.lib = new Lib(arena, path);
        this.handle = lib.Atrac9GetHandle();
    }

    public void initialize(byte[] configData) {
        if (configData.length != 4) {
            throw new IllegalArgumentException("configData must be 4 bytes long");
        }
        try (Arena arena = Arena.ofConfined()) {
            var configBuf = arena.allocateFrom(JAVA_BYTE, configData);
            var status = lib.Atrac9InitDecoder(handle, configBuf);
            if (status != Atrac9Status.ERR_SUCCESS) {
                throw new Atrac9Exception("Atrac9InitDecoder failed: " + status);
            }
        }
    }

    public int decode(MemorySegment pAtrac9Buffer, MemorySegment pPcmBuffer) {
        try (Arena arena = Arena.ofConfined()) {
            var pNBytesUsed = arena.allocate(JAVA_INT);
            var status = lib.Atrac9Decode(handle, pAtrac9Buffer, pPcmBuffer, pNBytesUsed);
            if (status != Atrac9Status.ERR_SUCCESS) {
                throw new Atrac9Exception("Atrac9Decode failed: " + status);
            }
            return pNBytesUsed.get(JAVA_INT, 0);
        }
    }

    public Atrac9CodecInfo getCodecInfo() {
        try (Arena arena = Arena.ofConfined()) {
            var info = arena.allocate(Atrac9CodecInfo.BYTES);
            var status = lib.Atrac9GetCodecInfo(handle, info);
            if (status != Atrac9Status.ERR_SUCCESS) {
                throw new Atrac9Exception("Atrac9GetCodecInfo failed: " + status);
            }
            return new Atrac9CodecInfo(
                info.get(JAVA_INT, 0),
                info.get(JAVA_INT, 4),
                info.get(JAVA_INT, 8),
                info.get(JAVA_INT, 12),
                info.get(JAVA_INT, 16),
                info.get(JAVA_INT, 20),
                info.get(JAVA_INT, 24),
                info.get(JAVA_INT, 28)
            );
        }
    }

    @Override
    public void close() {
        lib.Atrac9ReleaseHandle(handle);
    }

    private static final class Lib {
        private final MethodHandle Atrac9GetHandle;
        private final MethodHandle Atrac9ReleaseHandle;
        private final MethodHandle Atrac9InitDecoder;
        private final MethodHandle Atrac9Decode;
        private final MethodHandle Atrac9GetCodecInfo;

        Lib(Arena arena, Path path) {
            var linker = Linker.nativeLinker();
            var lookup = SymbolLookup.libraryLookup(path, arena);

            // void* Atrac9GetHandle(void);
            Atrac9GetHandle = linker.downcallHandle(
                lookup.findOrThrow("Atrac9GetHandle"),
                FunctionDescriptor.of(ADDRESS));

            // void Atrac9ReleaseHandle(void* handle);
            Atrac9ReleaseHandle = linker.downcallHandle(
                lookup.findOrThrow("Atrac9ReleaseHandle"),
                FunctionDescriptor.ofVoid(ADDRESS));

            // int Atrac9InitDecoder(void* handle, unsigned char *pConfigData);
            Atrac9InitDecoder = linker.downcallHandle(
                lookup.findOrThrow("Atrac9InitDecoder"),
                FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS));

            // int Atrac9Decode(void* handle, const unsigned char *pAtrac9Buffer, short *pPcmBuffer, int *pNBytesUsed);
            Atrac9Decode = linker.downcallHandle(
                lookup.findOrThrow("Atrac9Decode"),
                FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ADDRESS));

            // int Atrac9GetCodecInfo(void* handle, Atrac9CodecInfo *pCodecInfo);
            Atrac9GetCodecInfo = linker.downcallHandle(
                lookup.findOrThrow("Atrac9GetCodecInfo"),
                FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS));
        }

        // void* Atrac9GetHandle(void);
        MemorySegment Atrac9GetHandle() {
            try {
                return (MemorySegment) Atrac9GetHandle.invokeExact();
            } catch (Throwable e) {
                throw new AssertionError(e);
            }
        }

        // void Atrac9ReleaseHandle(void* handle);
        void Atrac9ReleaseHandle(MemorySegment handle) {
            try {
                Atrac9ReleaseHandle.invokeExact(handle);
            } catch (Throwable e) {
                throw new AssertionError(e);
            }
        }

        // int Atrac9InitDecoder(void* handle, unsigned char *pConfigData);
        Atrac9Status Atrac9InitDecoder(MemorySegment handle, MemorySegment pConfigData) {
            try {
                return Atrac9Status.valueOf((int) Atrac9InitDecoder.invokeExact(handle, pConfigData));
            } catch (Throwable e) {
                throw new AssertionError(e);
            }
        }

        // int Atrac9Decode(void* handle, const unsigned char *pAtrac9Buffer, short *pPcmBuffer, int *pNBytesUsed);
        Atrac9Status Atrac9Decode(MemorySegment handle, MemorySegment pAtrac9Buffer, MemorySegment pPcmBuffer, MemorySegment pNBytesUsed) {
            try {
                return Atrac9Status.valueOf((int) Atrac9Decode.invokeExact(handle, pAtrac9Buffer, pPcmBuffer, pNBytesUsed));
            } catch (Throwable e) {
                throw new AssertionError(e);
            }
        }

        // int Atrac9GetCodecInfo(void* handle, Atrac9CodecInfo *pCodecInfo);
        Atrac9Status Atrac9GetCodecInfo(MemorySegment handle, MemorySegment pCodecInfo) {
            try {
                return Atrac9Status.valueOf((int) Atrac9GetCodecInfo.invokeExact(handle, pCodecInfo));
            } catch (Throwable e) {
                throw new AssertionError(e);
            }
        }
    }
}
