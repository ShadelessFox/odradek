package sh.adelessfox.odradek.game.hfw.rtti.data.ref;

import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.game.ObjectWithIdHolder;

public record WeakPtr<T>(ObjectId objectId, T object) implements ObjectWithIdHolder<T> {
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof WeakPtr<?> that && objectId.equals(that.objectId);
    }

    @Override
    public int hashCode() {
        return objectId.hashCode();
    }

    @Override
    public String toString() {
        return "<weak ptr to " + objectId + ">";
    }
}
