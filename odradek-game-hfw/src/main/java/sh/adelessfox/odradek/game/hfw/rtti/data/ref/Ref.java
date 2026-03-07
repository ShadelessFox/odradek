package sh.adelessfox.odradek.game.hfw.rtti.data.ref;

import sh.adelessfox.odradek.game.ObjectHolder;
import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.rtti.data.TypedObject;

import java.util.Iterator;

public record Ref<T extends TypedObject>(ObjectId objectId, T object) implements ObjectHolder<T> {
    public static <T extends TypedObject> Iterable<T> unwrap(Iterable<Ref<T>> iterable) {
        return () -> unwrap(iterable.iterator());
    }

    public static <T extends TypedObject> Iterator<T> unwrap(Iterator<Ref<T>> iterator) {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next().get();
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof Ref<?> that && objectId.equals(that.objectId);
    }

    @Override
    public int hashCode() {
        return objectId.hashCode();
    }

    @Override
    public String toString() {
        return "<ref to " + objectId + ">";
    }
}
