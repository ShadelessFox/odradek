package sh.adelessfox.odradek.app.ui;

import java.nio.file.Path;

public record ApplicationParameters(
    Path sourcePath,
    boolean enableDarkTheme,
    boolean enableDebugMode
) {
}
