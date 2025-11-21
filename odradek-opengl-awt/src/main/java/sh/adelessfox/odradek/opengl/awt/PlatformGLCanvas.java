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
sealed abstract class PlatformGLCanvas permits PlatformLinuxGLCanvas, PlatformWin32GLCanvas {
    protected JAWT awt;
    protected JAWTDrawingSurface ds;

    public PlatformGLCanvas() {
        awt = JAWT.calloc();
        awt.version(JAWT_VERSION_1_4);

        if (!JAWT_GetAWT(awt)) {
            throw new AssertionError("GetAWT failed");
        }
    }

    public final long create(Canvas canvas, GLData data, GLData effective) {
        this.ds = JAWT_GetDrawingSurface(canvas, awt.GetDrawingSurface());

        lock();
        try {
            var info = JAWT_DrawingSurface_GetDrawingSurfaceInfo(ds, ds.GetDrawingSurfaceInfo());
            if (info == null) {
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
        awt.free();
    }

    protected abstract long create(JAWTDrawingSurfaceInfo dsi, GLData data, GLData effective);
}
