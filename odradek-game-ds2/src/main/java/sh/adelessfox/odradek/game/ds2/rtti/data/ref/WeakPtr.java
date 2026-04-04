package sh.adelessfox.odradek.game.ds2.rtti.data.ref;

import sh.adelessfox.odradek.game.ObjectHolder;
import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.game.ds2.rtti.DS2.RTTIRefObject;

public record WeakPtr<T extends RTTIRefObject>(ObjectId objectId, T object) implements ObjectHolder<T> {
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
