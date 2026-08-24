package sh.adelessfox.odradek.app.ui.bookmarks;

import sh.adelessfox.odradek.game.decima.ObjectId;

public sealed interface BookmarkKey {
    record OfObject(ObjectId objectId) implements BookmarkKey {
        @Override
        public String toString() {
            return "Object " + objectId;
        }
    }

    record OfGroup(int groupId) implements BookmarkKey {
        @Override
        public String toString() {
            return "Group " + groupId;
        }
    }
}
