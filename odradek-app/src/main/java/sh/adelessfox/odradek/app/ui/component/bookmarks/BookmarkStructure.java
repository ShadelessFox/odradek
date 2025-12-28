package sh.adelessfox.odradek.app.ui.component.bookmarks;

import sh.adelessfox.odradek.app.ui.bookmarks.Bookmarks;
import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.game.ObjectIdHolder;
import sh.adelessfox.odradek.ui.components.tree.TreeStructure;

import java.util.List;

public sealed interface BookmarkStructure extends TreeStructure<BookmarkStructure> {
    final class Root implements BookmarkStructure {
        final Bookmarks repository;

        Root(Bookmarks repository) {
            this.repository = repository;
        }

        @Override
        public List<? extends BookmarkStructure> getChildren() {
            return repository.getAll().stream()
                .map(b -> new Bookmark(repository, b.objectId(), b.name()))
                .toList();
        }

        @Override
        public boolean hasChildren() {
            return true;
        }
    }

    final class Bookmark implements BookmarkStructure, ObjectIdHolder {
        final Bookmarks repository;
        final ObjectId id;
        final String name;

        public Bookmark(Bookmarks repository, ObjectId id, String name) {
            this.repository = repository;
            this.id = id;
            this.name = name;
        }

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
