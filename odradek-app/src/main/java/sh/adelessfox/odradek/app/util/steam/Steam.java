package sh.adelessfox.odradek.app.util.steam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.util.steam.vdf.VdfParser;
import sh.adelessfox.odradek.util.system.OperatingSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class Steam {
    private static final Logger log = LoggerFactory.getLogger(Steam.class);

    private Steam() {
    }

    static List<Path> findAllGames() throws IOException {
        var paths = new ArrayList<Path>();
        for (var folderPath : findLibraryFolders()) {
            try (var stream = Files.newDirectoryStream(folderPath, "appmanifest_*.acf")) {
                for (var manifestPath : stream) {
                    var manifest = VdfParser.parse(manifestPath);
                    if (manifest.isError()) {
                        log.warn("Failed to parse appmanifest at {}: {}", manifestPath, manifest.unwrapError());
                        continue;
                    }
                    var appState = manifest.unwrap().getAsJsonObject("AppState");
                    var installDir = folderPath.resolve("common", appState.get("installdir").getAsString());
                    if (Files.exists(installDir)) {
                        paths.add(installDir);
                    }
                }
            }
        }
        return List.copyOf(paths);
    }

    static List<Path> findLibraryFolders() throws IOException {
        var libraryFoldersPath = getSteamPath().resolve("config", "libraryfolders.vdf");
        if (!Files.exists(libraryFoldersPath)) {
            log.warn("Steam libraryfolders.vdf not found at {}", libraryFoldersPath);
            return List.of();
        }
        var libraryFolders = VdfParser.parse(libraryFoldersPath);
        if (libraryFolders.isError()) {
            log.warn("Failed to parse libraryfolders.vdf: {}", libraryFolders.unwrapError());
            return List.of();
        }
        var folders = new ArrayList<Path>();
        for (var entry : libraryFolders.unwrap().getAsJsonObject("libraryfolders").entrySet()) {
            var folder = entry.getValue().getAsJsonObject();
            var folderPath = Path.of(folder.get("path").getAsString());
            if (Files.exists(folderPath)) {
                folders.add(folderPath.resolve("steamapps"));
            }
        }
        return List.copyOf(folders);
    }

    static Path getSteamPath() {
        String userHome = System.getProperty("user.home");
        if (userHome == null) {
            throw new IllegalStateException("Unable to determine user home directory");
        }
        return switch (OperatingSystem.name()) {
            case WINDOWS -> Path.of("C:\\Program Files (x86)\\Steam");
            case LINUX -> Path.of(userHome, ".steam", "steam");
            case MACOS -> Path.of(userHome, "Library", "Application Support", "Steam");
        };
    }
}
