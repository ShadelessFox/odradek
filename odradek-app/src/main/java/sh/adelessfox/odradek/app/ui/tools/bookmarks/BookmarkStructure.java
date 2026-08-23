package sh.adelessfox.odradek.app.ui.tools.bookmarks;

import sh.adelessfox.odradek.app.ui.bookmarks.Bookmarks;
import sh.adelessfox.odradek.app.ui.bookmarks.FolderId;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.game.decima.ObjectIdHolder;
import sh.adelessfox.odradek.ui.components.tree.TreeStructure;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public sealed interface BookmarkStructure extends TreeStructure<BookmarkStructure> {
    record Folder(Bookmarks repository, FolderId id, String name) implements BookmarkStructure {
        @Override
        public List<? extends BookmarkStructure> getChildren() {
            var folders = repository.getAllFoldersInFolder(id).stream()
                .map(f -> new Folder(repository, f.id(), f.name()))
                .sorted(Comparator.comparing(Folder::name));
            var bookmarks = repository.getAllInFolder(id).stream()
                .map(b -> new Bookmark(repository, b.objectId(), b.name()))
                .sorted(Comparator.comparing(Bookmark::name));
            return Stream.concat(folders, bookmarks).toList();
        }

        @Override
        public boolean hasChildren() {
            return true;
        }
    }

    record Bookmark(Bookmarks repository, ObjectId id, String name) implements BookmarkStructure, ObjectIdHolder {
        @Override
        public List<? extends BookmarkStructure> getChildren() {
            return List.of();
        }

        @Override
        public boolean hasChildren() {
            return false;
        }

        @Override
        public ObjectId objectId() {
            return id;
        }
    }
}
