package sh.adelessfox.odradek.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.util.system.OperatingSystem;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

final class VgmstreamDownloader {
    private static final Logger log = LoggerFactory.getLogger(VgmstreamDownloader.class);

    private static final String VERSION = "r2083";
    private static final String PREFIX = "https://github.com/vgmstream/vgmstream/releases/download/" + VERSION + "/";
    private static final String VGMSTREAM_WINDOWS_AMD64 = PREFIX + "vgmstream-win64.zip";
    private static final String VGMSTREAM_LINUX_AMD64 = PREFIX + "vgmstream-linux-cli.zip";
    private static final String VGMSTREAM_MACOS_ARM64 = PREFIX + "vgmstream-mac-cli.zip";

    private VgmstreamDownloader() {
    }

    static Path download() {
        var string = switch (OperatingSystem.name()) {
            case WINDOWS -> switch (OperatingSystem.arch()) {
                case AMD64 -> VGMSTREAM_WINDOWS_AMD64;
                default -> throw new UnsupportedOperationException(OperatingSystem.arch().name());
            };
            case LINUX -> switch (OperatingSystem.arch()) {
                case AMD64 -> VGMSTREAM_LINUX_AMD64;
                default -> throw new UnsupportedOperationException(OperatingSystem.arch().name());
            };
            case MACOS -> switch (OperatingSystem.arch()) {
                case AARCH64 -> VGMSTREAM_MACOS_ARM64;
                default -> throw new UnsupportedOperationException(OperatingSystem.arch().name());
            };
        };
        var cache = Path.of("vgmstream-" + VERSION);
        if (Files.notExists(cache)) {
            downloadZip(URI.create(string), cache);
        }
        var executable = switch (OperatingSystem.name()) {
            case WINDOWS -> "vgmstream-cli.exe";
            case LINUX, MACOS -> "vgmstream-cli";
        };
        return cache.resolve(executable);
    }

    private static void downloadZip(URI uri, Path path) {
        var client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

        try (client) {
            var request = HttpRequest.newBuilder()
                .uri(uri)
                .build();

            log.info("Downloading vgmstream from {}", uri);
            var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                throw new Exception("HTTP error: " + response.statusCode());
            }

            log.info("Unpacking to {}", path);
            try (var in = new ZipInputStream(response.body())) {
                for (ZipEntry entry; (entry = in.getNextEntry()) != null; ) {
                    log.info("Extracting {}", entry.getName());

                    var target = path.resolve(entry.getName());
                    Files.createDirectories(target.getParent());
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (Exception e) {
            log.error("Failed to download {}", uri, e);
            throw new RuntimeException(e);
        }
    }
}
