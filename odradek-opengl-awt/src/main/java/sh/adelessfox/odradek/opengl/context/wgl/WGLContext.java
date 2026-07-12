package sh.adelessfox.odradek.opengl.context.wgl;

import sh.adelessfox.odradek.NotImplementedException;
import sh.adelessfox.odradek.opengl.context.GLContext;
import sh.adelessfox.odradek.opengl.context.GLProfile;

public final class WGLContext extends GLContext {
    private WGLContext() {
    }

    public static WGLContext create(GLProfile profile, int major, int minor, boolean debug) {
        throw new NotImplementedException(); // TODO
    }

    @Override
    public void makeCurrent() {
        throw new NotImplementedException(); // TODO
    }

    @Override
    public void clearCurrent() {
        throw new NotImplementedException(); // TODO
    }

    @Override
    public void dispose() {
        throw new NotImplementedException(); // TODO
    }
}
