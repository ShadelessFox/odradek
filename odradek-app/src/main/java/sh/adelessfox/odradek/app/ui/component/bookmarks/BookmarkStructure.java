package sh.adelessfox.odradek.app.ui.component.bookmarks;

import sh.adelessfox.odradek.app.ui.bookmarks.BookmarkRepository;
import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.game.ObjectIdHolder;
import sh.adelessfox.odradek.ui.components.tree.TreeStructure;

import java.util.List;

sealed interface BookmarkStructure extends TreeStructure<BookmarkStructure> {
    final class Root implements BookmarkStructure {
        final BookmarkRepository repository;

        Root(BookmarkRepository repository) {
            this.repository = repository;
        }
    }

    final class Bookmark implements BookmarkStructure, ObjectIdHolder {
        final BookmarkRepository repository;
        final ObjectId id;
        final String name;

        public Bookmark(BookmarkRepository repository, ObjectId id, String name) {
            this.repository = repository;
            this.id = id;
            this.name = name;
        }

        @Override
        public ObjectId objectId() {
            return id;
        }
    }

    @Override
    default BookmarkStructure getRoot() {
        return this;
    }

    @Override
    default List<? extends BookmarkStructure> getChildren(BookmarkStructure parent) {
        return switch (parent) {
            case Root root -> root.repository.getAll().stream()
                .map(b -> new Bookmark(root.repository, b.objectId(), b.name()))
                .toList();
            case Bookmark _ -> List.of();
        };
    }

    @Override
    default boolean hasChildren(BookmarkStructure parent) {
        return switch (parent) {
            case Root _ -> true;
            case Bookmark _ -> false;
        };
    }
}
