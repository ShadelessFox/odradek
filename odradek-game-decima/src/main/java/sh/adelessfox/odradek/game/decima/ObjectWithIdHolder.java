package sh.adelessfox.odradek.game.decima;

import sh.adelessfox.odradek.game.ObjectHolder;

public interface ObjectWithIdHolder<T> extends ObjectIdHolder, ObjectHolder<T> {
    @Override
    default T get() {
        return object();
    }
}
