package sh.adelessfox.odradek.opengl;

import sh.adelessfox.odradek.geometry.ComponentType;
import sh.adelessfox.odradek.geometry.ElementType;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

public final class VertexArray implements GLObject {
    private final int array = glGenVertexArrays();
    private final List<VertexBuffer> buffers = new ArrayList<>(1);

    /**
     * Creates a 2D plane at 0,0 origin with the specified width and height.
     * <p>
     * The produced plane can be drawn using the following snippet:
     * {@snippet lang = java:
     * import static org.lwjgl.opengl.GL30.*;
     *
     * glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0)
     *}
     *
     * @param width  width of the plane
     * @param height height of the plane
     * @return a new vertex array object representing the plane
     */
    public static VertexArray createPlane(float width, float height) {
        try (var vao = new VertexArray().bind()) {
            var attributes = List.of(
                new VertexAttribute(0, ElementType.VEC2, ComponentType.FLOAT, 0, 8, false)
            );

            try (var vbo = vao.createBuffer(attributes)) {
                float[] vertices = {
                    -width / 2, -height / 2,
                    +width / 2, -height / 2,
                    +width / 2, +height / 2,
                    -width / 2, +height / 2
                };
                vbo.put(FloatBuffer.wrap(vertices), GL_STATIC_DRAW);
            }

            try (var ibo = vao.createIndexBuffer()) {
                int[] indices = {0, 1, 2, 0, 2, 3};
                ibo.put(IntBuffer.wrap(indices), GL_STATIC_DRAW);
            }

            return vao;
        }
    }

    /**
     * Creates a new array buffer. The returned buffer is <b>bound</b>.
     */
    public VertexBuffer createBuffer(List<VertexAttribute> attributes) {
        var buffer = createBuffer(GL_ARRAY_BUFFER);
        for (VertexAttribute attr : attributes) {
            int index = attr.location();
            glEnableVertexAttribArray(index);
            glVertexAttribPointer(index, attr.glSize(), attr.glType(), attr.normalized(), attr.stride(), attr.offset());
        }
        return buffer;
    }

    /**
     * Creates a new element array buffer. The returned buffer is <b>bound</b>.
     */
    public VertexBuffer createIndexBuffer() {
        return createBuffer(GL_ELEMENT_ARRAY_BUFFER);
    }

    @Override
    public VertexArray bind() {
        glBindVertexArray(array);
        buffers.forEach(VertexBuffer::bind);
        return this;
    }

    @Override
    public void unbind() {
        buffers.forEach(VertexBuffer::unbind);
        glBindVertexArray(0);
    }

    @Override
    public void dispose() {
        buffers.forEach(VertexBuffer::dispose);
        buffers.clear();
        glDeleteVertexArrays(array);
    }

    private VertexBuffer createBuffer(int target) {
        ensureBound();
        var buffer = new VertexBuffer(target);
        buffers.add(buffer);
        buffer.bind();
        return buffer;
    }

    private void ensureBound() {
        if (glGetInteger(GL_VERTEX_ARRAY_BINDING) != array) {
            throw new IllegalArgumentException("Vertex array is not bound");
        }
    }
}
