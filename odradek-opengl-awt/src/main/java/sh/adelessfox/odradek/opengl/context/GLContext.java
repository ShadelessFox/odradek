package sh.adelessfox.odradek.opengl.context;

import org.lwjgl.system.Platform;
import sh.adelessfox.odradek.opengl.context.glx.GLXContext;
import sh.adelessfox.odradek.opengl.context.wgl.WGLContext;
import sh.adelessfox.odradek.ui.Disposable;

public abstract sealed class GLContext implements Disposable permits GLXContext, WGLContext {
    public static GLContext create(GLProfile profile, int major, int minor, boolean debug) {
        return switch (Platform.get()) {
            case LINUX -> GLXContext.create(profile, major, minor, debug);
            case WINDOWS -> WGLContext.create(profile, major, minor, debug);
            default -> throw new UnsupportedOperationException("Unsupported platform: " + Platform.get());
        };
    }

    public abstract void makeCurrent();

    public abstract void clearCurrent();
}
