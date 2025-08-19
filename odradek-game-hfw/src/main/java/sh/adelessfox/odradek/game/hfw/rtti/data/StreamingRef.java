package sh.adelessfox.odradek.game.hfw.rtti.data;

import sh.adelessfox.odradek.rtti.data.Ref;

public record StreamingRef<T>(int groupId, int objectIndex) implements Ref<T> {
    @Override
    public String toString() {
        return "<streaming ref to " + groupId + ":" + objectIndex + ">";
    }

    @Override
    public T get() {
        return null;
    }
}
