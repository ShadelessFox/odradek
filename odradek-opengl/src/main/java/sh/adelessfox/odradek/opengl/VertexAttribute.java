package sh.adelessfox.odradek.opengl;

import sh.adelessfox.odradek.geometry.ComponentType;
import sh.adelessfox.odradek.geometry.ElementType;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_2_10_10_10_REV;
import static org.lwjgl.opengl.GL30.GL_HALF_FLOAT;
import static org.lwjgl.opengl.GL33.GL_INT_2_10_10_10_REV;

public record VertexAttribute(
    int location,
    ElementType elementType,
    ComponentType componentType,
    int offset,
    int stride,
    boolean normalized
) {
    public VertexAttribute {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be positive");
        }
        if (stride <= 0) {
            throw new IllegalArgumentException("stride must be positive");
        }
        if (normalized && (componentType == ComponentType.FLOAT || componentType == ComponentType.HALF_FLOAT)) {
            throw new IllegalArgumentException("normalized can only be used with integer component types");
        }
    }

    public int glType() {
        return switch (componentType) {
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

    public int glSize() {
        return switch (componentType) {
            case ComponentType.INT_10_10_10_2, ComponentType.UNSIGNED_INT_10_10_10_2 -> 4;
            default -> elementType.size();
        };
    }
}
