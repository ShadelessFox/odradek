package sh.adelessfox.odradek.game.hfw.game;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EPlatform;

import java.nio.file.Path;

public record ForbiddenWestFileSystem(Path source, EPlatform platform) {
    public Path resolve(String path) {
        String[] parts = path.split(":", 2);
        return switch (parts[0]) {
            case "source" -> source.resolve(parts[1]);
            case "cache" -> resolve("source:LocalCache" + platform).resolve(parts[1]);
            case "tools" -> resolve("source:tools").resolve(parts[1]);
            default -> throw new IllegalArgumentException("Unknown device path: " + path);
        };
    }
}
