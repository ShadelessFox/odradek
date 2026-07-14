package sh.adelessfox.odradek.opengl;

import java.nio.ByteBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL30.*;

public abstract sealed class Framebuffer implements GLObject.Bindable<Framebuffer> {
    final int id = glGenFramebuffers();
    private final int width;
    private final int height;

    Framebuffer(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public static Framebuffer create(int width, int height, int samples) {
        if (samples > 0) {
            return new MultiSampled(width, height, samples);
        } else if (samples < 0) {
            return new MultiSampled(width, height, glGetInteger(GL_MAX_SAMPLES));
        } else {
            return new Default(width, height);
        }
    }

    @Override
    public Framebuffer bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, id);
        return this;
    }

    @Override
    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void dispose() {
        glDeleteFramebuffers(id);
    }

    public void readPixels(int x, int y, int width, int height, int format, int type, ByteBuffer target) {
        var oldReadBuffer = glGetInteger(GL_READ_FRAMEBUFFER_BINDING);
        glBindFramebuffer(GL_READ_FRAMEBUFFER, id);
        glReadPixels(x, y, width, height, format, type, target);
        glBindFramebuffer(GL_READ_FRAMEBUFFER, oldReadBuffer);
    }

    public void blitTo(Framebuffer dst, int mask, int filter) {
        var oldDrawBuffer = glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING);
        var oldReadBuffer = glGetInteger(GL_READ_FRAMEBUFFER_BINDING);

        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, dst.id);
        glBindFramebuffer(GL_READ_FRAMEBUFFER, id);
        glBlitFramebuffer(0, 0, width, height, 0, 0, dst.width, dst.height, mask, filter);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, oldDrawBuffer);
        glBindFramebuffer(GL_READ_FRAMEBUFFER, oldReadBuffer);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    private static final class Default extends Framebuffer {
        private final int colorAttachment;
        private final int depthAttachment;

        public Default(int width, int height) {
            super(width, height);

            glBindFramebuffer(GL_FRAMEBUFFER, id);

            colorAttachment = glGenRenderbuffers();
            glBindRenderbuffer(GL_RENDERBUFFER, colorAttachment);
            glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA8, width, height);
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, colorAttachment);

            depthAttachment = glGenRenderbuffers();
            glBindRenderbuffer(GL_RENDERBUFFER, depthAttachment);
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height);
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, depthAttachment);

            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }

        @Override
        public void dispose() {
            glDeleteRenderbuffers(colorAttachment);
            glDeleteRenderbuffers(depthAttachment);

            super.dispose();
        }
    }

    private static final class MultiSampled extends Framebuffer {
        private final int colorAttachment;
        private final int depthAttachment;

        MultiSampled(int width, int height, int samples) {
            Objects.checkIndex(samples, glGetInteger(GL_MAX_SAMPLES) + 1);
            super(width, height);

            glBindFramebuffer(GL_FRAMEBUFFER, id);

            colorAttachment = glGenRenderbuffers();
            glBindRenderbuffer(GL_RENDERBUFFER, colorAttachment);
            glRenderbufferStorageMultisample(GL_RENDERBUFFER, samples, GL_RGBA8, width, height);
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, colorAttachment);

            depthAttachment = glGenRenderbuffers();
            glBindRenderbuffer(GL_RENDERBUFFER, depthAttachment);
            glRenderbufferStorageMultisample(GL_RENDERBUFFER, samples, GL_DEPTH24_STENCIL8, width, height);
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, depthAttachment);

            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }

        @Override
        public void dispose() {
            glDeleteRenderbuffers(colorAttachment);
            glDeleteRenderbuffers(depthAttachment);

            super.dispose();
        }
    }
}
