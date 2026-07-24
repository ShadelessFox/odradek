package sh.adelessfox.odradek.app.util.steam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.util.steam.vdf.VdfObject;
import sh.adelessfox.odradek.app.util.steam.vdf.VdfParser;
import sh.adelessfox.odradek.util.Result;
import sh.adelessfox.odradek.util.system.OperatingSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

final class Steam {
    private static final Logger log = LoggerFactory.getLogger(Steam.class);

    private Steam() {
    }

    static List<Path> findAllGames() throws IOException {
        var paths = new ArrayList<Path>();
        for (var folderPath : findLibraryFolders()) {
            findAllGames(folderPath, paths::add);
        }
        return List.copyOf(paths);
    }

    private static void findAllGames(Path folderPath, Consumer<Path> consumer) throws IOException {
        try (var stream = Files.newDirectoryStream(folderPath, "appmanifest_*.acf")) {
            for (var manifestPath : stream) {
                findInstallDir(folderPath, manifestPath).ifPresent(consumer);
            }
        }
    }

    private static Optional<Path> findInstallDir(Path folderPath, Path manifestPath) throws IOException {
        return switch (VdfParser.parse(manifestPath)) {
            case Result.Ok(var manifest) -> findInstallDir(folderPath, manifest);
            case Result.Error(var error) -> {
                log.warn("Failed to parse appmanifest at {}: {}", manifestPath, error);
                yield Optional.empty();
            }
        };
    }

    private static Optional<Path> findInstallDir(Path folderPath, VdfObject appState) {
        return appState.get("AppState")
            .flatMap(appState1 -> appState1.getAsObject().get("installdir"))
            .map(installDir -> folderPath.resolve("common", installDir.getAsString()))
            .filter(Files::exists);
    }

    static List<Path> findLibraryFolders() throws IOException {
        var libraryFoldersPath = getSteamPath().resolve("config", "libraryfolders.vdf");
        if (!Files.exists(libraryFoldersPath)) {
            log.warn("Steam libraryfolders.vdf not found at {}", libraryFoldersPath);
            return List.of();
        }
        return switch (VdfParser.parse(libraryFoldersPath)) {
            case Result.Ok(var libraryFolders) -> findLibraryFolderPaths(libraryFolders);
            case Result.Error(var error) -> {
                log.warn("Failed to parse libraryfolders.vdf: {}", error);
                yield List.of();
            }
        };
    }

    private static List<Path> findLibraryFolderPaths(VdfObject libraryFolders) {
        return libraryFolders.get("libraryfolders").stream()
            .flatMap(libraryFolders1 -> libraryFolders1.getAsObject().values().stream())
            .flatMap(libraryFolder -> libraryFolder.getAsObject().get("path")
                .map(path -> Path.of(path.getAsString(), "steamapps"))
                .filter(Files::exists)
                .stream())
            .toList();
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
