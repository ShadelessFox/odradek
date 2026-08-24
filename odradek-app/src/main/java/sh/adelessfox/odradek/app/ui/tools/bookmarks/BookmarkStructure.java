package sh.adelessfox.odradek.app.ui.tools.bookmarks;

import sh.adelessfox.odradek.app.ui.bookmarks.BookmarkKey;
import sh.adelessfox.odradek.app.ui.bookmarks.Bookmarkable;
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
                .map(b -> switch (b.key()) {
                    case BookmarkKey.OfGroup k -> new GroupBookmark(repository, k, b.name());
                    case BookmarkKey.OfObject k -> new ObjectBookmark(repository, k, b.name());
                })
                .sorted(Comparator.comparing(Bookmark::name));
            return Stream.concat(folders, bookmarks).toList();
        }

        @Override
        public boolean hasChildren() {
            return true;
        }
    }

    sealed interface Bookmark extends BookmarkStructure, Bookmarkable {
        BookmarkKey key();

        String name();

        @Override
        default BookmarkKey bookmarkKey() {
            return key();
        }
    }

    record GroupBookmark(Bookmarks repository, BookmarkKey.OfGroup key, String name) implements Bookmark {
        @Override
        public List<? extends BookmarkStructure> getChildren() {
            return List.of();
        }

        @Override
        public boolean hasChildren() {
            return false;
        }
    }

    record ObjectBookmark(Bookmarks repository, BookmarkKey.OfObject key, String name) implements Bookmark, ObjectIdHolder {
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
            return key.objectId();
        }
    }

    default boolean sameAs(BookmarkStructure other) {
        return switch (this) {
            case Folder a when other instanceof Folder b -> a.id.equals(b.id);
            case GroupBookmark a when other instanceof GroupBookmark b -> a.key.equals(b.key);
            case ObjectBookmark a when other instanceof ObjectBookmark b -> a.key.equals(b.key);
            default -> false;
        };
    }
}
