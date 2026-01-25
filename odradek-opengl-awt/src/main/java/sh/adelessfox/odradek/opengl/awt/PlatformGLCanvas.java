package sh.adelessfox.odradek.opengl.awt;

import org.lwjgl.system.jawt.JAWT;
import org.lwjgl.system.jawt.JAWTDrawingSurface;
import org.lwjgl.system.jawt.JAWTDrawingSurfaceInfo;

import java.awt.*;

import static org.lwjgl.system.jawt.JAWTFunctions.*;

/**
 * Interface for platform-specific implementations of {@link GLCanvas}.
 *
 * @author Kai Burjack
 */
abstract sealed class PlatformGLCanvas permits PlatformLinuxGLCanvas, PlatformWin32GLCanvas {
    protected JAWT awt;
    protected JAWTDrawingSurface ds;

    public final long create(Canvas canvas, GLData data, GLData effective) {
        if (awt != null) {
            throw new IllegalStateException("Drawing surface already created");
        }

        awt = JAWT.calloc();
        awt.version(JAWT_VERSION_1_4);

        if (!JAWT_GetAWT(awt)) {
            dispose();
            throw new IllegalStateException("Unable to get JAWT instance");
        }

        ds = JAWT_GetDrawingSurface(canvas, awt.GetDrawingSurface());

        lock();
        try {
            var info = JAWT_DrawingSurface_GetDrawingSurfaceInfo(ds, ds.GetDrawingSurfaceInfo());
            if (info == null) {
                dispose();
                throw new IllegalStateException("Unable to acquire drawing surface info");
            }
            try {
                return create(info, data, effective);
            } finally {
                JAWT_DrawingSurface_FreeDrawingSurfaceInfo(info, ds.FreeDrawingSurfaceInfo());
            }
        } finally {
            unlock();
        }
    }

    public void lock() {
        int lock = JAWT_DrawingSurface_Lock(ds, ds.Lock());
        if ((lock & JAWT_LOCK_ERROR) != 0) {
            throw new IllegalStateException("JAWT_DrawingSurface_Lock() failed");
        }
    }

    public void unlock() {
        JAWT_DrawingSurface_Unlock(ds, ds.Unlock());
    }

    public abstract boolean makeCurrent(long context);

    public abstract void swapBuffers();

    public void dispose() {
        if (ds != null) {
            JAWT_FreeDrawingSurface(ds, awt.FreeDrawingSurface());
            ds = null;
        }
        if (awt != null) {
            awt.free();
            awt = null;
        }
    }

    protected abstract long create(JAWTDrawingSurfaceInfo dsi, GLData data, GLData effective);
}
