package sh.adelessfox.odradek.app.ui;

import sh.adelessfox.odradek.app.cli.data.ObjectId;

import java.nio.file.Path;
import java.util.List;

public record ApplicationParameters(
    Path sourcePath,
    List<ObjectId> objectsToOpen,
    boolean enableDarkTheme,
    boolean enableDebugMode
) {
}
