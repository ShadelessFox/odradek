package sh.adelessfox.odradek.game.hfw.rtti.data;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.RTTIRefObject;
import sh.adelessfox.odradek.rtti.data.Ref;

public record StreamingLink<T extends RTTIRefObject>(T object, int groupId, int objectIndex) implements Ref<T> {
    @Override
    public T get() {
        return object;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return "<streaming link to " + object.getType() + ">";
    }
}
