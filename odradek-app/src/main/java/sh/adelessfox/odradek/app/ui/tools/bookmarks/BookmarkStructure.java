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
import java.util.Objects;
import java.util.stream.Stream;

public sealed interface BookmarkStructure extends TreeStructure<BookmarkStructure> {
    record Folder(Bookmarks repository, FolderId id, String name) implements BookmarkStructure {
        @Override
        public boolean equals(Object object) {
            return object instanceof Folder folder && Objects.equals(id, folder.id);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }
    }

    sealed interface Bookmark extends BookmarkStructure, Bookmarkable {
        static Bookmark forKey(Bookmarks repository, BookmarkKey key, String name) {
            return switch (key) {
                case BookmarkKey.OfGroup k -> new GroupBookmark(repository, k, name);
                case BookmarkKey.OfObject k -> new ObjectBookmark(repository, k, name);
            };
        }

        BookmarkKey key();

        String name();

        @Override
        default BookmarkKey bookmarkKey() {
            return key();
        }
    }

    record GroupBookmark(Bookmarks repository, BookmarkKey.OfGroup key, String name) implements Bookmark {
        @Override
        public boolean equals(Object object) {
            return object instanceof GroupBookmark that && Objects.equals(key, that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(key);
        }
    }

    record ObjectBookmark(Bookmarks repository, BookmarkKey.OfObject key, String name) implements Bookmark, ObjectIdHolder {
        @Override
        public ObjectId objectId() {
            return key.objectId();
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof ObjectBookmark that && Objects.equals(key, that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(key);
        }
    }

    @Override
    default List<? extends BookmarkStructure> getChildren() {
        return switch (this) {
            case Folder(var repository, var id, _) -> {
                var folders = repository.getAllFoldersInFolder(id).stream()
                    .map(folder -> new Folder(repository, folder.id(), folder.name()))
                    .sorted(Comparator.comparing(Folder::name));
                var bookmarks = repository.getAllInFolder(id).stream()
                    .map(bookmark -> Bookmark.forKey(repository, bookmark.key(), bookmark.name()))
                    .sorted(Comparator.comparing(Bookmark::name));
                yield Stream.concat(folders, bookmarks).toList();
            }
            case Bookmark _ -> List.of();
        };
    }

    @Override
    default boolean hasChildren() {
        return switch (this) {
            case Folder _ -> true;
            case Bookmark _ -> false;
        };
    }
}
