package sh.adelessfox.odradek.game;

import java.util.function.Supplier;

public interface ObjectHolder<T> extends Supplier<T> {
    T object();

    @Override
    T get();
}
