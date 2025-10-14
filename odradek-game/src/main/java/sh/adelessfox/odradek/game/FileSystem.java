package sh.adelessfox.odradek.game;

import java.nio.file.Path;

public interface FileSystem {
    Path resolve(String path);
}
