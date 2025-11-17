package sh.adelessfox.odradek.opengl;

import sh.adelessfox.odradek.geometry.ComponentType;
import sh.adelessfox.odradek.geometry.ElementType;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL45.*;

public final class VertexArray implements GLObject.Bindable<VertexArray> {
    private final int name = glCreateVertexArrays();
    private final List<Buffer> buffers = new ArrayList<>(2);

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

            var vertices = new float[]{
                -width / 2, -height / 2,
                +width / 2, -height / 2,
                +width / 2, +height / 2,
                -width / 2, +height / 2
            };
            var vbo = vao.createVertexBuffer(attributes, 0);
            vbo.put(FloatBuffer.wrap(vertices), 0);

            var elements = new int[]{0, 1, 2, 0, 2, 3};
            var ebo = vao.createElementBuffer();
            ebo.put(IntBuffer.wrap(elements), 0);

            return vao;
        }
    }

    /**
     * Creates a new vertex buffer.
     *
     * @param attributes a list of vertex attributes; must not be empty
     * @param slot       an index of the buffer within this vertex array. In OpenGL,
     *                   the maximum number of buffers per a vertex array is {@code 16}.
     */
    public Buffer createVertexBuffer(List<VertexAttribute> attributes, int slot) {
        if (attributes.isEmpty()) {
            throw new IllegalStateException("vertex buffer attributes cannot be empty");
        }

        for (VertexAttribute attr : attributes) {
            glEnableVertexArrayAttrib(name, attr.location());
            glVertexArrayAttribFormat(name, attr.location(), glSize(attr), glType(attr), attr.normalized(), attr.offset());
            glVertexArrayAttribBinding(name, attr.location(), slot);
        }

        var buffer = createBuffer();
        glVertexArrayVertexBuffer(name, slot, buffer.name(), 0, attributes.getFirst().stride());
        return buffer;
    }

    /**
     * Creates a new element array buffer. The returned buffer is <b>bound</b>.
     */
    public Buffer createElementBuffer() {
        var buffer = createBuffer();
        glVertexArrayElementBuffer(name, buffer.name());
        return buffer;
    }

    @Override
    public VertexArray bind() {
        glBindVertexArray(name);
        return this;
    }

    @Override
    public void unbind() {
        glBindVertexArray(0);
    }

    @Override
    public void dispose() {
        buffers.forEach(Buffer::dispose);
        buffers.clear();
        glDeleteVertexArrays(name);
    }

    private Buffer createBuffer() {
        ensureBound();
        var buffer = new Buffer();
        buffers.add(buffer);
        return buffer;
    }

    private void ensureBound() {
        if (glGetInteger(GL_VERTEX_ARRAY_BINDING) != name) {
            throw new IllegalArgumentException("Vertex array is not bound");
        }
    }

    private static int glType(VertexAttribute attribute) {
        return switch (attribute.componentType()) {
            case ComponentType.BYTE -> GL_BYTE;
            case ComponentType.UNSIGNED_BYTE -> GL_UNSIGNED_BYTE;
            case ComponentType.SHORT -> GL_SHORT;
            case ComponentType.UNSIGNED_SHORT -> GL_UNSIGNED_SHORT;
            case ComponentType.INT -> GL_INT;
            case ComponentType.UNSIGNED_INT -> GL_UNSIGNED_INT;
            case ComponentType.HALF_FLOAT -> GL_HALF_FLOAT;
            case ComponentType.FLOAT -> GL_FLOAT;
            case ComponentType.INT_10_10_10_2 -> GL_INT_2_10_10_10_REV;
            case ComponentType.UNSIGNED_INT_10_10_10_2 -> GL_UNSIGNED_INT_2_10_10_10_REV;
        };
    }

    private static int glSize(VertexAttribute attribute) {
        return switch (attribute.componentType()) {
            case ComponentType.INT_10_10_10_2, ComponentType.UNSIGNED_INT_10_10_10_2 -> 4;
            default -> attribute.elementType().size();
        };
    }
}
