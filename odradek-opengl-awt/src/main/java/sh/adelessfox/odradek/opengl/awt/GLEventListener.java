package sh.adelessfox.odradek.opengl.awt;

public interface GLEventListener {
    /** Called after an OpenGL context is created. */
    void onCreate();

    /** Called when the OpenGL context needs to be redrawn. */
    void onRender();

    /** Called when the OpenGL context is about to be destroyed. */
    void onDestroy();
}
