package sh.adelessfox.odradek.opengl.context.glx;

import sh.adelessfox.odradek.opengl.context.GLContext;
import sh.adelessfox.odradek.opengl.context.GLProfile;

import java.util.Objects;

import static org.lwjgl.opengl.GLX13.*;
import static org.lwjgl.opengl.GLXARBCreateContext.*;
import static org.lwjgl.opengl.GLXARBCreateContextProfile.*;
import static org.lwjgl.system.linux.X11.XDefaultScreen;
import static org.lwjgl.system.linux.X11.XOpenDisplay;

public final class GLXContext extends GLContext {
    private final long display;
    private final long pbuffer;
    private final long context;

    private GLXContext(long display, long pbuffer, long context) {
        this.display = display;
        this.pbuffer = pbuffer;
        this.context = context;
    }

    public static GLXContext create(GLProfile profile, int major, int minor, boolean debug) {
        var display = XOpenDisplay((String) null);

        var configs = glXChooseFBConfig(display, XDefaultScreen(display), new int[]{0});
        var config = Objects.requireNonNull(configs).get(0);

        long context = glXCreateContextAttribsARB(display, config, 0, true, new int[]{
            GLX_CONTEXT_MAJOR_VERSION_ARB, major,
            GLX_CONTEXT_MINOR_VERSION_ARB, minor,
            GLX_CONTEXT_PROFILE_MASK_ARB, profile == GLProfile.CORE ? GLX_CONTEXT_CORE_PROFILE_BIT_ARB : GLX_CONTEXT_COMPATIBILITY_PROFILE_BIT_ARB,
            GLX_CONTEXT_FLAGS_ARB, debug ? GLX_CONTEXT_DEBUG_BIT_ARB : 0,
            0
        });

        long pbuffer = glXCreatePbuffer(display, config, new int[]{
            GLX_PBUFFER_WIDTH, 32,
            GLX_PBUFFER_HEIGHT, 32,
            0
        });

        return new GLXContext(display, pbuffer, context);
    }

    @Override
    public void makeCurrent() {
        glXMakeCurrent(display, pbuffer, context);
    }

    @Override
    public void clearCurrent() {
        glXMakeCurrent(display, 0, 0);
    }

    @Override
    public void dispose() {
        glXDestroyPbuffer(display, pbuffer);
        glXDestroyContext(display, context);
    }
}
