package sh.adelessfox.odradek.app.ui;

import java.nio.file.Path;

public record ApplicationParameters(
    Path configPath,
    Path sourcePath,
    boolean enableDebugMode
) {
}
