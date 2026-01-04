package sh.adelessfox.odradek.audio.codec.libatrac9;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_INT;

public final class Atrac9Decoder implements AutoCloseable {
    private final Atrac9Library lib;
    private final MemorySegment handle;

    public Atrac9Decoder() {
        this.lib = Atrac9Library.load().orElseThrow(() -> new IllegalStateException("Failed to load libatrac9 library"));
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
        lib.close();
    }
}
