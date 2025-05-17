package sh.adelessfox.odradek.app.gl;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;

public final class VertexBuffer implements GLObject {
    private final int buffer = glGenBuffers();
    private final int target;

    VertexBuffer(int target) {
        this.target = target;
    }

    public void allocate(int size, int usage) {
        glBufferData(target, size, usage);
    }

    public void put(ByteBuffer data, int usage) {
        ensureBound();
        glBufferData(target, ensureDirect(data), usage);
    }

    public void put(IntBuffer data, int usage) {
        ensureBound();
        glBufferData(target, ensureDirect(data), usage);
    }

    public void put(FloatBuffer data, int usage) {
        ensureBound();
        glBufferData(target, ensureDirect(data), usage);
    }

    public void update(ByteBuffer data, int offset) {
        ensureBound();
        glBufferSubData(target, offset, ensureDirect(data));
    }

    public void update(IntBuffer data, int offset) {
        ensureBound();
        glBufferSubData(target, offset, ensureDirect(data));
    }

    public void update(FloatBuffer data, int offset) {
        ensureBound();
        glBufferSubData(target, offset, ensureDirect(data));
    }

    @Override
    public VertexBuffer bind() {
        glBindBuffer(target, buffer);
        return this;
    }

    @Override
    public void unbind() {
        glBindBuffer(target, 0);
    }

    @Override
    public void dispose() {
        glDeleteBuffers(buffer);
    }

    private ByteBuffer ensureDirect(ByteBuffer buffer) {
        if (buffer.isDirect()) {
            return buffer;
        } else {
            return BufferUtils.createByteBuffer(buffer.remaining()).put(buffer).flip();
        }
    }

    private IntBuffer ensureDirect(IntBuffer buffer) {
        if (buffer.isDirect()) {
            return buffer;
        } else {
            return BufferUtils.createIntBuffer(buffer.remaining()).put(buffer).flip();
        }
    }

    private FloatBuffer ensureDirect(FloatBuffer buffer) {
        if (buffer.isDirect()) {
            return buffer;
        } else {
            return BufferUtils.createFloatBuffer(buffer.remaining()).put(buffer).flip();
        }
    }

    private void ensureBound() {
        var name = switch (target) {
            case GL_ARRAY_BUFFER -> GL_ARRAY_BUFFER_BINDING;
            case GL_ELEMENT_ARRAY_BUFFER -> GL_ELEMENT_ARRAY_BUFFER_BINDING;
            default -> throw new IllegalArgumentException("Unknown buffer target: " + target);
        };
        if (glGetInteger(name) != buffer) {
            throw new IllegalArgumentException("Vertex buffer is not bound");
        }
    }
}
