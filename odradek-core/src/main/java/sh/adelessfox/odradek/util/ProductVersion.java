package sh.adelessfox.odradek.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.lang.foreign.ValueLayout.*;

/**
 * Represents a product version of a Windows executable.
 *
 * @param major    the major version number
 * @param minor    the minor version number
 * @param build    the build number
 * @param revision the revision number
 */
public record ProductVersion(int major, int minor, int build, int revision) {
    private static final Logger log = LoggerFactory.getLogger(ProductVersion.class);

    /**
     * Probes the given file path for a product version.
     * <p>
     * On non-Windows platforms, this method will always return {@link Optional#empty()}.
     *
     * @param path the file path to probe
     * @return an {@link Optional} containing the product version, otherwise empty
     * @throws IllegalArgumentException if the path represents a directory
     */
    public static Optional<ProductVersion> probe(Path path) {
        if (Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path represents a directory");
        }
        if (OS.name() != OS.Name.WINDOWS) {
            return Optional.empty();
        }
        try (Arena arena = Arena.ofConfined()) {
            var linker = Linker.nativeLinker();
            var lookup = SymbolLookup.libraryLookup("Version", arena);

            var getFileVersionInfoSizeW = linker.downcallHandle(
                lookup.findOrThrow("GetFileVersionInfoSizeW"),
                FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS));
            var getFileVersionInfoW = linker.downcallHandle(
                lookup.findOrThrow("GetFileVersionInfoW"),
                FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT, ADDRESS));
            var verQueryValueW = linker.downcallHandle(
                lookup.findOrThrow("VerQueryValueW"),
                FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ADDRESS));

            var pathSegment = allocateFromWString(arena, path.toAbsolutePath().toString());
            var querySegment = allocateFromWString(arena, "\\\\");

            var size = (int) getFileVersionInfoSizeW.invoke(pathSegment, MemorySegment.NULL);
            if (size == 0) {
                log.error("GetFileVersionInfoSizeW failed");
                return Optional.empty();
            }

            var data = arena.allocate(size);
            if ((int) getFileVersionInfoW.invoke(pathSegment, 0, size, data) == 0) {
                log.error("GetFileVersionInfoW failed");
                return Optional.empty();
            }

            var infoAddressSegment = arena.allocate(ADDRESS);
            var infoSizeSegment = arena.allocate(JAVA_INT);
            if ((int) verQueryValueW.invoke(data, querySegment, infoAddressSegment, infoSizeSegment) == 0) {
                log.error("VerQueryValueW failed");
                return Optional.empty();
            }

            var infoSize = infoSizeSegment.get(JAVA_INT, 0);
            var infoAddress = infoAddressSegment.get(ADDRESS, 0).reinterpret(infoSize);
            var dwProductVersionMS = infoAddress.get(JAVA_INT, 16);
            int dwProductVersionLS = infoAddress.get(JAVA_INT, 20);

            return Optional.of(new ProductVersion(
                dwProductVersionMS >>> 16,
                dwProductVersionMS & 0xffff,
                dwProductVersionLS >>> 16,
                dwProductVersionLS & 0xffff
            ));
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    private static MemorySegment allocateFromWString(Arena arena, String string) {
        var chars = string.toCharArray();
        var segment = arena.allocate((long) chars.length * Character.BYTES + Character.BYTES /* nul */);
        MemorySegment.copy(chars, 0, segment, JAVA_CHAR, 0, chars.length);
        return segment;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + build + "." + revision;
    }
}
