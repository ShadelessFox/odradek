package sh.adelessfox.odradek.opengl.awt;

import org.lwjgl.system.jawt.JAWT;

import java.awt.*;

import static org.lwjgl.system.jawt.JAWTFunctions.JAWT_GetAWT;
import static org.lwjgl.system.jawt.JAWTFunctions.JAWT_VERSION_1_4;

/**
 * Interface for platform-specific implementations of {@link GLCanvas}.
 *
 * @author Kai Burjack
 */
sealed abstract class PlatformGLCanvas permits PlatformLinuxGLCanvas, PlatformWin32GLCanvas {
    protected JAWT awt;

    public PlatformGLCanvas() {
        awt = JAWT.calloc();
        awt.version(JAWT_VERSION_1_4);

        if (!JAWT_GetAWT(awt)) {
            throw new AssertionError("GetAWT failed");
        }
    }

    public abstract long create(Canvas canvas, GLData data, GLData effective);

    public abstract boolean makeCurrent(long context);

    public abstract void swapBuffers();

    public abstract void lock();

    public abstract void unlock();

    public void dispose() {
        awt.free();
    }
}
