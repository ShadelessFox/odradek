package sh.adelessfox.odradek.game.hfw.rtti.data.ref;

import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.game.ObjectIdHolder;

@SuppressWarnings("unused")
public record StreamingRef<T>(ObjectId objectId) implements ObjectIdHolder {
    @Override
    public String toString() {
        return "<streaming ref to " + objectId + ">";
    }
}
