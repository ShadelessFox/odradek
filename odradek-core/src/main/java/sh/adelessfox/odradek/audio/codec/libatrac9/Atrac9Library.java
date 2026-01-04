package sh.adelessfox.odradek.audio.codec.libatrac9;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.util.OS;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

/**
 * A wrapper for the libatrac9 native library, licensed under the MIT License.
 *
 * @see <a href="https://github.com/Thealexbarney/LibAtrac9">https://github.com/Thealexbarney/LibAtrac9</a>
 */
final class Atrac9Library implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(Atrac9Library.class);

    private final Arena arena;
    private final Path file;

    private final MethodHandle Atrac9GetHandle;
    private final MethodHandle Atrac9ReleaseHandle;
    private final MethodHandle Atrac9InitDecoder;
    private final MethodHandle Atrac9Decode;
    private final MethodHandle Atrac9GetCodecInfo;

    private Atrac9Library(Arena arena, Path file, SymbolLookup lookup) {
        this.arena = arena;
        this.file = file;

        var linker = Linker.nativeLinker();

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

    static Optional<Atrac9Library> load() {
        return load(determineLibraryName(), Arena.ofConfined());
    }

    // region Native functions
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
    // endregion

    @Override
    public void close() {
        arena.close();

        // Delete the file after the arena is closed as the loaded library may still lock the file
        if (file != null) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                log.warn("Failed to delete temporary native library file '{}'", file, e);
            }
        }
    }

    // Vaguely based on com.formdev.flatlaf.util.NativeLibrary.loadLibraryFromJar
    private static Optional<Atrac9Library> load(String libraryName, Arena arena) {
        var url = Atrac9Library.class.getResource(libraryName);
        if (url == null) {
            log.error("Couldn't find native library '{}'", libraryName);
            return Optional.empty();
        }
        try {
            // For development environment
            if ("file".equals(url.getProtocol())) {
                File file = new File(url.getPath());
                if (file.isFile()) {
                    var lookup = SymbolLookup.libraryLookup(file.toPath(), arena);
                    var library = new Atrac9Library(arena, null, lookup);
                    return Optional.of(library);
                }
            }
            // Extract from jar to temp file
            var path = createTempFile(libraryName);
            try (InputStream is = url.openStream()) {
                Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
            }
            var lookup = SymbolLookup.libraryLookup(path, arena);
            var library = new Atrac9Library(arena, path, lookup);
            return Optional.of(library);
        } catch (Exception e) {
            log.error("Failed to load native library '{}'", libraryName, e);
            return Optional.empty();
        }
    }

    private static Path createTempFile(String libraryName) throws IOException {
        int sep = libraryName.lastIndexOf('/');
        var name = (sep >= 0) ? libraryName.substring(sep + 1) : libraryName;
        return Files.createTempFile("odradek", name);
    }

    private static String determineLibraryName() {
        if (OS.name() != OS.Name.WINDOWS || OS.arch() != OS.Arch.AMD64) {
            throw new IllegalStateException("Missing native library for " + OS.name() + " " + OS.arch());
        }
        var packageName = Atrac9Library.class.getPackageName();
        var libraryName = "libatrac9.dll";
        return '/' + packageName.replace('.', '/') + '/' + libraryName;
    }
}
