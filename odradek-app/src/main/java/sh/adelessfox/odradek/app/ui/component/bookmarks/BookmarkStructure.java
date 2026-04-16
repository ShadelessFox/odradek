package sh.adelessfox.odradek.app.ui.component.bookmarks;

import sh.adelessfox.odradek.app.ui.bookmarks.Bookmarks;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.game.decima.ObjectIdHolder;
import sh.adelessfox.odradek.ui.components.tree.TreeStructure;

import java.util.Comparator;
import java.util.List;

public sealed interface BookmarkStructure extends TreeStructure<BookmarkStructure> {
    record Root(Bookmarks repository) implements BookmarkStructure {
        @Override
        public List<? extends BookmarkStructure> getChildren() {
            return repository.getAll().stream()
                .map(b -> new Bookmark(repository, b.objectId(), b.name()))
                .sorted(Comparator.comparing(Bookmark::name))
                .toList();
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
