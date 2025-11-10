package sh.adelessfox.odradek.opengl;

import sh.adelessfox.odradek.opengl.rhi.AddressMode;
import sh.adelessfox.odradek.opengl.rhi.FilterMode;
import sh.adelessfox.odradek.opengl.rhi.SamplerDescriptor;

import static org.lwjgl.opengl.ARBBindlessTexture.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL14.GL_MIRRORED_REPEAT;
import static org.lwjgl.opengl.GL33.glDeleteSamplers;
import static org.lwjgl.opengl.GL33.glSamplerParameteri;
import static org.lwjgl.opengl.GL45.glCreateSamplers;

public final class Sampler implements GLObject {
    private final int name;
    private final long handle;

    Sampler(Texture texture, SamplerDescriptor descriptor) {
        name = glCreateSamplers();
        glSamplerParameteri(name, GL_TEXTURE_WRAP_S, glAddressMode(descriptor.addressModeU()));
        glSamplerParameteri(name, GL_TEXTURE_WRAP_T, glAddressMode(descriptor.addressModeV()));
        glSamplerParameteri(name, GL_TEXTURE_MIN_FILTER, glFilterMode(descriptor.minFilter()));
        glSamplerParameteri(name, GL_TEXTURE_MAG_FILTER, glFilterMode(descriptor.magFilter()));
        handle = glGetTextureSamplerHandleARB(texture.name(), name);
    }

    @Override
    public Sampler bind() {
        glMakeTextureHandleResidentARB(handle);
        return this;
    }

    @Override
    public void unbind() {
        glMakeTextureHandleNonResidentARB(handle);
    }

    @Override
    public void dispose() {
        glDeleteSamplers(name);
    }

    long handle() {
        return handle;
    }

    private static int glAddressMode(AddressMode addressMode) {
        return switch (addressMode) {
            case CLAMP_TO_EDGE -> GL_CLAMP_TO_EDGE;
            case REPEAT -> GL_REPEAT;
            case MIRROR_REPEAT -> GL_MIRRORED_REPEAT;
        };
    }

    private static int glFilterMode(FilterMode filterMode) {
        return switch (filterMode) {
            case NEAREST -> GL_NEAREST;
            case LINEAR -> GL_LINEAR;
        };
    }
}
