package sh.adelessfox.odradek.opengl;

import org.lwjgl.BufferUtils;

import java.awt.image.BufferedImage;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

public class Texture implements GLObject {
    private final int texture = glGenTextures();

    public Texture() {
        try (var _ = bind()) {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        }
    }

    public void put(BufferedImage image) {
        ensureBound();
        if (image.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            throw new IllegalArgumentException("Unsupported image type: " + image.getType());
        }
        int width = image.getWidth();
        int height = image.getHeight();
        var buffer = BufferUtils
            .createByteBuffer(width * height * 3)
            .put((byte[]) image.getRaster().getDataElements(0, 0, width, height, null))
            .flip();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, buffer);
    }

    @Override
    public Texture bind() {
        glBindTexture(GL_TEXTURE_2D, texture);
        return this;
    }

    @Override
    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    @Override
    public void dispose() {
        glDeleteTextures(texture);
    }

    private void ensureBound() {
        if (glGetInteger(GL_TEXTURE_BINDING_2D) != texture) {
            throw new IllegalArgumentException("Texture is not bound");
        }
    }
}
