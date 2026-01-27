package sh.adelessfox.odradek.viewer.texture.component;

import sh.adelessfox.odradek.ui.components.tree.TreeStructure;

import java.util.List;
import java.util.Optional;

sealed public interface FileStructure extends TreeStructure<FileStructure> {
    static FileStructure of(FileProvider provider) {
        return new Folder(Optional.empty(), FilePath.of(), provider);
    }

    Optional<FilePath> parent();

    FilePath path();

    @Override
    default List<? extends FileStructure> getChildren() {
        return switch (this) {
            case Folder(_, var path, var provider) -> provider.getChildren(path).stream()
                .map(p -> provider.isFile(p)
                    ? new File(Optional.of(path), p)
                    : new Folder(Optional.of(path), p, provider))
                .toList();
            case File _ -> List.of();
        };
    }

    @Override
    default boolean hasChildren() {
        return this instanceof Folder(_, var path, var provider) && provider.hasChildren(path);
    }

    record Folder(Optional<FilePath> parent, FilePath path, FileProvider provider) implements FileStructure {
    }

    record File(Optional<FilePath> parent, FilePath path) implements FileStructure {
    }
}
