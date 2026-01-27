package sh.adelessfox.odradek.viewer.texture.component;

import java.util.Collection;

public interface FileProvider {
    Collection<FilePath> getChildren(FilePath parent);

    boolean hasChildren(FilePath parent);

    boolean isFile(FilePath path);
}
