package sh.adelessfox.odradek.game.ds2.rtti.data.ref;

import sh.adelessfox.odradek.game.ObjectHolder;
import sh.adelessfox.odradek.game.ObjectId;

public record CPtr<T>(ObjectId objectId, T object) implements ObjectHolder<T> {
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof CPtr<?> that && objectId.equals(that.objectId);
    }

    @Override
    public int hashCode() {
        return objectId.hashCode();
    }
}
