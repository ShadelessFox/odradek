package sh.adelessfox.odradek.opengl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.ARBBindlessTexture.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL45.*;

public class Texture implements GLObject {
    private final int name;
    private final long handle;

    private Texture(int width, int height, ByteBuffer buffer, int format, int type) {
        if (!GL.getCapabilities().GL_ARB_bindless_texture) {
            throw new IllegalStateException("GL_ARB_bindless_texture is not supported but required");
        }

        name = glCreateTextures(GL_TEXTURE_2D);
        glTextureParameteri(name, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTextureParameteri(name, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTextureParameteri(name, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTextureParameteri(name, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTextureStorage2D(name, 1, GL_RGBA8, width, height);
        glTextureSubImage2D(name, 0, 0, 0, width, height, format, type, buffer);
        handle = glGetTextureHandleARB(name);
    }

    public static Texture load(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            throw new IllegalArgumentException("Unsupported image type: " + image.getType());
        }

        int width = image.getWidth();
        int height = image.getHeight();
        var buffer = BufferUtils
            .createByteBuffer(width * height * 3)
            .put((byte[]) image.getRaster().getDataElements(0, 0, width, height, null))
            .flip();

        return new Texture(width, height, buffer, GL_RGB, GL_UNSIGNED_BYTE);
    }

    @Override
    public Texture bind() {
        glMakeTextureHandleResidentARB(handle);
        return this;
    }

    @Override
    public void unbind() {
        glMakeTextureHandleNonResidentARB(handle);
    }

    @Override
    public void dispose() {
        glDeleteTextures(name);
    }

    public long handle() {
        return handle;
    }
}
