package sh.adelessfox.odradek.opengl.awt;

import java.awt.*;

/**
 * Interface for platform-specific implementations of {@link GLCanvas}.
 *
 * @author Kai Burjack
 */
sealed interface PlatformGLCanvas permits PlatformLinuxGLCanvas, PlatformWin32GLCanvas {
    long create(Canvas canvas, GLData data, GLData effective);

    boolean makeCurrent(long context);

    void swapBuffers();

    void lock();

    void unlock();

    void dispose();
}
