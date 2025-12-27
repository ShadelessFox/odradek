package sh.adelessfox.odradek.app.ui.component.bookmarks;

import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.ui.components.tree.TreeStructure;

import java.util.List;

sealed interface BookmarkStructure extends TreeStructure<BookmarkStructure> {
    final class Root implements BookmarkStructure {
    }

    final class Repository implements BookmarkStructure {
        public enum Type {
            ONLINE,
            USER
        }

        final Type type;

        public Repository(Type type) {
            this.type = type;
        }
    }

    final class Bookmark implements BookmarkStructure {
        final ObjectId id;
        String name;

        public Bookmark(ObjectId id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Override
    default BookmarkStructure getRoot() {
        return this;
    }

    @Override
    default List<? extends BookmarkStructure> getChildren(BookmarkStructure parent) {
        return switch (parent) {
            case Root _ -> List.of(
                new Repository(Repository.Type.ONLINE),
                new Repository(Repository.Type.USER)
            );
            case Repository r -> switch (r.type) {
                case ONLINE -> List.of();
                case USER -> List.of(
                    new Bookmark(new ObjectId(110643, 2), "far away shit"),
                    new Bookmark(new ObjectId(14178, 53), "a reef"),
                    new Bookmark(new ObjectId(20296, 8), "a big ass dense"),
                    new Bookmark(new ObjectId(66370, 0), "mockup"),
                    new Bookmark(new ObjectId(73507, 14), "a broken tree prefab"),
                    new Bookmark(new ObjectId(13422, 1030), "a broken BC5U texture"),
                    new Bookmark(new ObjectId(1723, 0), "a small tree"),
                    new Bookmark(new ObjectId(477, 4), " ^ its shadow caster?"),
                    new Bookmark(new ObjectId(42854, 0), "broken coordinates"),
                    new Bookmark(new ObjectId(64460, 5), ""),
                    new Bookmark(new ObjectId(4254, 1084), "cool ui animation"),
                    new Bookmark(new ObjectId(48348, 29), "positions at 0,0,0")
                );
            };
            default -> throw new IllegalArgumentException();
        };
    }

    @Override
    default boolean hasChildren(BookmarkStructure parent) {
        return switch (parent) {
            case Root _, Repository _ -> true;
            default -> false;
        };
    }
}
