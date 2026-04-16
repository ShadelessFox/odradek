package sh.adelessfox.odradek.game.ds2.rtti.data.ref;

import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.game.decima.ObjectWithIdHolder;

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
