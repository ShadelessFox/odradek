package sh.adelessfox.odradek.opengl;

public interface GLObject extends AutoCloseable {
    GLObject bind();

    void unbind();

    void dispose();

    @Override
    default void close() {
        unbind();
    }
}
