package sh.adelessfox.odradek.opengl;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Optional;

import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL45.*;

public final class Buffer implements GLObject {
    private final int name = glCreateBuffers();

    public void allocate(int size, int flags) {
        glNamedBufferStorage(name, size, flags);
    }

    public void put(ByteBuffer data, int flags) {
        glNamedBufferStorage(name, makeDirect(data), flags);
    }

    public void put(IntBuffer data, int flags) {
        glNamedBufferStorage(name, makeDirect(data), flags);
    }

    public void put(FloatBuffer data, int flags) {
        glNamedBufferStorage(name, makeDirect(data), flags);
    }

    public void update(ByteBuffer data, int offset) {
        glNamedBufferSubData(name, offset, makeDirect(data));
    }

    public void update(IntBuffer data, int offset) {
        glNamedBufferSubData(name, offset, makeDirect(data));
    }

    public void update(FloatBuffer data, int offset) {
        glNamedBufferSubData(name, offset, makeDirect(data));
    }

    public Optional<ByteBuffer> map(int access) {
        return map(access, size());
    }

    public Optional<ByteBuffer> map(int access, long length) {
        return map(access, 0, length);
    }

    public Optional<ByteBuffer> map(int access, long offset, long length) {
        return Optional.ofNullable(glMapNamedBufferRange(name, offset, length, access));
    }

    public long size() {
        return glGetNamedBufferParameteri(name, GL_BUFFER_SIZE);
    }

    public boolean unmap() {
        return glUnmapNamedBuffer(name);
    }

    @Override
    public void dispose() {
        glDeleteBuffers(name);
    }

    int name() {
        return name;
    }

    private ByteBuffer makeDirect(ByteBuffer buffer) {
        if (buffer.isDirect()) {
            return buffer;
        } else {
            return BufferUtils.createByteBuffer(buffer.remaining())
                .put(0, buffer, buffer.position(), buffer.remaining());
        }
    }

    private IntBuffer makeDirect(IntBuffer buffer) {
        if (buffer.isDirect()) {
            return buffer;
        } else {
            return BufferUtils.createIntBuffer(buffer.remaining())
                .put(0, buffer, buffer.position(), buffer.remaining());
        }
    }

    private FloatBuffer makeDirect(FloatBuffer buffer) {
        if (buffer.isDirect()) {
            return buffer;
        } else {
            return BufferUtils.createFloatBuffer(buffer.remaining())
                .put(0, buffer, buffer.position(), buffer.remaining());
        }
    }
}
