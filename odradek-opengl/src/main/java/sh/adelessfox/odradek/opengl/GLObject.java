package sh.adelessfox.odradek.opengl;

public interface GLObject {
    void dispose();

    interface Bindable<T extends Bindable<T>> extends GLObject, AutoCloseable {
        T bind();

        void unbind();

        @Override
        default void close() {
            unbind();
        }
    }
}
