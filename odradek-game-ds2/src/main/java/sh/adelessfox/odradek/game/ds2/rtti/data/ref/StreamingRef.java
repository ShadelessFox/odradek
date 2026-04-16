package sh.adelessfox.odradek.game.ds2.rtti.data.ref;

import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.game.decima.ObjectIdHolder;

@SuppressWarnings("unused")
public record StreamingRef<T>(ObjectId objectId) implements ObjectIdHolder {
    @Override
    public String toString() {
        return "<streaming ref to " + objectId + ">";
    }
}
