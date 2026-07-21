package sh.adelessfox.odradek.util.system;

import com.kichik.pecoff4j.constant.ResourceType;
import com.kichik.pecoff4j.io.DataReader;
import com.kichik.pecoff4j.io.PEParser;
import com.kichik.pecoff4j.resources.VersionInfo;
import com.kichik.pecoff4j.util.ResourceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

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
     * Attempts to find the product version of a Windows executable at the given path.
     *
     * @param path the file path to the Windows executable
     * @return an {@link Optional} containing the product version, otherwise empty if it could not be retrieved
     * @throws IllegalArgumentException if the path represents a directory
     */
    public static Optional<ProductVersion> find(Path path) {
        if (Files.isDirectory(path)) {
            throw new IllegalArgumentException("Path represents a directory");
        }
        try {
            var pe = PEParser.parse(path);
            var rd = pe.getImageData().getResourceTable();

            var entries = ResourceHelper.findResources(rd, ResourceType.VERSION_INFO);
            if (entries.length != 1) {
                log.warn("Expected exactly one version info resource, but found {} in {}", entries.length, path);
                return Optional.empty();
            }

            var version = VersionInfo.read(new DataReader(entries[0].getData()));
            var fixedFileInfo = version.getFixedFileInfo();
            if (fixedFileInfo == null) {
                log.warn("No fixed file info found in version info resource in {}", path);
                return Optional.empty();
            }

            return Optional.of(new ProductVersion(
                fixedFileInfo.getProductVersionMS() >>> 16,
                fixedFileInfo.getProductVersionMS() & 0xffff,
                fixedFileInfo.getProductVersionLS() >>> 16,
                fixedFileInfo.getProductVersionLS() & 0xffff
            ));
        } catch (IOException e) {
            log.error("Failed to parse PE file: {}", path, e);
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + build + "." + revision;
    }
}
