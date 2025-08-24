package sh.adelessfox.odradek.game.hfw.rtti.data;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest;
import sh.adelessfox.odradek.rtti.data.Ref;

public record UUIDRef<T>(HorizonForbiddenWest.GGUUID objectUUID) implements Ref<T> {
    @Override
    public T get() {
        return null;
    }

    @Override
    public String toString() {
        return "<uuid ref to " + objectUUID.toDisplayString() + ">";
    }
}
